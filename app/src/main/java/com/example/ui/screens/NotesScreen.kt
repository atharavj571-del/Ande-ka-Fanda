package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteItem
import com.example.data.model.StudySuite
import com.example.ui.theme.*

@Composable
fun NotesScreen(
    currentSuite: StudySuite?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    notesReadMap: Map<String, Boolean>,
    onToggleNoteRead: (String) -> Unit,
    onRegenerateUnlimited: () -> Unit = {}
) {
    if (currentSuite == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("No Study Material Selected", color = TextSecondaryDark)
        }
        return
    }

    var selectedChapterNum by remember { mutableStateOf(0) } // 0 = All chapters

    val chapters = remember(currentSuite) {
        currentSuite.notes.map { it.chapterNumber }.distinct().sorted()
    }

    val filteredNotes = remember(currentSuite, selectedChapterNum, searchQuery) {
        currentSuite.notes.filter { note ->
            (selectedChapterNum == 0 || note.chapterNumber == selectedChapterNum) &&
                    (searchQuery.isBlank() ||
                            note.title.contains(searchQuery, ignoreCase = true) ||
                            note.topicName.contains(searchQuery, ignoreCase = true) ||
                            note.detailedBody.contains(searchQuery, ignoreCase = true) ||
                            note.keyTakeaways.any { it.contains(searchQuery, ignoreCase = true) })
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Unlimited Notes Banner
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Unlimited Notes Active (0 Quota Used)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = onRegenerateUnlimited,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Regenerate Notes", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateDarkBg)
                    }
                }
            }
        }
        // Top Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notes_search_input"),
                placeholder = { Text("Search notes, definitions, formulas...", color = TextSecondaryDark) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondaryDark)
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryCyan,
                    unfocusedBorderColor = CardDarkBorder,
                    focusedContainerColor = CardDarkSurface,
                    unfocusedContainerColor = CardDarkSurface
                )
            )
        }

        // Chapter Filter Row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedChapterNum == 0,
                        onClick = { selectedChapterNum = 0 },
                        label = { Text("All Chapters (${currentSuite.notes.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryCyan,
                            selectedLabelColor = SlateDarkBg,
                            containerColor = CardDarkSurface,
                            labelColor = TextSecondaryDark
                        )
                    )
                }
                items(chapters) { chNum ->
                    val chTitle = currentSuite.notes.firstOrNull { it.chapterNumber == chNum }?.chapterTitle ?: "Ch $chNum"
                    FilterChip(
                        selected = selectedChapterNum == chNum,
                        onClick = { selectedChapterNum = chNum },
                        label = { Text("Ch $chNum") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryCyan,
                            selectedLabelColor = SlateDarkBg,
                            containerColor = CardDarkSurface,
                            labelColor = TextSecondaryDark
                        )
                    )
                }
            }
        }

        // Notes List
        items(filteredNotes) { note ->
            val isRead = notesReadMap[note.id] ?: false
            NoteCard(
                note = note,
                isRead = isRead,
                onToggleRead = { onToggleNoteRead(note.id) }
            )
        }
    }
}

@Composable
fun NoteCard(
    note: NoteItem,
    isRead: Boolean,
    onToggleRead: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isRead) AccentEmerald.copy(alpha = 0.5f) else CardDarkBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Chapter & Read Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Ch ${note.chapterNumber}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = note.topicName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondaryDark
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleRead() }
                ) {
                    Icon(
                        imageVector = if (isRead) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isRead) AccentEmerald else TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRead) "Studied" else "Mark Read",
                        fontSize = 11.sp,
                        color = if (isRead) AccentEmerald else TextSecondaryDark
                    )
                }
            }

            // Note Title
            Text(
                text = note.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Executive Summary
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SlateDarkBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = note.summaryText,
                    fontSize = 12.sp,
                    color = PrimaryCyanLight,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Detailed Study Body
            Text(
                text = note.detailedBody,
                fontSize = 13.sp,
                color = TextPrimaryDark,
                lineHeight = 20.sp
            )

            // Key Takeaways Section
            if (note.keyTakeaways.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Key Takeaways:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryViolet
                    )
                    note.keyTakeaways.forEach { takeaway ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("•", color = SecondaryViolet, fontWeight = FontWeight.Bold)
                            Text(takeaway, fontSize = 12.sp, color = TextSecondaryDark)
                        }
                    }
                }
            }

            // Formulas or Tables Card
            if (!note.formulasAndTables.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SecondaryViolet.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryViolet.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Functions, contentDescription = null, tint = SecondaryViolet, modifier = Modifier.size(16.dp))
                            Text("Formulas & Table Data", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondaryViolet)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(note.formulasAndTables!!, fontSize = 12.sp, color = Color.White)
                    }
                }
            }

            // Diagram Description Card
            if (!note.diagramDescription.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TertiaryAmber.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TertiaryAmber.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Schema, contentDescription = null, tint = TertiaryAmber, modifier = Modifier.size(16.dp))
                            Text("Diagram Representation", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TertiaryAmber)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(note.diagramDescription!!, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FlashcardItem
import com.example.data.model.StudySuite
import com.example.ui.theme.*

@Composable
fun FlashcardScreen(
    currentSuite: StudySuite?,
    flashcardMasteryMap: Map<String, Boolean>,
    onToggleMastery: (String) -> Unit,
    onRegenerateUnlimited: () -> Unit = {},
    onShuffle: () -> Unit = {}
) {
    if (currentSuite == null || currentSuite.flashcards.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("No Flashcards Available", color = TextSecondaryDark)
        }
        return
    }

    var currentIndex by remember { mutableStateOf(0) }
    var selectedStyleFilter by remember { mutableStateOf("ALL") }

    val filteredCards = remember(currentSuite, selectedStyleFilter) {
        if (selectedStyleFilter == "ALL") {
            currentSuite.flashcards
        } else {
            currentSuite.flashcards.filter { it.style == selectedStyleFilter }
        }
    }

    val safeIndex = currentIndex.coerceIn(0, (filteredCards.size - 1).coerceAtLeast(0))
    val currentCard = filteredCards.getOrNull(safeIndex)

    val masteredCount = filteredCards.count { flashcardMasteryMap[it.id] == true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Unlimited Generation Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CardDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.4f)),
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
                        text = "Unlimited Flashcards Active (0 Quota Used)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = onShuffle, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = SecondaryViolet, modifier = Modifier.size(16.dp))
                    }
                    Button(
                        onClick = onRegenerateUnlimited,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Regenerate", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateDarkBg)
                    }
                }
            }
        }
        // Deck Progress Header
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Flashcard Mastery Deck",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Card ${safeIndex + 1} of ${filteredCards.size} • $masteredCount Mastered",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AccentEmerald.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.FilterListOff, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(12.dp))
                        Text("Zero Dupes Guaranteed", fontSize = 10.sp, color = AccentEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Question Format Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedStyleFilter == "ALL",
                    onClick = { selectedStyleFilter = "ALL"; currentIndex = 0 },
                    label = { Text("All Formats") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SecondaryViolet,
                        selectedLabelColor = Color.White,
                        containerColor = CardDarkSurface,
                        labelColor = TextSecondaryDark
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedStyleFilter == "DIRECT_QA",
                    onClick = { selectedStyleFilter = "DIRECT_QA"; currentIndex = 0 },
                    label = { Text("Direct Q&A") }
                )
            }
            item {
                FilterChip(
                    selected = selectedStyleFilter == "FILL_IN_BLANK",
                    onClick = { selectedStyleFilter = "FILL_IN_BLANK"; currentIndex = 0 },
                    label = { Text("Fill in Blank") }
                )
            }
            item {
                FilterChip(
                    selected = selectedStyleFilter == "TRUE_FALSE",
                    onClick = { selectedStyleFilter = "TRUE_FALSE"; currentIndex = 0 },
                    label = { Text("True/False") }
                )
            }
            item {
                FilterChip(
                    selected = selectedStyleFilter == "APPLICATION",
                    onClick = { selectedStyleFilter = "APPLICATION"; currentIndex = 0 },
                    label = { Text("Application") }
                )
            }
        }

        // Active Flashcard Item
        if (currentCard != null) {
            val isMastered = flashcardMasteryMap[currentCard.id] ?: false
            FlipFlashcardView(
                card = currentCard,
                isMastered = isMastered,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // Mastery Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onToggleMastery(currentCard.id) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("toggle_mastery_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isMastered) AccentEmerald else TextPrimaryDark
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isMastered) AccentEmerald else CardDarkBorder
                    )
                ) {
                    Icon(
                        imageVector = if (isMastered) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isMastered) "Mastered" else "Mark Mastered", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (safeIndex < filteredCards.size - 1) {
                            currentIndex++
                        } else {
                            currentIndex = 0 // Loop around
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("next_flashcard_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                ) {
                    Text("Next Card", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateDarkBg)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = SlateDarkBg, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun FlipFlashcardView(
    card: FlashcardItem,
    isMastered: Boolean,
    modifier: Modifier = Modifier
) {
    var isFlipped by remember(card.id) { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "FlipCardRotation"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isMastered) AccentEmerald else PrimaryCyan.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { isFlipped = !isFlipped }
            .testTag("interactive_flashcard_view")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Front Side (Question)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SecondaryViolet.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = card.style.replace("_", " "),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryViolet,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Tap to Reveal Answer",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = PrimaryCyan)
                            }
                        }

                        Text(
                            text = card.question,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SlateDarkBg
                    ) {
                        Text(
                            text = "Concept: ${card.testedConcept}",
                            fontSize = 11.sp,
                            color = PrimaryCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                // Back Side (Answer - flipped graphics)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AccentEmerald.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "VERIFIED ANSWER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentEmerald,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Tap to Flip Back",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AccentEmerald.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.TaskAlt, contentDescription = null, tint = AccentEmerald)
                            }
                        }

                        Text(
                            text = card.answer,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )
                    }

                    Text(
                        text = "Source Topic: ${card.topicName}",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                }
            }
        }
    }
}

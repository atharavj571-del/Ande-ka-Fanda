package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.StudySuite
import com.example.ui.theme.*
import com.example.ui.viewmodel.UploadLogItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentSuite: StudySuite?,
    savedSuites: List<StudySuite>,
    isAnalyzing: Boolean,
    dailyUploadCount: Int,
    maxDailyUploads: Int,
    uploadError: String?,
    uploadHistory: List<UploadLogItem>,
    onSelectSuite: (StudySuite) -> Unit,
    onLoadBiology: () -> Unit,
    onLoadPhysics: () -> Unit,
    onLoadCS: () -> Unit,
    onAttemptUpload: (title: String, text: String, category: String, uploadType: String, fileName: String) -> Boolean,
    onClearUploadError: () -> Unit,
    onRegenerateUnlimited: () -> Unit,
    onShuffleContent: () -> Unit,
    onNavigateToValidation: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToPodcast: () -> Unit,
    onNavigateToDoubtSolver: () -> Unit = {},
    onSendDoubtQuery: (query: String, contextSnippet: String?) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var showInputSheet by remember { mutableStateOf(false) }
    var showQuotaReachedAlert by remember { mutableStateOf(false) }
    var inputTitle by remember { mutableStateOf("") }
    var inputCategory by remember { mutableStateOf("Biology") }
    var inputRawText by remember { mutableStateOf("") }

    // Launchers for Any File, Folders, and Photos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            var fileName = "Document_File"
            var contentText = "Uploaded document URI: $uri"
            try {
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                    }
                }
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val bytes = stream.readBytes()
                    val text = String(bytes, Charsets.UTF_8)
                    if (text.isNotBlank() && text.all { c -> c.code in 9..126 || c.code in 128..255 || c == '\n' || c == '\r' }) {
                        contentText = text.take(5000)
                    }
                }
            } catch (_: Exception) {}

            val success = onAttemptUpload(
                if (inputTitle.isBlank()) fileName else inputTitle,
                "Document File Content ($fileName):\n$contentText",
                inputCategory,
                "FILE",
                fileName
            )
            if (success) {
                showInputSheet = false
            } else {
                showQuotaReachedAlert = true
            }
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val folderName = it.lastPathSegment ?: "Uploaded_Folder"
            val success = onAttemptUpload(
                if (inputTitle.isBlank()) "Folder: $folderName" else inputTitle,
                "Directory study content from uploaded folder: $folderName ($it)",
                inputCategory,
                "FOLDER",
                folderName
            )
            if (success) {
                showInputSheet = false
            } else {
                showQuotaReachedAlert = true
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            var fileName = "Photo_Asset.jpg"
            try {
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {}

            val success = onAttemptUpload(
                if (inputTitle.isBlank()) "Photo: $fileName" else inputTitle,
                "Visual material scan & text summary from photo asset ($fileName).",
                inputCategory,
                "PHOTO",
                fileName
            )
            if (success) {
                showInputSheet = false
            } else {
                showQuotaReachedAlert = true
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val photoName = "CameraScan_${System.currentTimeMillis().toString().takeLast(4)}.jpg"
            val success = onAttemptUpload(
                if (inputTitle.isBlank()) "Camera Scan" else inputTitle,
                "Captured document page visual scan via device camera.",
                inputCategory,
                "PHOTO",
                photoName
            )
            if (success) {
                showInputSheet = false
            } else {
                showQuotaReachedAlert = true
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                PrimaryCyan.copy(alpha = 0.25f),
                                SecondaryViolet.copy(alpha = 0.35f),
                                SlateDarkBg
                            )
                        )
                    )
                    .border(1.dp, CardDarkBorder, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryCyan.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PrimaryCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "INTELLIGENCE ENGINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryCyan
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Content Validation & Syllabus Mapper",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Zero Hallucinations • 100% Synced Notes, Flashcards, Quiz & Podcast",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                }
            }
        }

        // Daily Restriction Tracker Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (dailyUploadCount >= maxDailyUploads) TertiaryAmber else CardDarkBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (dailyUploadCount >= maxDailyUploads) Icons.Default.Block else Icons.Default.LockClock,
                                contentDescription = null,
                                tint = if (dailyUploadCount >= maxDailyUploads) TertiaryAmber else PrimaryCyan
                            )
                            Text(
                                text = "Daily Upload Restriction",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (dailyUploadCount >= maxDailyUploads) TertiaryAmber.copy(alpha = 0.2f) else PrimaryCyan.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (dailyUploadCount >= maxDailyUploads) TertiaryAmber else PrimaryCyan
                            )
                        ) {
                            Text(
                                text = "$dailyUploadCount / $maxDailyUploads Used",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dailyUploadCount >= maxDailyUploads) TertiaryAmber else PrimaryCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (dailyUploadCount.toFloat() / maxDailyUploads.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (dailyUploadCount >= maxDailyUploads) TertiaryAmber else PrimaryCyan,
                        trackColor = SlateDarkBg
                    )

                    Text(
                        text = "Restriction Rule: You can upload files, folders, or photos up to 50 times per day.\n⚡ UNLIMITED GENERATION: Once uploaded, you can generate Unlimited Flashcards, Audio Podcasts, Practice Quizzes, and Notes continuously from your document without using any upload quota!",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )

                    if (uploadHistory.isNotEmpty()) {
                        Text(
                            text = "Today's Upload Activity (${uploadHistory.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uploadHistory) { log ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SlateDarkBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardDarkBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (log.type) {
                                                "FILE" -> Icons.Default.InsertDriveFile
                                                "FOLDER" -> Icons.Default.Folder
                                                "PHOTO" -> Icons.Default.Image
                                                else -> Icons.Default.Description
                                            },
                                            contentDescription = null,
                                            tint = PrimaryCyan,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "#${log.uploadNumberToday} ${log.name}",
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Suite Overview
        item {
            if (currentSuite != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentSuite.subjectCategory.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryCyan
                                )
                                Text(
                                    text = currentSuite.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                onClick = onNavigateToValidation,
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryViolet),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("audit_report_button")
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Audit Matrix", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Module Quick Navigation Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ModuleCard(
                                title = "Notes",
                                subtitle = "${currentSuite.notes.size} Sections",
                                icon = Icons.Outlined.MenuBook,
                                color = PrimaryCyan,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("nav_notes_card"),
                                onClick = onNavigateToNotes
                            )
                            ModuleCard(
                                title = "Flashcards",
                                subtitle = "${currentSuite.flashcards.size} Cards",
                                icon = Icons.Outlined.Style,
                                color = SecondaryViolet,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("nav_flashcards_card"),
                                onClick = onNavigateToFlashcards
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ModuleCard(
                                title = "Quiz Engine",
                                subtitle = "${currentSuite.quizQuestions.size} Questions",
                                icon = Icons.Outlined.Quiz,
                                color = TertiaryAmber,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("nav_quiz_card"),
                                onClick = onNavigateToQuiz
                            )
                            ModuleCard(
                                title = "Podcast Studio",
                                subtitle = "${currentSuite.podcastSegments.size} Dialogue Turns",
                                icon = Icons.Outlined.GraphicEq,
                                color = AccentEmerald,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("nav_podcast_card"),
                                onClick = onNavigateToPodcast
                            )
                        }

                        // Validation Badges Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateDarkBg, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ValidationBadgeItem(icon = Icons.Default.CheckCircle, label = "Unified Scope", color = AccentEmerald)
                            ValidationBadgeItem(icon = Icons.Default.FilterListOff, label = "Zero Dupes", color = PrimaryCyan)
                            ValidationBadgeItem(icon = Icons.Default.FactCheck, label = "Verified", color = TertiaryAmber)
                        }

                        // Unlimited Generator Banner for active document
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SlateDarkBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Unlimited Generation from Active PDF",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Surface(
                                        shape = CircleShape,
                                        color = AccentEmerald.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald)
                                    ) {
                                        Text(
                                            text = "0 Quota Used",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentEmerald,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Generate new flashcards, quiz variations, audio podcasts, and notes continuously from this document without affecting your daily 50 upload limit.",
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onRegenerateUnlimited,
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).height(38.dp).testTag("regenerate_unlimited_button")
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = SlateDarkBg, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Regenerate Suite", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateDarkBg)
                                    }
                                    OutlinedButton(
                                        onClick = onShuffleContent,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryViolet),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).height(38.dp).testTag("shuffle_unlimited_button")
                                    ) {
                                        Icon(Icons.Default.Shuffle, contentDescription = null, tint = SecondaryViolet, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Shuffle Practice", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondaryViolet)
                                    }
                                }
                            }
                        }

                        // Embedded AI Assistant Chat Quick Launcher
                        var quickDoubtInput by remember { mutableStateOf("") }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateDarkBg),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryViolet.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = SecondaryViolet, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "AI Assistant & Doubt Solver",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    TextButton(
                                        onClick = onNavigateToDoubtSolver,
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Text("Open Full Chat ➔", fontSize = 11.sp, color = SecondaryViolet, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = "Ask any doubt or question about \"${currentSuite.title}\". Gemini AI will analyze your syllabus and break it down step-by-step.",
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = quickDoubtInput,
                                        onValueChange = { quickDoubtInput = it },
                                        placeholder = { Text("Ask doubt e.g., 'Explain key formulas'", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f).testTag("quick_doubt_home_input"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SecondaryViolet,
                                            unfocusedBorderColor = CardDarkBorder,
                                            focusedContainerColor = CardDarkSurface,
                                            unfocusedContainerColor = CardDarkSurface,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            if (quickDoubtInput.isNotBlank()) {
                                                onSendDoubtQuery(quickDoubtInput, currentSuite.title)
                                                quickDoubtInput = ""
                                                onNavigateToDoubtSolver()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryViolet),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(38.dp).testTag("ask_doubt_home_button")
                                    ) {
                                        Text("Ask AI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upload & Scan Action Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = PrimaryCyan)
                        Text(
                            text = "Upload & Validate Study Material",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Select any file, folder, photo, or text notes to analyze. Enforces a 50 upload/day restriction limit.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Button(
                        onClick = {
                            if (dailyUploadCount >= maxDailyUploads) {
                                showQuotaReachedAlert = true
                            } else {
                                showInputSheet = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("analyze_material_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (dailyUploadCount >= maxDailyUploads) TertiaryAmber else PrimaryCyan
                        )
                    ) {
                        Icon(
                            imageVector = if (dailyUploadCount >= maxDailyUploads) Icons.Default.Block else Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = SlateDarkBg
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAnalyzing) "Analyzing Syllabus..."
                                   else if (dailyUploadCount >= maxDailyUploads) "Daily Limit Reached ($dailyUploadCount/$maxDailyUploads)"
                                   else "Upload File / Folder / Photo ($dailyUploadCount/$maxDailyUploads)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDarkBg
                        )
                    }
                }
            }
        }

        // Quick Sample Syllabi Presets
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Preset Syllabus Materials",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        PresetChip(
                            title = "Cellular Respiration & Photosynthesis",
                            category = "Biology",
                            icon = Icons.Default.Biotech,
                            color = AccentEmerald,
                            testTag = "preset_biology",
                            onClick = onLoadBiology
                        )
                    }
                    item {
                        PresetChip(
                            title = "Electromagnetism & Waves",
                            category = "Physics",
                            icon = Icons.Default.Bolt,
                            color = TertiaryAmber,
                            testTag = "preset_physics",
                            onClick = onLoadPhysics
                        )
                    }
                    item {
                        PresetChip(
                            title = "Graph Theory & Algorithms",
                            category = "Computer Science",
                            icon = Icons.Default.Code,
                            color = SecondaryViolet,
                            testTag = "preset_cs",
                            onClick = onLoadCS
                        )
                    }
                }
            }
        }

        // Saved Study Suites History
        if (savedSuites.isNotEmpty()) {
            item {
                Text(
                    text = "Saved Study Suites",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            items(savedSuites) { suite ->
                SavedSuiteItem(
                    suite = suite,
                    isSelected = suite.id == currentSuite?.id,
                    onClick = { onSelectSuite(suite) }
                )
            }
        }
    }

    // Daily Limit Warning Alert Dialog
    if (showQuotaReachedAlert || uploadError != null) {
        AlertDialog(
            onDismissRequest = {
                showQuotaReachedAlert = false
                onClearUploadError()
            },
            icon = {
                Icon(Icons.Default.Block, contentDescription = null, tint = TertiaryAmber, modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Daily Upload Restriction Limit", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Text(
                    text = uploadError ?: "You have reached the maximum allowed 50 uploads for today ($dailyUploadCount/$maxDailyUploads used). You can upload files, folders, or photos again tomorrow!",
                    fontSize = 13.sp,
                    color = TextSecondaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQuotaReachedAlert = false
                        onClearUploadError()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TertiaryAmber)
                ) {
                    Text("Understood", color = SlateDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardDarkSurface
        )
    }

    // Input & Upload Material Dialog / Bottom Sheet
    if (showInputSheet) {
        AlertDialog(
            onDismissRequest = { showInputSheet = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Upload Study Material", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("$dailyUploadCount/$maxDailyUploads Today", fontSize = 12.sp, color = PrimaryCyan, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("Study Unit / Topic Title (Optional)") },
                        placeholder = { Text("e.g. Organic Chemistry Chapter 3") },
                        modifier = Modifier.fillMaxWidth().testTag("input_title_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = CardDarkBorder,
                            focusedLabelColor = PrimaryCyan
                        )
                    )
                    OutlinedTextField(
                        value = inputCategory,
                        onValueChange = { inputCategory = it },
                        label = { Text("Subject Category") },
                        modifier = Modifier.fillMaxWidth().testTag("input_category_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = CardDarkBorder,
                            focusedLabelColor = PrimaryCyan
                        )
                    )

                    Text(
                        text = "Choose Upload Source (Counts towards $maxDailyUploads/day limit):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // 4 Quick Upload Sources Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateDarkBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("upload_file_button")
                        ) {
                            Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Any File", fontSize = 11.sp, color = Color.White)
                        }

                        Button(
                            onClick = { folderPickerLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateDarkBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryViolet),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("upload_folder_button")
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = SecondaryViolet, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Folder", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateDarkBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("upload_photo_button")
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Photo", fontSize = 11.sp, color = Color.White)
                        }

                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateDarkBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TertiaryAmber),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("upload_camera_button")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = TertiaryAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    HorizontalDivider(color = CardDarkBorder)

                    OutlinedTextField(
                        value = inputRawText,
                        onValueChange = { inputRawText = it },
                        label = { Text("Or Paste Direct Text / Syllabus Notes") },
                        placeholder = { Text("Paste chapter text, syllabus points or notes...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("input_text_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = CardDarkBorder,
                            focusedLabelColor = PrimaryCyan
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputRawText.isNotBlank()) {
                            val title = if (inputTitle.isBlank()) "Pasted Syllabus Notes" else inputTitle
                            val success = onAttemptUpload(title, inputRawText, inputCategory, "TEXT", "Pasted Notes")
                            if (success) {
                                showInputSheet = false
                            } else {
                                showQuotaReachedAlert = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                    modifier = Modifier.testTag("submit_analysis_button")
                ) {
                    Text("Analyze Text", color = SlateDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInputSheet = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = CardDarkSurface
        )
    }
}

@Composable
fun ModuleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateDarkBg),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, fontSize = 11.sp, color = TextSecondaryDark)
        }
    }
}

@Composable
fun ValidationBadgeItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
fun PresetChip(
    title: String,
    category: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardDarkBorder),
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SavedSuiteItem(
    suite: StudySuite,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isSelected) PrimaryCyan.copy(alpha = 0.15f) else CardDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) PrimaryCyan else CardDarkBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = if (isSelected) PrimaryCyan else TextSecondaryDark
                )
                Column {
                    Text(suite.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${suite.notes.size} Notes • ${suite.quizQuestions.size} Questions", fontSize = 11.sp, color = TextSecondaryDark)
                }
            }
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryCyan)
            }
        }
    }
}

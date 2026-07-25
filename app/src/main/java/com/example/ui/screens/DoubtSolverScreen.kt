package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Psychology
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
import com.example.data.model.StudySuite
import com.example.ui.theme.*
import com.example.ui.viewmodel.DoubtChatMessage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubtSolverScreen(
    currentSuite: StudySuite?,
    chatMessages: List<DoubtChatMessage>,
    isSolvingDoubt: Boolean,
    onSendQuery: (query: String, contextSnippet: String?) -> Unit,
    onClearChat: () -> Unit,
    initialQuery: String? = null
) {
    var inputText by remember { mutableStateOf(initialQuery ?: "") }
    var activeContext by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val quickQuestions = listOf(
        "Explain this concept simply",
        "Give a step-by-step example",
        "What is the memory trick for this?",
        "Why is this answer correct?",
        "Summarize key takeaways"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // AI Assistant Top Header
        Surface(
            color = CardDarkSurface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryCyan.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Psychology,
                                    contentDescription = null,
                                    tint = PrimaryCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "AI Doubt Solver",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = AccentEmerald.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald)
                                ) {
                                    Text(
                                        text = "Active",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentEmerald,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (currentSuite != null) "Context: ${currentSuite.title}" else "Ask any study question or concept doubt",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier.testTag("clear_doubt_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = TextSecondaryDark
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickQuestions.size) { idx ->
                val qText = quickQuestions[idx]
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SlateDarkBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clickable {
                            inputText = qText
                            onSendQuery(qText, activeContext)
                            inputText = ""
                        }
                        .testTag("quick_doubt_chip_$idx")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = qText,
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(chatMessages) { message ->
                DoubtBubbleItem(message = message)
            }

            if (isSolvingDoubt) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryCyan,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "AI Doubt Solver is analyzing and formatting step-by-step explanation...",
                            fontSize = 12.sp,
                            color = PrimaryCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Input Field Bar
        Surface(
            color = CardDarkSurface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask your study doubt or question...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("doubt_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = CardDarkBorder,
                        focusedContainerColor = SlateDarkBg,
                        unfocusedContainerColor = SlateDarkBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 3
                )

                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isSolvingDoubt) {
                            val query = inputText
                            inputText = ""
                            onSendQuery(query, activeContext)
                        }
                    },
                    containerColor = PrimaryCyan,
                    contentColor = SlateDarkBg,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_doubt_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Send Doubt"
                    )
                }
            }
        }
    }
}

@Composable
fun DoubtBubbleItem(message: DoubtChatMessage) {
    ChatMessage(message = message)
}

@Composable
fun ChatMessage(
    message: DoubtChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "USER"
    val timeFormatted = remember(message.timestampMs) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestampMs))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
        ) {
            if (!isUser) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryCyan.copy(alpha = 0.2f),
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Assistant Avatar",
                            tint = PrimaryCyan,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Text(
                    text = "Gemini AI Mentor",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryCyan
                )
            } else {
                Text(
                    text = "You",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryViolet
                )
                Surface(
                    shape = CircleShape,
                    color = SecondaryViolet.copy(alpha = 0.2f),
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = SecondaryViolet,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Text(
                text = "• $timeFormatted",
                fontSize = 10.sp,
                color = TextSecondaryDark
            )
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) SecondaryViolet else CardDarkSurface,
            border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, CardDarkBorder),
            modifier = Modifier
                .widthIn(max = 320.dp)
                .testTag(if (isUser) "user_chat_bubble" else "ai_chat_bubble")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!message.contextSnippet.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SlateDarkBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = PrimaryCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Context: ${message.contextSnippet}",
                                fontSize = 10.sp,
                                color = TextSecondaryDark,
                                maxLines = 2
                            )
                        }
                    }
                }

                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuizQuestion
import com.example.data.model.StudySuite
import com.example.ui.theme.*

@Composable
fun QuizScreen(
    currentSuite: StudySuite?,
    quizAnswersMap: Map<String, String>,
    onSubmitAnswer: (questionId: String, answer: String) -> Unit,
    onShuffleQuiz: () -> Unit,
    onRegenerateUnlimited: () -> Unit = {}
) {
    if (currentSuite == null || currentSuite.quizQuestions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("No Quiz Questions Available", color = TextSecondaryDark)
        }
        return
    }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    val questions = currentSuite.quizQuestions
    val safeIndex = currentQuestionIndex.coerceIn(0, (questions.size - 1).coerceAtLeast(0))
    val currentQuestion = questions.getOrNull(safeIndex)

    val answeredCount = questions.count { quizAnswersMap.containsKey(it.id) }
    val correctCount = questions.count { quizAnswersMap[it.id] == it.correctAnswer }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Unlimited Quiz Banner
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, TertiaryAmber.copy(alpha = 0.5f)),
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
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TertiaryAmber, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Unlimited Quiz Generation Active (0 Quota Used)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(onClick = onShuffleQuiz, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = PrimaryCyan, modifier = Modifier.size(16.dp))
                        }
                        Button(
                            onClick = onRegenerateUnlimited,
                            colors = ButtonDefaults.buttonColors(containerColor = TertiaryAmber),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("New Quiz", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateDarkBg)
                        }
                    }
                }
            }
        }
        // Quiz Header & Progress Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardDarkBorder),
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
                        Column {
                            Text(
                                text = "Syllabus Quiz Engine",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Question ${safeIndex + 1} of ${questions.size} • Score: $correctCount/$answeredCount",
                                fontSize = 12.sp,
                                color = TextSecondaryDark
                            )
                        }

                        IconButton(
                            onClick = onShuffleQuiz,
                            modifier = Modifier.testTag("shuffle_quiz_questions_button")
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = PrimaryCyan)
                        }
                    }

                    // Linear Progress Bar
                    LinearProgressIndicator(
                        progress = { (safeIndex + 1).toFloat() / questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PrimaryCyan,
                        trackColor = SlateDarkBg
                    )
                }
            }
        }

        // Active Question Item
        if (currentQuestion != null) {
            val selectedUserAnswer = quizAnswersMap[currentQuestion.id]

            item {
                QuizQuestionCard(
                    question = currentQuestion,
                    selectedAnswer = selectedUserAnswer,
                    onSelectOption = { option ->
                        if (selectedUserAnswer == null) {
                            onSubmitAnswer(currentQuestion.id, option)
                        }
                    }
                )
            }

            // Explanation & Verification Section
            if (selectedUserAnswer != null) {
                item {
                    val isCorrect = selectedUserAnswer == currentQuestion.correctAnswer
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) AccentEmerald.copy(alpha = 0.12f) else AccentRose.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCorrect) AccentEmerald else AccentRose
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isCorrect) AccentEmerald else AccentRose
                                )
                                Text(
                                    text = if (isCorrect) "Correct Answer!" else "Incorrect - Verified Answer Below",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) AccentEmerald else AccentRose
                                )
                            }

                            Text(
                                text = "Why Correct: ${currentQuestion.explanation}",
                                fontSize = 13.sp,
                                color = Color.White,
                                lineHeight = 18.sp
                            )

                            if (currentQuestion.whyIncorrect.isNotBlank()) {
                                Text(
                                    text = "Why Other Choices Wrong: ${currentQuestion.whyIncorrect}",
                                    fontSize = 12.sp,
                                    color = TextSecondaryDark,
                                    lineHeight = 16.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SlateDarkBg
                            ) {
                                Text(
                                    text = "Source Reference: ${currentQuestion.sourceTopicReference}",
                                    fontSize = 11.sp,
                                    color = PrimaryCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Next Question Navigation Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentQuestionIndex > 0) currentQuestionIndex--
                        },
                        enabled = currentQuestionIndex > 0,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }

                    Button(
                        onClick = {
                            if (currentQuestionIndex < questions.size - 1) currentQuestionIndex++
                        },
                        enabled = currentQuestionIndex < questions.size - 1,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                    ) {
                        Text("Next Question", color = SlateDarkBg, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = SlateDarkBg, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuizQuestionCard(
    question: QuizQuestion,
    selectedAnswer: String?,
    onSelectOption: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
        shape = RoundedCornerShape(16.dp),
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
                Surface(
                    shape = CircleShape,
                    color = TertiaryAmber.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = question.questionType.replace("_", " "),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TertiaryAmber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Ch ${question.chapterNumber} • ${question.topicName}",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }

            Text(
                text = question.questionText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 22.sp
            )

            // Options List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                question.options.forEach { option ->
                    val isSelected = selectedAnswer == option
                    val isCorrectOption = option == question.correctAnswer
                    val showResult = selectedAnswer != null

                    val borderColor = when {
                        showResult && isCorrectOption -> AccentEmerald
                        showResult && isSelected && !isCorrectOption -> AccentRose
                        isSelected -> PrimaryCyan
                        else -> CardDarkBorder
                    }

                    val containerColor = when {
                        showResult && isCorrectOption -> AccentEmerald.copy(alpha = 0.15f)
                        showResult && isSelected && !isCorrectOption -> AccentRose.copy(alpha = 0.15f)
                        isSelected -> PrimaryCyan.copy(alpha = 0.15f)
                        else -> SlateDarkBg
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectOption(option) }
                            .testTag("quiz_option_item")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )

                            if (showResult && isCorrectOption) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald)
                            } else if (showResult && isSelected && !isCorrectOption) {
                                Icon(Icons.Default.Cancel, contentDescription = null, tint = AccentRose)
                            }
                        }
                    }
                }
            }
        }
    }
}

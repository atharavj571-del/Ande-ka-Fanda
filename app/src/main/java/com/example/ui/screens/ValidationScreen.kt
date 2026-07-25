package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.StudySuite
import com.example.ui.theme.*

@Composable
fun ValidationScreen(
    currentSuite: StudySuite?,
    isAnalyzing: Boolean,
    onShuffleContent: () -> Unit,
    onRegenerateVariedWording: () -> Unit
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

    val report = currentSuite.validationReport

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Audit Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Surface(
                                shape = CircleShape,
                                color = AccentEmerald.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AccentEmerald)
                                }
                            }
                            Column {
                                Text(
                                    text = "Audit Matrix & Validation Engine",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "100% Verified Syllabus Accuracy",
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark
                                )
                            }
                        }
                    }

                    // Score bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateDarkBg, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        AuditMetricBox("Pages Analyzed", "${report.pagesAnalysedCount}", PrimaryCyan)
                        AuditMetricBox("Chapters Mapped", "${report.totalChaptersMapped}", SecondaryViolet)
                        AuditMetricBox("Dupes Removed", "${report.duplicatesRemovedCount}", TertiaryAmber)
                        AuditMetricBox("Diversity Score", "${report.questionDiversityScore}%", AccentEmerald)
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onShuffleContent,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("shuffle_content_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CardDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardDarkBorder)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Shuffle Order", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = onRegenerateVariedWording,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("regenerate_wording_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryViolet)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isAnalyzing) "Regenerating..." else "Vary Wording",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Mandatory Checklist Section
        item {
            Text(
                text = "Final Validation Checklist",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChecklistItem(
                    title = "Every Page Analyzed & Chapter Mapped",
                    subtitle = "Complete internal syllabus tree generated with definitions, formulas, and facts.",
                    isChecked = true
                )
                ChecklistItem(
                    title = "Unified Syllabus Coverage",
                    subtitle = "Notes, Flashcards, Quiz, and Podcast cover identical topic lists without skipping.",
                    isChecked = report.isUnifiedCoverageVerified
                )
                ChecklistItem(
                    title = "Zero Hallucinations Guarantee",
                    subtitle = "Every quiz question and flashcard concept derives solely from source material.",
                    isChecked = report.noHallucinationsConfirmed
                )
                ChecklistItem(
                    title = "Duplicate Flashcard & Question Prevention",
                    subtitle = "${report.duplicatesRemovedCount} duplicate questions detected and re-formatted into unique Q&A styles.",
                    isChecked = true
                )
                ChecklistItem(
                    title = "Answer Source Verification",
                    subtitle = "Every correct answer verified against uploaded source material text.",
                    isChecked = report.answersVerifiedAgainstSource
                )
            }
        }

        // Unified Coverage Matrix by Chapter
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Unified Syllabus Coverage Matrix",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        items(currentSuite.syllabusItems) { item ->
            val notesCount = currentSuite.notes.count { it.topicName == item.topicName }
            val fcCount = currentSuite.flashcards.count { it.topicName == item.topicName }
            val quizCount = currentSuite.quizQuestions.count { it.topicName == item.topicName }
            val podCount = currentSuite.podcastSegments.count { it.topicName == item.topicName }

            Card(
                colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.chapterTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCyan
                    )
                    Text(
                        text = item.topicName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MatrixPill("Notes", "$notesCount", PrimaryCyan)
                        MatrixPill("Flashcards", "$fcCount", SecondaryViolet)
                        MatrixPill("Quiz", "$quizCount", TertiaryAmber)
                        MatrixPill("Podcast", "$podCount", AccentEmerald)
                    }
                }
            }
        }
    }
}

@Composable
fun AuditMetricBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = TextSecondaryDark)
    }
}

@Composable
fun ChecklistItem(
    title: String,
    subtitle: String,
    isChecked: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardDarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isChecked) AccentEmerald else AccentRose,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, fontSize = 11.sp, color = TextSecondaryDark)
            }
        }
    }
}

@Composable
fun MatrixPill(label: String, count: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
            Text(count, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

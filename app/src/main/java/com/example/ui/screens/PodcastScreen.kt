package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PodcastSpeechEngine
import com.example.data.model.PodcastSegment
import com.example.data.model.StudySuite
import com.example.ui.theme.*

@Composable
fun PodcastScreen(
    currentSuite: StudySuite?,
    podcastEngine: PodcastSpeechEngine,
    onRegenerateUnlimited: () -> Unit = {}
) {
    if (currentSuite == null || currentSuite.podcastSegments.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("No Podcast Dialogue Generated", color = TextSecondaryDark)
        }
        return
    }

    val segments = currentSuite.podcastSegments
    val isPlaying by podcastEngine.isPlaying.collectAsState()
    val activeIndex by podcastEngine.currentSegmentIndex.collectAsState()
    val playbackSpeed by podcastEngine.playbackSpeed.collectAsState()

    val safeIndex = activeIndex.coerceIn(0, (segments.size - 1).coerceAtLeast(0))
    val activeSegment = segments.getOrNull(safeIndex)

    val listState = rememberLazyListState()

    // Auto scroll transcript to active segment
    LaunchedEffect(safeIndex) {
        try {
            if (safeIndex in segments.indices) {
                listState.animateScrollToItem(safeIndex)
            }
        } catch (_: Exception) {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Unlimited Podcast Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CardDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.5f)),
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
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Unlimited Podcast Episodes Active (0 Quota Used)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Button(
                    onClick = onRegenerateUnlimited,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("New Episode", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateDarkBg)
                }
            }
        }
        // Player Studio Visualizer Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                            Text("AI DUAL-HOST PODCAST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentEmerald)
                        }
                    }

                    Text(
                        text = "Turn ${safeIndex + 1}/${segments.size}",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }

                // Currently Speaking Host Info
                if (activeSegment != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (activeSegment.speaker == "FEMALE_HOST") PrimaryCyan.copy(alpha = 0.2f) else SecondaryViolet.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (activeSegment.speaker == "FEMALE_HOST") Icons.Default.Face3 else Icons.Default.Face,
                                    contentDescription = null,
                                    tint = if (activeSegment.speaker == "FEMALE_HOST") PrimaryCyan else SecondaryViolet,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = activeSegment.speakerName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (activeSegment.speaker == "FEMALE_HOST") "Female Host • Concept Intro & Qs" else "Male Host • Explanations & Memory Tricks",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }

                // Animated Audio Waveform Simulation
                AnimatedWaveform(isPlaying = isPlaying)

                // Audio Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speed selector
                    TextButton(
                        onClick = {
                            val nextSpeed = when (playbackSpeed) {
                                1.0f -> 1.25f
                                1.25f -> 1.5f
                                1.5f -> 2.0f
                                else -> 1.0f
                            }
                            podcastEngine.setSpeed(nextSpeed)
                        },
                        modifier = Modifier.testTag("speed_selector_button")
                    ) {
                        Text("${playbackSpeed}x", fontWeight = FontWeight.Bold, color = PrimaryCyan)
                    }

                    // Previous Turn
                    IconButton(
                        onClick = { podcastEngine.previousSegment() },
                        modifier = Modifier.testTag("prev_turn_button")
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    // Play / Pause FAB
                    FloatingActionButton(
                        onClick = {
                            if (isPlaying) podcastEngine.pause() else podcastEngine.play()
                        },
                        containerColor = PrimaryCyan,
                        contentColor = SlateDarkBg,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("play_pause_podcast_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Next Turn
                    IconButton(
                        onClick = { podcastEngine.nextSegment() },
                        modifier = Modifier.testTag("next_turn_button")
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    // Stop
                    IconButton(onClick = { podcastEngine.stop() }) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = TextSecondaryDark)
                    }
                }
            }
        }

        Text(
            text = "Interactive Conversation Script",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Interactive Transcript List
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(segments) { idx, segment ->
                val isActive = idx == safeIndex
                TranscriptBubbleItem(
                    segment = segment,
                    isActive = isActive,
                    onClick = { podcastEngine.seekToSegment(idx) }
                )
            }
        }
    }
}

@Composable
fun AnimatedWaveform(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnimation")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WavePhase"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .padding(horizontal = 24.dp)
    ) {
        val barWidth = 6.dp.toPx()
        val spacing = 4.dp.toPx()
        val totalBars = (size.width / (barWidth + spacing)).toInt()

        for (i in 0 until totalBars) {
            val heightMultiplier = if (isPlaying) {
                0.2f + 0.8f * kotlin.math.sin((i.toFloat() / totalBars.toFloat() * 3.14f * 2f) + phase * 3.14f).let { kotlin.math.abs(it) }
            } else {
                0.2f
            }
            val barHeight = size.height * heightMultiplier
            val x = i * (barWidth + spacing)
            val y = (size.height - barHeight) / 2f

            drawRoundRect(
                color = if (i % 2 == 0) PrimaryCyan else SecondaryViolet,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

@Composable
fun TranscriptBubbleItem(
    segment: PodcastSegment,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val isFemale = segment.speaker == "FEMALE_HOST"
    val avatarColor = if (isFemale) PrimaryCyan else SecondaryViolet

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) avatarColor.copy(alpha = 0.15f) else CardDarkSurface
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) avatarColor else CardDarkBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("transcript_segment_item")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = avatarColor.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isFemale) Icons.Default.Face3 else Icons.Default.Face,
                        contentDescription = null,
                        tint = avatarColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = segment.speakerName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = avatarColor
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SlateDarkBg
                    ) {
                        Text(
                            text = segment.segmentType,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondaryDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = segment.dialogueText,
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

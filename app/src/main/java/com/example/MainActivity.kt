package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SyllabusTheme {
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route ?: "home"

                val currentSuite by viewModel.currentSuite.collectAsStateWithLifecycle()
                val savedSuites by viewModel.savedSuites.collectAsStateWithLifecycle()
                val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val flashcardMasteryMap by viewModel.flashcardMasteryMap.collectAsStateWithLifecycle()
                val quizAnswersMap by viewModel.quizAnswersMap.collectAsStateWithLifecycle()
                val notesReadMap by viewModel.notesReadMap.collectAsStateWithLifecycle()
                val dailyUploadCount by viewModel.dailyUploadCount.collectAsStateWithLifecycle()
                val uploadError by viewModel.uploadError.collectAsStateWithLifecycle()
                val uploadHistory by viewModel.uploadHistory.collectAsStateWithLifecycle()
                val doubtChatMessages by viewModel.doubtChatMessages.collectAsStateWithLifecycle()
                val isSolvingDoubt by viewModel.isSolvingDoubt.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "SYLLABUS AI",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryCyan
                                    )
                                    Text(
                                        text = currentSuite?.title ?: "Intelligence Engine",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            },
                            actions = {
                                Surface(
                                    onClick = { navController.navigate("doubt_solver") },
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = SecondaryViolet.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryViolet),
                                    modifier = Modifier.padding(end = 8.dp).testTag("top_app_bar_doubt_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Psychology, contentDescription = "AI Doubt Solver", tint = SecondaryViolet, modifier = Modifier.size(14.dp))
                                        Text("AI Doubt Solver", fontSize = 10.sp, color = SecondaryViolet, fontWeight = FontWeight.Bold)
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateDarkBg)
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = SlateDarkBg,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == "home",
                                onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryCyan,
                                    selectedTextColor = PrimaryCyan,
                                    indicatorColor = PrimaryCyan.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_item_home")
                            )
                            NavigationBarItem(
                                selected = currentRoute == "doubt_solver",
                                onClick = { navController.navigate("doubt_solver") },
                                icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Doubt") },
                                label = { Text("AI Doubt", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SecondaryViolet,
                                    selectedTextColor = SecondaryViolet,
                                    indicatorColor = SecondaryViolet.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_item_doubt")
                            )
                            NavigationBarItem(
                                selected = currentRoute == "notes",
                                onClick = { navController.navigate("notes") },
                                icon = { Icon(Icons.Default.MenuBook, contentDescription = "Notes") },
                                label = { Text("Notes", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryCyan,
                                    selectedTextColor = PrimaryCyan,
                                    indicatorColor = PrimaryCyan.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_item_notes")
                            )
                            NavigationBarItem(
                                selected = currentRoute == "flashcards",
                                onClick = { navController.navigate("flashcards") },
                                icon = { Icon(Icons.Default.Style, contentDescription = "Cards") },
                                label = { Text("Cards", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryCyan,
                                    selectedTextColor = PrimaryCyan,
                                    indicatorColor = PrimaryCyan.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_item_flashcards")
                            )
                            NavigationBarItem(
                                selected = currentRoute == "quiz",
                                onClick = { navController.navigate("quiz") },
                                icon = { Icon(Icons.Default.Quiz, contentDescription = "Quiz") },
                                label = { Text("Quiz", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryCyan,
                                    selectedTextColor = PrimaryCyan,
                                    indicatorColor = PrimaryCyan.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_item_quiz")
                            )
                            NavigationBarItem(
                                selected = currentRoute == "podcast",
                                onClick = { navController.navigate("podcast") },
                                icon = { Icon(Icons.Default.GraphicEq, contentDescription = "Podcast") },
                                label = { Text("Podcast", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryCyan,
                                    selectedTextColor = PrimaryCyan,
                                    indicatorColor = PrimaryCyan.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_item_podcast")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = "home"
                        ) {
                            composable("home") {
                                HomeScreen(
                                    currentSuite = currentSuite,
                                    savedSuites = savedSuites,
                                    isAnalyzing = isAnalyzing,
                                    dailyUploadCount = dailyUploadCount,
                                    maxDailyUploads = viewModel.maxDailyUploads,
                                    uploadError = uploadError,
                                    uploadHistory = uploadHistory,
                                    onSelectSuite = { viewModel.selectSuite(it) },
                                    onLoadBiology = { viewModel.loadBiologyPreset() },
                                    onLoadPhysics = { viewModel.loadPhysicsPreset() },
                                    onLoadCS = { viewModel.loadComputerSciencePreset() },
                                    onAttemptUpload = { title, text, category, uploadType, fileName ->
                                        viewModel.attemptUpload(title, text, category, uploadType, fileName)
                                    },
                                    onClearUploadError = { viewModel.clearUploadError() },
                                    onRegenerateUnlimited = { viewModel.regenerateWithVariedWording() },
                                    onShuffleContent = { viewModel.shuffleCurrentSuiteContent() },
                                    onNavigateToValidation = { navController.navigate("validation") },
                                    onNavigateToNotes = { navController.navigate("notes") },
                                    onNavigateToFlashcards = { navController.navigate("flashcards") },
                                    onNavigateToQuiz = { navController.navigate("quiz") },
                                    onNavigateToPodcast = { navController.navigate("podcast") },
                                    onNavigateToDoubtSolver = { navController.navigate("doubt_solver") },
                                    onSendDoubtQuery = { query, snippet -> viewModel.sendDoubtQuery(query, snippet) }
                                )
                            }
                            composable("doubt_solver") {
                                DoubtSolverScreen(
                                    currentSuite = currentSuite,
                                    chatMessages = doubtChatMessages,
                                    isSolvingDoubt = isSolvingDoubt,
                                    onSendQuery = { query, snippet -> viewModel.sendDoubtQuery(query, snippet) },
                                    onClearChat = { viewModel.clearDoubtChat() }
                                )
                            }
                            composable("validation") {
                                ValidationScreen(
                                    currentSuite = currentSuite,
                                    isAnalyzing = isAnalyzing,
                                    onShuffleContent = { viewModel.shuffleCurrentSuiteContent() },
                                    onRegenerateVariedWording = { viewModel.regenerateWithVariedWording() }
                                )
                            }
                            composable("notes") {
                                NotesScreen(
                                    currentSuite = currentSuite,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    notesReadMap = notesReadMap,
                                    onToggleNoteRead = { viewModel.toggleNoteRead(it) },
                                    onRegenerateUnlimited = { viewModel.regenerateWithVariedWording() }
                                )
                            }
                            composable("flashcards") {
                                FlashcardScreen(
                                    currentSuite = currentSuite,
                                    flashcardMasteryMap = flashcardMasteryMap,
                                    onToggleMastery = { viewModel.toggleFlashcardMastery(it) },
                                    onRegenerateUnlimited = { viewModel.regenerateWithVariedWording() },
                                    onShuffle = { viewModel.shuffleCurrentSuiteContent() }
                                )
                            }
                            composable("quiz") {
                                QuizScreen(
                                    currentSuite = currentSuite,
                                    quizAnswersMap = quizAnswersMap,
                                    onSubmitAnswer = { qId, ans -> viewModel.submitQuizAnswer(qId, ans) },
                                    onShuffleQuiz = { viewModel.shuffleCurrentSuiteContent() },
                                    onRegenerateUnlimited = { viewModel.regenerateWithVariedWording() }
                                )
                            }
                            composable("podcast") {
                                PodcastScreen(
                                    currentSuite = currentSuite,
                                    podcastEngine = viewModel.podcastEngine,
                                    onRegenerateUnlimited = { viewModel.regenerateWithVariedWording() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

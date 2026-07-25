package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.PodcastSpeechEngine
import com.example.data.local.StudyDatabase
import com.example.data.local.StudyRepository
import com.example.data.model.*
import com.example.data.remote.IntelligenceEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class UploadLogItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "FILE", "FOLDER", "PHOTO", "TEXT"
    val timestampMs: Long = System.currentTimeMillis(),
    val uploadNumberToday: Int
)

data class DoubtChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val contextSnippet: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository
    val podcastEngine: PodcastSpeechEngine
    private val prefs = application.getSharedPreferences("syllabus_upload_restrictions", Context.MODE_PRIVATE)

    private val _dailyUploadCount = MutableStateFlow(0)
    val dailyUploadCount: StateFlow<Int> = _dailyUploadCount.asStateFlow()

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError.asStateFlow()

    private val _uploadHistory = MutableStateFlow<List<UploadLogItem>>(emptyList())
    val uploadHistory: StateFlow<List<UploadLogItem>> = _uploadHistory.asStateFlow()

    val maxDailyUploads = 50

    private val _doubtChatMessages = MutableStateFlow<List<DoubtChatMessage>>(
        listOf(
            DoubtChatMessage(
                sender = "AI",
                text = "Hello! I am your AI Assistant Doubt Solver. Ask me any doubt or question about your study material, flashcards, quizzes, or formulas, and I'll break it down step-by-step!"
            )
        )
    )
    val doubtChatMessages: StateFlow<List<DoubtChatMessage>> = _doubtChatMessages.asStateFlow()

    private val _isSolvingDoubt = MutableStateFlow(false)
    val isSolvingDoubt: StateFlow<Boolean> = _isSolvingDoubt.asStateFlow()

    init {
        podcastEngine = PodcastSpeechEngine(application)
        val dao = StudyDatabase.getInstance(application).studyDao()
        repository = StudyRepository(dao)
        syncDailyUploadCount()
        loadInitialDefault()
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun syncDailyUploadCount() {
        val today = getTodayDateString()
        val savedDate = prefs.getString("last_upload_date", "")
        if (savedDate == today) {
            _dailyUploadCount.value = prefs.getInt("daily_upload_count", 0)
        } else {
            // New calendar day reset
            prefs.edit().putString("last_upload_date", today).putInt("daily_upload_count", 0).apply()
            _dailyUploadCount.value = 0
        }
    }

    fun canUploadMore(): Boolean {
        syncDailyUploadCount()
        return _dailyUploadCount.value < maxDailyUploads
    }

    fun attemptUpload(
        title: String,
        rawText: String,
        category: String,
        uploadType: String,
        fileName: String
    ): Boolean {
        syncDailyUploadCount()
        val currentCount = _dailyUploadCount.value
        if (currentCount >= maxDailyUploads) {
            _uploadError.value = "Daily restriction reached! Maximum 50 uploads (Files/Folders/Photos) allowed per day ($currentCount/$maxDailyUploads used)."
            return false
        }

        val newCount = currentCount + 1
        val today = getTodayDateString()
        prefs.edit()
            .putString("last_upload_date", today)
            .putInt("daily_upload_count", newCount)
            .apply()

        _dailyUploadCount.value = newCount
        _uploadError.value = null

        val logItem = UploadLogItem(
            name = fileName,
            type = uploadType,
            uploadNumberToday = newCount
        )
        _uploadHistory.value = listOf(logItem) + _uploadHistory.value

        analyzeNewMaterial(title, rawText, category)
        return true
    }

    fun clearUploadError() {
        _uploadError.value = null
    }

    val savedSuites: StateFlow<List<StudySuite>> = repository.savedSuites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSuite = MutableStateFlow<StudySuite?>(null)
    val currentSuite: StateFlow<StudySuite?> = _currentSuite.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _selectedChapterIndex = MutableStateFlow(0)
    val selectedChapterIndex: StateFlow<Int> = _selectedChapterIndex.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _flashcardMasteryMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val flashcardMasteryMap: StateFlow<Map<String, Boolean>> = _flashcardMasteryMap.asStateFlow()

    private val _quizAnswersMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val quizAnswersMap: StateFlow<Map<String, String>> = _quizAnswersMap.asStateFlow()

    private val _notesReadMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val notesReadMap: StateFlow<Map<String, Boolean>> = _notesReadMap.asStateFlow()

    private fun loadInitialDefault() {
        viewModelScope.launch {
            try {
                val bioPreset = IntelligenceEngine.getBiologyPreset()
                _currentSuite.value = bioPreset
                podcastEngine.loadPlaylist(bioPreset.podcastSegments)
                repository.saveSuite(bioPreset)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    fun selectSuite(suite: StudySuite) {
        try {
            _currentSuite.value = suite
            _selectedChapterIndex.value = 0
            podcastEngine.loadPlaylist(suite.podcastSegments)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun loadBiologyPreset() {
        viewModelScope.launch {
            try {
                val preset = IntelligenceEngine.getBiologyPreset()
                selectSuite(preset)
                repository.saveSuite(preset)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    fun loadPhysicsPreset() {
        viewModelScope.launch {
            try {
                val preset = IntelligenceEngine.getPhysicsPreset()
                selectSuite(preset)
                repository.saveSuite(preset)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    fun loadComputerSciencePreset() {
        viewModelScope.launch {
            try {
                val preset = IntelligenceEngine.getComputerSciencePreset()
                selectSuite(preset)
                repository.saveSuite(preset)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    fun analyzeNewMaterial(title: String, rawText: String, category: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val newSuite = repository.analyzeAndSaveNewSuite(title, rawText, category)
                selectSuite(newSuite)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun setSelectedChapter(index: Int) {
        _selectedChapterIndex.value = index
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFlashcardMastery(cardId: String) {
        val current = _flashcardMasteryMap.value.toMutableMap()
        current[cardId] = !(current[cardId] ?: false)
        _flashcardMasteryMap.value = current
    }

    fun submitQuizAnswer(questionId: String, answer: String) {
        val current = _quizAnswersMap.value.toMutableMap()
        current[questionId] = answer
        _quizAnswersMap.value = current
    }

    fun toggleNoteRead(noteId: String) {
        val current = _notesReadMap.value.toMutableMap()
        current[noteId] = !(current[noteId] ?: false)
        _notesReadMap.value = current
    }

    fun shuffleCurrentSuiteContent() {
        val suite = _currentSuite.value ?: return
        val shuffledFlashcards = suite.flashcards.shuffled()
        val shuffledQuiz = suite.quizQuestions.shuffled()
        val shuffledPodcast = suite.podcastSegments.shuffled()

        val updatedSuite = suite.copy(
            flashcards = shuffledFlashcards,
            quizQuestions = shuffledQuiz,
            podcastSegments = shuffledPodcast
        )
        _currentSuite.value = updatedSuite
        podcastEngine.loadPlaylist(shuffledPodcast)
    }

    fun regenerateWithVariedWording() {
        val suite = _currentSuite.value ?: return
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val regenerated = IntelligenceEngine.analyzeAndGenerate(
                    inputTitle = "${suite.title} (Regenerated)",
                    rawTextContent = suite.rawInputSource,
                    subjectCategory = suite.subjectCategory
                )
                selectSuite(regenerated)
                repository.saveSuite(regenerated)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun sendDoubtQuery(userQuery: String, contextSnippet: String? = null) {
        if (userQuery.isBlank()) return
        val userMsg = DoubtChatMessage(sender = "USER", text = userQuery, contextSnippet = contextSnippet)
        _doubtChatMessages.value = _doubtChatMessages.value + userMsg

        viewModelScope.launch {
            _isSolvingDoubt.value = true
            try {
                val answer = IntelligenceEngine.solveDoubt(
                    userQuery = userQuery,
                    contextSnippet = contextSnippet,
                    studySuite = _currentSuite.value
                )
                val aiMsg = DoubtChatMessage(sender = "AI", text = answer, contextSnippet = contextSnippet)
                _doubtChatMessages.value = _doubtChatMessages.value + aiMsg
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = DoubtChatMessage(sender = "AI", text = "Sorry, I encountered an issue resolving that doubt. Please try asking again.")
                _doubtChatMessages.value = _doubtChatMessages.value + errorMsg
            } finally {
                _isSolvingDoubt.value = false
            }
        }
    }

    fun clearDoubtChat() {
        _doubtChatMessages.value = listOf(
            DoubtChatMessage(
                sender = "AI",
                text = "Chat cleared! Ask me any doubt or question about your study material, flashcards, or quizzes."
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        podcastEngine.shutdown()
    }
}

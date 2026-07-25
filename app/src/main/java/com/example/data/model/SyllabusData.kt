package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SyllabusItem(
    val id: String,
    val chapterNumber: Int,
    val chapterTitle: String,
    val topicName: String,
    val subtopics: List<String>,
    val definitions: List<String>,
    val formulas: List<String>,
    val examples: List<String>,
    val keywords: List<String>,
    val importantFacts: List<String>
)

@JsonClass(generateAdapter = true)
data class NoteItem(
    val id: String,
    val chapterNumber: Int,
    val chapterTitle: String,
    val topicName: String,
    val title: String,
    val summaryText: String,
    val detailedBody: String,
    val keyTakeaways: List<String>,
    val formulasAndTables: String? = null,
    val diagramDescription: String? = null,
    val isRead: Boolean = false
)

enum class FlashcardStyle {
    DIRECT_QA,
    FILL_IN_BLANK,
    TRUE_FALSE,
    IDENTIFICATION,
    APPLICATION
}

@JsonClass(generateAdapter = true)
data class FlashcardItem(
    val id: String,
    val chapterNumber: Int,
    val chapterTitle: String,
    val topicName: String,
    val question: String,
    val answer: String,
    val style: String = "DIRECT_QA",
    val testedConcept: String,
    val isMastered: Boolean = false,
    val isDuplicateResolved: Boolean = true
)

enum class QuestionType {
    MCQ,
    FILL_BLANK,
    TRUE_FALSE,
    ASSERTION_REASON,
    MATCHING
}

@JsonClass(generateAdapter = true)
data class QuizQuestion(
    val id: String,
    val chapterNumber: Int,
    val chapterTitle: String,
    val topicName: String,
    val questionType: String = "MCQ",
    val questionText: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val explanation: String,
    val whyIncorrect: String = "",
    val sourceTopicReference: String,
    val selectedAnswer: String? = null,
    val isAnsweredCorrectly: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class PodcastSegment(
    val id: String,
    val chapterNumber: Int,
    val chapterTitle: String,
    val topicName: String,
    val speaker: String, // "FEMALE_HOST" or "MALE_HOST"
    val speakerName: String, // e.g. "Dr. Sarah (Host)" or "Alex (Co-Host)"
    val dialogueText: String,
    val segmentType: String // "INTRO", "EXPLANATION", "MEMORY_TRICK", "SUMMARY"
)

@JsonClass(generateAdapter = true)
data class ValidationChecklist(
    val pagesAnalysedCount: Int,
    val totalChaptersMapped: Int,
    val totalTopicsMapped: Int,
    val isUnifiedCoverageVerified: Boolean = true,
    val noHallucinationsConfirmed: Boolean = true,
    val duplicatesRemovedCount: Int,
    val answersVerifiedAgainstSource: Boolean = true,
    val questionDiversityScore: Int = 100,
    val auditTimestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class StudySuite(
    val id: String,
    val title: String,
    val subjectCategory: String,
    val rawInputSource: String,
    val syllabusItems: List<SyllabusItem>,
    val notes: List<NoteItem>,
    val flashcards: List<FlashcardItem>,
    val quizQuestions: List<QuizQuestion>,
    val podcastSegments: List<PodcastSegment>,
    val validationReport: ValidationChecklist
)

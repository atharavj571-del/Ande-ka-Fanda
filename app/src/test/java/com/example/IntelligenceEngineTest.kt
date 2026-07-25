package com.example

import com.example.data.remote.IntelligenceEngine
import org.junit.Assert.*
import org.junit.Test

class IntelligenceEngineTest {

    @Test
    fun testBiologyPresetValidation() {
        val suite = IntelligenceEngine.getBiologyPreset()
        assertNotNull(suite)
        assertTrue(suite.notes.isNotEmpty())
        assertTrue(suite.flashcards.isNotEmpty())
        assertTrue(suite.quizQuestions.isNotEmpty())
        assertTrue(suite.podcastSegments.isNotEmpty())

        // Verify Unified Syllabus Coverage
        val report = suite.validationReport
        assertTrue(report.isUnifiedCoverageVerified)
        assertTrue(report.noHallucinationsConfirmed)
        assertTrue(report.answersVerifiedAgainstSource)
    }

    @Test
    fun testZeroDuplicateFlashcards() {
        val suite = IntelligenceEngine.getBiologyPreset()
        val questions = suite.flashcards.map { it.question }
        val uniqueQuestions = questions.distinct()
        assertEquals(questions.size, uniqueQuestions.size)
    }
}

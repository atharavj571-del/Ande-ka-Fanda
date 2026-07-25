package com.example.data.local

import com.example.data.model.StudySuite
import com.example.data.remote.IntelligenceEngine
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StudyRepository(private val dao: StudyDao) {

    private val moshi by lazy {
        try {
            Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
        } catch (t: Throwable) {
            Moshi.Builder().build()
        }
    }

    private val adapter by lazy {
        try {
            moshi.adapter(StudySuite::class.java)
        } catch (t: Throwable) {
            null
        }
    }

    val savedSuites: Flow<List<StudySuite>> = dao.getAllStudySuites().map { list ->
        list.mapNotNull { entity ->
            try {
                adapter?.fromJson(entity.jsonPayload)
            } catch (t: Throwable) {
                null
            }
        }
    }

    suspend fun getSuiteById(id: String): StudySuite? = withContext(Dispatchers.IO) {
        val entity = dao.getStudySuiteById(id) ?: return@withContext null
        try {
            adapter?.fromJson(entity.jsonPayload)
        } catch (t: Throwable) {
            null
        }
    }

    suspend fun saveSuite(suite: StudySuite) = withContext(Dispatchers.IO) {
        try {
            val json = adapter?.toJson(suite) ?: return@withContext
            val entity = StudySuiteEntity(
                id = suite.id,
                title = suite.title,
                subjectCategory = suite.subjectCategory,
                rawInputSource = suite.rawInputSource,
                jsonPayload = json,
                updatedTimestamp = System.currentTimeMillis()
            )
            dao.insertOrUpdateSuite(entity)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    suspend fun deleteSuite(id: String) = withContext(Dispatchers.IO) {
        dao.deleteSuiteById(id)
    }

    suspend fun analyzeAndSaveNewSuite(
        title: String,
        rawText: String,
        category: String
    ): StudySuite = withContext(Dispatchers.IO) {
        val suite = IntelligenceEngine.analyzeAndGenerate(title, rawText, category)
        saveSuite(suite)
        suite
    }
}

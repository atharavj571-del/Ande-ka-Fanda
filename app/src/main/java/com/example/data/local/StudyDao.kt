package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_suites ORDER BY updatedTimestamp DESC")
    fun getAllStudySuites(): Flow<List<StudySuiteEntity>>

    @Query("SELECT * FROM study_suites WHERE id = :id LIMIT 1")
    suspend fun getStudySuiteById(id: String): StudySuiteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSuite(suite: StudySuiteEntity)

    @Query("DELETE FROM study_suites WHERE id = :id")
    suspend fun deleteSuiteById(id: String)
}

package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_suites")
data class StudySuiteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subjectCategory: String,
    val rawInputSource: String,
    val jsonPayload: String, // Full JSON serialization of StudySuite
    val updatedTimestamp: Long = System.currentTimeMillis()
)

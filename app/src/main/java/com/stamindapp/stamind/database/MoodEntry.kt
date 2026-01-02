package com.stamindapp.stamind.database

data class MoodEntry(
    val date: String,  // Format: "yyyy-MM-dd"
    val emoji: String,
    val moodIndex: Int,
    val timestamp: Long = System.currentTimeMillis()
)
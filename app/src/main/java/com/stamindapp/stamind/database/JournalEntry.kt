package com.stamindapp.stamind.database

import com.stamindapp.stamind.engine.Suggestion

data class JournalEntry(
    val date: String, // "yyyy-MM-dd" formatında
    val journalText: String, // Kullanıcının yazdığı günlük
    val duygusal_durum: String,
    val analiz_ozeti: String,
    val ham_puani: Int,
    val puan_aciklamasi: String = "",
    val destek_mesaji: String,
    val temalar: List<String> = emptyList(),
    val gunluk_oneri: List<Suggestion> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false  // Favoriler filtresi için
)

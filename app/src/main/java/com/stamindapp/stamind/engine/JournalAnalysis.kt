package com.stamindapp.stamind.engine

import kotlinx.serialization.Serializable

@Serializable
data class JournalAnalysis(
    val duygusal_durum: String,
    val analiz_ozeti: String,
    val ham_puani: Int,
    val puan_aciklamasi: String = "",
    val destek_mesaji: String,
    val temalar: List<String> = emptyList(),
    val gunluk_oneri: List<Suggestion> = emptyList()
)

fun JournalAnalysis.getQuantizedScore(): Int {
    val block = 10
    return ((this.ham_puani + 5) / block) * block
}

@Serializable
data class Suggestion(
    val baslik: String,
    val detay: String
)
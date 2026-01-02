package com.stamindapp.stamind.model

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stamindapp.stamind.auth.SubscriptionManager
import com.stamindapp.stamind.database.JournalEntry
import com.stamindapp.stamind.database.MoodEntry
import com.stamindapp.stamind.engine.GeminiService
import com.stamindapp.stamind.engine.JournalAnalysis
import com.stamindapp.stamind.repository.SyncedJournalRepository
import com.stamindapp.stamind.repository.SyncedMoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class JournalViewModel(context: Context) : ViewModel() {

    private val _navigateToDailyResult = MutableStateFlow(false)
    val navigateToDailyResult = _navigateToDailyResult.asStateFlow()

    private val _showOffer = MutableStateFlow(false)
    val showOffer = _showOffer.asStateFlow()

    var latestAnalysis by mutableStateOf<JournalAnalysis?>(null)
        private set

    var selectedDateAnalysis by mutableStateOf<JournalEntry?>(null)
        private set

    private val geminiService = GeminiService(context)
    private val subscriptionManager = SubscriptionManager(context)


    private val journalRepository = SyncedJournalRepository()
    private val moodRepository = SyncedMoodRepository()

    val allJournalEntries: StateFlow<List<JournalEntry>> =
        journalRepository.allJournalEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val lastSevenDaysMoods: StateFlow<List<MoodEntry>> =
        moodRepository.getLastSevenDays().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isPremium = subscriptionManager.isPremiumFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .format(Calendar.getInstance().time)

    val todayJournalCount = allJournalEntries
        .map { list -> list.count { it.date == todayDate } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val remainingQuota = combine(isPremium, todayJournalCount) { premium, count ->
        val maxAllowed = if (premium) 10 else 1
        (maxAllowed - count).coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    // ================== HAFTALIK INSIGHT CACHE ==================
    private val _weeklyInsight = MutableStateFlow<String?>(null)
    val weeklyInsight: StateFlow<String?> = _weeklyInsight.asStateFlow()

    private val _weeklyInsightLoading = MutableStateFlow(false)
    val weeklyInsightLoading: StateFlow<Boolean> = _weeklyInsightLoading.asStateFlow()

    // Haftalık entry'lerin hash'ini tutarak sadece değişiklikte yeniden üret
    private var lastWeeklyEntriesHash: Int = 0

    init {
        // Haftalık entry'leri izle ve değişiklikte insight üret
        viewModelScope.launch {
            combine(allJournalEntries, isPremium) { entries, premium ->
                Pair(entries, premium)
            }.distinctUntilChanged().collect { (entries, premium) ->
                if (premium) {
                    generateWeeklyInsightIfNeeded(entries)
                }
            }
        }
    }

    private suspend fun generateWeeklyInsightIfNeeded(allEntries: List<JournalEntry>) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val weekAgo = calendar.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val weeklyEntries = allEntries.filter { entry ->
            try {
                val entryDate = dateFormat.parse(entry.date)
                entryDate?.after(weekAgo) == true
            } catch (e: Exception) {
                false
            }
        }

        // Entry'lerin hash'ini hesapla (timestamp ve tarih bazlı)
        val currentHash =
            weeklyEntries.map { "${it.timestamp}-${it.date}-${it.ham_puani}" }.hashCode()

        // Hash değişmediyse ve zaten insight varsa, yeniden üretme
        if (currentHash == lastWeeklyEntriesHash && _weeklyInsight.value != null) {
            return
        }

        // Yeni entry'ler var, insight üret
        if (weeklyEntries.isEmpty()) {
            _weeklyInsight.value = null
            lastWeeklyEntriesHash = currentHash
            return
        }

        _weeklyInsightLoading.value = true

        try {
            val scores = weeklyEntries.map { it.ham_puani }
            val avgScore = scores.average().toInt()
            val trend = if (scores.size >= 2) {
                val firstHalf = scores.take(scores.size / 2).average()
                val secondHalf = scores.drop(scores.size / 2).average()
                when {
                    secondHalf > firstHalf + 10 -> "yükselen"
                    secondHalf < firstHalf - 10 -> "düşen"
                    else -> "stabil"
                }
            } else "belirsiz"

            // Haftalık notları hazırla
            val weeklyNotes = weeklyEntries.take(3).joinToString("; ") { it.analiz_ozeti }

            // GeminiService'deki generateWeeklyInsight fonksiyonunu kullan
            _weeklyInsight.value = geminiService.generateWeeklyInsight(
                weeklyCount = weeklyEntries.size,
                avgScore = avgScore,
                trend = trend,
                weeklyNotes = weeklyNotes
            )
            lastWeeklyEntriesHash = currentHash
        } catch (e: Exception) {
            _weeklyInsight.value = "Bu hafta ${weeklyEntries.size} günlük yazdın. Devam et!"
            lastWeeklyEntriesHash = currentHash
        } finally {
            _weeklyInsightLoading.value = false
        }
    }

    // Manuel olarak insight yenileme (kullanıcı isterse)
    fun refreshWeeklyInsight() {
        lastWeeklyEntriesHash = 0 // Hash'i sıfırla, yeniden üretilsin
        viewModelScope.launch {
            generateWeeklyInsightIfNeeded(allJournalEntries.value)
        }
    }

    fun onNavigationDone() {
        _navigateToDailyResult.value = false
    }

    fun onOfferNavigationDone() {
        _showOffer.value = false
    }

    fun analyzeJournalEntry(entry: String) {
        viewModelScope.launch {
            val jsonString = geminiService.analyzeJournalEntry(entry)

            try {
                val cleanJsonString = jsonString
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                val analysisObject = jsonParser.decodeFromString<JournalAnalysis>(cleanJsonString)

                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Calendar.getInstance().time)

                val journalEntry = JournalEntry(
                    date = currentDate,
                    journalText = entry,
                    duygusal_durum = analysisObject.duygusal_durum,
                    analiz_ozeti = analysisObject.analiz_ozeti,
                    ham_puani = analysisObject.ham_puani,
                    puan_aciklamasi = analysisObject.puan_aciklamasi,
                    destek_mesaji = analysisObject.destek_mesaji,
                    temalar = analysisObject.temalar,
                    gunluk_oneri = analysisObject.gunluk_oneri
                )

                journalRepository.insertJournalEntry(journalEntry)


                selectedDateAnalysis = journalEntry
                latestAnalysis = JournalAnalysis(
                    duygusal_durum = analysisObject.duygusal_durum,
                    analiz_ozeti = analysisObject.analiz_ozeti,
                    ham_puani = analysisObject.ham_puani,
                    puan_aciklamasi = analysisObject.puan_aciklamasi,
                    destek_mesaji = analysisObject.destek_mesaji,
                    temalar = analysisObject.temalar,
                    gunluk_oneri = analysisObject.gunluk_oneri
                )

                _navigateToDailyResult.value = true

            } catch (e: Exception) {
                println("JSON Ayrıştırma Hatası: ${e.message}")
                latestAnalysis = JournalAnalysis(
                    duygusal_durum = "Analiz Hatası",
                    analiz_ozeti = "Yapay zeka yanıtı işlenirken bir sorun oluştu.",
                    ham_puani = 0,
                    destek_mesaji = "Lütfen metninizi kontrol edip tekrar deneyin.",
                    gunluk_oneri = emptyList()
                )
                _navigateToDailyResult.value = true
            }
        }
    }

    fun loadJournalByDate(date: String) {
        viewModelScope.launch {
            selectedDateAnalysis = journalRepository.getJournalByDate(date)
        }
    }

    fun onJournalEntrySelected(entry: JournalEntry) {
        selectedDateAnalysis = entry
    }

    fun deleteJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            try {
                journalRepository.deleteJournalEntry(entry)
                selectedDateAnalysis = null
            } catch (e: Exception) {
                println("Silme Hatası: ${e.message}")
            }
        }
    }

    fun tryAnalyzeJournalEntry(entry: String) {
        viewModelScope.launch {
            if (_isSubmitting.value) return@launch
            if (entry.length < 5) {
                latestAnalysis = JournalAnalysis(
                    duygusal_durum = "Uyarı",
                    analiz_ozeti = "Biraz daha detaylı yazmayı dener misin?",
                    ham_puani = 0,
                    destek_mesaji = "En az 5 karakter olmalı.",
                    gunluk_oneri = emptyList()
                )
                return@launch
            }
            if (remainingQuota.value <= 0) {
                _showOffer.value = true
            } else {
                _isSubmitting.value = true
                try {
                    analyzeJournalEntry(entry)
                } finally {
                    _isSubmitting.value = false
                }
            }
        }
    }
}

class JournalViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JournalViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

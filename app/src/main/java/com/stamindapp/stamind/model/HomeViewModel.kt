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
import com.stamindapp.stamind.repository.SyncedJournalRepository
import com.stamindapp.stamind.repository.SyncedMoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeViewModel(context: Context) : ViewModel() {

    private val moodRepository: SyncedMoodRepository = SyncedMoodRepository()
    private val journalRepository: SyncedJournalRepository = SyncedJournalRepository()
    private val subscriptionManager: SubscriptionManager = SubscriptionManager(context)

    var selectedMoodIndex by mutableStateOf(-1)
        private set

    val lastSevenDaysMoods = moodRepository.getLastSevenDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastSevenDaysJournals = journalRepository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastAnalysis = journalRepository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isPremium: StateFlow<Boolean> = subscriptionManager.isPremiumFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentStreak: StateFlow<Int> = moodRepository.observeStreak()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Firebase'den kullanıcı ismini al (ilk isim)
    val userName: String = com.google.firebase.auth.FirebaseAuth.getInstance()
        .currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Dostum"

    fun selectMood(index: Int, emoji: String, date: String? = null) {
        if (date == null) {
            selectedMoodIndex = index
        }
        viewModelScope.launch {
            val targetDate = date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().time)
            moodRepository.saveMood(targetDate, emoji, index)
            // Refresh moods to ensure UI consistency immediately (optional if repository is reactive)
        }
    }

    private fun checkAndAutoSelectMood(entries: List<JournalEntry>) {
        // Otomatik mood seçimi gerekirse buraya eklenebilir.
        // Şu an için manuel seçim veya repository verisi kullanılıyor.
    }


    fun getMoodEmojiForDate(date: String): String? {
        return lastSevenDaysMoods.value.find { it.date == date }?.emoji
    }

    init {
        // Journal entries değiştiğinde (yeni analiz geldiğinde) kontrol et
        viewModelScope.launch {
            lastAnalysis.collect { entries ->
                checkAndAutoSelectMood(entries)
            }
        }
    }
}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

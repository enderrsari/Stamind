package com.stamindapp.stamind.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stamindapp.stamind.database.MoodEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SyncedMoodRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getMoodByDate(date: String): Flow<MoodEntry?> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val reg = firestore.collection("users")
            .document(currentUser.uid)
            .collection("moods")
            .document(date)
            .addSnapshotListener { snap, _ ->
                val data = snap?.data
                val item = if (data != null) {
                    MoodEntry(
                        date = data["date"] as? String ?: date,
                        emoji = data["emoji"] as? String ?: "",
                        moodIndex = (data["moodIndex"] as? Number)?.toInt() ?: 0,
                        timestamp = (data["timestamp"] as? Number)?.toLong()
                            ?: System.currentTimeMillis()
                    )
                } else null
                trySend(item)
            }
        awaitClose { reg.remove() }
    }

    fun getLastSevenDays(): Flow<List<MoodEntry>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val reg = firestore.collection("users")
            .document(currentUser.uid)
            .collection("moods")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(7)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { d ->
                    val data = d.data ?: return@mapNotNull null
                    MoodEntry(
                        date = data["date"] as? String ?: d.id,
                        emoji = data["emoji"] as? String ?: "",
                        moodIndex = (data["moodIndex"] as? Number)?.toInt() ?: 0,
                        timestamp = (data["timestamp"] as? Number)?.toLong()
                            ?: System.currentTimeMillis()
                    )
                }?.sortedBy { it.date } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun saveMood(date: String, emoji: String, moodIndex: Int) {
        val currentUser = auth.currentUser ?: return
        firestore.collection("users")
            .document(currentUser.uid)
            .collection("moods")
            .document(date)
            .set(
                hashMapOf(
                    "date" to date,
                    "emoji" to emoji,
                    "moodIndex" to moodIndex,
                    "timestamp" to System.currentTimeMillis()
                )
            )
            .await()
    }

    suspend fun deleteMood(date: String) {
        val currentUser = auth.currentUser ?: return
        firestore.collection("users")
            .document(currentUser.uid)
            .collection("moods")
            .document(date)
            .delete()
            .await()
    }

    fun observeStreak(): Flow<Int> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(0)
            awaitClose { }
            return@callbackFlow
        }

        // Sadece tarihleri alıp sıralıyoruz
        val reg = firestore.collection("users")
            .document(currentUser.uid)
            .collection("moods")
            .addSnapshotListener { snap, _ ->
                if (snap == null || snap.isEmpty) {
                    trySend(0)
                    return@addSnapshotListener
                }

                val dates = snap.documents.mapNotNull { it.id }.sortedDescending()
                if (dates.isEmpty()) {
                    trySend(0)
                    return@addSnapshotListener
                }

                // Bugünün tarihi (format: yyyy-MM-dd)
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(java.util.Calendar.getInstance().time)

                // Dünün tarihi
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                val yesterday =
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(calendar.time)

                // Streak hesaplama
                var currentStreak = 0

                // Başlangıç noktasını belirle: Bugün girildiyse bugünden, girilmediyse dünden kontrol et (eğer dün varsa)
                // Eğer ne bugün ne dün varsa streak zaten 0'dır (veya kırılmıştır)

                var checkDate: String? = null

                // Eğer bugün mood girdiyse
                if (dates.contains(today)) {
                    checkDate = today
                }
                // Bugün girmemiş ama dün girmişse (streak devam ediyor)
                else if (dates.contains(yesterday)) {
                    checkDate = yesterday
                }

                if (checkDate != null) {
                    currentStreak = 1
                    var loopDateStr = checkDate

                    // Geriye doğru ardışık günleri kontrol et
                    while (true) {
                        // loopDateStr'den bir önceki günü bul
                        val sdf =
                            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val dateObj = sdf.parse(loopDateStr)
                        val cal = java.util.Calendar.getInstance()
                        cal.time = dateObj!!
                        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                        val prevDateStr = sdf.format(cal.time)

                        if (dates.contains(prevDateStr)) {
                            currentStreak++
                            loopDateStr = prevDateStr
                        } else {
                            break
                        }
                    }
                }

                trySend(currentStreak)
            }
        awaitClose { reg.remove() }
    }
}

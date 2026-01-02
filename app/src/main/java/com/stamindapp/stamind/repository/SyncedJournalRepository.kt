package com.stamindapp.stamind.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stamindapp.stamind.database.JournalEntry
import com.stamindapp.stamind.engine.Suggestion
import com.stamindapp.stamind.util.FirestoreIdUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SyncedJournalRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val allJournalEntries: Flow<List<JournalEntry>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val reg = firestore.collection("users")
            .document(uid)
            .collection("journals")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { d ->
                    val data = d.data ?: return@mapNotNull null
                    val suggestions = (data["gunluk_oneri"] as? List<*>)?.mapNotNull { any ->
                        (any as? Map<*, *>)?.let { m ->
                            Suggestion(
                                baslik = m["baslik"] as? String ?: "",
                                detay = m["detay"] as? String ?: ""
                            )
                        }
                    } ?: emptyList()
                    JournalEntry(
                        date = data["date"] as? String ?: d.id,
                        journalText = data["journalText"] as? String ?: "",
                        duygusal_durum = data["duygusal_durum"] as? String ?: "",
                        analiz_ozeti = data["analiz_ozeti"] as? String ?: "",
                        ham_puani = (data["ham_puani"] as? Number)?.toInt() ?: 0,
                        destek_mesaji = data["destek_mesaji"] as? String ?: "",
                        puan_aciklamasi = data["puan_aciklamasi"] as? String ?: "",
                        temalar = (data["temalar"] as? List<*>)?.mapNotNull { it as? String }
                            ?: emptyList(),
                        gunluk_oneri = suggestions,
                        timestamp = (data["timestamp"] as? Number)?.toLong()
                            ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }


    suspend fun insertJournalEntry(entry: JournalEntry) {
        val uid = auth.currentUser?.uid ?: return
        val docId = FirestoreIdUtils.journalDocumentId(entry.date, entry.timestamp)
        firestore.collection("users")
            .document(uid)
            .collection("journals")
            .document(docId)
            .set(
                hashMapOf(
                    "date" to entry.date,
                    "journalText" to entry.journalText,
                    "duygusal_durum" to entry.duygusal_durum,
                    "analiz_ozeti" to entry.analiz_ozeti,
                    "ham_puani" to entry.ham_puani,
                    "destek_mesaji" to entry.destek_mesaji,
                    "puan_aciklamasi" to entry.puan_aciklamasi,
                    "temalar" to entry.temalar,
                    "gunluk_oneri" to entry.gunluk_oneri.map {
                        mapOf(
                            "baslik" to it.baslik,
                            "detay" to it.detay
                        )
                    },
                    "timestamp" to entry.timestamp
                )
            )
            .await()
    }

    suspend fun getJournalByDate(date: String): JournalEntry? {
        val uid = auth.currentUser?.uid ?: return null
        val querySnapshot = firestore.collection("users")
            .document(uid)
            .collection("journals")
            .whereEqualTo("date", date)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
        return querySnapshot.documents.firstOrNull()?.data?.let { data ->
            val suggestions = (data["gunluk_oneri"] as? List<*>)?.mapNotNull { any ->
                (any as? Map<*, *>)?.let { m ->
                    Suggestion(
                        baslik = m["baslik"] as? String ?: "",
                        detay = m["detay"] as? String ?: ""
                    )
                }
            } ?: emptyList()
            JournalEntry(
                date = data["date"] as? String ?: date,
                journalText = data["journalText"] as? String ?: "",
                duygusal_durum = data["duygusal_durum"] as? String ?: "",
                analiz_ozeti = data["analiz_ozeti"] as? String ?: "",
                ham_puani = (data["ham_puani"] as? Number)?.toInt() ?: 0,
                destek_mesaji = data["destek_mesaji"] as? String ?: "",
                puan_aciklamasi = data["puan_aciklamasi"] as? String ?: "",
                temalar = (data["temalar"] as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList(),
                gunluk_oneri = suggestions,
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }

    suspend fun getLatestJournalForDate(date: String): JournalEntry? {
        return getJournalByDate(date)
    }

    suspend fun deleteJournalEntry(entry: JournalEntry) {
        val uid = auth.currentUser?.uid ?: return
        val docId = FirestoreIdUtils.journalDocumentId(entry.date, entry.timestamp)
        try {
            firestore.collection("users")
                .document(uid)
                .collection("journals")
                .document(docId)
                .delete()
                .await()
        } catch (e: Exception) {
            println("Firestore silme hatası: ${e.message}")
        }
    }
}

package com.stamindapp.stamind.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SubscriptionManager(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Dinamik premium durumu (Firestore users/{uid} doc, field: premium: Boolean)
    fun isPremiumFlow(): Flow<Boolean> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(false)
            awaitClose { }
            return@callbackFlow
        }
        val reg = firestore.collection("users")
            .document(user.uid)
            .addSnapshotListener { snap, _ ->
                val premium = snap?.getBoolean("premium") ?: false
                trySend(premium)
            }
        awaitClose { reg.remove() }
    }


    suspend fun upgradeUserToPremium(planId: String) {
        val user = auth.currentUser ?: return
        val data = mapOf(
            "premium" to true,
            "premium_plan" to planId,
            "premium_since" to System.currentTimeMillis()
        )
        firestore.collection("users")
            .document(user.uid)
            .set(data, SetOptions.merge())
            .await()
    }
}

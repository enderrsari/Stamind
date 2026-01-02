package com.stamindapp.stamind.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser


    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun signInWithGoogle(idToken: String): Result<Pair<FirebaseUser, Boolean>> {
        return try {
            val credential =
                com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val isNewUser = result.additionalUserInfo?.isNewUser ?: false
            Result.success(Pair(result.user!!, isNewUser))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

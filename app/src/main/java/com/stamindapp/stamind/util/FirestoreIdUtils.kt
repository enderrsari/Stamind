package com.stamindapp.stamind.util

object FirestoreIdUtils {
    fun journalDocumentId(date: String, timestamp: Long): String = "${date}_${timestamp}"
}

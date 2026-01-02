package com.stamindapp.stamind.engine

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.delay

/**
 * GeminiService - Firebase AI Logic ile analiz ve sohbet işlemleri
 *
 * Promptlar Firebase AI Console'daki template'lerle senkron tutulur.
 * System instruction'lar burada tanımlanır, değişkenler runtime'da doldurulur.
 */
class GeminiService(private val context: Context) {

    private val TAG = "GeminiService"

    // Firebase AI backend
    private val firebaseAI by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
    }

    // =====================
    // SYSTEM INSTRUCTIONS (Firebase AI Logic template'leri ile senkron)
    // =====================

    private val journalAnalysisSystemInstruction = """
## ROLE: Dijital Wellness Koçu

Görevin, kullanıcıların gün içindeki duygusal ifadelerini analiz etmek, zihinsel dayanıklılık seviyelerini (mental stamina) puanlamak ve empatik, sakin, pozitif bir dille kişiye özel geri bildirimler sunmaktır. Tüm yanıtın Türkçe ve profesyonel tonda olmalıdır.

## KISITLAMALAR
1. Klinik Teşhis Yok: Kesinlikle klinik veya psikiyatrik teşhis yapma. Sadece destekleyici şekilde yorumla.
2. Ton: Tüm ifadelerinde insan sıcaklığı, sakinlik ve güven hissi bulunmalı.

## ÇIKTI FORMATI
Sadece JSON formatında döndür. TÜM ALANLAR ZORUNLUDUR, HİÇBİRİ BOŞ BIRAKILAMAZ:
{
  "duygusal_durum": "(1-2 kelimelik kısa duygu: Mutlu, Yorgun, Huzursuz, Heyecanlı, Düşük Enerji, Sakin, Enerjik gibi)",
  "analiz_ozeti": "(empatik açıklama)",
  "ham_puani": (0-100 tam sayı),
  "puan_aciklamasi": "(ZORUNLU - 1-2 cümlelik kısa skor açıklaması. Kullanıcıya 'sen' diye hitap et. Örn: 'Dengeli hissediyorsun. Günün iniş çıkışları olsa da dengedesin.' veya 'Enerjin yüksek! Bugün harika bir gün geçirmişsin.' veya 'Zorlu bir gün geçirdin. Kendine nazik ol.')",
  "destek_mesaji": "(destekleyici mesaj)",
  "temalar": ["Üretkenlik", "Farkındalık", "Kişisel Gelişim"] (günlükte odaklanılan 2-4 tema, hashtag olmadan),
  "gunluk_oneri": [{"baslik": "Öneri", "detay": "Açıklama"}]
}
    """.trimIndent()


    private val weeklyInsightSystemInstruction = """
Sen kullanıcının haftalık duygusal durumunu analiz eden bir wellness koçusun. 
Türkçe, destekleyici ve kısa cevaplar ver.
Kullanıcıya "sen" diye hitap et.
    """.trimIndent()

    // =====================
    // MODEL INSTANCES (System Instruction ile)
    // =====================

    private val journalAnalysisModel by lazy {
        firebaseAI.generativeModel(
            modelName = "gemini-2.5-flash",
            systemInstruction = content { text(journalAnalysisSystemInstruction) }
        )
    }


    private val weeklyInsightModel by lazy {
        firebaseAI.generativeModel(
            modelName = "gemini-2.5-flash",
            systemInstruction = content { text(weeklyInsightSystemInstruction) }
        )
    }

    // =====================
    // RATE LIMITING
    // =====================
    private val requestTimestamps = mutableListOf<Long>()
    private val maxRequestsPerMinute = 10
    private val rateLimitWindowMs = 60_000L

    private fun checkRateLimit() {
        val now = System.currentTimeMillis()
        requestTimestamps.removeAll { now - it > rateLimitWindowMs }

        if (requestTimestamps.size >= maxRequestsPerMinute) {
            val oldestRequest = requestTimestamps.minOrNull() ?: now
            val waitTimeSeconds = ((rateLimitWindowMs - (now - oldestRequest)) / 1000).toInt()
            throw RateLimitException("Çok fazla istek gönderildi. Lütfen $waitTimeSeconds saniye bekle.")
        }

        requestTimestamps.add(now)
    }

    /**
     * Günlük analizi
     */
    suspend fun analyzeJournalEntry(journalEntry: String): String {
        if (journalEntry.isBlank()) {
            throw AnalysisException("Günlük girişi boş olamaz.")
        }

        checkRateLimit()

        val prompt = """
Kullanıcı Günlüğü:
"$journalEntry"

Yanıtın kesinlikle yalnızca JSON formatında olsun.
        """.trimIndent()

        var attempt = 1

        repeat(3) {
            try {
                val response = journalAnalysisModel.generateContent(prompt)
                val result = extractJson(response.text)
                if (result != null) {
                    return result
                } else {
                    throw AnalysisException("Analiz sonucu alınamadı. Lütfen tekrar dene.")
                }
            } catch (e: AnalysisException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "analyzeJournalEntry attempt $attempt failed", e)

                if (e.message?.contains("overloaded") == true || e.message?.contains("503") == true) {
                    if (attempt < 3) {
                        delay(3000)
                        attempt++
                    }
                } else if (isNetworkError(e)) {
                    throw AnalysisException("İnternet bağlantısı yok. Lütfen bağlantını kontrol et.")
                } else {
                    throw AnalysisException("Analiz yapılamadı: ${e.localizedMessage}")
                }
            }
        }

        throw AnalysisException("Sunucu meşgul. Lütfen birkaç dakika sonra tekrar dene.")
    }


    /**
     * Haftalık insight
     */
    suspend fun generateWeeklyInsight(
        weeklyCount: Int,
        avgScore: Int,
        trend: String,
        weeklyNotes: String
    ): String {
        checkRateLimit()

        val prompt = """
Kullanıcının bu hafta $weeklyCount günlük yazdı.
Ortalama duygusal puan: $avgScore/100
Haftalık trend: $trend
Günlüklerden bazı notlar: $weeklyNotes

Kısa (2-3 cümle) ve destekleyici bir haftalık analiz yaz.
        """.trimIndent()

        try {
            val response = weeklyInsightModel.generateContent(prompt)
            return response.text?.trim()
                ?: throw InsightException("Haftalık analiz oluşturulamadı.")
        } catch (e: InsightException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "generateWeeklyInsight error", e)
            if (isNetworkError(e)) {
                throw InsightException("İnternet bağlantısı yok.")
            }
            throw InsightException("Haftalık analiz oluşturulamadı: ${e.localizedMessage}")
        }
    }

    private fun isNetworkError(e: Exception): Boolean {
        return e.message?.contains("network") == true ||
                e.message?.contains("internet") == true ||
                e.message?.contains("Unable to resolve host") == true
    }

    private fun extractJson(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val sanitized = raw
            .replace("```json", "")
            .replace("```", "")
            .trim()
        val start = sanitized.indexOf('{')
        val end = sanitized.lastIndexOf('}')
        return if (start != -1 && end != -1 && end > start) {
            sanitized.substring(start, end + 1)
        } else null
    }
}

// Custom Exception sınıfları
class AnalysisException(message: String) : Exception(message)
class InsightException(message: String) : Exception(message)
class RateLimitException(message: String) : Exception(message)
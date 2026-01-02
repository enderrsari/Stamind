package com.stamindapp.stamind.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stamindapp.stamind.database.JournalEntry
import com.stamindapp.stamind.ui.theme.LexendTypography
import com.stamindapp.stamind.ui.theme.StamindColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Radar/Spider Chart for displaying mental health profile
 *
 * Axes:
 * - Enerji (Energy)
 * - Sosyal (Social)
 * - Odak (Focus)
 * - Sakinlik (Calmness)
 * - Üretkenlik (Productivity)
 */

data class RadarChartData(
    val enerji: Int,        // 0-100
    val sosyal: Int,        // 0-100
    val odak: Int,          // 0-100
    val sakinlik: Int,      // 0-100
    val uretkenlik: Int     // 0-100
)

@Composable
fun MentalHealthRadarChart(
    journalEntries: List<JournalEntry>,
    modifier: Modifier = Modifier
) {
    // Analyze journals to extract radar data
    val radarData = remember(journalEntries) {
        analyzeJournalsForRadarData(journalEntries)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Zihinsel Sağlık Profili",
            style = LexendTypography.Bold6,
            color = StamindColors.HeaderColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Haftalık analizlerine göre genel değerlendirme",
            style = LexendTypography.Regular8,
            color = StamindColors.TextColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StamindColors.White, RoundedCornerShape(32.dp))
                .border(2.dp, StamindColors.CardStrokeColor, RoundedCornerShape(32.dp))
                .padding(16.dp)
        ) {
            // Radar Chart with integrated labels
            RadarChartCanvas(
                data = radarData,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
    }
}

@Composable
private fun RadarChartCanvas(
    data: RadarChartData,
    modifier: Modifier = Modifier
) {
    val axes = remember { listOf("ENERJİ", "SOSYAL", "ODAK", "SAKİNLİK", "ÜRETKENLİK") }
    val values = remember(data) {
        listOf(data.enerji, data.sosyal, data.odak, data.sakinlik, data.uretkenlik)
    }
    val numberOfAxes = axes.size
    val angleStep = remember { (2 * PI / numberOfAxes).toFloat() }

    val gridColor = StamindColors.Green200
    val dataColor = StamindColors.Green500
    val fillColor = remember { StamindColors.Green500.copy(alpha = 0.3f) }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasCenterX = size.width / 2
            val canvasCenterY = size.height / 2
            val labelPadding = 40.dp.toPx()
            val canvasMaxRadius = (minOf(canvasCenterX, canvasCenterY) - labelPadding) * 0.9f

            // Draw concentric grid pentagons (25%, 50%, 75%, 100%)
            for (level in 1..4) {
                val radius = canvasMaxRadius * (level / 4f)
                val gridPath = Path()
                for (i in 0 until numberOfAxes) {
                    val angle = -PI.toFloat() / 2 + i * angleStep
                    val x = canvasCenterX + radius * cos(angle)
                    val y = canvasCenterY + radius * sin(angle)
                    if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                }
                gridPath.close()
                drawPath(
                    path = gridPath,
                    color = gridColor,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Draw axis lines
            for (i in 0 until numberOfAxes) {
                val angle = -PI.toFloat() / 2 + i * angleStep
                val endX = canvasCenterX + canvasMaxRadius * cos(angle)
                val endY = canvasCenterY + canvasMaxRadius * sin(angle)
                drawLine(
                    color = gridColor,
                    start = Offset(canvasCenterX, canvasCenterY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw data polygon
            val dataPath = Path()
            for (i in 0 until numberOfAxes) {
                val angle = -PI.toFloat() / 2 + i * angleStep
                val value = values[i].coerceIn(0, 100) / 100f
                val radius = canvasMaxRadius * value
                val x = canvasCenterX + radius * cos(angle)
                val y = canvasCenterY + radius * sin(angle)
                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()

            drawPath(path = dataPath, color = fillColor)
            drawPath(
                path = dataPath,
                color = dataColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw data points
            for (i in 0 until numberOfAxes) {
                val angle = -PI.toFloat() / 2 + i * angleStep
                val value = values[i].coerceIn(0, 100) / 100f
                val radius = canvasMaxRadius * value
                val x = canvasCenterX + radius * cos(angle)
                val y = canvasCenterY + radius * sin(angle)
                drawCircle(color = dataColor, radius = 5.dp.toPx(), center = Offset(x, y))
                drawCircle(
                    color = StamindColors.White,
                    radius = 2.5.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        // Eksen etiketleri
        // ENERJİ - Üst
        Text(
            text = "ENERJİ",
            style = LexendTypography.Bold8,
            color = StamindColors.HeaderColor,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // SOSYAL - Sağ üst
        Text(
            text = "SOSYAL",
            style = LexendTypography.Bold8,
            color = StamindColors.HeaderColor,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(y = (-50).dp)
        )

        // ODAK - Sağ alt
        Text(
            text = "ODAK",
            style = LexendTypography.Bold8,
            color = StamindColors.HeaderColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-20).dp, x = (-10).dp)
        )

        // SAKİNLİK - Sol alt
        Text(
            text = "SAKİNLİK",
            style = LexendTypography.Bold8,
            color = StamindColors.HeaderColor,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(y = (-20).dp, x = 10.dp)
        )

        // ÜRETKENLİK - Sol üst
        Text(
            text = "ÜRETKENLİK",
            style = LexendTypography.Bold8,
            color = StamindColors.HeaderColor,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(y = (-50).dp)
        )
    }
}


/**
 * Analyzes journal entries to extract radar chart data.
 * Uses keyword matching and sentiment analysis.
 */
private fun analyzeJournalsForRadarData(entries: List<JournalEntry>): RadarChartData {
    if (entries.isEmpty()) {
        return RadarChartData(50, 50, 50, 50, 50) // Default neutral values
    }

    // Get entries from last 7 days
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -7)
    val weekAgo = calendar.time

    val weeklyEntries = entries.filter { entry ->
        try {
            val entryDate = dateFormat.parse(entry.date)
            entryDate?.after(weekAgo) == true
        } catch (e: Exception) {
            false
        }
    }

    if (weeklyEntries.isEmpty()) {
        return RadarChartData(50, 50, 50, 50, 50)
    }

    val allText = weeklyEntries.joinToString(" ") { it.journalText.lowercase(Locale("tr")) }
    val avgScore = weeklyEntries.map { it.ham_puani }.average().toInt()

    // Keyword-based analysis for new axes
    // Enerji (Energy)
    val enerjiKeywords = listOf("enerji", "canlı", "dinamik", "güçlü", "aktif", "hareketli")
    val enerjiNegativeKeywords = listOf("yorgun", "bitkin", "halsiz", "uykulu", "tükendi")

    // Sosyal (Social)
    val sosyalKeywords =
        listOf("arkadaş", "aile", "buluş", "sohbet", "birlikte", "sosyal", "eş", "sevgili")
    val sosyalNegativeKeywords = listOf("yalnız", "izole", "tek başına", "kimse yok")

    // Odak (Focus)
    val odakKeywords = listOf("odak", "konsantre", "dikkat", "verimli", "üretken", "başardım")
    val odakNegativeKeywords = listOf("dağınık", "dikkat", "odaklanamıyorum", "kayıp")

    // Sakinlik (Calmness)
    val sakinlikKeywords =
        listOf("sakin", "rahat", "huzur", "dinlen", "nefes", "yoga", "meditasyon")
    val sakinlikNegativeKeywords = listOf("stres", "gergin", "endişe", "kaygı", "panik", "bunaltı")

    // Üretkenlik (Productivity)
    val uretkenlikKeywords = listOf("hedef", "başar", "tamamladım", "proje", "plan", "çalıştım")
    val uretkenlikNegativeKeywords = listOf("erteledim", "yapamadım", "bitmedi", "tembel")

    fun calculateScore(positiveKeywords: List<String>, negativeKeywords: List<String>): Int {
        val positiveCount = positiveKeywords.sumOf { allText.split(it).size - 1 }
        val negativeCount = negativeKeywords.sumOf { allText.split(it).size - 1 }

        val baseScore = avgScore
        val keywordImpact = (positiveCount - negativeCount) * 5
        return (baseScore + keywordImpact).coerceIn(10, 100)
    }

    return RadarChartData(
        enerji = calculateScore(enerjiKeywords, enerjiNegativeKeywords),
        sosyal = calculateScore(sosyalKeywords, sosyalNegativeKeywords),
        odak = calculateScore(odakKeywords, odakNegativeKeywords),
        sakinlik = calculateScore(sakinlikKeywords, sakinlikNegativeKeywords),
        uretkenlik = calculateScore(uretkenlikKeywords, uretkenlikNegativeKeywords)
    )
}

@Preview(showBackground = true)
@Composable
private fun RadarChartPreview() {
    val sampleData = RadarChartData(
        enerji = 75,
        sosyal = 60,
        odak = 85,
        sakinlik = 45,
        uretkenlik = 70
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(StamindColors.Green50)
            .padding(16.dp)
    ) {
        MentalHealthRadarChart(
            journalEntries = emptyList()
        )
    }
}

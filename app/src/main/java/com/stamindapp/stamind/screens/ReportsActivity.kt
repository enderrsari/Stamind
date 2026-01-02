package com.stamindapp.stamind.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.stamindapp.stamind.database.JournalEntry
import com.stamindapp.stamind.model.JournalViewModel
import com.stamindapp.stamind.ui.components.MentalHealthRadarChart
import com.stamindapp.stamind.ui.theme.LexendTypography
import com.stamindapp.stamind.ui.theme.NunitoTypography
import com.stamindapp.stamind.ui.theme.StamindColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsActivity(
    viewModel: JournalViewModel,
    navController: NavController
) {
    val journalEntries by viewModel.allJournalEntries.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val weeklyInsight by viewModel.weeklyInsight.collectAsState()
    val weeklyInsightLoading by viewModel.weeklyInsightLoading.collectAsState()

    ReportsScreenContent(
        journalEntries = journalEntries,
        isPremium = isPremium,
        weeklyInsight = weeklyInsight,
        weeklyInsightLoading = weeklyInsightLoading,
        onNavigateToOffer = { navController.navigate("offer") }
    )
}

@Composable
private fun ReportsScreenContent(
    journalEntries: List<JournalEntry> = emptyList(),
    isPremium: Boolean = false,
    weeklyInsight: String? = null,
    weeklyInsightLoading: Boolean = false,
    onNavigateToOffer: () -> Unit = {}
) {
    // Status bar padding hesaplama
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StamindColors.BackgroundColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Sayfa Başlığı
        item {
            Text(
                text = "Raporlarım",
                style = LexendTypography.Bold4,
                color = StamindColors.HeaderColor,
                modifier = Modifier
                    .padding(top = statusBarPadding + 16.dp)
            )
        }

        // Grafik 1: Haftalık Ruh Hali Grafiği (dikey eksenli, detaylı)
        item {
            WeeklyMoodGraphReports(
                journalEntries = journalEntries,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Grafik 3: Zihinsel Sağlık Profili Radar Grafiği
        item {

            MentalHealthRadarChart(
                journalEntries = journalEntries,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Sta Haftalık Analizi (Premium) - Duygusal puan grafiğinin altında
        item {
            StaWeeklyInsightCard(
                isPremium = isPremium,
                weeklyInsight = weeklyInsight,
                isLoading = weeklyInsightLoading,
                onUnlockClick = onNavigateToOffer
            )
        }

        // Kelime Bulutu
        item {
            WordCloudCard(journalEntries = journalEntries)
        }

        // Tema Analizi
        item {
            ThemeAnalysisCard(journalEntries = journalEntries)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ================== YENİ ANALİTİK KARTLARI ==================

@Composable
private fun StaWeeklyInsightCard(
    isPremium: Boolean,
    weeklyInsight: String?,
    isLoading: Boolean,
    onUnlockClick: () -> Unit
) {
    // Varsayılan placeholder metin (premium olmayan veya veri yokken)
    val defaultText =
        "Bu hafta dalgalı bir değişim görüyorum. Pazartesi günleri moralin biraz düşmüş ama hafta ortasına doğru toparlamışsın. Kendine zaman ayırmayı unutma!"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(StamindColors.Green700, StamindColors.Green500)
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .border(2.dp, StamindColors.CardStrokeColor, RoundedCornerShape(32.dp))
            .then(
                if (!isPremium) Modifier.clickable { onUnlockClick() } else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sta'nın Haftalık Analizi",
                    style = LexendTypography.Bold6,
                    color = StamindColors.White
                )

                if (!isPremium) {
                    Box(
                        modifier = Modifier
                            .background(
                                StamindColors.White.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PRO",
                            style = LexendTypography.Bold9,
                            color = StamindColors.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (!isPremium) Modifier.blur(8.dp) else Modifier)
            ) {
                if (isLoading) {
                    Text(
                        text = "Analiz hazırlanıyor...",
                        style = LexendTypography.Regular7,
                        color = StamindColors.White.copy(alpha = 0.8f)
                    )
                } else {
                    Text(
                        text = weeklyInsight ?: defaultText,
                        style = LexendTypography.Regular7,
                        color = StamindColors.White.copy(alpha = 0.9f)
                    )
                }
            }

            if (!isPremium) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onUnlockClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StamindColors.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Premium ile Aç",
                        style = LexendTypography.Bold8,
                        color = StamindColors.Green700
                    )
                }
            }
        }
    }
}

@Composable
private fun WordCloudCard(journalEntries: List<JournalEntry>) {
    // En çok kullanılan kelimeleri çıkar
    val wordCounts = remember(journalEntries) {
        val stopWords = setOf(
            "bir", "ve", "bu", "da", "de", "için", "ile", "ben", "benim", "beni",
            "çok", "daha", "gibi", "kadar", "ama", "fakat", "ki", "ne", "nasıl",
            "olan", "olarak", "var", "yok", "şey", "zaman", "gün", "bugün", "o", "onu"
        )

        journalEntries
            .flatMap { it.journalText.lowercase().split(Regex("[\\s,.!?;:]+")) }
            .filter { it.length > 2 && it !in stopWords }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(10)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Başlık - Kart dışında
        Text(
            text = "En Çok Kullandığın Kelimeler",
            style = LexendTypography.Bold6,
            color = StamindColors.HeaderColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Günlüklerindeki en sık geçen kelimeler",
            style = LexendTypography.Regular8,
            color = StamindColors.TextColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Kelime Bulutu Kartı
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = StamindColors.Green100,
                    shape = RoundedCornerShape(32.dp)
                )
                .border(
                    width = 2.dp,
                    color = StamindColors.Green500,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(20.dp)
        ) {
            if (wordCounts.isEmpty()) {
                Text(
                    text = "Henüz yeterli günlük yok",
                    style = LexendTypography.Regular7,
                    color = StamindColors.TextColor
                )
            } else {
                // Kelime bulutu görünümü
                val maxCount = wordCounts.maxOfOrNull { it.value } ?: 1

                // Renk seçenekleri
                val colors = listOf(
                    StamindColors.Green800,
                    StamindColors.HeaderColor,
                    StamindColors.TextColor
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    wordCounts.chunked(3).forEachIndexed { rowIndex, row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEachIndexed { colIndex, (word, count) ->
                                // Font boyutu: kullanım sıklığına göre
                                val sizeFactor = 0.7f + (count.toFloat() / maxCount) * 0.9f
                                val fontSize = (11 * sizeFactor).sp

                                // Stil: az kullanılan = ince, çok kullanılan = kalın
                                val normalizedCount = count.toFloat() / maxCount
                                val fontWeight = when {
                                    normalizedCount > 0.8f -> FontWeight.ExtraBold
                                    normalizedCount > 0.6f -> FontWeight.Bold
                                    normalizedCount > 0.4f -> FontWeight.SemiBold
                                    normalizedCount > 0.2f -> FontWeight.Medium
                                    else -> FontWeight.Light
                                }

                                // Renk: pozisyona göre
                                val colorIndex = (rowIndex + colIndex) % colors.size
                                val color = colors[colorIndex]

                                Text(
                                    text = word,
                                    fontSize = fontSize,
                                    fontWeight = fontWeight,
                                    fontFamily = com.stamindapp.stamind.ui.theme.LexendRegularFamily,
                                    color = color,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeAnalysisCard(journalEntries: List<JournalEntry>) {
    // Tema analizi
    val themes = remember(journalEntries) {
        val themeKeywords = mapOf(
            "💼 İş/Kariyer" to listOf(
                "iş",
                "çalış",
                "proje",
                "toplantı",
                "ofis",
                "patron",
                "kariyer",
                "meslek"
            ),
            "👨‍👩‍👧 Aile" to listOf("aile", "anne", "baba", "kardeş", "çocuk", "eş", "akraba"),
            "❤️ İlişkiler" to listOf("sevgi", "aşk", "arkadaş", "ilişki", "partner", "sev"),
            "💪 Sağlık" to listOf("sağlık", "spor", "egzersiz", "uyku", "yemek", "hasta", "doktor"),
            "🧘 Ruh Hali" to listOf("stres", "mutlu", "üzgün", "endişe", "huzur", "rahat", "gergin")
        )

        val allText = journalEntries.joinToString(" ") { it.journalText.lowercase() }

        themeKeywords.map { (theme, keywords) ->
            val count = keywords.sumOf { keyword ->
                allText.split(keyword).size - 1
            }
            theme to count
        }.filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(4)
    }

    val totalCount = themes.sumOf { it.second }.coerceAtLeast(1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(StamindColors.White, RoundedCornerShape(32.dp))
            .border(2.dp, StamindColors.CardStrokeColor, RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Odaklandığın Temalar",
                style = LexendTypography.Bold6,
                color = StamindColors.HeaderColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (themes.isEmpty()) {
                Text(
                    text = "Henüz yeterli günlük yok",
                    style = LexendTypography.Regular7,
                    color = StamindColors.TextColor
                )
            } else {
                themes.forEach { (theme, count) ->
                    val percentage = (count * 100) / totalCount

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = theme,
                            style = LexendTypography.Medium7,
                            color = StamindColors.TextColor,
                            modifier = Modifier.width(120.dp)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(12.dp)
                                .background(StamindColors.Green100, RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(percentage / 100f)
                                    .height(12.dp)
                                    .background(StamindColors.Green500, RoundedCornerShape(6.dp))
                            )
                        }

                        Text(
                            text = "%$percentage",
                            style = LexendTypography.Bold8,
                            color = StamindColors.Green600,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}


@Preview(name = "1 - Free Kullanıcı", showBackground = true, widthDp = 390, heightDp = 1900)
@Composable
private fun ReportsActivityFreePreview() {
    val sampleEntries = listOf(
        JournalEntry(
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            journalText = "Bugün iş yerinde toplantı vardı. Aile ile vakit geçirdim. Çok mutlu hissediyorum.",
            duygusal_durum = "Pozitif",
            analiz_ozeti = "Güzel bir gün geçirdin.",
            ham_puani = 82,
            puan_aciklamasi = "Puan açıklaması",
            destek_mesaji = "Harika!",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )
    )

    ReportsScreenContent(
        journalEntries = sampleEntries,
        isPremium = false
    )
}

@Preview(name = "2 - Premium Kullanıcı", showBackground = true, widthDp = 390, heightDp = 1900)
@Composable
private fun ReportsActivityPremiumPreview() {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val sampleEntries = (0 until 7).map { i ->
        val cal = (calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -6 + i) }
        JournalEntry(
            date = dateFormat.format(cal.time),
            journalText = "Bugün iş yerinde toplantı vardı. Aile ile vakit geçirdim. Stresli bir gün oldu ama mutlu hissediyorum. Proje tamamlandı.",
            duygusal_durum = if (i % 2 == 0) "Pozitif" else "Nötr",
            analiz_ozeti = "Gün genel olarak iyi geçti.",
            ham_puani = 60 + (i * 5),
            puan_aciklamasi = "Puan açıklaması",
            destek_mesaji = "Harika gidiyorsun!",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis() - (i * 86400000L),
            isFavorite = false
        )
    }

    ReportsScreenContent(
        journalEntries = sampleEntries,
        isPremium = true
    )
}

// ================== GRAFIK FONKSİYONLARI ==================
// Bu fonksiyonlar ProfileScreen tarafından da kullanılıyor


// Reports için HomeActivity tarzı grafik + Y ekseni + AI yorum
@Composable
fun WeeklyMoodGraphReports(
    journalEntries: List<JournalEntry>,
    weeklyInsight: String? = null,
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf("Pt", "Sa", "Ça", "Pe", "Cu", "Ct", "Pz")

    val dates = remember {
        val datesList = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)

        for (i in 0 until 7) {
            val currentDate = Calendar.getInstance()
            currentDate.timeInMillis = calendar.timeInMillis
            currentDate.add(Calendar.DAY_OF_YEAR, i)
            datesList.add(dateFormat.format(currentDate.time))
        }
        datesList
    }

    // Puana göre renk belirleme fonksiyonu (HomeActivity ile aynı)
    // Kötü: Red, Orta: Orange, İyi: Green
    fun getColorForScore(score: Int): Pair<Color, Color> {
        return when {
            score < 40 -> Pair(StamindColors.Red100, StamindColors.Red500) // Kötü
            score < 70 -> Pair(StamindColors.Orange100, StamindColors.Orange500) // Orta
            else -> Pair(StamindColors.Green100, StamindColors.Green500) // İyi
        }
    }

    // Haftalık ortalama puan hesaplama (HomeActivity ile aynı)


    Column(modifier = modifier) {
        // Responsive sütun boyutları (HomeActivity ile aynı: 37x140 @ 390dp ekran genişliği)
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp.toFloat()
        val baseWidthDp = 390f
        val scaleFactor = screenWidthDp / baseWidthDp

        val barWidthDp = (37 * scaleFactor).dp
        val barHeightDp = (140 * scaleFactor).dp
        val spacingDp = 8.dp

        // Başlık - LexendBold6, HeaderColor (HomeActivity ile aynı)
        Text(
            text = "Haftalık Analiz Raporu",
            style = LexendTypography.Bold6,
            color = StamindColors.HeaderColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Alt yazı - LexendRegular8, TextColor
        Text(
            text = "Günlük yaptığın analizlerle kendini dinle",
            style = LexendTypography.Regular8,
            color = StamindColors.TextColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Grafik Kartı - Beyaz arka plan, 32dp radius, 2dp stroke (HomeActivity ile aynı)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = StamindColors.White,
                    shape = RoundedCornerShape(32.dp)
                )
                .border(
                    width = 2.dp,
                    color = StamindColors.CardStrokeColor,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Sütunların toplam genişliği = 7 sütun + 6 boşluk
            val totalGraphWidth = (barWidthDp * 7) + (spacingDp * 6)

            // Haftalık değişim hesaplama (önceki hafta ile karşılaştırma)
            val previousWeekDates = remember {
                val datesList = mutableListOf<String>()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val daysFromMonday =
                    if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
                calendar.add(
                    Calendar.DAY_OF_YEAR,
                    -daysFromMonday - 7
                ) // Bir önceki haftanın pazartesisi

                for (i in 0 until 7) {
                    val currentDate = Calendar.getInstance()
                    currentDate.timeInMillis = calendar.timeInMillis
                    currentDate.add(Calendar.DAY_OF_YEAR, i)
                    datesList.add(dateFormat.format(currentDate.time))
                }
                datesList
            }

            val previousWeekAverage = remember(journalEntries, previousWeekDates) {
                val prevWeekEntries =
                    journalEntries.filter { entry -> previousWeekDates.contains(entry.date) }
                if (prevWeekEntries.isNotEmpty()) {
                    prevWeekEntries.map { it.ham_puani }.average()
                } else {
                    null
                }
            }

            val currentWeekAverageDecimal = remember(journalEntries, dates) {
                val weekEntries = journalEntries.filter { entry -> dates.contains(entry.date) }
                if (weekEntries.isNotEmpty()) {
                    weekEntries.map { it.ham_puani }.average() / 10.0 // 0-10 aralığına çevir
                } else {
                    0.0
                }
            }

            val weeklyChange = remember(currentWeekAverageDecimal, previousWeekAverage) {
                if (previousWeekAverage != null && previousWeekAverage > 0) {
                    val currentAvg = currentWeekAverageDecimal * 10
                    val prevAvg = previousWeekAverage
                    ((currentAvg - prevAvg) / prevAvg * 100).toInt()
                } else {
                    null
                }
            }

            // HAFTALIK DUYGU bölümü (sol hizalı)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Küçük başlık
                Text(
                    text = "HAFTALIK DUYGU",
                    style = LexendTypography.Bold9,
                    color = StamindColors.SecondaryGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Büyük rakam ve trend
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ortalama puan (7.2 formatında)
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%.1f",
                            currentWeekAverageDecimal
                        ),
                        style = LexendTypography.Bold3,
                        color = StamindColors.HeaderColor
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Trend ikonu ve yüzde
                    if (weeklyChange != null) {
                        val (trendIcon, trendColor, trendPrefix) = when {
                            weeklyChange > 0 -> Triple(
                                Icons.Default.TrendingUp,
                                StamindColors.Green500,
                                "+"
                            )

                            weeklyChange < 0 -> Triple(
                                Icons.Default.TrendingDown,
                                StamindColors.Red500,
                                ""
                            )

                            else -> Triple(Icons.Default.TrendingFlat, StamindColors.Orange500, "")
                        }

                        Icon(
                            imageVector = trendIcon,
                            contentDescription = "Trend",
                            tint = trendColor,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(2.dp))

                        Text(
                            text = "$trendPrefix$weeklyChange%",
                            style = LexendTypography.Bold8,
                            color = trendColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grafik sütunları (HomeActivity ile aynı)
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    spacingDp,
                    Alignment.CenterHorizontally
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                dates.forEachIndexed { index, date ->
                    val entry = journalEntries.find { it.date == date }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Sütun
                        if (entry != null) {
                            // Analiz var: renkli görüntü (HomeActivity ile aynı)
                            val score = entry.ham_puani.coerceIn(0, 100)
                            val (bgColor, barColor) = getColorForScore(score)
                            val normalizedHeight = (score / 100f).coerceIn(0.05f, 1f)

                            Box(
                                modifier = Modifier
                                    .width(barWidthDp)
                                    .height(barHeightDp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(normalizedHeight)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(barColor)
                                )
                            }
                        } else {
                            // Analiz yok: CardStrokeColor dolgulu sütun (HomeActivity ile aynı)
                            Box(
                                modifier = Modifier
                                    .width(barWidthDp)
                                    .height(barHeightDp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StamindColors.CardStrokeColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Gün kısaltması - BÜYÜK HARF, LexendBold9, HeaderColor (HomeActivity ile aynı)
                        Text(
                            text = dayLabels[index].uppercase(),
                            style = LexendTypography.Bold9,
                            color = StamindColors.HeaderColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(barWidthDp)
                        )
                    }
                }
            }

            // Haftalık AI Yorum Bölümü (Divider ile ayrılmış)
            val weekEntries = journalEntries.filter { entry -> dates.contains(entry.date) }
            if (weekEntries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                // Divider
                Divider(
                    color = StamindColors.CardStrokeColor,
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                val avgScore = weekEntries.sumOf { it.ham_puani } / weekEntries.size
                val trend = if (weekEntries.size >= 2) {
                    val firstHalf =
                        weekEntries.take(weekEntries.size / 2).map { it.ham_puani }.average()
                    val secondHalf =
                        weekEntries.drop(weekEntries.size / 2).map { it.ham_puani }.average()
                    when {
                        secondHalf > firstHalf + 10 -> "📈 Yükselen"
                        secondHalf < firstHalf - 10 -> "📉 Düşen"
                        else -> "➡️ Stabil"
                    }
                } else "➡️ Stabil"

                val interpretation = weeklyInsight ?: when {
                    avgScore >= 80 -> "Harika bir hafta geçirmişsin! Enerji ve motivasyonun yüksek."
                    avgScore >= 60 -> "Genel olarak dengeli bir hafta geçirdin."
                    avgScore >= 40 -> "Bu hafta biraz iniş çıkışlar olmuş. Kendine zaman ayır."
                    else -> "Zorlu bir hafta geçirmişsin. Bir adım geri at ve dinlen."
                }

                // Puana göre renk belirleme (100'ü 3'e böl)
                val scoreColors = when {
                    avgScore < 33 -> Pair(StamindColors.Red100, StamindColors.Red500)
                    avgScore < 67 -> Pair(StamindColors.Orange100, StamindColors.Orange500)
                    else -> Pair(StamindColors.Green100, StamindColors.Green500)
                }

                // Trend ve Puan Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trend: $trend",
                        style = LexendTypography.Bold7,
                        color = StamindColors.HeaderColor
                    )

                    // Puan Badge (renkli dikdörtgen)
                    Box(
                        modifier = Modifier
                            .background(
                                color = scoreColors.first,
                                shape = RoundedCornerShape(32.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = scoreColors.second,
                                shape = RoundedCornerShape(32.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "%$avgScore",
                            style = LexendTypography.Bold8,
                            color = scoreColors.second
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // AI Yorum (Psychology ikonu ile)
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Psychology İkonu
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = scoreColors.first,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI",
                            tint = scoreColors.second,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // AI Yorum Metni
                    Text(
                        text = interpretation,
                        style = NunitoTypography.SemiBold7,
                        color = StamindColors.TextColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ================== TEMEL GRAFIK FONKSİYONLARI ==================


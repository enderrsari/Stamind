package com.stamindapp.stamind.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stamindapp.stamind.R
import com.stamindapp.stamind.database.JournalEntry
import com.stamindapp.stamind.database.MoodEntry
import com.stamindapp.stamind.model.HomeViewModel
import com.stamindapp.stamind.ui.components.TodayMoodCard
import com.stamindapp.stamind.ui.theme.LexendTypography
import com.stamindapp.stamind.ui.theme.StamindColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DateInfo(
    val dayOfWeek: String,
    val dayOfMonth: String,
    val fullDate: String
)

// Zamana göre Action Card tipi
data class TimeBasedConfig(
    val greetingMessage: String,
    val heroImageRes: Int, // Karşılama ve Hero Card resmi
    val actionTitle: String,
    val actionSubtitle: String,
    val actionButtonText: String
)

@Composable
fun HomeActivity(
    viewModel: HomeViewModel,
    onNavigateToJournal: () -> Unit = {},
    onNavigateToAnalysis: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToOffer: () -> Unit = {} // Premium olmayan kullanıcılar için
) {
    val lastSevenDaysMoods by viewModel.lastSevenDaysMoods.collectAsState()
    val lastAnalysis by viewModel.lastAnalysis.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    // MVP: currentStreak kaldırıldı - seri takip özelliği ileride eklenecek

    HomeScreenContent(
        selectedMoodIndex = viewModel.selectedMoodIndex,
        onSelectMood = { idx, emoji, date -> viewModel.selectMood(idx, emoji, date) },
        lastSevenDaysMoods = lastSevenDaysMoods,
        lastAnalysis = lastAnalysis.maxByOrNull { it.timestamp },
        journalEntries = lastAnalysis,
        userName = viewModel.userName,
        onNavigateToJournal = onNavigateToJournal,
        onNavigateToAnalysis = onNavigateToAnalysis,
        onNavigateToOffer = onNavigateToOffer
    )
}

@Composable
fun HomeScreenContent(
    selectedMoodIndex: Int,
    onSelectMood: (Int, String, String?) -> Unit,
    lastSevenDaysMoods: List<MoodEntry>,
    lastAnalysis: JournalEntry?,
    journalEntries: List<JournalEntry> = emptyList(),
    userName: String = "Dostum",
    onNavigateToJournal: () -> Unit = {},
    onNavigateToAnalysis: () -> Unit = {},
    onNavigateToOffer: () -> Unit = {},
    greetingMessageOverride: String? = null
) {
    // Yeni mood sıralaması: Harika, İyi, Ortalama, Kötü, Berbat
    val moods = listOf("😊", "🙂", "😐", "😔", "😫")

    // Zamana göre karşılama mesajı ve Action Card yapılandırması
    val timeConfig = remember {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        when {
            hour in 5..17 -> TimeBasedConfig(
                greetingMessage = if (hour < 12) "Günaydın ☀️" else "İyi Günler 🌤️",
                heroImageRes = if (hour < 12) R.drawable.morning_hero else R.drawable.noon_hero,
                actionTitle = "Günün Nasıl Gidiyor?",
                actionSubtitle = "Gününü değerlendirmek ve farkındalık kazanmak için harika bir zaman.",
                actionButtonText = "Günlük Yaz"
            )

            hour in 18..21 -> TimeBasedConfig(
                greetingMessage = "İyi Akşamlar 🌆",
                heroImageRes = R.drawable.evening_hero,
                actionTitle = "Günün Nasıl Geçti?",
                actionSubtitle = "Gününü değerlendirmek için harika bir zaman. Hadi günlük yazalım!",
                actionButtonText = "Günlük Yaz"
            )

            else -> TimeBasedConfig(
                greetingMessage = "İyi Geceler 🌙",
                heroImageRes = R.drawable.evening_hero,
                actionTitle = "Gününü Kaydet",
                actionSubtitle = "Uyumadan önce gününü değerlendir. Yarın daha güzel olacak!",
                actionButtonText = "Günlük Yaz"
            )
        }
    }
    val greetingText = greetingMessageOverride ?: timeConfig.greetingMessage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StamindColors.BackgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Karşılama Kartı ve Metin - Metin resmin üstünde (overlay)
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Arka plan resmi kartı - zamana göre değişiyor
                WelcomeHeroCard(heroImageRes = timeConfig.heroImageRes)

                // Karşılama Metni - Selamlama, isim ve emoji
                // Format: "Günaydın,\nEnder ☀️"
                val greetingEmoji = when {
                    greetingText.contains("☀️") -> "☀️"
                    greetingText.contains("🌤️") -> "🌤️"
                    greetingText.contains("🌆") -> "🌆"
                    greetingText.contains("🌙") -> "🌙"
                    else -> "☀️"
                }
                val greetingWithoutEmoji =
                    greetingText.replace(" ☀️", "").replace(" 🌤️", "").replace(" 🌆", "")
                        .replace(" 🌙", "")

                Text(
                    text = "$greetingWithoutEmoji,\n$userName $greetingEmoji",
                    style = LexendTypography.Bold4,
                    color = StamindColors.HeaderColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                )
            }

            // Bugün Nasıl Hissediyorsun - WelcomeHeroCard üstüne biner
            TodayMoodCard(
                selectedMoodIndex = selectedMoodIndex,
                onMoodSelected = { index -> onSelectMood(index, moods[index], null) },
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        // 80dp yukarı kaydır, layout alanını da küçült
                        val offsetY = -80.dp.roundToPx()
                        layout(placeable.width, placeable.height + offsetY) {
                            placeable.placeRelative(0, offsetY)
                        }
                    }
            )

            // TodayMoodCard ile Haftalık Duygu Durum arası 32dp
            Spacer(modifier = Modifier.height(32.dp))

            // Haftalık Duygu Durum Başlığı ve Geçmiş linki
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Haftalık Duygu Durum",
                    style = LexendTypography.Bold6,
                    color = StamindColors.HeaderColor
                )

                Text(
                    text = "Geçmiş>",
                    style = LexendTypography.Medium8,
                    color = StamindColors.Green500,
                    modifier = Modifier.clickable { /* TODO: Navigate to history */ }
                )
            }

            // Başlık ile takvim arası 16dp boşluk
            Spacer(modifier = Modifier.height(16.dp))

            // Tarih Kartları
            DateCardScroller(
                selectedMoodIndex = selectedMoodIndex,
                savedMoods = lastSevenDaysMoods
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Gününü Anlat - Hero Card (zamana göre dinamik içerik)
            ActionImageCard(
                imageRes = timeConfig.heroImageRes,
                title = timeConfig.actionTitle,
                subtitle = timeConfig.actionSubtitle,
                buttonText = timeConfig.actionButtonText,
                onButtonClick = onNavigateToJournal
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Haftalık Mood Grafiği
            WeeklyMoodGraphHome(
                journalEntries = journalEntries,
                onNavigateToAnalysis = onNavigateToAnalysis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


@Composable
fun ActionImageCard(
    imageRes: Int,
    title: String,
    subtitle: String,
    buttonText: String = "Başla",
    onButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Responsive resim boyutu (358x170 @ 390dp ekran genişliği)
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.toFloat()
    val baseWidthDp = 390f
    val scaleFactor = screenWidthDp / baseWidthDp

    // Resim için orantılı boyutlar
    val imageAspectRatio = 358f / 170f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                color = StamindColors.White,
                shape = RoundedCornerShape(32.dp)
            )
            .border(
                width = 2.dp,
                color = StamindColors.CardStrokeColor,
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Resim - zamana göre değişen hero resmi, 32dp radius
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(imageAspectRatio)
                    .clip(RoundedCornerShape(32.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Başlık - LexendBold7, HeaderColor, ortalanmış
            Text(
                text = title,
                style = LexendTypography.Bold7,
                color = StamindColors.HeaderColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle - LexendRegular8, TextColor, ortalanmış
            Text(
                text = subtitle,
                style = LexendTypography.Regular8,
                color = StamindColors.TextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buton - 32dp radius, Green900 arka plan, Green100 yazı
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StamindColors.Green900
                ),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
            ) {
                Text(
                    text = buttonText,
                    style = LexendTypography.SemiBold8,
                    color = StamindColors.Green100
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun DateCardScroller(
    selectedMoodIndex: Int,
    savedMoods: List<MoodEntry>
) {
    val today =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

    // Bugünden 1 hafta öncesine kadar tarihler (bugün dahil 7 gün)
    val dates = remember {
        val datesList = mutableListOf<DateInfo>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat =
            SimpleDateFormat("EEE", Locale.Builder().setLanguage("tr").setRegion("TR").build())
        val dayOfMonthFormat = SimpleDateFormat("d", Locale.getDefault())

        val calendar = Calendar.getInstance()

        // Bugünden 6 gün önceye git (toplam 7 gün)
        calendar.add(Calendar.DAY_OF_YEAR, -6)

        for (i in 0 until 7) {
            val currentDate = Calendar.getInstance()
            currentDate.timeInMillis = calendar.timeInMillis
            currentDate.add(Calendar.DAY_OF_YEAR, i)

            val dayName = dayFormat.format(currentDate.time)
            // 3 harfli gün kısaltması, büyük harflerle
            val dayOfWeekFormatted = dayName.take(3).uppercase()
            val dayOfMonth = dayOfMonthFormat.format(currentDate.time)
            val fullDate = dateFormat.format(currentDate.time)

            datesList.add(DateInfo(dayOfWeekFormatted, dayOfMonth, fullDate))
        }
        datesList
    }

    // Responsive boyutlar (390dp baz ekran genişliği)
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.toFloat()
    val baseWidthDp = 390f
    val scaleFactor = screenWidthDp / baseWidthDp
    val cardWidth = (56 * scaleFactor).dp
    val cardHeight = (111 * scaleFactor).dp
    val emojiSize = (42 * scaleFactor).dp

    // Bugün en sağda görünsün diye son elemana scroll yap
    val listState = androidx.compose.foundation.lazy.rememberLazyListState(
        initialFirstVisibleItemIndex = (dates.size - 1).coerceAtLeast(0)
    )

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
    ) {
        items(dates) { date ->
            val savedMood = savedMoods.find { it.date == date.fullDate }
            val isToday = (date.fullDate == today)

            DateCard(
                dateInfo = date,
                isToday = isToday,
                moodIndex = when {
                    savedMood != null -> savedMood.moodIndex
                    isToday && selectedMoodIndex >= 0 -> selectedMoodIndex
                    else -> null
                },
                cardWidth = cardWidth,
                cardHeight = cardHeight,
                emojiSize = emojiSize
            )
        }
    }
}

@Composable
fun DateCard(
    dateInfo: DateInfo,
    isToday: Boolean,
    moodIndex: Int?,
    cardWidth: androidx.compose.ui.unit.Dp,
    cardHeight: androidx.compose.ui.unit.Dp,
    emojiSize: androidx.compose.ui.unit.Dp
) {
    // Mood renk ve icon mapping (TodayMoodCard ile aynı)
    val moodData = remember {
        listOf(
            Triple(R.drawable.sentiment_very_satisfied, StamindColors.Green600, "Harika"),
            Triple(R.drawable.sentiment_satisfied, StamindColors.Green400, "İyi"),
            Triple(R.drawable.sentiment_neutral, StamindColors.Orange400, "Ortalama"),
            Triple(R.drawable.sentiment_dissatisfied, StamindColors.Orange600, "Kötü"),
            Triple(R.drawable.sentiment_very_dissatisfied, StamindColors.Red500, "Berbat")
        )
    }

    // Stroke rengi: mood seçilmişse emoji rengi, seçilmemişse CardStrokeColor
    val strokeColor =
        if (moodIndex != null) moodData[moodIndex].second else StamindColors.CardStrokeColor
    val emojiColor = moodIndex?.let { moodData[it].second }
    val emojiIcon = moodIndex?.let { moodData[it].first }

    // Dashed stroke path effect
    val dashPathEffect =
        androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    // Mood seçilmemişse dashed, seçilmişse solid stroke
    val useDashedStroke = moodIndex == null

    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .background(
                color = StamindColors.White,
                shape = RoundedCornerShape(20.dp)
            )
            .then(
                if (useDashedStroke) {
                    // Bugün veya mood seçilmemiş: dashed stroke
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = strokeColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = dashPathEffect,
                                cap = StrokeCap.Round
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
                        )
                    }
                } else {
                    // Mood seçilmiş: solid stroke (emoji rengiyle)
                    Modifier.border(
                        width = 2.dp,
                        color = strokeColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gün kısaltması - BUGÜN veya PAZ, PZT vb.
            Text(
                text = if (isToday) "BUGÜN" else dateInfo.dayOfWeek,
                style = LexendTypography.Bold9,
                color = StamindColors.SecondaryGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Gün numarası
            Text(
                text = dateInfo.dayOfMonth,
                style = LexendTypography.Bold5,
                color = StamindColors.HeaderColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Emoji alanı
            Box(
                modifier = Modifier.size(emojiSize),
                contentAlignment = Alignment.Center
            ) {
                if (moodIndex == null) {
                    // Mood seçilmemiş: 24x24 daire ve soru işareti
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = StamindColors.SecondaryGray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "?",
                            style = LexendTypography.Bold7,
                            color = StamindColors.CardStrokeColor,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (emojiIcon != null && emojiColor != null) {
                    // Mood var: emoji göster
                    Image(
                        painter = painterResource(id = emojiIcon),
                        contentDescription = "Mood",
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(emojiColor),
                        modifier = Modifier.size(emojiSize)
                    )
                }
            }
        }
    }
}


@Composable
fun WeeklyMoodGraphHome(
    journalEntries: List<JournalEntry>,
    onNavigateToAnalysis: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf("Pz", "Sa", "Ça", "Pe", "Cu", "Ct", "Pz")

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

    // Puana göre renk belirleme fonksiyonu (100 tonu potansiyel, 500 tonu alınan)
    // Kötü: Red, Orta: Orange, İyi: Green
    fun getColorForScore(score: Int): Pair<Color, Color> {
        return when {
            score < 40 -> Pair(StamindColors.Red100, StamindColors.Red500) // Kötü
            score < 70 -> Pair(StamindColors.Orange100, StamindColors.Orange500) // Orta
            else -> Pair(StamindColors.Green100, StamindColors.Green500) // İyi
        }
    }

    // Haftalık ortalama puan hesaplama
    // Önce her puanı yuvarlayıp, sonra aritmetik ortalamasını alıyoruz
    val weeklyAverage = remember(journalEntries, dates) {
        val weekEntries = journalEntries.filter { entry -> dates.contains(entry.date) }
        if (weekEntries.isNotEmpty()) {
            // Her puanı yuvarlama formülü: ((ham_puani + 5) / 10) * 10
            val quantizedScores = weekEntries.map { entry ->
                ((entry.ham_puani + 5) / 10) * 10
            }
            // Yuvarlanmış puanların toplamını 7'ye böl
            quantizedScores.sum() / 7
        } else {
            0
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Responsive sütun boyutları (37x140 @ 390dp ekran genişliği)
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp.toFloat()
        val baseWidthDp = 390f
        val scaleFactor = screenWidthDp / baseWidthDp

        val barWidthDp = (37 * scaleFactor).dp
        val barHeightDp = (140 * scaleFactor).dp
        val spacingDp = 8.dp

        // Başlık - "Haftalık Analiz Raporu" LexendBold7, HeaderColor, ekrandan 16dp
        // Not: modifier.padding kaldırıldı çünkü WeeklyMoodGraphHome zaten padding(horizontal = 16.dp) ile çağırılıyor
        Text(
            text = "Haftalık Analiz Raporu",
            style = LexendTypography.Bold6,
            color = StamindColors.HeaderColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Alt yazı - LexendRegular9, TextColor
        Text(
            text = "Günlük yaptığın analizlerle kendini dinle",
            style = LexendTypography.Regular8,
            color = StamindColors.TextColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Grafik Kartı - Beyaz arka plan, 32dp radius, 2dp stroke
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


            // Ortalama puan satırı
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Yüzde - LexendBold5, HeaderColor
                Text(
                    text = "%$weeklyAverage",
                    style = LexendTypography.Bold5,
                    color = StamindColors.HeaderColor
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Açıklama - LexendRegular9, TextColor
                Text(
                    text = "Bu hafta yapılan analizlerinin ortalaması. Detaylar için tıkla",
                    style = LexendTypography.Regular9,
                    color = StamindColors.TextColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grafik sütunları Row içinde - kartın tamamına yayılır
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                dates.forEachIndexed { index, date ->
                    val entry = journalEntries.find { it.date == date }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Sütun
                        if (entry != null) {
                            // Analiz var: renkli görüntü
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
                            // Analiz yok: CardStrokeColor dolgulu sütun
                            Box(
                                modifier = Modifier
                                    .width(barWidthDp)
                                    .height(barHeightDp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StamindColors.CardStrokeColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Gün kısaltması - BÜYÜK HARF, LexendBold9, HeaderColor
                        Text(
                            text = dayLabels[index].uppercase(),
                            style = LexendTypography.Bold9,
                            color = StamindColors.HeaderColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "1 - Sabah (Free Kullanıcı)", showBackground = true, widthDp = 390, heightDp = 1500)
@Composable
fun HomeMorningPreview() {
    val sampleMoods = listOf(
        MoodEntry(date = "2025-12-01", emoji = "😊", moodIndex = 0),
        MoodEntry(date = "2025-12-02", emoji = "😐", moodIndex = 1)
    )

    val sampleAnalysis = JournalEntry(

        date = "2025-12-06",
        journalText = "Bugün işte yoğun bir gün geçirdim ama akşam arkadaşlarımla buluştum.",
        duygusal_durum = "Dengeli",
        analiz_ozeti = "Günün zorluklarına rağmen sosyal bağlantılarını korumayı başardın. Arkadaşlarınla geçirdiğin zaman seni yeniledi.",
        ham_puani = 72,
        puan_aciklamasi = "Puan açıklaması",
        destek_mesaji = "Harika gidiyorsun!",
        temalar = emptyList(),
        gunluk_oneri = emptyList(),
        timestamp = System.currentTimeMillis(),
        isFavorite = false
    )
    val sampleJournalEntries = listOf(
        JournalEntry(
            date = "2025-12-02",
            journalText = "",
            duygusal_durum = "Mutlu",
            analiz_ozeti = "",
            ham_puani = 85,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        ),
        JournalEntry(
            date = "2025-12-03",
            journalText = "",
            duygusal_durum = "Nötr",
            analiz_ozeti = "",
            ham_puani = 55,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        ),
        JournalEntry(
            date = "2025-12-04",
            journalText = "",
            duygusal_durum = "Stresli",
            analiz_ozeti = "",
            ham_puani = 35,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        ),
        JournalEntry(
            date = "2025-12-05",
            journalText = "",
            duygusal_durum = "Çok Mutlu",
            analiz_ozeti = "",
            ham_puani = 95,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        ),
        JournalEntry(
            date = "2025-12-06",
            journalText = "",
            duygusal_durum = "Yorgun",
            analiz_ozeti = "",
            ham_puani = 45,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )
    )

    HomeScreenContent(

        selectedMoodIndex = 0,
        onSelectMood = { _, _, _ -> },
        lastSevenDaysMoods = sampleMoods,
        lastAnalysis = sampleAnalysis,
        journalEntries = sampleJournalEntries,
        onNavigateToJournal = {},
        onNavigateToAnalysis = {},
        greetingMessageOverride = "Günaydın ☀️"
    )
}

@Preview(
    name = "2 - Sabah (Premium Kullanıcı)",
    showBackground = true,
    widthDp = 390,
    heightDp = 1500
)
@Composable
fun HomePremiumPreview() {
    val sampleMoods = listOf(
        MoodEntry(date = "2025-12-01", emoji = "😊", moodIndex = 0),
        MoodEntry(date = "2025-12-02", emoji = "😐", moodIndex = 1)
    )

    val sampleAnalysis = JournalEntry(

        date = "2025-12-06",
        journalText = "Bugün işte yoğun bir gün geçirdim ama akşam arkadaşlarımla buluştum.",
        duygusal_durum = "Dengeli",
        analiz_ozeti = "Günün zorluklarına rağmen sosyal bağlantılarını korumayı başardın. Arkadaşlarınla geçirdiğin zaman seni yeniledi.",
        ham_puani = 72,
        puan_aciklamasi = "Puan açıklaması",
        destek_mesaji = "Harika gidiyorsun!",
        temalar = emptyList(),
        gunluk_oneri = emptyList(),
        timestamp = System.currentTimeMillis(),
        isFavorite = false
    )
    val sampleJournalEntries = listOf(
        JournalEntry(
            date = "2025-12-02",
            journalText = "",
            duygusal_durum = "Mutlu",
            analiz_ozeti = "",
            ham_puani = 85,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        ),
        JournalEntry(
            date = "2025-12-03",
            journalText = "",
            duygusal_durum = "Nötr",
            analiz_ozeti = "",
            ham_puani = 55,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        ),
        JournalEntry(
            date = "2025-12-04",
            journalText = "",
            duygusal_durum = "Stresli",
            analiz_ozeti = "",
            ham_puani = 35,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        ),
        JournalEntry(
            date = "2025-12-05",
            journalText = "",
            duygusal_durum = "Çok Mutlu",
            analiz_ozeti = "",
            ham_puani = 95,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        ),
        JournalEntry(
            date = "2025-12-06",
            journalText = "",
            duygusal_durum = "Yorgun",
            analiz_ozeti = "",
            ham_puani = 45,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )
    )

    HomeScreenContent(

        selectedMoodIndex = 1,
        onSelectMood = { _, _, _ -> },
        lastSevenDaysMoods = sampleMoods,
        lastAnalysis = sampleAnalysis,
        journalEntries = sampleJournalEntries,
        onNavigateToJournal = {},
        onNavigateToAnalysis = {},
        greetingMessageOverride = "Tünaydın 🌤️"
    )
}


@Preview(name = "Gününü Anlat Kartı (Sabah)", showBackground = true, widthDp = 390)
@Composable
fun ActionImageCardPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(StamindColors.BackgroundColor)
            .padding(vertical = 16.dp)
    ) {
        ActionImageCard(
            imageRes = R.drawable.morning_hero,
            title = "Günün Nasıl Geçti?",
            subtitle = "Gününü değerlendirmek için harika bir zaman. Hadi günlük yazalım!",
            buttonText = "Günlük Yaz",
            onButtonClick = {}
        )
    }
}

@Preview(name = "Haftalık Mood Grafiği", showBackground = true, widthDp = 390)
@Composable
fun WeeklyMoodGraphPreview() {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    // Monday of this week
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
    calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)

    val baseTime = calendar.timeInMillis
    val dayMs = 24 * 60 * 60 * 1000L

    val sampleJournalEntries = listOf(
        JournalEntry(
            date = dateFormat.format(baseTime),
            journalText = "",
            duygusal_durum = "Harika",
            analiz_ozeti = "",
            ham_puani = 90,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        ),
        JournalEntry(
            date = dateFormat.format(baseTime + dayMs),
            journalText = "",
            duygusal_durum = "İyi",
            analiz_ozeti = "",
            ham_puani = 75,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        ),
        JournalEntry(
            date = dateFormat.format(baseTime + 2 * dayMs),
            journalText = "",
            duygusal_durum = "Ortalama",
            analiz_ozeti = "",
            ham_puani = 55,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        ),
        JournalEntry(
            date = dateFormat.format(baseTime + 3 * dayMs),
            journalText = "",
            duygusal_durum = "Kötü",
            analiz_ozeti = "",
            ham_puani = 30,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        ),
        JournalEntry(
            date = dateFormat.format(baseTime + 4 * dayMs),
            journalText = "",
            duygusal_durum = "Harika",
            analiz_ozeti = "",
            ham_puani = 95,
            destek_mesaji = "",
            puan_aciklamasi = "",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(StamindColors.BackgroundColor)
            .padding(16.dp)
    ) {
        WeeklyMoodGraphHome(
            journalEntries = sampleJournalEntries,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}


/**
 * Karşılama Hero Kartı
 * - Full genişlik, responsive yükseklik (390:220 oranı)
 * - Sol ve sağ alt köşeler 50dp radius (üst köşeler düz)
 * - Arka plan tamamen resim (zamana göre değişiyor)
 */
@Composable
fun WelcomeHeroCard(
    heroImageRes: Int = R.drawable.morning_hero,
    modifier: Modifier = Modifier
) {
    // 390:220 oranını koruyarak responsive yükseklik hesapla
    val aspectRatio = 390f / 220f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(
                RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 50.dp,
                    bottomEnd = 50.dp
                )
            )
    ) {
        // Arka plan resmi - tüm kartı kaplıyor (zamana göre değişiyor)
        Image(
            painter = painterResource(id = heroImageRes),
            contentDescription = "Karşılama Arka Planı",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}



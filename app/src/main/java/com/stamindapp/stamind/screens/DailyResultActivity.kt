package com.stamindapp.stamind.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.stamindapp.stamind.R
import com.stamindapp.stamind.auth.SubscriptionManager
import com.stamindapp.stamind.database.JournalEntry
import com.stamindapp.stamind.engine.JournalAnalysis
import com.stamindapp.stamind.engine.Suggestion
import com.stamindapp.stamind.engine.getQuantizedScore
import com.stamindapp.stamind.model.JournalViewModel
import com.stamindapp.stamind.ui.theme.LexendTypography
import com.stamindapp.stamind.ui.theme.NunitoTypography
import com.stamindapp.stamind.ui.theme.StamindColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==================== MOOD THEME SYSTEM ====================

/**
 * Mood tema türleri
 */
enum class MoodTheme {
    GOOD,    // İyi (60+) - Yeşil tema
    AVERAGE, // Ortalama (40-59) - Turuncu tema
    BAD      // Kötü (0-39) - Kırmızı tema
}

/**
 * Mood temasına göre renk paleti
 */
data class MoodThemeColors(
    val gradientStart: Color,
    val gradientMid: Color,
    val gradientEnd: Color,
    val cardBackground: Color,      // 50 tonu
    val cellBackground: Color,      // 100 tonu (bar arkaplanı, duygu cell)
    val primaryColor: Color,        // 500 tonu (stroke, bar dolu kısım)
    val iconColor600: Color,        // 600 tonu (emoji ikonu)
    val textColor900: Color,        // 900 tonu (puan, "Duygusal Mod" yazısı)
    val titleGradientColors: List<Color>
)

/**
 * Puana göre MoodTheme belirle
 */
private fun getMoodTheme(score: Int): MoodTheme {
    return when {
        score >= 60 -> MoodTheme.GOOD
        score >= 40 -> MoodTheme.AVERAGE
        else -> MoodTheme.BAD
    }
}

/**
 * MoodTheme'e göre renk paleti getir
 */
@Composable
private fun getMoodThemeColors(theme: MoodTheme): MoodThemeColors {
    return when (theme) {
        MoodTheme.GOOD -> MoodThemeColors(
            gradientStart = StamindColors.Green50,
            gradientMid = StamindColors.Green50,
            gradientEnd = StamindColors.BackgroundColor,
            cardBackground = StamindColors.Green50,
            cellBackground = StamindColors.Green100,
            primaryColor = StamindColors.Green500,
            iconColor600 = StamindColors.Green600,
            textColor900 = StamindColors.Green900,
            titleGradientColors = listOf(
                StamindColors.Green900,
                StamindColors.Green600,
                StamindColors.Green300
            )
        )

        MoodTheme.AVERAGE -> MoodThemeColors(
            gradientStart = StamindColors.Orange50,
            gradientMid = StamindColors.Orange50,
            gradientEnd = StamindColors.BackgroundColor,
            cardBackground = StamindColors.Orange50,
            cellBackground = StamindColors.Orange100,
            primaryColor = StamindColors.Orange500,
            iconColor600 = StamindColors.Orange600,
            textColor900 = StamindColors.Orange900,
            titleGradientColors = listOf(
                StamindColors.Orange900,
                StamindColors.Orange600,
                StamindColors.Orange300
            )
        )

        MoodTheme.BAD -> MoodThemeColors(
            gradientStart = StamindColors.Red50,
            gradientMid = StamindColors.Red50,
            gradientEnd = StamindColors.BackgroundColor,
            cardBackground = StamindColors.Red50,
            cellBackground = StamindColors.Red100,
            primaryColor = StamindColors.Red500,
            iconColor600 = StamindColors.Red600,
            textColor900 = StamindColors.Red900,
            titleGradientColors = listOf(
                StamindColors.Red900,
                StamindColors.Red600,
                StamindColors.Red300
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyResultActivity(
    navController: NavController,
    viewModel: JournalViewModel,
    onNavigateToOffer: () -> Unit
    // MVP: onNavigateToChat kaldırıldı - Sta Chat ileride eklenecek
) {
    val displayAnalysis = viewModel.selectedDateAnalysis ?: viewModel.latestAnalysis
    val context = LocalContext.current
    val subscriptionManager = remember { SubscriptionManager(context) }
    val isPremium by subscriptionManager.isPremiumFlow().collectAsState(initial = false)


    if (displayAnalysis == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StamindColors.BackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Analiz yükleniyor...",
                    style = LexendTypography.Bold5,
                    color = StamindColors.HeaderColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = StamindColors.Green500,
                    strokeWidth = 4.dp
                )
            }
        }
        return
    }

    val analysisDateStr = if (displayAnalysis is JournalEntry) {
        displayAnalysis.date
    } else {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val trLocale = Locale.forLanguageTag("tr-TR")
    val outputFormat = SimpleDateFormat("d MMMM", trLocale)  // Gün adı kaldırıldı
    val date = inputFormat.parse(analysisDateStr)
    val formattedDate = date?.let { outputFormat.format(it) } ?: analysisDateStr

    val analysis = if (displayAnalysis is JournalEntry) {
        val entry: JournalEntry = displayAnalysis
        JournalAnalysis(
            duygusal_durum = entry.duygusal_durum,
            analiz_ozeti = entry.analiz_ozeti,
            ham_puani = entry.ham_puani,
            puan_aciklamasi = entry.puan_aciklamasi,
            destek_mesaji = entry.destek_mesaji,
            temalar = entry.temalar,
            gunluk_oneri = entry.gunluk_oneri
        )
    } else {
        displayAnalysis as JournalAnalysis
    }

    val onNavigateBack: () -> Unit = {
        navController.popBackStack()
    }

    // MVP: analysisContextForChat kaldırıldı - Sta Chat ileride eklenecek

    // Günlük metni
    val journalText = if (displayAnalysis is JournalEntry) {
        displayAnalysis.journalText
    } else {
        ""
    }

    // Kullanıcı adını al
    val userName =
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName?.split(" ")
            ?.firstOrNull() ?: "Dostum"

    DailyResultScreenContent(
        analysis = analysis,
        journalText = journalText,
        onNavigateBack = onNavigateBack,
        displayDate = formattedDate,
        userName = userName,
        isPremium = isPremium,
        onNavigateToOffer = onNavigateToOffer
        // MVP: onNavigateToChat kaldırıldı - Sta Chat ileride eklenecek
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyResultScreenContent(
    analysis: JournalAnalysis?,
    journalText: String = "",
    onNavigateBack: () -> Unit,
    displayDate: String,
    userName: String = "Dostum",
    modifier: Modifier = Modifier,
    isPremium: Boolean = false,
    onNavigateToOffer: () -> Unit = {}
    // MVP: onNavigateToChat kaldırıldı - Sta Chat ileride eklenecek
) {
    val scrollState = rememberScrollState()
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val statusBarPadding =
        if (isPreview) 0.dp else WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Mood temasını belirle (puana göre)
    val score = analysis?.getQuantizedScore() ?: 50
    val moodTheme = getMoodTheme(score)
    val themeColors = getMoodThemeColors(moodTheme)

    // Header yüksekliğini ölç - gradient bu noktaya kadar sürecek
    var headerHeightPx by remember { mutableStateOf(0f) }

    // Tüm ekran kaydırılabilir - gradient arka plan en üstten başlayıp EmotionalScoreCard tepesinde bitiyor
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StamindColors.BackgroundColor)
    ) {
        if (analysis == null) {
            // Preview'da veya loading durumunda null gelirse tam sayfa yükleme göster
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Analiz yükleniyor...",
                    style = LexendTypography.Bold5,
                    color = StamindColors.HeaderColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = StamindColors.Green500,
                    strokeWidth = 4.dp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // ==================== KARŞILAMA BÖLÜMÜ (Gradient Arkaplan) ====================
                // Gradient en tepeden başlar, header bölümünün sonunda BackgroundColor'a geçer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            headerHeightPx = coordinates.size.height.toFloat()
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    themeColors.gradientStart,
                                    themeColors.gradientStart,
                                    themeColors.gradientMid,
                                    themeColors.gradientEnd,
                                    StamindColors.BackgroundColor
                                ),
                                startY = 0f,
                                // Dinamik bitiş - header yüksekliğine kadar gradient sürer
                                endY = if (headerHeightPx > 0f) headerHeightPx else 1200f
                            )
                        )
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(statusBarPadding + 32.dp))

                        // AI İkonu ve Karşılama Mesajı (Ortalanmış)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, bottom = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Dinamik Duygu Emojisi - duygusal duruma göre değişir
                            val emotionIconRes = if (analysis != null) {
                                getEmotionIcon(
                                    analysis.duygusal_durum,
                                    analysis.getQuantizedScore()
                                )
                            } else {
                                R.drawable.sentiment_satisfied
                            }

                            // Emoji Box - 100 tonu arkaplan, gradient stroke
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .shadow(
                                        elevation = 12.dp,
                                        shape = CircleShape,
                                        ambientColor = themeColors.primaryColor.copy(alpha = 0.3f),
                                        spotColor = themeColors.primaryColor.copy(alpha = 0.2f)
                                    )
                                    .border(
                                        width = 2.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.9f),
                                                Color.White.copy(alpha = 0.3f),
                                                themeColors.primaryColor.copy(alpha = 0.6f)
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                                    .background(
                                        color = themeColors.cellBackground,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = emotionIconRes),
                                    contentDescription = "Duygu Durumu",
                                    colorFilter = ColorFilter.tint(themeColors.iconColor600),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .align(Alignment.Center)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Dinamik başlık - puana göre değişir
                            val dynamicTitle = if (analysis != null) {
                                when {
                                    analysis.getQuantizedScore() <= 39 -> "Senin Yanındayım, $userName"
                                    analysis.getQuantizedScore() <= 59 -> "Dengede Kalmayı Başardın, $userName"
                                    else -> "Harika Gidiyorsun, $userName"
                                }
                            } else {
                                "Merhaba, $userName"
                            }

                            Text(
                                text = dynamicTitle,
                                style = LexendTypography.Bold4.copy(
                                    brush = Brush.horizontalGradient(
                                        colors = themeColors.titleGradientColors
                                    )
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Statik karşılama notu
                            Text(
                                text = "İşte yazdıklarının bize anlattıkları.",
                                style = LexendTypography.Regular7,
                                color = StamindColors.HeaderColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    } // Column kapanışı
                } // Gradient Box kapanışı

                // ==================== KARTLAR VE İÇERİK ====================

                // İçerik - yatay padding ile
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    EmotionalScoreCard(
                        emotionalState = analysis.duygusal_durum,
                        score = analysis.getQuantizedScore(),
                        puanAciklamasi = analysis.puan_aciklamasi.takeIf { it.isNotBlank() }
                            ?: "Duygusal durumunu gösteren skor",
                        themeColors = themeColors
                    )

                    Spacer(modifier = Modifier.height(24.dp))  // EmotionalScoreCard - DailySummaryCard arası 24dp

                    DailySummaryCard(
                        summary = analysis.analiz_ozeti
                    )

                    Spacer(modifier = Modifier.height(16.dp))  // DailySummaryCard - SupportMessageCard arası 16dp

                    SupportMessageCard(
                        supportMessage = analysis.destek_mesaji,
                        themeColors = themeColors
                    )

                    Spacer(modifier = Modifier.height(32.dp))  // SupportMessageCard - StaChatCard arası 32dp
                }

                // MVP: StaChatCard (Sta'ya soruların mı var) kaldırıldı - ileride eklenecek

                // Sta'dan Öneriler - LexendBold6, HeaderColor, soldan 16dp
                Text(
                    text = "Sta'dan Öneriler",
                    style = LexendTypography.Bold6,
                    color = StamindColors.HeaderColor,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))  // Başlık - kartlar arası 12dp

                SuggestionsList(
                    suggestions = analysis.gunluk_oneri.ifEmpty {
                        listOf(
                            Suggestion("Derin Nefes Al", "5 dakika nefes egzersizi yap"),
                            Suggestion("Yürüyüş Yap", "Kısa bir yürüyüş yapabilirsin")
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))  // Öneriler - Tespit Edilen Temalar arası 32dp

                // Tespit Edilen Temalar
                if (analysis.temalar.isNotEmpty()) {
                    ThemesSection(themes = analysis.temalar)
                    Spacer(modifier = Modifier.height(32.dp))  // Temalar - Günlük arası 32dp
                }

                // Genişletilebilir Günlük Bölümü - 16dp sağ/sol boşluk
                if (journalText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        ExpandableJournalSection(journalText = journalText)
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))  // Günlük - Buton arası 48dp

                // Raporlara Dön butonu - 32dp sağ/sol boşluk
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StamindColors.HeaderColor,
                        contentColor = StamindColors.White
                    ),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)  // Toplam 32dp (16dp parent + 16dp burada)
                ) {
                    Text(
                        text = "Raporlara Dön",
                        style = LexendTypography.Bold8,
                        textAlign = TextAlign.Center,
                        color = StamindColors.White,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}


// ==================== HELPER FUNCTIONS ====================


/**
 * Duygu durumuna ve puana göre emoji ikonu seçer (Özel Drawable Kaynakları)
 * Önce duygusal duruma bakılır, eşleşme bulunamazsa puana göre fallback yapılır
 * @return Drawable resource ID (R.drawable.sentiment_*)
 */
private fun getEmotionIcon(emotionalState: String, score: Int): Int {
    val lower = emotionalState.lowercase()

    // Önce duygusal duruma göre ikon seç
    val emotionBasedIcon = when {
        // Mutlu/Pozitif - Very Satisfied (çok mutlu)
        lower.contains("mutlu") || lower.contains("harika") || lower.contains("keyifli") ||
                lower.contains("neşeli") || lower.contains("pozitif") || lower.contains("heyecanlı") ||
                lower.contains("coşkulu") || lower.contains("enerjik") -> R.drawable.sentiment_very_satisfied

        // Sakin/Nötr - Satisfied (dengeli gülümseme)
        lower.contains("nötr") || lower.contains("sakin") || lower.contains("durgun") ||
                lower.contains("normal") || lower.contains("dengeli") -> R.drawable.sentiment_satisfied

        // Yorgun/Düşük Enerji - Neutral
        lower.contains("yorgun") || lower.contains("halsiz") || lower.contains("bitkin") ||
                lower.contains("uykulu") || lower.contains("bıkkın") || lower.contains("düşük") -> R.drawable.sentiment_neutral

        // Üzgün - Dissatisfied  
        lower.contains("üzgün") || lower.contains("kırgın") || lower.contains("mutsuz") ||
                lower.contains("hüzünlü") || lower.contains("yalnız") -> R.drawable.sentiment_dissatisfied

        // Endişeli/Kaygılı - Dissatisfied
        lower.contains("endişe") || lower.contains("kaygı") || lower.contains("huzursuz") ||
                lower.contains("tedirgin") -> R.drawable.sentiment_dissatisfied

        // Kızgın/Stresli - Very Dissatisfied (kızgın)
        lower.contains("öfke") || lower.contains("kızgın") || lower.contains("sinir") ||
                lower.contains("gergin") || lower.contains("stres") -> R.drawable.sentiment_very_dissatisfied

        else -> null // Eşleşme yoksa puana bak
    }

    // Duygu eşleşmesi varsa onu döndür, yoksa puana göre seç
    return emotionBasedIcon ?: when {
        score >= 60 -> R.drawable.sentiment_satisfied
        score >= 40 -> R.drawable.sentiment_neutral
        else -> R.drawable.sentiment_dissatisfied
    }
}

// ==================== EMOTIONAL SCORE CARD ====================

/**
 * Yeni birleşik duygusal skor kartı
 * - 32dp corner radius, 2dp stroke
 * - Puana göre değişen emoji (tema rengi)
 * - Duygusal durum cell'i (tema arkaplan)
 * - 16dp kalınlığında, 32dp radius progress bar
 */
@Composable
fun EmotionalScoreCard(
    emotionalState: String,
    score: Int,
    puanAciklamasi: String = "Duygusal durumunu gösteren skor",
    modifier: Modifier = Modifier,
    themeColors: MoodThemeColors = getMoodThemeColors(getMoodTheme(score))
) {
    val emotionIconRes = getEmotionIcon(emotionalState, score)
    val progress = score.coerceIn(0, 100) / 100f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = themeColors.cardBackground,  // 50 tonu
                shape = RoundedCornerShape(32.dp)
            )
            .border(
                width = 2.dp,
                color = themeColors.primaryColor,    // 500 tonu (stroke)
                shape = RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 32.dp, vertical = 24.dp)  // Sol/Sağ 32dp, Üst/Alt 24dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Üst Satır: Emoji + Duygusal Mod | Durum Cell
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sol: Emoji + Duygusal Mod yazısı (8dp yatay boşluk)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = emotionIconRes),
                        contentDescription = "Duygu Durumu",
                        colorFilter = ColorFilter.tint(themeColors.primaryColor),  // 500 tonu (emoji)
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Duygusal Mod",
                        style = LexendTypography.Bold7,
                        color = themeColors.textColor900  // 900 tonu
                    )
                }

                // Sağ: Duygusal durum cell'i - 100 tonu arkaplan
                Box(
                    modifier = Modifier
                        .background(
                            color = themeColors.cellBackground,  // 100 tonu
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp)  // Sağ/Sol 8dp, Üst/Alt 5dp
                ) {
                    Text(
                        text = emotionalState,
                        style = LexendTypography.SemiBold8,
                        color = themeColors.primaryColor  // 500 tonu
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))  // Duygusal Mod - Puan arası 16dp

            // Orta Satır: Büyük puan (900 tonu) ve /100 (TextColor) - Solda, dikeyde ortalı
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$score",
                    style = LexendTypography.ExtraBold1,
                    color = themeColors.textColor900  // 900 tonu
                )
                Spacer(modifier = Modifier.width(4.dp))  // Puan - /100 arası 4dp
                Text(
                    text = "/100",
                    style = LexendTypography.SemiBold7,
                    color = StamindColors.TextColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))  // Puan - Bar arası 16dp

            // Progress Bar - 100 tonu arkaplan, 500 tonu dolu kısım
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(themeColors.cellBackground)  // 100 tonu
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(32.dp))
                        .background(themeColors.primaryColor)  // 500 tonu
                )
            }

            Spacer(modifier = Modifier.height(16.dp))  // Bar - Yazı arası 16dp

            // Bar altı açıklama - AI-generated veya default
            Text(
                text = puanAciklamasi,
                style = LexendTypography.Regular8,
                color = themeColors.textColor900
            )
        }
    }
}

// ==================== DAILY SUMMARY CARD ====================

/**
 * Günün Özeti kartı
 * - 32dp corner radius, 2dp stroke
 * - Article ikonu ile başlık (Lexend Bold)
 * - İçerik Lexend Medium
 */
@Composable
fun DailySummaryCard(
    summary: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
            .padding(24.dp)  // Tüm yönlerden 24dp
    ) {
        Column {
            // Başlık: Article icon (32dp) + Günün Özeti - 4dp yatay mesafe
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Article,
                    contentDescription = "Günün Özeti",
                    tint = StamindColors.Green500,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Günün Özeti",
                    style = LexendTypography.Bold6,
                    color = StamindColors.HeaderColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))  // Başlık-İçerik arası 8dp

            // İçerik - NunitoMedium7, TextColor
            Text(
                text = summary,
                style = NunitoTypography.Medium7,
                color = StamindColors.TextColor
            )
        }
    }
}

// ==================== SUPPORT MESSAGE CARD ====================

/**
 * Destek Mesajı kartı
 * - 32dp corner radius, 2dp dashed stroke (dash=9)
 * - Kalp ikonu 16x16dp, başlık LexendBold8 (500 tonu)
 * - İçerik NunitoBoldItalic8, HeaderColor
 * - Padding: dikey 16dp, yatay 24dp
 */
@Composable
fun SupportMessageCard(
    supportMessage: String,
    modifier: Modifier = Modifier,
    themeColors: MoodThemeColors = getMoodThemeColors(MoodTheme.GOOD)
) {
    val cornerRadius = 32.dp
    val strokeWidth = 4.dp
    val dashLength = 32f
    val gapLength = 16f  // Daha seyrek aralık

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val strokePx = strokeWidth.toPx()
                val radiusPx = cornerRadius.toPx()
                drawRoundRect(
                    color = themeColors.primaryColor,
                    style = Stroke(
                        width = strokePx,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(dashLength, gapLength),
                            0f
                        )
                    ),
                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                )
            }
            .background(
                color = themeColors.cardBackground,  // 50 tonu
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column {
            // Başlık: Kalp icon (16dp) + Destek Mesajı - LexendBold8, 500 tonu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Destek Mesajı",
                    tint = themeColors.primaryColor,  // 500 tonu
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Destek Mesajı",
                    style = LexendTypography.Bold7,
                    color = themeColors.primaryColor  // 500 tonu
                )
            }

            Spacer(modifier = Modifier.height(8.dp))  // Başlık-İçerik arası 8dp

            // İçerik - NunitoBoldItalic8, HeaderColor
            Text(
                text = "\"$supportMessage\"",
                style = NunitoTypography.BoldItalic7,
                color = StamindColors.HeaderColor
            )
        }
    }
}

// ==================== THEMES SECTION ====================

// Tema etiketleri için renkler - index'e göre döngüsel olarak kullanılır
private val themeTagColors = listOf(
    Color(0xFF2B6CB0), // Mavi
    Color(0xFF2F855A), // Yeşil
    Color(0xFFC53030), // Kırmızı
    Color(0xFFD97706), // Turuncu
    Color(0xFF6B46C1), // Mor
    Color(0xFF0891B2)  // Teal
)

/**
 * Tespit Edilen Temalar bölümü
 * - Hashtag etiketleri
 * - Renkli arka planlar
 */
@Composable
fun ThemesSection(
    themes: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally  // Başlık yatayda ortada
    ) {
        // Başlık - LexendBold6, HeaderColor, yatayda ortalı
        Text(
            text = "Tespit Edilen Temalar",
            style = LexendTypography.Bold6,
            color = StamindColors.HeaderColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))  // Başlık - etiketler arası 12dp

        // Tema etiketleri - FlowRow benzeri yapı
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                12.dp,
                Alignment.CenterHorizontally
            )  // 12dp aralık
        ) {
            themes.take(2).forEachIndexed { index, theme ->
                ThemeTag(
                    theme = theme,
                    colorIndex = index
                )
            }
        }

        if (themes.size > 2) {
            Spacer(modifier = Modifier.height(12.dp))  // Satırlar arası 12dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                themes.drop(2).take(2).forEachIndexed { index, theme ->
                    ThemeTag(
                        theme = theme,
                        colorIndex = index + 2
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeTag(
    theme: String,
    colorIndex: Int = 0
) {
    // Öneri kartlarıyla aynı renk paleti (6 renk)
    val tagColors = listOf(
        Pair(Color(0xFFEDE9FE), Color(0xFF6B46C1)), // Mor
        Pair(Color(0xFFFEF3C7), Color(0xFFD97706)), // Turuncu
        Pair(Color(0xFFFEE2E2), Color(0xFFC53030)), // Kırmızı
        Pair(Color(0xFFCFFAFE), Color(0xFF0891B2)), // Teal
        Pair(Color(0xFFD1FAE5), Color(0xFF059669)), // Yeşil
        Pair(Color(0xFFDBEAFE), Color(0xFF2563EB))  // Mavi
    )
    val (bgColor, textColor) = tagColors[colorIndex % tagColors.size]

    Box(
        modifier = Modifier
            .background(
                color = bgColor,  // 50 tonu arkaplan
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.5.dp,
                color = textColor,  // 700 tonu stroke
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)  // Yatay 16dp, dikey 8dp
    ) {
        Text(
            text = "#$theme",
            style = LexendTypography.SemiBold8,
            color = textColor  // 700 tonu metin
        )
    }
}

// AnalysisSummary kaldırıldı - DailySummaryCard ve SupportMessageCard ile değiştirildi

// ==================== SMART ICON SELECTOR ====================

/**
 * Öneri başlığına göre akıllı ikon seçimi
 * Anahtar kelimelere göre uygun Material Icon döndürür
 */
private fun getSuggestionIcon(title: String): ImageVector {
    val lowerTitle = title.lowercase()
    return when {
        // Uyku/Dinlenme
        lowerTitle.contains("uyku") || lowerTitle.contains("dinlen") ||
                lowerTitle.contains("istirahat") || lowerTitle.contains("yat") -> Icons.Filled.Bedtime

        // Nefes/Soluk
        lowerTitle.contains("nefes") || lowerTitle.contains("soluk") -> Icons.Filled.Air

        // Egzersiz/Hareket
        lowerTitle.contains("yürü") || lowerTitle.contains("koş") ||
                lowerTitle.contains("egzersiz") || lowerTitle.contains("spor") ||
                lowerTitle.contains("hareket") -> Icons.Filled.DirectionsWalk

        // Meditasyon/Yoga
        lowerTitle.contains("meditasyon") || lowerTitle.contains("yoga") ||
                lowerTitle.contains("farkındalık") || lowerTitle.contains("mindful") -> Icons.Filled.SelfImprovement

        // Su/Hidrasyon
        lowerTitle.contains("su") || lowerTitle.contains("içecek") ||
                lowerTitle.contains("hidrat") -> Icons.Filled.WaterDrop

        // Müzik/Ses
        lowerTitle.contains("müzik") || lowerTitle.contains("dinle") ||
                lowerTitle.contains("şarkı") || lowerTitle.contains("ses") -> Icons.Filled.MusicNote

        // Yazı/Günlük
        lowerTitle.contains("yaz") || lowerTitle.contains("günlük") ||
                lowerTitle.contains("not") || lowerTitle.contains("kaydet") -> Icons.Filled.Edit

        // Sosyal/Arkadaş
        lowerTitle.contains("arkadaş") || lowerTitle.contains("sosyal") ||
                lowerTitle.contains("aile") || lowerTitle.contains("sohbet") -> Icons.Filled.People

        // Varsayılan: Ampul
        else -> Icons.Outlined.Lightbulb
    }
}

// ==================== SUGGEST CARD ====================

// Öneri kartları için renkli ikon arkaplan renkleri
private val suggestionIconColors = listOf(
    Pair(Color(0xFF6B46C1), Color(0xFFEDE9FE)), // Mor - açık mor arkaplan
    Pair(Color(0xFFD97706), Color(0xFFFEF3C7)), // Turuncu - açık turuncu arkaplan
    Pair(Color(0xFFC53030), Color(0xFFFEE2E2)), // Kırmızı - açık kırmızı arkaplan
    Pair(Color(0xFF0891B2), Color(0xFFCFFAFE)), // Teal - açık teal arkaplan
    Pair(Color(0xFF059669), Color(0xFFD1FAE5)), // Yeşil - açık yeşil arkaplan
    Pair(Color(0xFF2563EB), Color(0xFFDBEAFE))  // Mavi - açık mavi arkaplan
)

/**
 * Yenilenmiş öneri kartı
 * - 32dp corner radius, 2dp stroke
 * - Akıllı ikon seçimi
 * - Daire formatında renkli ikon arkaplanları
 */
@Composable
fun SuggestCard(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    colorIndex: Int = 0,
    onClick: (() -> Unit)? = null
) {
    val suggestionIcon = getSuggestionIcon(title)
    val (iconColor, iconBgColor) = suggestionIconColors[colorIndex % suggestionIconColors.size]

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .background(
                color = StamindColors.White,
                shape = RoundedCornerShape(32.dp)
            )
            .border(
                width = 2.dp,
                color = StamindColors.CardStrokeColor,
                shape = RoundedCornerShape(32.dp)
            )
            .padding(
                start = 24.dp,
                end = 32.dp,
                top = 16.dp,
                bottom = 16.dp
            )  // Sol 24, sağ 32, üst/alt 16
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // İkon kutusu - 48x48dp Daire formatında, renkli arkaplan
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = iconBgColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = suggestionIcon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Metin içeriği
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = LexendTypography.SemiBold7,
                    color = StamindColors.HeaderColor
                )
                Spacer(modifier = Modifier.height(4.dp))  // Başlık-öneri yazısı arası 4dp
                Text(
                    text = detail,
                    style = NunitoTypography.Medium7,
                    color = StamindColors.SecondaryGray // #4B5952
                )
            }
        }
    }
}

@Composable
fun SuggestionsList(suggestions: List<Suggestion>, modifier: Modifier = Modifier) {
    if (suggestions.isEmpty()) {
        Text(
            text = "Öneri yok",
            style = LexendTypography.Regular7,
            color = StamindColors.TextColor,
            modifier = modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {  // Kart araları 12dp
        suggestions.forEachIndexed { index, suggestion ->
            SuggestCard(
                title = suggestion.baslik,
                detail = suggestion.detay,
                colorIndex = index,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


// Genişletilebilir Günlük Bölümü - Açılır kart yapısı
@Composable
fun ExpandableJournalSection(journalText: String) {
    var isExpanded by remember { mutableStateOf(false) }

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
    ) {
        // Başlık satırı - tıklanabilir
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol: HistoryEdu ikonu + Başlık
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.HistoryEdu,
                    contentDescription = "Günlük",
                    tint = StamindColors.Green500,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Yazdığın Günlük Burada",
                    style = LexendTypography.Bold8,
                    color = StamindColors.HeaderColor
                )
            }

            // Sağ: Aşağı/Yukarı ok ikonu
            Icon(
                imageVector = if (isExpanded)
                    Icons.Filled.KeyboardArrowUp
                else
                    Icons.Filled.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Kapat" else "Aç",
                tint = StamindColors.TextColor,
                modifier = Modifier.size(24.dp)
            )
        }

        // Açılır içerik
        androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
            ) {
                HorizontalDivider(
                    color = StamindColors.CardStrokeColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = journalText,
                    style = NunitoTypography.Medium7,
                    color = StamindColors.TextColor
                )
            }
        }
    }
}

// ==================== MOCK DATA FOR PREVIEWS ====================

// İyi puan (60-100)
val mockAnalysisGood = JournalAnalysis(
    duygusal_durum = "Enerjik",
    analiz_ozeti = "Bugün harika bir gün geçirdin! Enerjin yüksek ve motivasyonun tam.",
    ham_puani = 75,
    puan_aciklamasi = "Enerjin yüksek! Bugün harika bir gün geçirmişsin.",
    destek_mesaji = "Bu pozitif enerjiyi korumaya devam et!",
    temalar = listOf("Üretkenlik", "Farkındalık", "Kişisel Gelişim", "Şükran"),
    gunluk_oneri = listOf(
        Suggestion("Başarılarını Kutla", "Bugünkü başarılarını not al"),
        Suggestion("Yürüyüş Yap", "Bu güzel enerjiyle doğada vakit geçir")
    )
)

// Ortalama puan (40-59)
val mockAnalysisAverage = JournalAnalysis(
    duygusal_durum = "Dengeli",
    analiz_ozeti = "Bugün orta düzeyde bir gün geçirdin. İniş çıkışlar olsa da dengede kaldın.",
    ham_puani = 50,
    puan_aciklamasi = "Dengeli hissediyorsun. Günün iniş çıkışları olsa da dengedesin.",
    destek_mesaji = "Kendine biraz zaman ayırabilirsin.",
    temalar = listOf("İş", "Stres", "Denge"),
    gunluk_oneri = listOf(
        Suggestion("Derin Nefes Al", "5 dakika nefes egzersizi yap"),
        Suggestion("Müzik Dinle", "Sakinleştirici müziklerle rahatlayabilirsin")
    )
)

// Kötü puan (0-39)
val mockAnalysisBad = JournalAnalysis(
    duygusal_durum = "Düşük Enerji",
    analiz_ozeti = "Bugün zorlu bir gün geçirdiğini görüyorum. Kendine nazik ol.",
    ham_puani = 25,
    puan_aciklamasi = "Zorlu bir gün geçirdin. Kendine nazik ol.",
    destek_mesaji = "Zor günler geçici. Yanındayım.",
    temalar = listOf("Kaygı", "Yorgunluk"),
    gunluk_oneri = listOf(
        Suggestion("Dinlen", "Biraz mola ver, dinlenmek önemli"),
        Suggestion("Bir Arkadaşını Ara", "Sevdiklerinle konuşmak iyi gelebilir")
    )
)

@Preview(showBackground = true, name = "İyi Puan (75)", widthDp = 390, heightDp = 1700)
@Composable
fun DailyResultPreview_Good() {
    DailyResultScreenContent(
        analysis = mockAnalysisGood,
        onNavigateBack = {},
        displayDate = "23 Aralık"
    )
}

@Preview(showBackground = true, name = "Ortalama Puan (50)", widthDp = 390, heightDp = 1700)
@Composable
fun DailyResultPreview_Average() {
    DailyResultScreenContent(
        analysis = mockAnalysisAverage,
        onNavigateBack = {},
        displayDate = "23 Aralık"
    )
}

@Preview(showBackground = true, name = "Kötü Puan (25)", widthDp = 390, heightDp = 1700)
@Composable
fun DailyResultPreview_Bad() {
    DailyResultScreenContent(
        analysis = mockAnalysisBad,
        onNavigateBack = {},
        displayDate = "23 Aralık",
        journalText = "Bugün kendimi çok kötü hissettim. İşler yolunda gitmiyor ve motivasyonum düşük. Umarım yarın daha iyi olur."
    )
}

@Preview(showBackground = true, name = "Yükleniyor")
@Composable
fun DailyResultPreview_Loading() {
    DailyResultScreenContent(
        analysis = null,
        onNavigateBack = {},
        displayDate = ""
    )
}


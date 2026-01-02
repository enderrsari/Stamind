package com.stamindapp.stamind.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stamindapp.stamind.R
import com.stamindapp.stamind.ui.theme.LexendTypography
import com.stamindapp.stamind.ui.theme.StamindColors

/**
 * Yeni Duygu Seçim Kartı
 * - 32dp radius, 2dp stroke
 * - Ekrandan 16dp yatay boşluk
 * - Ekran üstünden 140dp boşluk (hero image üstüne biner)
 * - 5 emoji: Harika, İyi, Ortalama, Kötü, Berbat
 */
@Composable
fun TodayMoodCard(
    selectedMoodIndex: Int,
    onMoodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Mood verileri (emoji drawable, ikon rengi, arka plan rengi, etiket)
    val moodData = remember {
        listOf(
            MoodItem(
                iconRes = R.drawable.sentiment_very_satisfied,
                iconColor = StamindColors.Green600,
                backgroundColor = StamindColors.Green100,
                label = "Harika"
            ),
            MoodItem(
                iconRes = R.drawable.sentiment_satisfied,
                iconColor = StamindColors.Green400,
                backgroundColor = StamindColors.Green50,
                label = "İyi"
            ),
            MoodItem(
                iconRes = R.drawable.sentiment_neutral,
                iconColor = StamindColors.Orange400,
                backgroundColor = StamindColors.Orange50,
                label = "Ortalama"
            ),
            MoodItem(
                iconRes = R.drawable.sentiment_dissatisfied,
                iconColor = StamindColors.Orange600,
                backgroundColor = StamindColors.Orange100,
                label = "Kötü"
            ),
            MoodItem(
                iconRes = R.drawable.sentiment_very_dissatisfied,
                iconColor = StamindColors.Red500,
                backgroundColor = StamindColors.Red100,
                label = "Berbat"
            )
        )
    }

    // Kart yapısı - sağdan soldan 16dp boşluk
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
            .padding(horizontal = 15.dp, vertical = 16.dp) // İç padding
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Başlık - "Bugün nasıl hissediyorsun?"
            Text(
                text = "Bugün nasıl hissediyorsun?",
                style = LexendTypography.Bold7,
                color = StamindColors.HeaderColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Emoji satırı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Top
            ) {
                val anySelected = selectedMoodIndex >= 0

                moodData.forEachIndexed { index, mood ->
                    val isSelected = selectedMoodIndex == index

                    MoodEmojiItem(
                        mood = mood,
                        isSelected = isSelected,
                        anySelected = anySelected,
                        onClick = { onMoodSelected(index) }
                    )
                }
            }
        }
    }
}

/**
 * Tek bir mood item'ı - emoji kutusu ve etiket
 */
@Composable
private fun MoodEmojiItem(
    mood: MoodItem,
    isSelected: Boolean,
    anySelected: Boolean,
    onClick: () -> Unit
) {
    // Renk mantığı: seçiliyse veya hiçbiri seçilmediyse orijinal renkler, aksi halde gri
    val isColorful = isSelected || !anySelected
    val displayStrokeColor = if (isColorful) mood.iconColor else StamindColors.SecondaryGray
    val displayBackgroundColor =
        if (isColorful) mood.backgroundColor else StamindColors.CardStrokeColor
    val displayIconColor = if (isColorful) mood.iconColor else StamindColors.SecondaryGray

    val dashPathEffect = remember { PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        // Emoji kutusu - 56x56dp, dashed stroke (1dp)
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = displayBackgroundColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .drawBehind {
                    drawRoundRect(
                        color = displayStrokeColor,
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = dashPathEffect,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Emoji ikonu - 32x32dp
            Image(
                painter = painterResource(id = mood.iconRes),
                contentDescription = mood.label,
                colorFilter = ColorFilter.tint(displayIconColor),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Etiket
        Text(
            text = mood.label,
            style = LexendTypography.SemiBold9,
            color = displayIconColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Mood item data class
 */
private data class MoodItem(
    val iconRes: Int,
    val iconColor: Color,
    val backgroundColor: Color,
    val label: String
)

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun TodayMoodCardPreview_NoSelection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(StamindColors.BackgroundColor)
    ) {
        TodayMoodCard(
            selectedMoodIndex = -1,
            onMoodSelected = {},
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayMoodCardPreview_Selected() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(StamindColors.Green100)
    ) {
        TodayMoodCard(
            selectedMoodIndex = 0,
            onMoodSelected = {},
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

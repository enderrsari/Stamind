package com.stamindapp.stamind.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stamindapp.stamind.screens.DateInfo
import com.stamindapp.stamind.ui.theme.LexendTypography
import com.stamindapp.stamind.ui.theme.StamindColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaySummaryBottomSheet(
    dateInfo: DateInfo,
    currentMoodEmoji: String?,
    onMoodSelected: (Int, String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val moods = remember { listOf("Harika 😊", "Ortalama 😐", "Stresli 😔", "Kızgın 😡", "Yorgun 😴") }
    val moodEmojisOnly = remember { listOf("😊", "😐", "😔", "😡", "😴") }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = StamindColors.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "${dateInfo.dayOfMonth} ${dateInfo.dayOfWeek}",
                style = LexendTypography.Bold5,
                color = StamindColors.HeaderColor,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Bu gün için hissini seç",
                style = LexendTypography.Regular7,
                color = StamindColors.TextColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mood Selection Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // First Row (3 items)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (i in 0..2) {
                        MoodSelectionItem(
                            text = moods[i],
                            isSelected = currentMoodEmoji == moodEmojisOnly[i],
                            onClick = { onMoodSelected(i, moodEmojisOnly[i]) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Second Row (2 items)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (i in 3..4) {
                        MoodSelectionItem(
                            text = moods[i],
                            isSelected = currentMoodEmoji == moodEmojisOnly[i],
                            onClick = { onMoodSelected(i, moodEmojisOnly[i]) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoodSelectionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                color = if (isSelected) StamindColors.Green500 else StamindColors.White,
                shape = RoundedCornerShape(32.dp)
            )
            .border(
                width = 2.dp,
                color = if (isSelected) StamindColors.Green500 else StamindColors.CardStrokeColor,
                shape = RoundedCornerShape(32.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) StamindColors.White else StamindColors.HeaderColor,
            style = LexendTypography.Bold8,
            textAlign = TextAlign.Center
        )
    }
}

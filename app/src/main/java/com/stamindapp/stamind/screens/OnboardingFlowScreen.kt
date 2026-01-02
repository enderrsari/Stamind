package com.stamindapp.stamind.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stamindapp.stamind.ui.theme.LexendTypography
import com.stamindapp.stamind.ui.theme.StamindColors

data class OnboardingUserData(
    val name: String = "",
    val birthDate: String = ""
)

@Composable
fun OnboardingFlowScreen(
    onComplete: (OnboardingUserData) -> Unit
) {
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    var userName by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StamindColors.BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Spacer for top area
            Spacer(modifier = Modifier.height(24.dp))

            // Progress indicator - sadece adım 1, 2, 3'te göster (tanıtım hariç)
            if (currentStep > 0) {
                OnboardingProgressIndicator(
                    currentStep = currentStep - 1,
                    totalSteps = 2 // İsim, Doğum Tarihi
                )
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Content based on step
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentStep) {
                    0 -> OnboardingIntroStep()
                    1 -> OnboardingNameStep(
                        name = userName,
                        onNameChange = { userName = it }
                    )

                    2 -> OnboardingBirthDateStep(
                        birthDate = birthDate,
                        onBirthDateChange = { birthDate = it }
                    )
                }
            }

            // Navigation button
            Button(
                onClick = {
                    if (currentStep < 2) {
                        currentStep++
                    } else {
                        onComplete(
                            OnboardingUserData(
                                name = userName,
                                birthDate = birthDate
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StamindColors.Green600),
                shape = RoundedCornerShape(32.dp),
                enabled = when (currentStep) {
                    1 -> userName.isNotBlank()
                    2 -> birthDate.length == 10 // DD/MM/YYYY format
                    else -> true
                }
            ) {
                Text(
                    text = if (currentStep < 2) "Devam Et" else "Başlayalım",
                    style = LexendTypography.Bold7,
                    color = StamindColors.White,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingProgressIndicator(
    currentStep: Int,
    totalSteps: Int
) {
    // Yüzde hesapla - ilk adım %25, son adım %100
    val percentage = ((currentStep + 1) * 100) / totalSteps

    Column(modifier = Modifier.fillMaxWidth()) {
        // Yüzde göstergesi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "İlerleme",
                style = LexendTypography.Medium8,
                color = StamindColors.TextColor
            )
            Text(
                text = "%$percentage",
                style = LexendTypography.Bold7,
                color = StamindColors.Green600
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(StamindColors.Green200)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StamindColors.Green500)
            )
        }
    }
}

@Composable
fun OnboardingIntroStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🧘",
            style = LexendTypography.Bold1,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Zihinsel Sağlığını\nÖncelikli Kıl",
            style = LexendTypography.Bold3,
            color = StamindColors.HeaderColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Stamind, günlük ruh halini takip etmeni, meditasyon yapmını ve zihinsel iyi oluşunu desteklemeni sağlar.",
            style = LexendTypography.Regular6,
            color = StamindColors.TextColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OnboardingNameStep(
    name: String,
    onNameChange: (String) -> Unit
) {
    // Karakter sınırlamaları
    val maxChars = 50
    val minChars = 2

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sana nasıl hitap edelim?",
            style = LexendTypography.Bold4,
            color = StamindColors.HeaderColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Deneyimini kişiselleştirmek için ismini öğrenmek istiyoruz.",
            style = LexendTypography.Regular6,
            color = StamindColors.TextColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { newText ->
                if (newText.length <= maxChars) {
                    onNameChange(newText)
                }
            },
            placeholder = {
                Text(
                    text = "Adın",
                    style = LexendTypography.Regular6,
                    color = StamindColors.Green500.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StamindColors.Green500,
                unfocusedBorderColor = StamindColors.CardStrokeColor,
                focusedContainerColor = StamindColors.White,
                unfocusedContainerColor = StamindColors.White
            ),
            textStyle = LexendTypography.Medium6,
            singleLine = true,
            supportingText = {
                Text(
                    text = "${name.length} / $maxChars",
                    style = LexendTypography.Regular8,
                    color = if (name.isNotEmpty() && name.length < minChars) StamindColors.Red500 else StamindColors.TextColor
                )
            },
            isError = name.isNotEmpty() && name.length < minChars
        )
    }
}

@Composable
fun OnboardingBirthDateStep(
    birthDate: String,
    onBirthDateChange: (String) -> Unit
) {
    // Parse existing date or use defaults
    val parts = birthDate.split("/")
    val initialDay = parts.getOrNull(0)?.toIntOrNull() ?: 15
    val initialMonth = parts.getOrNull(1)?.toIntOrNull() ?: 6
    val initialYear = parts.getOrNull(2)?.toIntOrNull() ?: 2000

    var selectedDay by rememberSaveable { mutableIntStateOf(initialDay) }
    var selectedMonth by rememberSaveable { mutableIntStateOf(initialMonth) }
    var selectedYear by rememberSaveable { mutableIntStateOf(initialYear) }

    var isDayExpanded by rememberSaveable { mutableStateOf(false) }
    var isMonthExpanded by rememberSaveable { mutableStateOf(false) }
    var isYearExpanded by rememberSaveable { mutableStateOf(false) }

    // Update the formatted string whenever selection changes
    val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth, selectedYear)
    if (formattedDate != birthDate) {
        onBirthDateChange(formattedDate)
    }

    val days = (1..31).toList()
    val months = listOf(
        1 to "Ocak", 2 to "Şubat", 3 to "Mart", 4 to "Nisan",
        5 to "Mayıs", 6 to "Haziran", 7 to "Temmuz", 8 to "Ağustos",
        9 to "Eylül", 10 to "Ekim", 11 to "Kasım", 12 to "Aralık"
    )
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = (currentYear - 100..currentYear - 10).toList().reversed()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Doğum tarihin nedir?",
            style = LexendTypography.Bold4,
            color = StamindColors.HeaderColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Yaşına uygun içerikler sunmamıza yardımcı olur.",
            style = LexendTypography.Regular6,
            color = StamindColors.TextColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Dropdown Date Picker - Her biri açılır kapanır liste
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Day Dropdown
            DateDropdownSelector(
                label = "Gün",
                value = selectedDay.toString(),
                isExpanded = isDayExpanded,
                onExpandChange = {
                    isDayExpanded = it
                    if (it) {
                        isMonthExpanded = false
                        isYearExpanded = false
                    }
                },
                items = days.map { it.toString() },
                onItemSelected = { selectedDay = days[it] },
                modifier = Modifier.weight(1f)
            )

            // Month Dropdown
            DateDropdownSelector(
                label = "Ay",
                value = months.find { it.first == selectedMonth }?.second ?: "",
                isExpanded = isMonthExpanded,
                onExpandChange = {
                    isMonthExpanded = it
                    if (it) {
                        isDayExpanded = false
                        isYearExpanded = false
                    }
                },
                items = months.map { it.second },
                onItemSelected = { selectedMonth = months[it].first },
                modifier = Modifier.weight(1.3f)
            )

            // Year Dropdown
            DateDropdownSelector(
                label = "Yıl",
                value = selectedYear.toString(),
                isExpanded = isYearExpanded,
                onExpandChange = {
                    isYearExpanded = it
                    if (it) {
                        isDayExpanded = false
                        isMonthExpanded = false
                    }
                },
                items = years.map { it.toString() },
                onItemSelected = { selectedYear = years[it] },
                modifier = Modifier.weight(1.2f)
            )
        }
    }
}

@Composable
fun DateDropdownSelector(
    label: String,
    value: String,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    items: List<String>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = LexendTypography.Bold8,
            color = StamindColors.TextColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box {
            // Seçim butonu
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandChange(!isExpanded) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpanded) StamindColors.Green100 else StamindColors.White
                ),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = if (isExpanded) StamindColors.Green500 else StamindColors.CardStrokeColor
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = value,
                        style = LexendTypography.Bold7,
                        color = StamindColors.HeaderColor
                    )
                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        style = LexendTypography.Regular8,
                        color = StamindColors.Green600
                    )
                }
            }

            // Açılır liste
            if (isExpanded) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    colors = CardDefaults.cardColors(containerColor = StamindColors.White),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        StamindColors.CardStrokeColor
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        items(items.size) { index ->
                            val isSelected = items[index] == value
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) StamindColors.Green100 else Color.Transparent
                                    )
                                    .clickable {
                                        onItemSelected(index)
                                        onExpandChange(false)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = items[index],
                                    style = if (isSelected) LexendTypography.Bold7 else LexendTypography.Regular7,
                                    color = if (isSelected) StamindColors.Green700 else StamindColors.TextColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingFlowPreview() {
    OnboardingFlowScreen(onComplete = {})
}

@Preview(name = "1 - Giriş", showBackground = true)
@Composable
fun OnboardingIntroPreview() {
    Box(modifier = Modifier.background(StamindColors.BackgroundColor)) {
        OnboardingIntroStep()
    }
}

@Preview(name = "2 - İsim", showBackground = true)
@Composable
fun OnboardingNamePreview() {
    Box(modifier = Modifier.background(StamindColors.BackgroundColor)) {
        OnboardingNameStep(name = "Ender", onNameChange = {})
    }
}

@Preview(name = "3 - Doğum Tarihi", showBackground = true)
@Composable
fun OnboardingBirthDatePreview() {
    Box(modifier = Modifier.background(StamindColors.BackgroundColor)) {
        OnboardingBirthDateStep(
            birthDate = "15/06/2000",
            onBirthDateChange = {}
        )
    }
}


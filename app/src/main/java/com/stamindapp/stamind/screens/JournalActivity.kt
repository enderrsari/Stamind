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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.stamindapp.stamind.R
import com.stamindapp.stamind.database.JournalEntry
import com.stamindapp.stamind.model.JournalViewModel
import com.stamindapp.stamind.ui.theme.LexendTypography
import com.stamindapp.stamind.ui.theme.NunitoTypography
import com.stamindapp.stamind.ui.theme.StamindColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun JournalActivity(navController: NavController, viewModel: JournalViewModel) {
    var isWritingMode by rememberSaveable { mutableStateOf(false) }
    var journalEntry by rememberSaveable { mutableStateOf("") }

    val currentDate = remember {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("EEEE, d MMMM", Locale.forLanguageTag("tr-TR"))
        formatter.format(calendar.time)
    }

    val remainingQuota by viewModel.remainingQuota.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val allJournalEntries by viewModel.allJournalEntries.collectAsState()
    val isAnalyzing by viewModel.isSubmitting.collectAsState()

    val navigateToDailyResult by viewModel.navigateToDailyResult.collectAsState()
    val showOffer by viewModel.showOffer.collectAsState()

    LaunchedEffect(navigateToDailyResult) {
        if (navigateToDailyResult) {
            navController.navigate("dailyResult")
            viewModel.onNavigationDone()
            isWritingMode = false
        }
    }

    LaunchedEffect(showOffer) {
        if (showOffer) {
            navController.navigate("offer")
            viewModel.onOfferNavigationDone()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isWritingMode) {
            JournalWriteScreen(
                journalEntry = journalEntry,
                onJournalChange = { newText -> journalEntry = newText },
                onSaveClick = {
                    viewModel.tryAnalyzeJournalEntry(journalEntry)
                    journalEntry = ""
                },
                isSaveEnabled = journalEntry.isNotBlank() && !isAnalyzing,
                currentDate = currentDate,
                remainingQuota = remainingQuota,
                isPremium = isPremium,
                onBackClick = { isWritingMode = false }
            )
        } else {
            JournalListScreen(
                entries = allJournalEntries,
                onNewJournalClick = { isWritingMode = true },
                onEntryClick = { entry ->
                    viewModel.onJournalEntrySelected(entry)
                    navController.navigate("dailyResult")
                },
                onDeleteEntry = { entry ->
                    viewModel.deleteJournalEntry(entry)
                }
            )
        }

        // Analysis Loading Overlay
        if (isAnalyzing) {
            AnalysisLoadingOverlay()
        }
    }
}

@Composable
fun JournalListScreen(
    entries: List<JournalEntry>,
    onNewJournalClick: () -> Unit,
    onEntryClick: (JournalEntry) -> Unit,
    onDeleteEntry: (JournalEntry) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(JournalFilter.ALL) }
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Filtrelenmiş entries
    val filteredEntries = remember(entries, searchQuery, selectedFilter) {
        entries
            .filter { entry ->
                // Arama filtresi
                if (searchQuery.isBlank()) true
                else {
                    entry.journalText.contains(searchQuery, ignoreCase = true) ||
                            entry.duygusal_durum.contains(searchQuery, ignoreCase = true) ||
                            entry.analiz_ozeti.contains(searchQuery, ignoreCase = true)
                }
            }
            .filter { entry ->
                // Kategori filtresi
                when (selectedFilter) {
                    JournalFilter.ALL -> true
                    JournalFilter.HAPPY_MOMENTS -> entry.ham_puani >= 60
                    JournalFilter.SAD_MOMENTS -> entry.ham_puani < 40
                    JournalFilter.ANGRY_MOMENTS -> entry.ham_puani in 40..59
                }
            }
            .sortedByDescending { it.timestamp }
    }

    // Silme onay dialog'u
    if (showDeleteDialog && entryToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                entryToDelete = null
            },
            title = {
                Text(
                    text = "Günlüğü Sil",
                    style = LexendTypography.Bold6,
                    color = StamindColors.HeaderColor
                )
            },
            text = {
                Text(
                    text = "Bu günlük kalıcı olarak silinecek. Devam etmek istiyor musun?",
                    style = LexendTypography.Regular7,
                    color = StamindColors.TextColor
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        entryToDelete?.let { onDeleteEntry(it) }
                        showDeleteDialog = false
                        entryToDelete = null
                    }
                ) {
                    Text(
                        text = "Sil",
                        style = LexendTypography.Bold7,
                        color = StamindColors.Red500
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        entryToDelete = null
                    }
                ) {
                    Text(
                        text = "Vazgeç",
                        style = LexendTypography.Bold7,
                        color = StamindColors.Green600
                    )
                }
            },
            containerColor = StamindColors.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Hiç günlük yoksa (ilk kez kullanım) farklı, filtreleme sonucu boşsa farklı UI
    val isFirstTimeEmpty = entries.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = StamindColors.BackgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)  // Buton için boşluk
        ) {
            // ==================== HEADER BÖLÜMÜ ====================
            item {
                Text(
                    text = "Günlüklerim",
                    style = LexendTypography.Bold4,
                    color = StamindColors.HeaderColor,
                    modifier = Modifier
                        .padding(start = 16.dp, top = statusBarPadding + 32.dp)
                )
            }

            // Search bar ve filtre chip'leri sadece günlük varsa göster
            if (!isFirstTimeEmpty) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // ==================== SEARCH BAR ====================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp)
                            .background(
                                color = StamindColors.White,
                                shape = RoundedCornerShape(32.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = StamindColors.CardStrokeColor,
                                shape = RoundedCornerShape(32.dp)
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Büyüteç ikonu - 20x20dp
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Ara",
                                tint = StamindColors.SecondaryGray,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Arama input
                            androidx.compose.foundation.text.BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                textStyle = LexendTypography.Medium8.copy(
                                    color = StamindColors.HeaderColor
                                ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Anılarını ve duygularını ara...",
                                                style = LexendTypography.Medium8,
                                                color = StamindColors.SecondaryGray
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // ==================== FİLTRE CHİP'LERİ ====================
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        item {
                            FilterChip(
                                text = "Hepsi",
                                isSelected = selectedFilter == JournalFilter.ALL,
                                onClick = { selectedFilter = JournalFilter.ALL }
                            )
                        }
                        item {
                            FilterChip(
                                text = "Mutlu Anlar",
                                isSelected = selectedFilter == JournalFilter.HAPPY_MOMENTS,
                                onClick = { selectedFilter = JournalFilter.HAPPY_MOMENTS }
                            )
                        }
                        item {
                            FilterChip(
                                text = "Üzgün Anlar",
                                isSelected = selectedFilter == JournalFilter.SAD_MOMENTS,
                                onClick = { selectedFilter = JournalFilter.SAD_MOMENTS }
                            )
                        }
                        item {
                            FilterChip(
                                text = "Sinirli Anlar",
                                isSelected = selectedFilter == JournalFilter.ANGRY_MOMENTS,
                                onClick = { selectedFilter = JournalFilter.ANGRY_MOMENTS }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // ==================== GÜNLÜK LİSTESİ / BOŞ DURUM ====================
            if (isFirstTimeEmpty) {
                // ==================== İLK KEZ KULLANIM - BOŞ DURUM ====================
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Resim - drawable'dan
                        Image(
                            painter = painterResource(id = R.drawable.mini_card_image),
                            contentDescription = "Günlük Yazma",
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(32.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Başlık
                        Text(
                            text = "Henüz Günlük Yazmadın",
                            style = LexendTypography.Bold5,
                            color = StamindColors.HeaderColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Açıklama
                        Text(
                            text = "Haydi bugün nasıl hissettiğini keşfet.\nİçindekileri dökmek sana iyi gelecek.",
                            style = LexendTypography.Regular7,
                            color = StamindColors.TextColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Yeni Günlük Yaz Butonu
                        Button(
                            onClick = onNewJournalClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StamindColors.Green500,
                                contentColor = StamindColors.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HistoryEdu,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Yeni Günlük Yaz",
                                style = LexendTypography.Bold7,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // ==================== FİKİR CELL'LERİ (OTOMATİK KAYDIRMALI) ====================
                        // Infinite scroll için item listesini çoğalt
                        val repeatedRow1 = remember {
                            (0 until 100).flatMap { ideaPromptsRow1 }
                        }
                        val repeatedRow2 = remember {
                            (0 until 100).flatMap { ideaPromptsRow2 }
                        }

                        // LazyListState'ler
                        val listState1 = rememberLazyListState()
                        val listState2 = rememberLazyListState()

                        // Satır 1 - Smooth otomatik kaydırma
                        LaunchedEffect(Unit) {
                            while (true) {
                                delay(30L) // Daha hızlı güncelleme
                                // Smooth scroll - direkt scroll kullan (animateScrollToItem yerine)
                                val currentOffset = listState1.firstVisibleItemScrollOffset
                                val currentIndex = listState1.firstVisibleItemIndex

                                // Her 200 piksel civarında yeni item'a geç
                                if (currentOffset > 180) {
                                    listState1.scrollToItem(currentIndex + 1, 0)
                                } else {
                                    listState1.scrollToItem(currentIndex, currentOffset + 3)
                                }

                                // Liste sonuna yaklaşınca başa dön
                                if (currentIndex > repeatedRow1.size - 10) {
                                    listState1.scrollToItem(0)
                                }
                            }
                        }

                        LazyRow(
                            state = listState1,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            userScrollEnabled = true // Kullanıcı da kaydırabilir
                        ) {
                            items(repeatedRow1.size) { index ->
                                val originalIndex = index % ideaPromptsRow1.size
                                IdeaCell(
                                    text = ideaPromptsRow1[originalIndex].first,
                                    emoji = ideaPromptsRow1[originalIndex].second,
                                    colorIndex = originalIndex
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Satır 2 - Smooth otomatik kaydırma
                        LaunchedEffect(Unit) {
                            // Satır 2 için başlangıç pozisyonu (ortadan başlasın)
                            listState2.scrollToItem(repeatedRow2.size / 2)
                            while (true) {
                                delay(30L) // Daha hızlı güncelleme
                                val currentOffset = listState2.firstVisibleItemScrollOffset
                                val currentIndex = listState2.firstVisibleItemIndex

                                if (currentOffset > 180) {
                                    listState2.scrollToItem(currentIndex + 1, 0)
                                } else {
                                    listState2.scrollToItem(currentIndex, currentOffset + 3)
                                }

                                // Liste sonuna yaklaşınca başa dön
                                if (currentIndex > repeatedRow2.size - 10) {
                                    listState2.scrollToItem(0)
                                }
                            }
                        }

                        LazyRow(
                            state = listState2,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            userScrollEnabled = true // Kullanıcı da kaydırabilir
                        ) {
                            items(repeatedRow2.size) { index ->
                                val originalIndex = index % ideaPromptsRow2.size
                                IdeaCell(
                                    text = ideaPromptsRow2[originalIndex].first,
                                    emoji = ideaPromptsRow2[originalIndex].second,
                                    colorIndex = originalIndex + ideaPromptsRow1.size
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            } else if (filteredEntries.isEmpty()) {
                // Filtreleme sonucu boş
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Sonuç bulunamadı",
                                style = LexendTypography.Bold5,
                                color = StamindColors.HeaderColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Farklı bir arama veya filtre dene",
                                style = LexendTypography.Regular6,
                                color = StamindColors.TextColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Günlük entry'leri
                items(
                    items = filteredEntries,
                    key = { "${it.date}_${it.timestamp}" }
                ) { entry ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                entryToDelete = entry
                                showDeleteDialog = true
                                false // Dialog'dan onay bekle, otomatik silme
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            // Swipe arkaplanı - Kırmızı silme göstergesi
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = StamindColors.Red500,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(end = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Sil",
                                    tint = StamindColors.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        },
                        content = {
                            JournalEntryCard(
                                entry = entry,
                                onClick = { onEntryClick(entry) },
                                onDeleteClick = {
                                    entryToDelete = entry
                                    showDeleteDialog = true
                                }
                            )
                        },
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
        }

        // ==================== SABİT ALT BUTON (Sadece günlük varsa göster) ====================
        if (!isFirstTimeEmpty) {
            Button(
                onClick = onNewJournalClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StamindColors.Green500,
                    contentColor = StamindColors.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                shape = RoundedCornerShape(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HistoryEdu,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Yeni Günlük Yaz",
                    style = LexendTypography.Bold7,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

// Filtre enumı
enum class JournalFilter {
    ALL, HAPPY_MOMENTS, SAD_MOMENTS, ANGRY_MOMENTS
}

// ==================== FİKİR CELL VERİLERİ ====================
// Satır 1 fikirleri (emoji + metin)
private val ideaPromptsRow1 = listOf(
    Pair("Bugün neye minnettarsın?", "🌟"),
    Pair("En iyi anın ne oldu?", "✨"),
    Pair("Yarın için hedefin ne?", "🎯"),
    Pair("Kendine ne söylerdin?", "💭")
)

// Satır 2 fikirleri
private val ideaPromptsRow2 = listOf(
    Pair("Şu an ne düşünüyorsun?", "💬"),
    Pair("Bugün nasıl hissettin?", "❤️"),
    Pair("Neler öğrendin?", "📚"),
    Pair("Kimi özlüyorsun?", "🤗")
)

// Fikir cell'leri için renk paleti (suggestionIconColors ile aynı - öneri kartlarıyla tutarlı)
private val ideaCellColors = listOf(
    Pair(Color(0xFFEDE9FE), Color(0xFF6B46C1)), // Mor
    Pair(Color(0xFFFEF3C7), Color(0xFFD97706)), // Turuncu
    Pair(Color(0xFFFEE2E2), Color(0xFFC53030)), // Kırmızı
    Pair(Color(0xFFCFFAFE), Color(0xFF0891B2)), // Teal
    Pair(Color(0xFFD1FAE5), Color(0xFF059669)), // Yeşil
    Pair(Color(0xFFDBEAFE), Color(0xFF2563EB))  // Mavi
)

// Fikir Cell Composable
@Composable
private fun IdeaCell(
    text: String,
    emoji: String,
    colorIndex: Int = 0
) {
    val (bgColor, textColor) = ideaCellColors[colorIndex % ideaCellColors.size]

    Box(
        modifier = Modifier
            .background(
                color = bgColor,
                shape = RoundedCornerShape(32.dp)
            )
            .border(
                width = 1.dp,
                color = textColor,
                shape = RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = emoji,
                style = LexendTypography.Medium7
            )
            Text(
                text = text,
                style = LexendTypography.Medium8,
                color = textColor
            )
        }
    }
}

// Filtre chip composable
@Composable
private fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                color = if (isSelected) StamindColors.Green500 else StamindColors.White
            )
            .then(
                if (isSelected) {
                    Modifier  // Seçili durumda stroke yok
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = StamindColors.CardStrokeColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = LexendTypography.Medium8,
            color = if (isSelected) StamindColors.White else StamindColors.TextColor
        )
    }
}

@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Puana göre renk temasını belirle
    val moodColors = getMoodColorsForScore(entry.ham_puani)
    val dayLabel = getDayLabel(entry.date)
    val formattedDate = formatDateFull(entry.date)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = StamindColors.White,
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 2.dp,
                color = StamindColors.CardStrokeColor,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // ==================== ÜST BÖLÜM: Gün/Tarih + Duygu Cell ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Sol taraf: Gün ve Tarih
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Gün adı (BUGÜN, DÜN, SALI vb.)
                    Text(
                        text = dayLabel,
                        style = LexendTypography.Bold9,
                        color = StamindColors.SecondaryGray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Tarih (27 Aralık 2025)
                    Text(
                        text = formattedDate,
                        style = LexendTypography.Bold6,
                        color = StamindColors.HeaderColor
                    )
                }

                // Sağ taraf: Duygu Cell
                MoodCell(
                    emotionalState = entry.duygusal_durum,
                    score = entry.ham_puani,
                    moodColors = moodColors
                )
            }

            // ==================== GÜNLÜK ÖNİZLEME ====================
            Text(
                text = entry.journalText,
                style = NunitoTypography.SemiBold7,
                color = StamindColors.TextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== YATAY DİVİDER ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(1.dp)
                    .background(StamindColors.DividerColor)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== AI DESTEK MESAJI BÖLÜMÜ ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // AI Dairesi - 40x40dp
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = moodColors.color100,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI",
                        tint = moodColors.primary500,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // AI Destek Mesajı
                Text(
                    text = entry.destek_mesaji,
                    style = NunitoTypography.BoldItalic7,
                    color = StamindColors.HeaderColor,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// Duygu Cell Composable
@Composable
private fun MoodCell(
    emotionalState: String,
    score: Int,
    moodColors: JournalMoodColors
) {
    val emotionIcon = getEmotionIconForScore(emotionalState, score)

    Box(
        modifier = Modifier
            .background(
                color = moodColors.background50,
                shape = RoundedCornerShape(32.dp)
            )
            .border(
                width = 1.dp,
                color = moodColors.stroke200,
                shape = RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = emotionIcon,
                contentDescription = "Duygu",
                tint = moodColors.primary500,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = if (emotionalState.length > 10)
                    emotionalState.take(10) + "..."
                else emotionalState,
                style = LexendTypography.Bold8,
                color = moodColors.primary500,
                maxLines = 1
            )
        }
    }
}

// Mood renk sınıfı
data class JournalMoodColors(
    val background50: Color,
    val color100: Color,      // AI dairesi için
    val stroke200: Color,
    val primary500: Color
)

// Puana göre renk paleti
private fun getMoodColorsForScore(score: Int): JournalMoodColors {
    return when {
        score >= 60 -> JournalMoodColors(
            background50 = StamindColors.Green50,
            color100 = StamindColors.Green100,
            stroke200 = StamindColors.Green200,
            primary500 = StamindColors.Green500
        )

        score >= 40 -> JournalMoodColors(
            background50 = StamindColors.Orange50,
            color100 = StamindColors.Orange100,
            stroke200 = StamindColors.Orange200,
            primary500 = StamindColors.Orange500
        )

        else -> JournalMoodColors(
            background50 = StamindColors.Red50,
            color100 = StamindColors.Red100,
            stroke200 = StamindColors.Red200,
            primary500 = StamindColors.Red500
        )
    }
}

// Gün etiketi (BUGÜN, DÜN, veya gün adı)
private fun getDayLabel(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return dateString

        val today = Calendar.getInstance()
        val entryDate = Calendar.getInstance().apply { time = date }

        val diffDays =
            ((today.timeInMillis - entryDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        when (diffDays) {
            0 -> "BUGÜN"
            1 -> "DÜN"
            else -> {
                val dayFormat = SimpleDateFormat("EEEE", Locale.forLanguageTag("tr-TR"))
                dayFormat.format(date).uppercase(Locale.forLanguageTag("tr-TR"))
            }
        }
    } catch (e: Exception) {
        dateString
    }
}

// Tam tarih formatı (27 Aralık 2025)
private fun formatDateFull(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("tr-TR"))
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

// Duygu durumuna ve puana göre emoji ikonu
private fun getEmotionIconForScore(
    emotionalState: String,
    score: Int
): androidx.compose.ui.graphics.vector.ImageVector {
    val lower = emotionalState.lowercase()

    // Önce duygusal duruma göre ikon seç
    val emotionBasedIcon = when {
        lower.contains("mutlu") || lower.contains("harika") || lower.contains("keyifli") ||
                lower.contains("neşeli") || lower.contains("pozitif") || lower.contains("heyecanlı") ||
                lower.contains("coşkulu") || lower.contains("enerjik") -> Icons.Filled.SentimentVerySatisfied

        lower.contains("nötr") || lower.contains("sakin") || lower.contains("durgun") ||
                lower.contains("normal") || lower.contains("dengeli") -> Icons.Filled.SentimentSatisfied

        lower.contains("yorgun") || lower.contains("halsiz") || lower.contains("bitkin") ||
                lower.contains("uykulu") || lower.contains("bıkkın") || lower.contains("düşük") -> Icons.Filled.SentimentNeutral

        lower.contains("üzgün") || lower.contains("kırgın") || lower.contains("mutsuz") ||
                lower.contains("hüzünlü") || lower.contains("yalnız") -> Icons.Filled.SentimentDissatisfied

        lower.contains("endişe") || lower.contains("kaygı") || lower.contains("huzursuz") ||
                lower.contains("tedirgin") -> Icons.Filled.SentimentDissatisfied

        lower.contains("öfke") || lower.contains("kızgın") || lower.contains("sinir") ||
                lower.contains("gergin") || lower.contains("stres") -> Icons.Filled.SentimentVeryDissatisfied

        else -> null
    }

    return emotionBasedIcon ?: when {
        score >= 60 -> Icons.Filled.SentimentSatisfied
        score >= 40 -> Icons.Filled.SentimentNeutral
        else -> Icons.Filled.SentimentDissatisfied
    }
}

@Composable
fun JournalWriteScreen(
    journalEntry: String,
    onJournalChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    isSaveEnabled: Boolean,
    currentDate: String,
    remainingQuota: Int,
    isPremium: Boolean,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Permission handling (Removed SpeechToText)

    var showQuotaDialog by remember { mutableStateOf(false) }

    // Show error toast

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = StamindColors.BackgroundColor)
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarPadding + 24.dp, start = 16.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Geri Dön",
                    tint = StamindColors.Green700,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(color = StamindColors.BackgroundColor)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = currentDate,
                    style = LexendTypography.Bold4,
                    color = StamindColors.HeaderColor,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 8.dp)
                )



                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Yazmak için ilham al:",
                    style = LexendTypography.Bold7,
                    color = StamindColors.TextColor,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                )

                // İlham Prompt'ları - IdeaCell tarzında
                val journalPrompts = listOf(
                    Triple("Bugün ne için minnettarsın?", "🌟", 0),
                    Triple("Nasıl hissediyorum?", "💭", 1),
                    Triple("Hangi anım aklımda kaldı?", "✨", 2)
                )

                LazyRow(
                    contentPadding = PaddingValues(0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    items(journalPrompts.size) { index ->
                        val (text, emoji, colorIdx) = journalPrompts[index]
                        val (bgColor, textColor) = ideaCellColors[colorIdx % ideaCellColors.size]

                        Box(
                            modifier = Modifier
                                .background(
                                    color = bgColor,
                                    shape = RoundedCornerShape(32.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = textColor,
                                    shape = RoundedCornerShape(32.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = emoji,
                                    style = LexendTypography.Medium7
                                )
                                Text(
                                    text = text,
                                    style = LexendTypography.Medium8,
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                // Karakter sınırlaması: Free 2000, Premium 4000
                val maxCharLimit = if (isPremium) 4000 else 2000
                val minCharLimit = 10
                val currentCharCount = journalEntry.length
                val isOverLimit = currentCharCount > maxCharLimit
                val isUnderLimit = currentCharCount < minCharLimit && currentCharCount > 0

                Box {
                    OutlinedTextField(
                        value = journalEntry,
                        onValueChange = { newText ->
                            // Sadece limit dahilindeki metni kabul et
                            if (newText.length <= maxCharLimit) {
                                onJournalChange(newText)
                            }
                        },
                        enabled = remainingQuota > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 300.dp)
                            .border(
                                width = 2.dp,
                                color = when {
                                    isOverLimit || isUnderLimit -> StamindColors.Red500
                                    journalEntry.isNotEmpty() -> StamindColors.Green500
                                    else -> StamindColors.CardStrokeColor
                                },
                                shape = RoundedCornerShape(32.dp)
                            ),
                        shape = RoundedCornerShape(32.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = StamindColors.Green600,
                            focusedContainerColor = StamindColors.White,
                            unfocusedContainerColor = StamindColors.White,
                            disabledContainerColor = StamindColors.White,
                            disabledBorderColor = Color.Transparent,
                            disabledTextColor = StamindColors.TextColor.copy(alpha = 0.6f),
                            disabledPlaceholderColor = StamindColors.TextColor.copy(alpha = 0.4f)
                        ),
                        textStyle = NunitoTypography.Medium7,
                        placeholder = {
                            Text(
                                text = "Düşüncelerini, duygularını kaydet...",
                                style = NunitoTypography.Regular7,
                                color = StamindColors.TextColor
                            )
                        },
                        singleLine = false,
                        maxLines = Int.MAX_VALUE
                    )

                    if (remainingQuota <= 0) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showQuotaDialog = true }
                        )
                    }
                }

                // Karakter Sayacı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isUnderLimit) {
                        Text(
                            text = "En az $minCharLimit karakter gerekli",
                            style = NunitoTypography.Regular8,
                            color = StamindColors.Red500
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Text(
                        text = "$currentCharCount / $maxCharLimit",
                        style = NunitoTypography.Regular8,
                        color = if (currentCharCount > maxCharLimit * 0.9)
                            StamindColors.Red500
                        else
                            StamindColors.TextColor
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StamindColors.Green500,
                        contentColor = StamindColors.White
                    ),
                    enabled = isSaveEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Kaydet ve Analizi Başlat",
                        style = LexendTypography.Bold7,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Yapay zeka tarafından yapılan analizler klinik amaçlı değildir.\nYapay zeka yanılabilir lütfen bir profesyonelden destek alın",
                    style = LexendTypography.Regular9,
                    color = StamindColors.Green700,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


@Composable
private fun AnalysisLoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StamindColors.Green900.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated progress indicator
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = StamindColors.Green300,
                strokeWidth = 6.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Analiz yapılıyor...",
                style = LexendTypography.Bold5,
                color = StamindColors.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sta yazını inceliyor 🧠",
                style = LexendTypography.Regular7,
                color = StamindColors.Green200
            )
        }
    }
}

@Preview(name = "Günlük Listesi - Boş", showBackground = true, showSystemUi = true)
@Composable
fun JournalListEmptyPreview() {
    JournalListScreen(
        entries = emptyList(),
        onNewJournalClick = {},
        onEntryClick = {},
        onDeleteEntry = {}
    )
}

@Preview(name = "Günlük Listesi - Dolu", showBackground = true, showSystemUi = true)
@Composable
fun JournalListFilledPreview() {
    val sampleEntries = listOf(
        // YEŞİL KART - Yüksek puan (60+)
        JournalEntry(
            date = "2024-12-28",
            journalText = "Bugün parkta yürüyüş yaparken kuş sesleri dinlemek beni çok rahatlattı. Doğayla iç içe olmak gerçekten farklı bir huzur veriyor.",
            duygusal_durum = "Mutlu",
            analiz_ozeti = "Pozitif bir gün geçiriyorsunuz",
            ham_puani = 85,
            destek_mesaji = "Doğa ile bağlantı kurmak stres seviyeni gözle görülür şekilde azaltıyor. Bunu haftalık rutine eklemelisin."
        ),
        // TURUNCU KART - Orta puan (40-59)
        JournalEntry(
            date = "2024-12-27",
            journalText = "Bugün işte biraz yoğun bir gün geçirdim. Toplantılar art arda geldi ama akşama doğru toparladım.",
            duygusal_durum = "Kararsız",
            analiz_ozeti = "Stresle başa çıkabiliyorsunuz",
            ham_puani = 50,
            destek_mesaji = "Yoğun günlerde kendine küçük molalar vermeyi unutma. Bu enerji seviyeni korumana yardımcı olacaktır."
        ),
        // KIRMIZI KART - Düşük puan (0-39)
        JournalEntry(
            date = "2024-12-21",
            journalText = "Bugün kendimi çok yorgun ve motivasyonsuz hissettim. Hiçbir şey yapmak istemiyorum, sadece dinlenmek istiyorum.",
            duygusal_durum = "Üzgün",
            analiz_ozeti = "Zor bir dönemden geçiyorsunuz",
            ham_puani = 25,
            destek_mesaji = "Yorgun hissetmen çok normal, bedenin sana dinlenmen gerektiğini söylüyor. Kendine nazik ol ve bu duyguları kabul et."
        )
    )
    JournalListScreen(
        entries = sampleEntries,
        onNewJournalClick = {},
        onEntryClick = {},
        onDeleteEntry = {}
    )
}

@Preview(name = "Günlük Yazma - Ücretsiz", showBackground = true, showSystemUi = true)
@Composable
fun JournalWritePreview() {
    JournalWriteScreen(
        journalEntry = "",
        onJournalChange = {},
        onSaveClick = {},
        isSaveEnabled = false,
        currentDate = "Pazar, 8 Aralık",
        remainingQuota = 1,
        isPremium = false,
        onBackClick = {}
    )
}

@Preview(name = "Günlük Yazma - Premium", showBackground = true, showSystemUi = true)
@Composable
fun JournalWritePremiumPreview() {
    JournalWriteScreen(
        journalEntry = "Bugün kendimi çok iyi hissediyorum. Premium olmanın avantajlarını yaşıyorum!",
        onJournalChange = {},
        onSaveClick = {},
        isSaveEnabled = true,
        currentDate = "Pazartesi, 9 Aralık",
        remainingQuota = 0, // Premiumda kota sonsuzdur ama UI'da 0 gösterilmez, kontrol edilir
        isPremium = true,
        onBackClick = {}
    )
}

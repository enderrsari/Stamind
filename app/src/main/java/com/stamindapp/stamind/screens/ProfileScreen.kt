package com.stamindapp.stamind.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.stamindapp.stamind.auth.AuthManager
import com.stamindapp.stamind.auth.SubscriptionManager
import com.stamindapp.stamind.database.JournalEntry
import com.stamindapp.stamind.model.JournalViewModel
import com.stamindapp.stamind.ui.theme.LexendTypography
import com.stamindapp.stamind.ui.theme.StamindColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    authManager: AuthManager,
    subscriptionManager: SubscriptionManager,
    journalViewModel: JournalViewModel,
    onSignInClick: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPremium by subscriptionManager.isPremiumFlow().collectAsState(initial = false)
    val currentUser = authManager.getCurrentUser()
    val journalEntries by journalViewModel.allJournalEntries.collectAsState()

    ProfileScreenContent(
        modifier = modifier,
        isGuest = currentUser == null,
        isPremium = isPremium,
        displayName = currentUser?.displayName,
        email = currentUser?.email,
        journalEntries = journalEntries,
        onSignInClick = onSignInClick,
        onSignOut = onSignOut,
        subscriptionManager = subscriptionManager
    )
}

@Composable
private fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    isGuest: Boolean,
    isPremium: Boolean,
    displayName: String?,
    email: String?,
    journalEntries: List<JournalEntry> = emptyList(),
    onSignInClick: () -> Unit,
    onSignOut: () -> Unit,
    subscriptionManager: SubscriptionManager? = null
) {
    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    var isRestoring by remember { mutableStateOf(false) }

    // BillingManager for restore purchases - Skip in preview
    val billingManager = if (isPreview) null else remember {
        com.stamindapp.stamind.billing.BillingManager(context) { planId ->
            // Premium satın alım geri yüklendi, Firestore'u güncelle
            kotlinx.coroutines.GlobalScope.launch {
                subscriptionManager?.upgradeUserToPremium(planId)
            }
        }
    }

    // Start billing connection - Skip in preview
    LaunchedEffect(Unit) {
        if (!isPreview) {
            billingManager?.startConnection()
        }
    }

    // Cleanup - Skip in preview
    DisposableEffect(Unit) {
        onDispose {
            if (!isPreview) {
                billingManager?.endConnection()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StamindColors.BackgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Profil Header
            item {
                ProfileHeader(
                    displayName = displayName,
                    isPremium = isPremium
                )
            }

            // Kullanıcı Bilgileri Kartı
            item {
                UserInfoCard(
                    displayName = displayName,
                    email = email,
                    isPremium = isPremium
                )
            }

            // İstatistik Özeti
            item {
                QuickStatsCard(
                    journalCount = journalEntries.size,
                    avgScore = if (journalEntries.isNotEmpty()) {
                        journalEntries.map { it.ham_puani }.average().toInt()
                    } else 0
                )
            }

            // AYARLAR BÖLÜMÜ
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "AYARLAR",
                    style = LexendTypography.Bold8,
                    color = StamindColors.TextColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = "Uygulama Dili",
                        subtitle = "Türkçe",
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                "Dil seçeneği yakında eklenecek!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                    SettingsItem(
                        icon = Icons.Default.Refresh,
                        title = if (isRestoring) "Geri Yükleniyor..." else "Satın Alımları Geri Yükle",
                        subtitle = "Premium üyeliğini kontrol et",
                        onClick = {
                            if (!isRestoring && billingManager != null) {
                                isRestoring = true
                                billingManager.restorePurchases { success, message ->
                                    isRestoring = false
                                    android.widget.Toast.makeText(
                                        context,
                                        message,
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )

                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = "Uygulamayı Değerlendir",
                        subtitle = "Bize destek olun",
                        onClick = {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    "market://details?id=${context.packageName}".toUri()
                                )
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()
                                    )
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                }
                            }
                        }
                    )

                    SettingsItem(
                        icon = Icons.Default.PrivacyTip,
                        title = "Gizlilik Politikası",
                        onClick = {
                            val intent =
                                Intent(Intent.ACTION_VIEW, "https://stamind.app/privacy".toUri())
                            context.startActivity(intent)
                        }
                    )

                    SettingsItem(
                        icon = Icons.Default.Description,
                        title = "Kullanım Koşulları",
                        onClick = {
                            val intent =
                                Intent(Intent.ACTION_VIEW, "https://stamind.app/terms".toUri())
                            context.startActivity(intent)
                        }
                    )

                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Hakkında",
                        subtitle = "Stamind v1.0.0",
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                "Stamind ile zihinsel sağlığını keşfet!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsItem(
                        icon = Icons.Default.Logout,
                        title = "Çıkış Yap",
                        onClick = onSignOut,
                        isDestructive = true
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun UserInfoCard(
    displayName: String?,
    email: String?,
    isPremium: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = StamindColors.White),
        border = BorderStroke(2.dp, StamindColors.CardStrokeColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Hesap Bilgileri",
                style = LexendTypography.Bold6,
                color = StamindColors.HeaderColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "İsim",
                    style = LexendTypography.Regular7,
                    color = StamindColors.TextColor
                )
                Text(
                    text = displayName ?: "-",
                    style = LexendTypography.Medium7,
                    color = StamindColors.HeaderColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "E-posta",
                    style = LexendTypography.Regular7,
                    color = StamindColors.TextColor
                )
                Text(
                    text = email ?: "-",
                    style = LexendTypography.Medium7,
                    color = StamindColors.HeaderColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Üyelik Durumu",
                    style = LexendTypography.Regular7,
                    color = StamindColors.TextColor
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isPremium) StamindColors.Green600 else StamindColors.Green100,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isPremium) "Premium" else "Ücretsiz",
                        style = LexendTypography.Bold8,
                        color = if (isPremium) StamindColors.White else StamindColors.Green700
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsCard(
    journalCount: Int,
    avgScore: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = StamindColors.White),
        border = BorderStroke(2.dp, StamindColors.CardStrokeColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Hızlı Bakış",
                style = LexendTypography.Bold6,
                color = StamindColors.HeaderColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$journalCount",
                        style = LexendTypography.Bold3,
                        color = StamindColors.Green600
                    )
                    Text(
                        text = "Toplam Günlük",
                        style = LexendTypography.Regular8,
                        color = StamindColors.TextColor
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (avgScore > 0) "$avgScore" else "-",
                        style = LexendTypography.Bold3,
                        color = StamindColors.Green600
                    )
                    Text(
                        text = "Ort. Skor",
                        style = LexendTypography.Regular8,
                        color = StamindColors.TextColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    displayName: String?,
    isPremium: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Avatar - İsmin baş harfi
        val initial = displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(StamindColors.Green500),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = LexendTypography.Bold3,
                color = StamindColors.White
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // İsim ve PRO etiketi
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayName ?: "Kullanıcı",
                    style = LexendTypography.Bold5,
                    color = StamindColors.HeaderColor
                )

                if (isPremium) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = StamindColors.Green600,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PRO",
                            style = LexendTypography.Bold9,
                            color = StamindColors.White
                        )
                    }
                }
            }
            Text(
                text = if (isPremium) "Premium Üye" else "Standart Üye",
                style = LexendTypography.Regular8,
                color = StamindColors.TextColor
            )
        }
    }
}


@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = StamindColors.White),
        border = BorderStroke(
            width = 2.dp,
            color = if (isDestructive) StamindColors.Red500.copy(alpha = 0.2f) else StamindColors.CardStrokeColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDestructive) StamindColors.Red100 else StamindColors.Green100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isDestructive) StamindColors.Red500 else StamindColors.Green600,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = LexendTypography.Bold7,
                    color = if (isDestructive) StamindColors.Red500 else StamindColors.HeaderColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = LexendTypography.Regular8,
                        color = StamindColors.TextColor
                    )
                }
            }

            if (!isDestructive) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = StamindColors.TextColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Previews
@Preview(name = "1 - Free Kullanıcı", showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenFreePreview() {
    val sampleJournalEntries = listOf(
        JournalEntry(
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            journalText = "Bugün harika geçti.",
            duygusal_durum = "Pozitif",
            analiz_ozeti = "Güzel bir gün.",
            ham_puani = 82,
            puan_aciklamasi = "Puan açıklaması",
            destek_mesaji = "Harika!",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )
    )

    ProfileScreenContent(
        isGuest = false,
        isPremium = false,
        displayName = "Ender Sari",
        email = "endersari@gmail.com",
        journalEntries = sampleJournalEntries,
        onSignInClick = {},
        onSignOut = {}
    )
}

@Preview(name = "2 - Premium Kullanıcı", showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPremiumPreview() {
    val sampleJournalEntries = listOf(
        JournalEntry(
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            journalText = "Bugün harika geçti.",
            duygusal_durum = "Pozitif",
            analiz_ozeti = "Güzel bir gün.",
            ham_puani = 82,
            puan_aciklamasi = "Puan açıklaması",
            destek_mesaji = "Harika!",
            temalar = emptyList(),
            gunluk_oneri = emptyList(),
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )
    )

    ProfileScreenContent(
        isGuest = false,
        isPremium = true,
        displayName = "Ender Sari",
        email = "endersari@gmail.com",
        journalEntries = sampleJournalEntries,
        onSignInClick = {},
        onSignOut = {}
    )
}

@Preview(name = "3 - Profil İçeriği", showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(StamindColors.BackgroundColor)) {
        ProfileScreenContent(
            isGuest = false,
            isPremium = true,
            displayName = "Ender Sari",
            email = "endersari@gmail.com",
            onSignInClick = {},
            onSignOut = {}
        )
    }
}

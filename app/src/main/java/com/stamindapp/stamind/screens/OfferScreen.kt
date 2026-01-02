package com.stamindapp.stamind.screens

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.stamindapp.stamind.R
import com.stamindapp.stamind.auth.SubscriptionManager
import com.stamindapp.stamind.billing.BillingManager
import com.stamindapp.stamind.ui.theme.LexendTypography
import com.stamindapp.stamind.ui.theme.StamindColors
import kotlinx.coroutines.launch

private data class PlanInfo(
    val id: String,
    val title: String,
    val price: String,
    val highlight: Boolean = false,
    val originalProduct: ProductDetails? = null
)

@Composable
fun OfferScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()
    val subscriptionManager = remember { SubscriptionManager(context) }

    val billingManager = remember {
        BillingManager(context) { planId ->
            scope.launch {
                subscriptionManager.upgradeUserToPremium(planId)
                Toast.makeText(context, "Premium etkinleştirildi.", Toast.LENGTH_SHORT).show()
                onBack()
            }
        }
    }

    DisposableEffect(billingManager) {
        billingManager.startConnection()
        onDispose {
            billingManager.endConnection()
        }
    }

    val productDetails by billingManager.productDetails.collectAsState()
    val plans = productDetails.map {
        PlanInfo(
            id = it.productId,
            title = it.name,
            price = it.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                ?: "",
            highlight = it.productId.contains("yearly"),
            originalProduct = it
        )
    }.sortedByDescending { it.highlight } // Yıllık planı üste al

    var selectedPlanId by remember {
        mutableStateOf<String?>(
            plans.firstOrNull()?.id ?: (if (plans.isNotEmpty()) plans[0].id else null)
        )
    }

    // Planlar yüklendiğinde otomatik seç (yıllık plan tercih edilir)
    LaunchedEffect(plans) {
        if (selectedPlanId == null && plans.isNotEmpty()) {
            selectedPlanId = plans.find { it.highlight }?.id ?: plans.first().id
        }
    }

    OfferScreenContent(
        plans = plans,
        selectedPlanId = selectedPlanId,
        onPlanSelected = { selectedPlanId = it },
        onPurchaseClick = {
            val selectedPlan = plans.find { it.id == selectedPlanId }
            selectedPlan?.originalProduct?.let {
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(it)
                                .setOfferToken(
                                    it.subscriptionOfferDetails?.get(0)?.offerToken ?: ""
                                )
                                .build()
                        )
                    )
                    .build()
                billingManager.launchBillingFlow(activity, billingFlowParams)
            }
        },
        onBack = onBack
    )
}

@Composable
private fun OfferScreenContent(
    plans: List<PlanInfo>,
    selectedPlanId: String?,
    onPlanSelected: (String) -> Unit,
    onPurchaseClick: () -> Unit,
    onBack: () -> Unit
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StamindColors.BackgroundColor)
    ) {
        // Dekoratif Üst Görsel (Wave)
        Image(
            painter = painterResource(id = R.drawable.welcome_decor),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .align(Alignment.TopCenter),
            alpha = 0.6f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Header (Kapat Butonu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarPadding + 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(StamindColors.White.copy(alpha = 0.8f), CircleShape)
                        .border(1.dp, StamindColors.CardStrokeColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = StamindColors.HeaderColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Stamind Premium",
                    style = LexendTypography.Bold3,
                    color = StamindColors.Green900,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Zihinsel sağlığın için en iyi araçları keşfet.",
                    style = LexendTypography.Regular7,
                    color = StamindColors.TextColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Özellikler Listesi
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StamindColors.White, RoundedCornerShape(24.dp))
                        .border(1.dp, StamindColors.CardStrokeColor, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FeatureItem(Icons.AutoMirrored.Filled.Article, "Sınırsız Günlük Analizi")
                    FeatureItem(Icons.Default.HistoryEdu, "Detaylı Haftalık Wellness Raporu")
                    FeatureItem(Icons.Default.AutoAwesome, "Sta Chat: Kişisel AI Rehberin")
                    FeatureItem(Icons.Default.Star, "Öncelikli Yeni Özellik Erişimi")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Plan Seçenekleri Başlığı
                Text(
                    text = "Bir Plan Seç",
                    style = LexendTypography.Bold6,
                    color = StamindColors.HeaderColor,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (plans.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = StamindColors.Green500)
                    }
                } else {
                    plans.forEach { plan ->
                        PlanCard(
                            plan = plan,
                            isSelected = selectedPlanId == plan.id,
                            onClick = { onPlanSelected(plan.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Sabit Alt Buton
            Button(
                onClick = onPurchaseClick,
                enabled = selectedPlanId != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StamindColors.Green600,
                    contentColor = StamindColors.White,
                    disabledContainerColor = StamindColors.Green200
                ),
                shape = RoundedCornerShape(32.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = if (selectedPlanId == null) "Plan Seçin" else "Şimdi Abone Ol",
                    style = LexendTypography.Bold6,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(StamindColors.Green50, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StamindColors.Green600,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = LexendTypography.Medium7,
            color = StamindColors.TextColor
        )
    }
}

@Composable
private fun PlanCard(
    plan: PlanInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) StamindColors.Green50 else StamindColors.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) StamindColors.Green600 else StamindColors.CardStrokeColor,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.title,
                        style = LexendTypography.Bold6,
                        color = StamindColors.HeaderColor
                    )
                    if (plan.highlight) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(StamindColors.Green600, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "EN POPÜLER",
                                style = LexendTypography.Bold9,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plan.price,
                    style = LexendTypography.Medium7,
                    color = if (isSelected) StamindColors.Green700 else StamindColors.SecondaryGray
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = StamindColors.Green600,
                    unselectedColor = StamindColors.Green200
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OfferScreenPreview() {
    val samplePlans = listOf(
        PlanInfo(id = "yearly", title = "Yıllık Plan", price = "₺349.90 / yıl", highlight = true),
        PlanInfo(id = "monthly", title = "Aylık Plan", price = "₺39.90 / ay", highlight = false)
    )
    OfferScreenContent(
        plans = samplePlans,
        selectedPlanId = "yearly",
        onPlanSelected = {},
        onPurchaseClick = {},
        onBack = {}
    )
}

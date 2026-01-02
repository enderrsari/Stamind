package com.stamindapp.stamind.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(
    private val context: Context,
    private val onPremiumPurchased: (String) -> Unit
) {

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails = _productDetails.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    handlePurchase(purchase)
                }
            }
        } else {
            // Handle other error codes...
        }
    }

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isReady.value = true
                    queryProductDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                _isReady.value = false
                // Implement retry logic here
            }
        })
    }

    private fun queryProductDetails() {
        // TODO: Play Console hesabınız onaylandığında bu bölümü kendi ürün kimliklerinizle güncelleyin.
        // Örnek: .setProductId("monthly_plan_id")
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("android.test.purchased") // GEÇİCİ TEST KİMLİĞİ
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = productDetailsList
            }
        }
    }

    fun launchBillingFlow(activity: Activity, params: BillingFlowParams) {
        billingClient.launchBillingFlow(activity, params)
    }

    private fun handlePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Purchase acknowledged, now grant entitlement.
                    val planId = purchase.products.firstOrNull() ?: "android.test.purchased"
                    onPremiumPurchased(planId)
                }
            }
        }
    }

    /**
     * Restore previous purchases (for users who reinstall or switch devices)
     * Queries existing subscription purchases and triggers onPremiumPurchased if valid
     */
    fun restorePurchases(onResult: (Boolean, String) -> Unit) {
        if (!billingClient.isReady) {
            onResult(false, "Bağlantı hazır değil, lütfen tekrar deneyin")
            return
        }

        billingClient.queryPurchasesAsync(
            com.android.billingclient.api.QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val validPurchase = purchasesList.find {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (validPurchase != null) {
                    val planId = validPurchase.products.firstOrNull() ?: "restored_plan"
                    onPremiumPurchased(planId)
                    onResult(true, "Premium üyeliğiniz geri yüklendi!")
                } else {
                    onResult(false, "Geri yüklenecek satın alma bulunamadı")
                }
            } else {
                onResult(false, "Satın alma sorgulanamadı, lütfen tekrar deneyin")
            }
        }
    }

    fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}

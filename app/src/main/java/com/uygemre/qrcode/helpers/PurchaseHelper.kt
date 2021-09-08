package com.uygemre.qrcode.helpers

import androidx.fragment.app.FragmentActivity
import com.android.billingclient.api.*
import com.android.billingclient.api.Purchase.PurchaseState.PURCHASED

class PurchaseHelper(
    private val activity: FragmentActivity,
    private val purchaseHelperListener: PurchaseHelperListener
) : PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient

    fun setupPurchase() {
        billingClient = BillingClient.newBuilder(activity)
            .enablePendingPurchases()
            .setListener(this)
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    getAlreadyExistProductList()
                    getProductList()
                }
            }
            override fun onBillingServiceDisconnected() {
                billingClient.startConnection(this)
            }
        })
    }

    fun getProductList() {
        val skuList = mutableListOf<String>()
        skuList.add("monthly")
        skuList.add("annual")

        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.SUBS)

        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                purchaseHelperListener.getProductList(skuDetailsList)
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    purchases.map { _purchases ->
                        handleNonConsumableProduct(_purchases)
                    }

                    purchaseHelperListener.purchaseSuccess()
                }
            } BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {

            }
            else -> purchaseHelperListener.purchaseFailed(billingResult.responseCode)
        }
    }

    private fun handleNonConsumableProduct(purchase: Purchase) {
        if (purchase.purchaseState == PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) {}
            }
        }
    }

    fun purchase(skuDetails: SkuDetails) {
        val flowParams = BillingFlowParams
            .newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    private fun getAlreadyExistProductList() {
        val result = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            if (!result.purchasesList.isNullOrEmpty()) {
                purchaseHelperListener.getAlreadyExistProductList(result.purchasesList)
            } else {
                purchaseHelperListener.purchaseFailed(-1)
            }
        }
    }

    fun destroyBilling() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    interface PurchaseHelperListener {
        fun getProductList(products: List<SkuDetails>)
        fun getAlreadyExistProductList(alreadyProducts: List<Purchase>?)
        fun purchaseSuccess()
        fun purchaseFailed(responseCode: Int?)
    }
}
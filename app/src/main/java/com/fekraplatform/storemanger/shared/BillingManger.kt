package com.fekraplatform.storemanger.shared

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.fekraplatform.storemanger.application.MyApplication


class BillingManager(val productIds:List<String>): PurchasesUpdatedListener {


    private var params: PendingPurchasesParams = PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .build()


    private var billingClient: BillingClient = BillingClient.newBuilder(MyApplication.AppContext)
        .enablePendingPurchases(params)
        .setListener(this) // Set the listener for purchase updates
        .build() // Pending purchases are enabled by default

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Billing client is ready
//                    queryProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection later
            }
        })
    }

    fun queryProducts(onFail:(String)->Unit,onSuccess:(List<ProductDetails>)->Unit) {
        val skuList = productIds // Replace with your product IDs
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                skuList.map { skuId ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(skuId)
                        .setProductType(BillingClient.ProductType.INAPP) // Use INAPP for one-time purchases
                        .build()
                }
            )
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
                onSuccess(productDetailsList)
                // Handle the product details list
                Log.e("eeeeww",productDetailsList.toString())
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        // Grant the product to the user
                        if (!purchase.isAcknowledged) {
                            // Acknowledge the purchase
                            acknowledgePurchase(purchase)
                        }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                // User canceled the purchase
            }
            else -> {
                // Handle other errors
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Purchase acknowledged
            }
        }
    }
}
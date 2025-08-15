package com.fekraplatform.storemanger.activities

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.application.MyApplication
import com.fekraplatform.storemanger.models.Currency
import com.fekraplatform.storemanger.models.PageModel
import com.fekraplatform.storemanger.models.StoreCurrency
import com.fekraplatform.storemanger.shared.confirmDialog
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomRow
import com.fekraplatform.storemanger.shared.CustomRow2
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.MyHeader
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm3
import com.fekraplatform.storemanger.shared.confirmDialog2
import com.fekraplatform.storemanger.shared.formatPrice
import com.fekraplatform.storemanger.storage.GoogleBillingStorage
import com.fekraplatform.storemanger.storage.MyBilling
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import kotlinx.serialization.Serializable
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


class SubscriptionsStoreActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)


    var productDetails: ProductDetails? = null
    private val pendingParams = PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    purchases?.forEach { purchase ->
                        when (purchase.purchaseState) {
                            PurchaseState.PURCHASED -> {
                                setGooglePurchaseStorage(false,purchase)
                                updatePoints(false,purchase.products.first(),purchase.purchaseToken)
                            }
                            PurchaseState.PENDING -> {
                                setGooglePurchaseStorage(true,purchase)
                                updatePoints(true,purchase.products.first(),purchase.purchaseToken)
                            }
                        }
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    stateController.showMessage("Cenceled")
                    // User canceled the purchase
                }
                else -> {
                }
            }
        }

    val googleBillingStorage = GoogleBillingStorage()
    var inAppProducts by mutableStateOf(emptyList<SubscriptionModel>())
    var myBillings by mutableStateOf(googleBillingStorage.getMyBillings())

    var subs by mutableStateOf(emptyList< ProductDetails. SubscriptionOfferDetails>())
    private fun setGooglePurchaseStorage(isPending: Boolean, purchase: Purchase) {
        val b = googleBillingStorage.getMyBillings().toMutableList()
        val inAppProduct = inAppProducts.find { it.productId == purchase.products.first() }
        purchase.products.first()
        b += MyBilling(
            isPending, inAppProduct?.name ?: "Unk name",purchase.products.first(), purchase.purchaseToken
        )
        googleBillingStorage.setMyBillings(b)
    }

    lateinit var billingClient :BillingClient


    override fun onCreate(savedInstanceState: Bundle?) {

        billingClient =  BillingClient.newBuilder(this)
            .enablePendingPurchases(pendingParams)
            .setListener(purchasesUpdatedListener)
            .build()




        super.onCreate(savedInstanceState)
        stateController.successState()
        runBilling()
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme{

                MainCompose1(0.dp, stateController, this, { runBilling() }
                ){
                    SubscriptionScreen(this@SubscriptionsStoreActivity, billingClient)
                }
            }
        }
    }


    fun updatePoints(isPending:Boolean, productId: String,purchaseToken:String,onSuccess: () -> Unit = {}) {
        if (isPending){
            stateController.showMessage("جاري حفظ معلومات الدفع حتى اكتمال معالجة الدفع")
        }else{

                stateController.showMessage("جاري الاشتراك")
        }

        stateController.startAud()
//        val googleBillingStorage = GoogleBillingStorage()
        val body = builderForm3()
            .addFormDataPart("productId",productId)
            .addFormDataPart("purchaseToken",purchaseToken)
            .build()

        requestServer.request2(body, "updatePoints", { code, fail ->
            myBillings = googleBillingStorage.getMyBillings()
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val result:SubscriptionModel =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            inAppProducts = inAppProducts.map {
                if(it.id == result.id){
                    result
                }else it
            }
            if (!result.isPending){

                    stateController.showMessage("تم الاشتراك بنجاح")


//                stateController.showMessage("Add Done: "+ result.points)

            }
            googleBillingStorage.setMyBillings(googleBillingStorage.getMyBillings().filterNot { it.productId == productId })
            myBillings.filterNot { it.productId == productId }
            onSuccess()
            stateController.successStateAUD()
        }
    }



    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun SubscriptionScreen(activity: Activity, billingClient: BillingClient) {

        LazyColumn (modifier = Modifier
            .fillMaxSize()) {

            stickyHeader {
                Header()
            }

            if (CustomSingleton.isPremiumStore())
            item {
                CustomCard2(modifierBox = Modifier
                    .fillMaxSize()
                    .clickable {

                    }){
                    Row {
                        Text("نوع الاشتراك", Modifier.padding(8.dp))
                        if (CustomSingleton.isPremiumStore()) {
                            Text("Premium", Modifier.padding(8.dp))
                        }
                        else{
                            Text("Shared", Modifier.padding(8.dp))
                        }
                    }

                    Row {
                        Text("تنتهي في ", Modifier.padding(8.dp))
                        Text(CustomSingleton.selectedStore!!.subscription.expireAt, Modifier.padding(8.dp))
                    }
                }
            }



            itemsIndexed(subs) { index, item ->
                CustomCard2(modifierBox = Modifier
                    .fillMaxSize()
                    .clickable {

                    }) {

                    CustomRow {
                        Text(item.offerId.toString())
                        Button(onClick = {
                            if (productDetails!= null){

                                val billingParams = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(
                                        listOf(
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                .setProductDetails(productDetails!!)
                                                .setOfferToken(item.offerToken)
                                                .build()
                                        )
                                    )
                                    .build()
                                billingClient.launchBillingFlow(activity, billingParams)
                            }
                        }) {
                            Text(   "شراء")
                        }
                    }}
            }

            if (myBillings.isNotEmpty()){
                itemsIndexed(myBillings) { index, item ->
                    CustomCard2(modifierBox = Modifier
                        .fillMaxSize()
                        .clickable {

                        }) {
                        CustomRow {
                            Text(item.name)
                            Button(onClick = {
                                updatePoints(item.isPending,item.productId,item.purchaseToken)
                            }) {
                                Text("حفظ في السرفر والتحقق")
                            }
                        }}
                }
            }


        }

        // تحميل تفاصيل الاشتراك
//        LaunchedEffect(Unit) {
//
//        }
    }

    @Composable
    private fun Header() {
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface), verticalAlignment = Alignment.CenterVertically,) {
            Row(verticalAlignment = Alignment.CenterVertically,) {
                CustomIcon(Icons.AutoMirrored.Default.ArrowBack) {
                    finish()
                }
                Text(
                    "الاشتراكات",
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        HorizontalDivider()
    }

    private fun runBilling(){
        stateController.startRead()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(
                            listOf(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId("premium_access") // ✅ استخدم الـ ID الفعلي من Play Console
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build()
                            )
                        ).build()

                    billingClient.queryProductDetailsAsync(params) { result, productList ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK ) {
                            if (productList.isNotEmpty()){
                                val productDetails1 = productList[0]

                                val basePlans = productDetails1.subscriptionOfferDetails
                                if (basePlans.isNullOrEmpty()) {
                                    Log.e("Billing", "❌ لا توجد Base Plans مفعلة")
                                    stateController.errorStateAUD("NO Found Base Plans")
                                    return@queryProductDetailsAsync
                                }


//                                for (plan in basePlans) {
//                                    val basePlanId = plan.basePlanId
//                                    val offerToken = plan.offerToken
//                                    val pricing = plan.pricingPhases.pricingPhaseList.firstOrNull()
//
//                                    Log.i("Billing", "✅ BasePlan: $basePlanId - ${pricing?.formattedPrice} - ${pricing?.billingPeriod}")
//
//                                    // يمكن لاحقًا استخدام offerToken لتشغيل الدفع لهذه الخطة
//                                }


                                this@SubscriptionsStoreActivity.productDetails = productDetails1
                                subs = basePlans
                                // مثال: تشغيل الدفع لأول Base Plan



                                stateController.successState()
                            }else{
                                stateController.errorStateAUD("no products")
                                println("no products")
                            }
                        }
                        else{
                            println("ERROR")
                            println("Error response billing"+result.responseCode)
                            println("Error response billing"+result.debugMessage)

                            stateController.errorStateAUD("Error response billing"+result.debugMessage)
                        }
                    }
                } else {
                    // ❌ حدث خطأ — تعامل معه
                    Log.e("Billing", "Setup failed: ${billingResult.debugMessage}")
                    stateController.errorStateAUD("Setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                // ⚠️ الخدمة غير متصلة — جرب إعادة الاتصال لاحقاً
                Log.w("Billing", "Service disconnected")

                stateController.errorStateAUD("Service disconnected")
            }
        })
    }
}
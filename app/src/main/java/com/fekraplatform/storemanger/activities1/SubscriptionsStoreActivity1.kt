package com.fekraplatform.storemanger.activities1

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.models.Subscription
import com.fekraplatform.storemanger.repositories.BillingRepository
import com.fekraplatform.storemanger.shared.AToken1
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.CustomSingleton.selectedStore
import com.fekraplatform.storemanger.shared.MainComposeRead
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.ServerConfig
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.storage.BillingEntity
import com.fekraplatform.storemanger.storage.MyAppStorage1
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProductTypeEnum {
    INAPP,
    SUBS
}

@HiltViewModel
class SubscriptionsStoreViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    private val serverConfig: ServerConfig,
    val appSession: AppSession,
    private val myAppStorage: MyAppStorage1,
    private val builder: FormBuilder,
    private val aToken: AToken1,
    private val billingRepository: BillingRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
): ViewModel()
{

    val productType: ProductTypeEnum = try {
        ProductTypeEnum.valueOf(savedStateHandle["product_type"] ?: "SUBS")
    } catch (e: Exception) {
        ProductTypeEnum.SUBS
    }
    var selectedStore by mutableStateOf(appSession.selectedStore)

    val stateController = StateController()
    lateinit var billingClient: BillingClient
    var productDetails by mutableStateOf(emptyList<ProductDetails>())
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    purchases?.forEach { purchase ->
                        when (purchase.purchaseState) {
                            PurchaseState.PURCHASED -> {
                                setGooglePurchaseStorage(false,purchase)
//                                updatePoints(false,purchase.products.first(),purchase.purchaseToken)
                            }
                            PurchaseState.PENDING -> {
                                setGooglePurchaseStorage(true,purchase)
//                                updatePoints(true,purchase.products.first(),purchase.purchaseToken)
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

    fun read(){
        viewModelScope.launch {
            stateController.startRead()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                if (productType == ProductTypeEnum.INAPP) {
                    body.addFormDataPart("isSubs", "0")
                } else if (productType == ProductTypeEnum.SUBS) {
                    body.addFormDataPart("isSubs", "1")
                }

                val data = requestServer.request(body, "getInAppProducts")
                val products:List<SubscriptionModel> = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                queryProducts(products.map { it.productId })
            } catch (e: Exception) {
                stateController.errorStateRead(e.message.toString())
            }
        }
    }
    fun updatePoints(
        isPending: Boolean,
        purchaseToken: String
    ) {
        val a= myBilling.firstOrNull { it.purchaseToken == purchaseToken }
        if (a != null)
            deleteBilling(a)
        ////
        if (isPending) {
            stateController.showMessage("جاري حفظ معلومات الدفع حتى اكتمال معالجة الدفع")
        } else {
            stateController.showMessage("جاري الاشتراك")
        }

        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                val data = requestServer.request(body, "updatePoints")
                val result:Subscription = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                val newStore = selectedStore.copy(subscription = result)
                appSession.setStoreAndUpdateStores(newStore)
                selectedStore = newStore
                stateController.successStateAUD("تمت العملية بنجاح")
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }

    }

    fun initBillingClient(){
        val params: PendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()

        billingClient= BillingClient.newBuilder(context)
            .enablePendingPurchases(params)
            .setListener(purchasesUpdatedListener) // Set the listener for purchase updates
            .build()
    }

    private fun setGooglePurchaseStorage(isPending: Boolean, purchase: Purchase) {
        addBilling(
            BillingEntity(purchase.products.first(), purchase.purchaseToken
            )
        )
    }
    ////
    var myBilling:List<BillingEntity> by mutableStateOf(emptyList())

    fun loadBillings() {
        viewModelScope.launch {
            myBilling = billingRepository.getAllBillings()
        }
    }

    fun addBilling(billing: BillingEntity) {
        viewModelScope.launch {
            billingRepository.addBilling(billing)
            loadBillings()
        }
    }

    fun deleteBilling(billing: BillingEntity) {
        viewModelScope.launch {
            billingRepository.removeBilling(billing)
            loadBillings()
        }
    }

    private fun queryProducts(productIds: List<String>, ) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val skuList = productIds // Replace with your product IDs
                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(
                            skuList.map { skuId ->
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(skuId)
                                    .setProductType(  if (productType.name.trim().lowercase() == "inapp")
                                        BillingClient.ProductType.INAPP
                                    else
                                        BillingClient.ProductType.SUBS) // Use IN APP for one-time purchases
                                    .build()
                            }
                        )
                        .build()

                    billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            productDetails =   productDetailsList
                            stateController.successState()
                            Log.e("eeeeww", productDetailsList.toString())
                        } else {
                            stateController.errorStateRead("Error response billing" + billingResult.responseCode)
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

    init {
        initBillingClient()
        read()
    }
    fun isPremiumStore(): Boolean {
        return selectedStore.subscription.isPremium == 1
    }
}

@AndroidEntryPoint
class SubscriptionsStoreActivity1 : ComponentActivity() {

    val viewModel : SubscriptionsStoreViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme{
                MainComposeRead(if (viewModel.productType == ProductTypeEnum.SUBS) "الاشتراكات" else "النقاط",viewModel.stateController,{finish()},{viewModel.read()}) {
                    SubscriptionScreen(this@SubscriptionsStoreActivity1, viewModel.billingClient)
                }
            }
        }
    }


//    fun updatePoints(isPending:Boolean, productId: String,purchaseToken:String,onSuccess: () -> Unit = {}) {
//        if (isPending){
//            stateController.showMessage("جاري حفظ معلومات الدفع حتى اكتمال معالجة الدفع")
//        }else{
//
//                stateController.showMessage("جاري الاشتراك")
//        }
//
//        stateController.startAud()
////        val googleBillingStorage = GoogleBillingStorage()
//        val body = builderForm3()
//            .addFormDataPart("productId",productId)
//            .addFormDataPart("purchaseToken",purchaseToken)
//            .build()
//
//        requestServer.request2(body, "updatePoints", { code, fail ->
//            myBillings = googleBillingStorage.getMyBillings()
//            stateController.errorStateAUD(fail)
//        }
//        ) { data ->
//            val result:SubscriptionModel =
//                MyJson.IgnoreUnknownKeys.decodeFromString(
//                    data
//                )
//
//            inAppProducts = inAppProducts.map {
//                if(it.id == result.id){
//                    result
//                }else it
//            }
//            if (!result.isPending){
//
//                    stateController.showMessage("تم الاشتراك بنجاح")
//
//
////                stateController.showMessage("Add Done: "+ result.points)
//
//            }
//            googleBillingStorage.setMyBillings(googleBillingStorage.getMyBillings().filterNot { it.productId == productId })
//            myBillings.filterNot { it.productId == productId }
//            onSuccess()
//            stateController.successStateAUD()
//        }
//    }



    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun SubscriptionScreen(activity: Activity, billingClient: BillingClient,) {

        LazyColumn (modifier = Modifier
            .fillMaxSize()) {
            if (viewModel.appSession.myStore != null)
                item {
                    Text(viewModel.appSession.myStore!!.subscription.points.toString())
                }

            stickyHeader {
                SubscriptionStickyHeader(viewModel.isPremiumStore(),viewModel.selectedStore.subscription.points)
            }


            if (viewModel.productType == ProductTypeEnum.SUBS){
                viewModel.productDetails.forEach { productDetails: ProductDetails ->
                    item { ProductStickyHeader(productDetails.title) }
                    productDetails.subscriptionOfferDetails?.forEach { subscriptionOfferDetails ->
                        subscriptionOfferDetails.pricingPhases.pricingPhaseList.forEach { pricingPhase ->
                            item {
                                val price = pricingPhase.formattedPrice
                                val periodText = parsePeriod(pricingPhase.billingPeriod)

                                SubscriptionItem(
                                    title = subscriptionOfferDetails.basePlanId,
                                    price = price,
                                    period = periodText,
                                    onSubscribeClick = {
                                        val billingParams = BillingFlowParams.newBuilder()
                                            .setProductDetailsParamsList(
                                                listOf(
                                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                                        .setProductDetails(productDetails)
                                                        .setOfferToken(subscriptionOfferDetails.offerToken)
                                                        .build()
                                                )
                                            )
                                            .build()

                                        billingClient.launchBillingFlow(activity, billingParams)
                                    }
                                )
                            }

                        }
                    }
                }
            }

            else if (viewModel.productType == ProductTypeEnum.INAPP){
                viewModel.productDetails.forEach { productDetails: ProductDetails ->
                    item {
                        val title = productDetails.title
                        val price = productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?: "؟"
                        ProductCardItem(
                            title = title,
                            price = price,
                            onBuyClick = {
                                val billingParams = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(
                                        listOf(
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                .setProductDetails(productDetails)
                                                .build()
                                        )
                                    )
                                    .build()

                                billingClient.launchBillingFlow(activity, billingParams)
                            }
                        )
                    }
                }
            }

//            stickyHeader {
//                Header()
//            }


//
//            if (CustomSingleton.isPremiumStore())
//            item {
//                CustomCard2(modifierBox = Modifier
//                    .fillMaxSize()
//                    .clickable {
//
//                    }){
//                    Row {
//                        Text("نوع الاشتراك", Modifier.padding(8.dp))
//                        if (CustomSingleton.isPremiumStore()) {
//                            Text("Premium", Modifier.padding(8.dp))
//                        }
//                        else{
//                            Text("Shared", Modifier.padding(8.dp))
//                        }
//                    }
//
//                    Row {
//                        Text("تنتهي في ", Modifier.padding(8.dp))
//                        Text(CustomSingleton.selectedStore!!.subscription.expireAt, Modifier.padding(8.dp))
//                    }
//                }
//            }
        }
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

    fun parsePeriod(period: String): String {
        return when (period) {
            "P1W" -> "أسبوع"
            "P1M" -> "شهر"
            "P3M" -> "3 أشهر"
            "P6M" -> "6 أشهر"
            "P1Y" -> "سنة"
            else -> period
        }
    }

    @Composable
    fun SubscriptionItem(
        title: String,
        price: String,
        period: String,
        onSubscribeClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F6FA))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {



                Text(
                    text = "$period",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF5A5E6A)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "$price",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF5A5E6A)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSubscribeClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = "اشترك الآن")
                }
            }
        }
    }

    @Composable
    fun ProductStickyHeader(
        title: String,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEF2F6))
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2C3E50)
                )
            }
        }
    }

    @Composable
    fun ProductCardItem(
        title: String,
        price: String,
        onBuyClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "السعر: $price",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF455A64)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onBuyClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("اشتر الآن")
                }
            }
        }
    }

    @Composable
    fun SubscriptionStickyHeader(
        isPremium: Boolean,
        points: Int
    ) {
        val backgroundColor = if (isPremium) Color(0xFFDCE775) else Color(0xFFB0BEC5)
        val subscriptionType = if (isPremium) "Premium" else "Shared"
        val subscriptionColor = if (isPremium) Color(0xFF33691E) else Color(0xFF37474F)
        val icon = if (isPremium) Icons.Default.Star else Icons.Default.People

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor),
            tonalElevation = 3.dp,
            shadowElevation = 4.dp,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = subscriptionColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نوع الاشتراك: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        Text(
                            text = subscriptionType,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = subscriptionColor
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFA000)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "النقاط: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        Text(
                            text = points.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF424242)
                        )
                    }
                }
            }
        }
    }







}
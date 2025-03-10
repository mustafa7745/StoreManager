package com.fekraplatform.storemanger.activities

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
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
import com.fekraplatform.storemanger.shared.CustomRow
import com.fekraplatform.storemanger.shared.CustomRow2
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.MyHeader
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm3
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


class SettingsStoreActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)
    var uriFile by mutableStateOf<Uri?>(null)
//    lateinit var store: Store

    var file by mutableStateOf<String?>(null)
    var storeCurrencies by mutableStateOf<List<StoreCurrency>>(emptyList())
    var isShowAddCurrency by mutableStateOf(false)

    val pages = listOf(
        PageModel("",0),
        PageModel("شراء الاشتراكات",1),
        PageModel("شراء النقاط",2),
        PageModel("أوقات الدوام",3)
    )
    var page by mutableStateOf(pages.first())


    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SingletonHome.setStateController1(stateController)
        SingletonHome.setReqestController(requestServer)
        stateController.successState()
        enableEdgeToEdge()
        setContent {
            MainView()
        }
    }

    @Composable
    @OptIn(ExperimentalFoundationApi::class)
    private fun MainView() {
        StoreMangerTheme {
            BackHand()
            MainCompose2(
                0.dp, stateController, this,

                ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    stickyHeader {
                        MyHeader({
                            backHandler()
                        }) {
                            Row {
                                Text("اعدادات المتجر")
                                if (page != pages.first()) {
                                    Text(" | ")
                                    Text(page.pageName)
                                }
                            }

                        }
                    }

                    if (page.pageId == 0)
                        MainPage()
                    else if (page.pageId == 1 || page.pageId == 2)
                        SubscriptionPage()
                    else if (page.pageId == 3)
                        StoreTimePage()


                    //                        if (CustomSingleton.selectedStore!!.app != null){
    //
    //
    //                            item {
    //                                LaunchedEffect(null) {
    //                                readFileFromCache()
    //                            }
    //                                CustomCard( modifierBox = Modifier.fillMaxSize().clickable {
    //
    //                                }) {
    //
    //                                    CustomRow {
    //                                        Text("اعدادات الخدمة",Modifier.padding(8.dp))
    //
    //                                        Button(onClick = {
    //                                            if (file == null){
    //                                                getContentFile.launch("application/json")
    //                                            }else{
    //                                                saveFileToCache()
    //                                            }
    //
    //                                        }) {
    //                                            Text(if (file != null) "edit" else "add")
    ////                                            Text("add")
    //                                        }
    //                                    }
    //                                    if (uriFile != null){
    //                                        Button(onClick = {
    //                                            saveFileToCache()
    //                                        }) {
    //                                            Text("Save this file")
    //                                        }
    //                                    }
    //                                }
    //                            }
    //                        }
                    ///


                }
                if (isShowUpdateStoreTime) modalUpdateStoreTime()
                if (isShowUpdateDeliveryPrice) modalUpdateDeliveryPrice()
                if (isShowAddCurrency) modalShowCurrencyOfStore()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun LazyListScope.MainPage() {
        item {
            CustomCard2(modifierBox = Modifier) {
                Column(Modifier.selectableGroup()) {
                    Text("الرئيسية", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                    SettingItem("الموقع"){ gotoStoreLocation()}
                    SettingItem("اوقات الدوام"){ page = pages[3]}
                }
            }
        }
        item {
            CustomCard2(modifierBox = Modifier) {
                Column(Modifier.selectableGroup()) {
                    Text("العملات والتسعير", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                    SettingItem("عملات المتجر"){getStoreCurrencies()}
                    SettingItem("سعر التوصيل"){ isShowUpdateDeliveryPrice = true}
                    SettingItem("اقل مبلغ يمكن طليه"){}
                    SettingItem("توصيل مجاني للطلبات الاكبر من"){}
                }
                }
        }


        stickyHeader {
            Text("الاشتراكات والنقاط")
            HorizontalDivider()
        }
        item {
            CustomCard2(modifierBox = Modifier
                .fillMaxSize()
                .clickable {

                }) {
                CustomRow {
                    Text("نوع الاشتراك", Modifier.padding(8.dp))
                    if (CustomSingleton.isPremiumStore()) {
                        Text("Premium", Modifier.padding(8.dp))
                    }
        //                    else {
                    Button(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        onClick = {
                            isSubs = "1"
                            page = pages[1]
                        }) {
                        Text("ترقية")
                    }
        //                    }
                }
                CustomRow {

                    if (CustomSingleton.isPremiumStore()) {
                        val time = LocalDateTime.parse(
                            CustomSingleton.selectedStore!!.subscription.expireAt.replace(
                                " ",
                                "T"
                            )
                        )
                        val diff = Duration.between(getCurrentDate(), time).toDays()
                        Text("الايام المتبقية", Modifier.padding(8.dp))
                        Text(diff.toString(), Modifier.padding(8.dp))
        //                        if (diff <= 5) {
        //                            Button(
        //                                modifier = Modifier.fillMaxWidth().padding(8.dp),
        //                                onClick = {
        //                                isSubs = "1"
        //                                page = pages[1]
        //                            }) {
        //                                Text("تجديد ")
        //                            }
        //                        } else {
        //
        //
        //                        }

                    }
                }
            }
        }
        if (CustomSingleton.isPremiumStore())
            if (CustomSingleton.selectedStore!!.app != null) {
                item {
                    CustomCard2(modifierBox = Modifier
                        .fillMaxSize()
                        .clickable {

                        }) {
                        Column {
                            CustomRow {
                                Text(
                                    "هذا المتجر لديه تطبيق في متجر التطبيقات",
                                    Modifier.padding(8.dp)
                                )


                                //                                            else{
                                //                                            Button(onClick = {
                                //
                                //                                            }) {
                                //                                                Text("ارسال طلب تصدير المتجر الى تطبيق")
                                //                                            }
                                //                                        }
                            }
                            CustomRow {
                                Text("اعدادات الخدمة", Modifier.padding(8.dp))
                                if (CustomSingleton.selectedStore!!.app!!.hasServiceAccount) {
                                    Button(onClick = {
                                        getContentServiceAccount.launch("application/json")
                                    }) {
                                        Text("تعديل")
                                    }
                                } else {
                                    Button(onClick = {
                                        getContentServiceAccount.launch("application/json")
                                    }) {
                                        Text("اضافه")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        item {
            CustomCard2(modifierBox = Modifier
                .fillMaxSize()
                .clickable {

                }) {
                CustomRow {
                    Text("النقاط", Modifier.padding(8.dp))
                    Text(
                        CustomSingleton.selectedStore!!.subscription.points.toString(),
                        Modifier.padding(8.dp)
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    onClick = {

                        isSubs = "0"
                        page = pages[2]
                    }) {
                    Text("شراء النقاط")
                }
            }
        }

        if (SelectedStore.store.value!!.typeId == 1)
            item {
                CustomCard2(modifierBox = Modifier
                    .fillMaxSize()
                    .clickable {

                    }) {
                    CustomRow {
                        Text("وضع التعديل")
                        Switch(
                            checked = SingletonHome.isEditMode.value,
                            onCheckedChange = { SingletonHome.isEditMode.value = it },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        if (CustomSingleton.selectedStore != null && CustomSingleton.isSharedStore()) {
            if ((SingletonHome.categories.value != CustomSingleton.selectedStore!!.storeConfig!!.categories ||
                        SingletonHome.sections.value != CustomSingleton.selectedStore!!.storeConfig!!.sections ||
                        SingletonHome.nestedSection.value != CustomSingleton.selectedStore!!.storeConfig!!.nestedSections ||
                        SingletonHome.products.value != CustomSingleton.selectedStore!!.storeConfig!!.products)
                && SingletonHome.isEditMode.value
            ) {
                item {
                    CustomCard2(modifierBox = Modifier
                        .fillMaxSize()
                        .clickable {
                            SingletonHome.updateStoreConfig()
                        }) {
                        if (SingletonHome.stateController.isLoadingAUD.value)
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                        else
                            Text("حفظ التعديلات", Modifier.clickable {
                                SingletonHome.updateStoreConfig()
                                Log.e("wwww", SingletonHome.categories.value.toString())
                            })
                    }

                }
            }
        }
    }

    @Composable
    private fun SettingItem(text:String,onClick:()-> Unit) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable {
                   onClick()
                }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        HorizontalDivider()
    }

    var storeTime by mutableStateOf(emptyList<StoreTime2>())
    @OptIn(ExperimentalFoundationApi::class)
    private fun LazyListScope.StoreTimePage(){
        if (storeTime.isEmpty()) getStoreTime()

        itemsIndexed(storeTime){index, item ->
            CustomCard2(modifierBox = Modifier
                .fillMaxSize()
                .clickable {
                    selectedStoreTime = item
                    isShowUpdateStoreTime = true
                }) {
                CustomRow2 {
                    Text(item.day)
                    if (item.storeTime == null){
                        Button(onClick = {
                            updateStoreTime((index+1).toString(),null)
        //                        updatePoints(item.isPending,item.productId,item.purchaseToken)
                        }) {
                            Text("اضافة")
                        }
                    }
                }
                if (item.storeTime != null){
                    CustomRow2 {
                        Text("يفتح الساعة: " + formatTime(item.storeTime!!.openAt)?.get(0)
                            .toString() )
                        Text(formatTime(item.storeTime!!.openAt)?.get(1)
                            .toString()).toString()
                    }
                    CustomRow2 {
                        Text("يغلق الساعة: " + formatTime(item.storeTime.closeAt)?.get(0)
                            .toString())
                        Text(formatTime(item.storeTime.closeAt)?.get(1)
                            .toString())
                    }
                    CustomRow2 {
                        if (item.storeTime.isOpen == 1) Text("مفتوح")  else  Text("مغلق")
                    }
                }
            }

        }

    }
    val googleBillingStorage = GoogleBillingStorage()
    var inAppProducts by mutableStateOf(emptyList<SubscriptionModel>())
    var productDetails by mutableStateOf(emptyList<ProductDetails>())
    var myBillings by mutableStateOf(googleBillingStorage.getMyBillings())
    lateinit var isSubs:String
    private fun LazyListScope.SubscriptionPage() {
        item {
            LaunchedEffect(null) {
                inAppProducts = emptyList()
                getInAppProducts()
            }
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

        itemsIndexed(inAppProducts) { index, item ->
            CustomCard2(modifierBox = Modifier
                .fillMaxSize()
                .clickable {

                }) {
                CustomRow {
                    Text(item.name.toString(), Modifier.padding(8.dp))
                    Text( "$"+ formatPrice(item.price.toString()),
                        Modifier
                            .padding(8.dp)
                            .clickable {
                                updatePoints(true,item.productId,"")
                            })
        //                    val myPurchase = myPurchases.find { it.productId == item.productId }

                    Button(
                        enabled = !item.isPending,
                        onClick = {
                            val sku = productDetails.map { it.productId }
                            if (item.productId !in sku)
                                stateController.showMessage("هذا غير موجود في قائمة منتجات الدفع")
                            else {
                                val p = productDetails.find { it.productId == item.productId }
                                if (p != null) {
                                    launchPurchaseFlow(p)
        //                                    if (myPurchase == null)
        //
        //                                    else if (myPurchase.status == PurchaseState.PENDING) {
        //
        //                                    } else if (!myPurchase.isAcked) {
        //                                        acknowledgePurchase(myPurchase.token)
        //                                    }
        //                                    else if (myPurchase.isAcked) {
        //                                        updatePoints(MyJson.MyJson.encodeToString(listOf(myPurchase.productId))) {
        //                                            consumePurchases(myPurchase.token)
        //                                        }
        //                                    }
                                }

        //                            stateController.showMessage("processing")
                            }

                        }) {


                        Text(
                            "شراء"
        //                            if (myPurchase == null)
        //
        //                            else if (myPurchase.status == PurchaseState.PENDING)
        //                                "بانتظار معالجة الدفع"
        //                            else if (!myPurchase.isAcked)
        //                                "الحصول على النقاط (A)"
        //                            else "الحصول على النقاط (P)"

                        )
                    }
                }
        //                val ma = myBillings.find { it.productId == item.productId }
        //                if (ma != null) {
        //                    ma.purchases.forEach {
        //                        if (it.status == Purchase.PurchaseState.PURCHASED) {
        //                            Button(onClick = {
        //
        //                            }) { Text("") }
        //                        }
        //                    }
        //                }
            }

        }
//        item {
//            Button(onClick = {
//                getPreviousPurchases()
//            }) {
//                Text("get history")
//            }
//        }

//        if (myPreviousPurchases.isNotEmpty()) {
//            item {
//                Text("مشترياتي السابقة")
//            }
//
//            itemsIndexed(myPreviousPurchases) { index, item ->
//                CustomCard(modifierBox = Modifier
//                    .fillMaxSize()
//                    .clickable {
//
//                    }) {
//                    Text(item.products.toString())
//                    Text(item.orderId.toString())
//                    Text(MyJson.IgnoreUnknownKeys.encodeToString(item))
//                    Button(onClick = {
//                        consumePurchases(item.purchaseToken)
//                    }) {
//                        Text("consume")
//                    }
//                }
//            }
//        }
    }

    private fun gotoStoreLocation() {
        val intent = Intent(
            this,
            LocationStoreActivity::class.java
        )
        intent.putExtra("latLng", SelectedStore.store.value!! .latLng)
//        val intent =
//            Intent(this, SettingsStoreActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
//        finish()
    }
    fun saveFileToCache() {
        val cacheFile = File(cacheDir, "service.json")
        try {
            val fos = FileOutputStream(cacheFile)
            contentResolver.openInputStream(uriFile!!)?.use { input ->
                fos.write(input.readBytes())
            }
            fos.close()
            println("File saved to cache: ${cacheFile.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun readFileFromCache(): String? {
        val cacheFile = File(cacheDir, "service.json")
        if (cacheFile.exists()) {
            try {
                val fis = FileInputStream(cacheFile)
                val fileContents = fis.bufferedReader().use { it.readText() }
                fis.close()
                file = fileContents
                return fileContents
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    val getContentFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null){
            uriFile = uri
        }
    }
    private fun backHandler() {
        if (page.pageId != 0) {
            page = pages.first()
        } else
            finish()
    }
    @Composable
    private fun BackHand() {
        BackHandler {
            backHandler()
        }
    }


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

    private fun setGooglePurchaseStorage(isPending: Boolean, purchase: Purchase) {
        val b = googleBillingStorage.getMyBillings().toMutableList()
        val inAppProduct = inAppProducts.find { it.productId == purchase.products.first() }
        purchase.products.first()
        b += MyBilling(
            isPending, inAppProduct?.name ?: "Unk name",purchase.products.first(), purchase.purchaseToken
        )
        googleBillingStorage.setMyBillings(b)
    }

    private var params: PendingPurchasesParams = PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .build()

    private var billingClient: BillingClient = BillingClient.newBuilder(MyApplication.AppContext)
        .enablePendingPurchases(params)
        .setListener(purchasesUpdatedListener) // Set the listener for purchase updates
        .build()

    fun getInAppProducts() {
        stateController.startAud()

        val body = builderForm3()
            .addFormDataPart("isSubs",isSubs)
            .build()

        requestServer.request2(body, "getInAppProducts", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val inAppProducts1:List<SubscriptionModel> =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            getProducts(inAppProducts1.map { it.productId },{
                stateController.errorStateAUD(it)
            }){

                productDetails = it
                inAppProducts = inAppProducts1
                inAppProducts.forEach { inAppProduct ->
                    if (!inAppProduct.isPending){
                        googleBillingStorage.setMyBillings(googleBillingStorage.getMyBillings().filterNot { it.productId == inAppProduct.productId })
                    }
                }
                stateController.successStateAUD()
            }
        }
    }

    fun getStoreTime() {
        stateController.startAud()

        val body = builderForm3()
            .build()

        requestServer.request2(body, "getStoreTime", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val result:List<StoreTime2> =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )
            storeTime = result
            stateController.successStateAUD()
        }
    }
    fun getStoreCurrencies() {
        stateController.startAud()

        val body = builderForm3()
            .build()

        requestServer.request2(body, "getStoreCurrencies", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            storeCurrencies =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )


            CustomSingleton.selectedStore =  CustomSingleton.selectedStore!!.copy(storeCurrencies = storeCurrencies)

            val updatedStores = CustomSingleton.stores.map {
                if (it.id ==CustomSingleton.selectedStore!!.id )
                    CustomSingleton.selectedStore!!
                else
                    it
            }
            CustomSingleton.stores = updatedStores

            isShowAddCurrency = true
            stateController.successStateAUD()

        }
    }

    fun updatePoints(isPending:Boolean, productId: String,purchaseToken:String,onSuccess: () -> Unit = {}) {
        if (isPending){
            stateController.showMessage("جاري حفظ معلومات الدفع حتى اكتمال معالجة الدفع")
        }else{
            if (isSubs == "0"){
                stateController.showMessage("جاري الحصول على النقاط")
            }else{
                stateController.showMessage("جاري الاشتراك")
            }

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
                if (isSubs == "0"){
                    CustomSingleton.selectedStore = CustomSingleton.selectedStore!!.copy(subscription =CustomSingleton.selectedStore!!.subscription.copy(points = CustomSingleton.selectedStore!!.subscription.points + result.points) )
                    stateController.showMessage("تم الحصول على النقاط بنجاح")
                }else{
                    stateController.showMessage("تم الاشتراك بنجاح")
                }

//                stateController.showMessage("Add Done: "+ result.points)

            }
            googleBillingStorage.setMyBillings(googleBillingStorage.getMyBillings().filterNot { it.productId == productId })
            myBillings.filterNot { it.productId == productId }
            onSuccess()
            stateController.successStateAUD()
        }
    }

    fun updateStoreTime(day: String, storeTimeParameter: StoreTime?) {
        stateController.startAud()
//        val googleBillingStorage = GoogleBillingStorage()
        val body = builderForm3()
            .addFormDataPart("day",day)
            if (storeTimeParameter != null){
                body.addFormDataPart("openAt", storeTimeParameter.openAt)
                body.addFormDataPart("closeAt",storeTimeParameter.closeAt)
                body.addFormDataPart("isOpen", storeTimeParameter.isOpen.toString())
            }


        requestServer.request2(body.build(), "updateStoreTime", { code, fail ->

            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val result:StoreTime2 =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            storeTime = storeTime.map {
                if (it.day == result.day)
                    result
                else it
            }
            isShowUpdateStoreTime = false
            stateController.successStateAUD()
        }
    }

    fun getProducts(productIds: List<String>,onFail:(String)->Unit,onSuccess:(List<ProductDetails>)->Unit){
        startConnection({
            onFail(it)
        }){
            queryProducts(productIds,{
                onFail(it)
            }){
                onSuccess(it)
            }
        }
    }
    fun queryProducts(productIds: List<String>,onFail:(String)->Unit,onSuccess:(List<ProductDetails>)->Unit) {
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
            }else{
                onFail("Error response billing"+billingResult.responseCode )
            }
        }
    }
    private fun startConnection(onFail: (String) -> Unit, onSuccess: () -> Unit) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    onSuccess()
                }else{
                    onFail("connection billing fail: "+ billingResult.responseCode)
                }
            }

            override fun onBillingServiceDisconnected() {
                onFail("billing Service Disconnect")
                // Try to restart the connection later
            }
        })
    }
    fun launchPurchaseFlow(productDetails: ProductDetails) {
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        billingClient.launchBillingFlow(this, billingFlowParams)
    }


    var uriServiceAccount by  mutableStateOf<Uri?>(null)
    val getContentServiceAccount = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null){
            uriServiceAccount = uri

            confirmDialog(this) {

                updateServiceAccount(it)
            }

        }
    }

    private var currencies by mutableStateOf<List<Currency>>(listOf())

    private var isShowUpdateDeliveryPrice by mutableStateOf(false)
    private var isShowUpdateStoreTime by mutableStateOf(false)
    private fun updateDeliveryPrice(deliveryPrice:String,freeDeliveryPrice:String,lessCartPrice:String,) {
        stateController.startAud()
        val body = builderForm3()

        if (deliveryPrice.isNotEmpty()){
            body
                .addFormDataPart("deliveryPrice",deliveryPrice)
        }
        if (freeDeliveryPrice.isNotEmpty()){
            body
                .addFormDataPart("freeDeliveryPrice",freeDeliveryPrice)
        }
        if (lessCartPrice.isNotEmpty()){
            body
                .addFormDataPart("lessCartPrice",lessCartPrice)
        }


        requestServer.request2(body.build(), "updateStoreCurrencyPricing", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val result:StoreCurrency = MyJson.IgnoreUnknownKeys.decodeFromString(data)
            val updatedStoreCurrencies =  CustomSingleton.selectedStore!!.storeCurrencies.map {
                if (it.id == result.id)
                    result
                else it
            }
           CustomSingleton.selectedStore =  CustomSingleton.selectedStore!!.copy(storeCurrencies = updatedStoreCurrencies)

            val updatedStores = CustomSingleton.stores.map {
                if (it.id ==CustomSingleton.selectedStore!!.id )
                    CustomSingleton.selectedStore!!
                else
                    it
            }
            CustomSingleton.stores = updatedStores
            stateController.successStateAUD("تمت   بنجاح")
            isShowUpdateDeliveryPrice = false
        }
    }
    private fun updateDefaultCurrency(id:String) {
        stateController.startAud()
        val body = builderForm3()
        body
            .addFormDataPart("storeCurrencyId",id)


        requestServer.request2(body.build(), "updateDefaultCurrency", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val result:List<StoreCurrency> = MyJson.IgnoreUnknownKeys.decodeFromString(data)
            val updatedStoreCurrencies =  result
            storeCurrencies = updatedStoreCurrencies

            CustomSingleton.selectedStore =  CustomSingleton.selectedStore!!.copy(storeCurrencies = updatedStoreCurrencies)

            val updatedStores = CustomSingleton.stores.map {
                if (it.id ==CustomSingleton.selectedStore!!.id )
                    CustomSingleton.selectedStore!!
                else
                    it
            }
            CustomSingleton.stores = updatedStores
            stateController.successStateAUD("تمت   بنجاح")
        }
    }
    private fun updateServiceAccount(password:String) {
        stateController.startAud()
        val body = builderForm3()
        if (uriServiceAccount != null){
            val requestBodyIcon = object : RequestBody() {
                val mediaType = "json".toMediaTypeOrNull()
                override fun contentType(): MediaType? {
                    return mediaType
                }

                override fun writeTo(sink: BufferedSink) {
                    contentResolver.openInputStream(uriServiceAccount!!)?.use { input ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            sink.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
            body.addFormDataPart("jsonService", "file1.jpg", requestBodyIcon)
        }
        body.addFormDataPart("passwordService",password)

//            .build()
        requestServer.request2(body.build(), "updateStoreServiceAccount", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            uriServiceAccount = null
            stateController.successStateAUD("تمت   بنجاح")
        }
    }
    fun readCurrencies() {
        stateController.startAud()
        //
        val body = builderForm3()
            .addFormDataPart("d", "e")
            .build()

        requestServer.request2(body, "getCurrencies", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { it ->
            currencies = MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )


            stateController.successStateAUD()
        }
    }


    var isShowSelectDate by mutableStateOf(false)
    var selectedStoreTime by mutableStateOf<StoreTime2?>(null)
    var selectedTime by mutableStateOf<String?>(null)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DatePickerModal(
        onTimeSelected: (String) -> Unit // MutableState to hold the selected time
    ) {
        val datePickerState = rememberTimePickerState()

        DatePickerDialog(
            modifier = Modifier.padding(16.dp),
            onDismissRequest = { isShowSelectDate = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected("${datePickerState.hour}:${datePickerState.minute}")
//                    selectedTime =
//                    Log.e("sdsdh",datePickerState.hour.toString())
                    Log.e("sdsdm",selectedTime!!)
                    isShowSelectDate = false
//                    if (datePickerState. != null){
//                        if (isFrom)fromDate = convertMillisToDate(datePickerState.selectedDateMillis!!)
//                        else
//                            toDate = convertMillisToDate(datePickerState.selectedDateMillis!!)
//                        isShowSelectDate = false
//                    }

//                    datePickerState.selectedDateMillis
//                    onDateSelected(datePickerState.selectedDateMillis)
//                    onDismiss()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { isShowSelectDate = false }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = datePickerState)
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalShowCurrencyOfStore() {
        ModalBottomSheet(
            onDismissRequest = { isShowAddCurrency = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                 item {
                     CustomCard2(modifierBox = Modifier) {
                         Column(Modifier.selectableGroup()) {
                             Text(" عملات المتجر", modifier = Modifier.padding(14.dp))
                             storeCurrencies.forEach { text ->
                                 Row(
                                     Modifier.fillMaxWidth().height(56.dp)
                                         .padding(horizontal = 16.dp),
                                     verticalAlignment = Alignment.CenterVertically,
                                     horizontalArrangement = Arrangement.SpaceBetween
                                 )
                                 {
                                     val isDefault = text.isSelected == 1
                                     Text(text = text.currencyName,style = MaterialTheme. typography. bodyLarge,modifier = Modifier. padding(start = 16.dp))
                                         Button(
                                             enabled = !isDefault,
                                             onClick = {
                                                 confirmDialog(this@SettingsStoreActivity, withTextField = false){
                                                     updateDefaultCurrency(text.id.toString())
                                                 }
                                         }) {
                                             Text(if (isDefault) "افتراضي" else "تععين ك افتراضي")
                                         }

                                 }
                             }
                         }
                     }
                 }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalUpdateDeliveryPrice() {
        ModalBottomSheet(
            onDismissRequest = { isShowUpdateDeliveryPrice = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    item {

                        val selected  = CustomSingleton.selectedStore!!.storeCurrencies.find { it.isSelected == 1 }
                        if (selected == null){
                            Text("قد لايكون هناك عملات او عملات افتراضيه")
                        }else{
                            var deliveryPrice by remember { mutableDoubleStateOf(selected.deliveryPrice) }
                            var freeDeliveryPrice by remember { mutableDoubleStateOf(selected.freeDeliveryPrice)}
                            var lessCartPrice by remember { mutableDoubleStateOf(selected.lessCartPrice) }

                            CustomCard2(modifierBox = Modifier) {
                                Column(Modifier.selectableGroup()) {
                                    Text("سعر التوصيل", modifier = Modifier.padding(14.dp))
                                    //

                                    OutlinedTextField(
                                        modifier = Modifier.padding(8.dp),
                                        value = selected.deliveryPrice.toString() ,
                                        enabled = false,
                                        label = {
                                            Text("السعر الحالي")
                                        },
                                        onValueChange = {
                                        }
                                    )
                                    OutlinedTextField(
                                        modifier = Modifier.padding(8.dp),
                                        value = deliveryPrice.toString(),
                                        label = {
                                            Text("السعر الجديد")
                                        },
                                        onValueChange = {
                                            try {
                                                deliveryPrice = it.toDouble()
                                            }catch (e:Exception){

                                            }
                                        },
                                        textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
                                    )
                                }
                            }

                            CustomCard2(modifierBox = Modifier) {
                                Column(Modifier.selectableGroup()) {
                                    Text("اقل مبلغ يمكن طلبه", modifier = Modifier.padding(14.dp))
                                    //

                                    OutlinedTextField(
                                        modifier = Modifier.padding(8.dp),
                                        value = selected.lessCartPrice.toString() ,
                                        enabled = false,
                                        label = {
                                            Text("السعر الحالي")
                                        },
                                        onValueChange = {
                                        },

                                    )
                                    OutlinedTextField(
                                        modifier = Modifier.padding(8.dp),
                                        value = lessCartPrice.toString(),
                                        label = {
                                            Text("السعر الجديد")
                                        },
                                        onValueChange = {
                                            try {
                                                lessCartPrice = it.toDouble()
                                            }catch (e:Exception){

                                            }
                                        },
                                        textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
                                    )
                                }
                            }

                            CustomCard2(modifierBox = Modifier) {
                                Column(Modifier.selectableGroup()) {
                                    Text("توصيل مجاني للطلبات الاكبر من", modifier = Modifier.padding(14.dp))
                                    //
                                    OutlinedTextField(
                                        modifier = Modifier.padding(8.dp),
                                        value = selected.freeDeliveryPrice.toString() ,
                                        enabled = false,
                                        label = {
                                            Text("السعر الحالي")
                                        },
                                        onValueChange = {
                                        }
                                    )
                                    OutlinedTextField(
                                        modifier = Modifier.padding(8.dp),
                                        value = freeDeliveryPrice.toString(),
                                        label = {
                                            Text("السعر الجديد")
                                        },
                                        onValueChange = {
                                            try {
                                                freeDeliveryPrice = it.toDouble()
                                            }catch (e:Exception){

                                            }
                                        },
                                        textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
                                    )
                                }
                            }
                            Card(Modifier.padding(8.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column {
                                        val enabled = selected.deliveryPrice != deliveryPrice.toDouble() || selected.freeDeliveryPrice != freeDeliveryPrice.toDouble() || selected.lessCartPrice != lessCartPrice.toDouble();
                                        Button(
                                            enabled = enabled,
                                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                                            onClick = {
                                                if (enabled){
                                                    confirmDialog(this@SettingsStoreActivity, withTextField = false){
                                                        updateDeliveryPrice(deliveryPrice.toString(),freeDeliveryPrice.toString(),lessCartPrice.toString())
                                                    }
                                                }
                                            }) {
                                            Text("حفظ")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalUpdateStoreTime() {


        ModalBottomSheet(
            onDismissRequest = { isShowUpdateStoreTime = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                var storeTime by remember { mutableStateOf(selectedStoreTime!!) }

                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    item {
                        Card(Modifier.padding(8.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {

                                Column {

                                    CustomCard2(modifierBox = Modifier.clickable {

                                        showTimePicker {
                                            storeTime= storeTime.copy(storeTime = storeTime.storeTime!!.copy(openAt = it)
                                            )
                                        }
                                //                            selectedTime = null
                                //                            isShowSelectDate = true
                                    }) {
                                        CustomRow {
                                            Text("يفتح الساعة: " + formatTime(storeTime.storeTime!!.openAt)?.get(0)
                                                .toString() )
                                            Text(formatTime(storeTime.storeTime!!.openAt)?.get(1)
                                                .toString()).toString()
                                        }
                                    }
                                    CustomCard2(modifierBox = Modifier.clickable {
                                        showTimePicker {
                                            storeTime= storeTime.copy(storeTime = storeTime.storeTime!!.copy(closeAt = it)
                                            )
                                        }
                                    }) {
                                        CustomRow {
                                            Text("يغلق الساعة: " + formatTime(storeTime.storeTime!!.closeAt)?.get(0)
                                                .toString())
                                            Text(formatTime(storeTime.storeTime!!.closeAt)?.get(1)
                                                .toString())
                                        }
                                    }
                                    CustomRow {
                                        Text("مفتوح-مغلق")
                                        Switch(
                                            checked = storeTime.storeTime!!.isOpen == 1,
                                            onCheckedChange = {
                                                storeTime= storeTime.copy(storeTime = storeTime.storeTime!!.copy(isOpen = if (storeTime.storeTime!!.isOpen == 1) 0 else 1)
                                                )
                                            },
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }


                                    Button(
                                        enabled = selectedStoreTime != storeTime,
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        onClick = {
                                            updateStoreTime(storeTime.storeTime!!.day.toString(),storeTime.storeTime)
                                        }) {
                                        Text("حفظ")
                                    }
                                }

                            }
                        }
                    }



                }
            }
        }
    }


    private fun showTimePicker(
        onSuccess: (String) -> Unit,
    ) {
         TimePickerDialog(this,{t,i,o->

             val time = t.hour.toString()+":"+t.minute.toString()
             Log.e("tttime",time)
             onSuccess(time)

         },24,50,false).show()

    }
}

fun normalizeTimeInput(input: String): String {
    val parts = input.split(":")

    // If there is no colon, assume it's only the hour part and add ":00" for minutes
    if (parts.size == 1) {
        return "${parts[0].padStart(2, '0')}:00"
    }

    // If there's a colon but no minutes, assume ":00"
    if (parts.size == 2 && parts[1].isEmpty()) {
        return "${parts[0].padStart(2, '0')}:00"
    }

    // Ensure both parts have at least two digits
    val hour = parts[0].padStart(2, '0')
    val minute = parts[1].padStart(2, '0')

    return "$hour:$minute"
}
fun formatTime(timeString:String): List<String>? {
    val s =toLocalTime(timeString)
    if (s != null)
    return listOf(
        s.format(DateTimeFormatter.ofPattern("hh:mm")),
        detectAmPm(timeString).toString()
    )
    else return null

}
fun toLocalTime(timeString:String): LocalTime? {
    val timeFormats = listOf(          // 24-hour format (e.g., "14:30")
        DateTimeFormatter.ofPattern("hh:mm"),          // 12-hour format with AM/PM (e.g., "02:30 PM")
        DateTimeFormatter.ofPattern("HH:mm"),            // 12-hour format without AM/PM (e.g., "02:30")
//        DateTimeFormatter.ofPattern("HH:mm:ss"),         // 24-hour format with seconds (e.g., "14:30:00")
//        DateTimeFormatter.ofPattern("hh:mm:ss a"),       // 12-hour format with AM/PM and seconds (e.g., "02:30:00 PM")
//        DateTimeFormatter.ofPattern("HH:mm:ss.SSS"),     // Time with milliseconds (e.g., "14:30:00.123")
//        DateTimeFormatter.ofPattern("hh:mm:ss a.SSS")    // 12-hour format with AM/PM, seconds, and milliseconds (e.g., "02:30:00 PM.123")
    )

    // Try each format and return the first valid LocalTime
    for (formatter in timeFormats) {
        try {
            val s = normalizeTimeInput(timeString)
            Log.e("rer",s)
            return LocalTime.parse(s, formatter)
        } catch (e: DateTimeParseException) {
            // Ignore exception and try the next format
        }
    }

    // If no valid time format was found
    println("Invalid time format: $timeString")
    return null

}
fun detectAmPm(timeString:String): String? {
    try {
        val time = toLocalTime(timeString)
        if (time != null){
            val hour = time.hour
            Log.e("hourr",hour.toString())
            Log.e("hourr",(hour < 12).toString())
            return if (hour < 12) "صباحا" else "مساءا"
        }

    } catch (e: DateTimeParseException) {
        // Continue to the next formatter
    }
    return null // Return null if no formatter succeeds

}

@Serializable
data class SubscriptionModel(val id:Int,
                             val points:Int,
                             val name:String, val price:Double, val productId:String,
                             val isPending:Boolean,)

@Serializable
data class StoreTime(val day:Int,val openAt:String, val closeAt:String, val isOpen:Int)
@Serializable
data class StoreTime2(val day:String, val storeTime: StoreTime?)

@Serializable
data class MyPurchase(val productId: String, val status:Int,val isAcked : Boolean, val token:String)
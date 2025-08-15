package com.fekraplatform.storemanger.activities1

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.MutableState
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.fekraplatform.storemanger.activities.LocationStoreActivity
import com.fekraplatform.storemanger.activities.SingletonHome
import com.fekraplatform.storemanger.application.MyApplication
import com.fekraplatform.storemanger.models.Currency
import com.fekraplatform.storemanger.models.PageModel
import com.fekraplatform.storemanger.models.Store
import com.fekraplatform.storemanger.models.StoreCurrency
import com.fekraplatform.storemanger.repositories.BillingRepository
import com.fekraplatform.storemanger.shared.AToken1
import com.fekraplatform.storemanger.shared.confirmDialog
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomRow
import com.fekraplatform.storemanger.shared.CustomRow2
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.MainComposeAUD
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.ServerConfig
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.confirmDialog2
import com.fekraplatform.storemanger.shared.formatPrice
import com.fekraplatform.storemanger.storage.BillingEntity
import com.fekraplatform.storemanger.storage.GoogleBillingStorage
import com.fekraplatform.storemanger.storage.MyAppStorage1
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class SettingStoreViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    val appSession: AppSession,
    private val builder: FormBuilder,
): ViewModel()
{
    fun editMode(it: Boolean) {
        appSession.isEditMode = it
    }

    fun isSharedStore(): Boolean {
        return selectedStore.typeId == 1
    }

    var storeTime by mutableStateOf(emptyList<StoreTime2>())
    var selectedStore = appSession.selectedStore
    var uriServiceAccount by  mutableStateOf<Uri?>(null)
    val stateController = StateController()
    var uriFile by mutableStateOf<Uri?>(null)
    var file by mutableStateOf<String?>(null)
    var storeCurrencies by mutableStateOf<List<StoreCurrency>>(emptyList())
    var isShowAddCurrency by mutableStateOf(false)
    var isEditMode by mutableStateOf(appSession.isEditMode)

    ////
//    val categories = appSession.categories
//    val sections = appSession.sections
//    val nestedSection = appSession.nestedSection
//    val products = appSession.products


    val pages = listOf(
        PageModel("", 0),
        PageModel("أوقات الدوام", 1)
    )
    var page by mutableStateOf(pages.first())
    /////

    var isShowUpdateDeliveryPrice by mutableStateOf(false)
    var isShowUpdateStoreTime by mutableStateOf(false)
    fun updateDeliveryPrice(deliveryPrice:String,freeDeliveryPrice:String,lessCartPrice:String) {
        stateController.startAud()
        val body = builder.sharedBuilderFormWithStoreId()

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

        viewModelScope.launch {
            stateController.startAud()
            try {
                val data = requestServer.request(body, "updateStoreCurrencyPricing")
                val result:StoreCurrency  = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                val updatedStoreCurrencies =  appSession.selectedStore.storeCurrencies.map {
                    if (it.id == result.id)
                        result
                    else it
                }
                appSession.setStoreAndUpdateStores(selectedStore.copy(storeCurrencies = updatedStoreCurrencies))
            isShowUpdateDeliveryPrice = false
            stateController.successStateAUD("تمت   بنجاح")
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun updateDefaultCurrency(id:String) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId().addFormDataPart("storeCurrencyId", id)
                val data = requestServer.request(body, "updateDefaultCurrency")
                storeCurrencies = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                appSession.setStoreAndUpdateStores(appSession.selectedStore.copy(storeCurrencies = storeCurrencies))
                stateController.successStateAUD("تم بنجاح")
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun updateServiceAccount(password:String,requestBodyFile: RequestBody) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId().addFormDataPart("jsonService", "file1.jpg", requestBodyFile)
                requestServer.request(body, "updateStoreServiceAccount")
                uriServiceAccount = null
                stateController.successStateAUD("تمت   بنجاح")
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
//        stateController.startAud()
//        val body = builderForm3()

//        body.addFormDataPart("passwordService",password)
//
////            .build()
//        requestServer.request2(body.build(), "updateStoreServiceAccount", { code, fail ->
//            stateController.errorStateAUD(fail)
//        }
//        ) { data ->
//            uriServiceAccount = null
//            stateController.successStateAUD("تمت   بنجاح")
//        }
    }
    var selectedStoreTime by mutableStateOf<StoreTime2?>(null)
    fun getStoreTime() {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                val data = requestServer.request(body, "getStoreTime")
                storeTime = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun getStoreCurrencies() {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                val data = requestServer.request(body, "getStoreCurrencies")
                storeCurrencies = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                setStoreAndUpdateStores(appSession.selectedStore.copy(storeCurrencies = storeCurrencies))
                isShowAddCurrency = true
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    private fun setStoreAndUpdateStores(store: Store) {
        appSession.selectedStore = store
        selectedStore = appSession.selectedStore
        val updatedStores = appSession.stores.map {
            if (it.id == appSession.selectedStore.id)
                appSession.selectedStore
            else
                it
        }
        appSession.stores = updatedStores
    }
    fun updateStoreTime(day: String, storeTimeParameter: StoreTime?) {
        stateController.startAud()
//        val googleBillingStorage = GoogleBillingStorage()
        val body = builder.sharedBuilderFormWithStoreId()
            .addFormDataPart("day",day)
        if (storeTimeParameter != null){
            body.addFormDataPart("openAt", storeTimeParameter.openAt)
            body.addFormDataPart("closeAt",storeTimeParameter.closeAt)
            body.addFormDataPart("isOpen", storeTimeParameter.isOpen.toString())
        }

        viewModelScope.launch {
            stateController.startAud()
            try {
                val data = requestServer.request(body, "updateStoreTime")
                val result:StoreTime2 = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                storeTime = storeTime.map {
                    if (it.day == result.day)
                        result
                    else it
                }
                isShowUpdateStoreTime = false

                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }

//        requestServer.request2(body.build(), "updateStoreTime", { code, fail ->
//
//            stateController.errorStateAUD(fail)
//        }
//        ) { data ->
//            val result:StoreTime2 =
//                MyJson.IgnoreUnknownKeys.decodeFromString(
//                    data
//                )
//
//            storeTime = storeTime.map {
//                if (it.day == result.day)
//                    result
//                else it
//            }
//            isShowUpdateStoreTime = false
//            stateController.successStateAUD()
//        }
    }

}

@AndroidEntryPoint
class SettingsStoreActivity1 : ComponentActivity() {
    private val viewModel:SettingStoreViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            val  s=  if (viewModel.page != viewModel.pages.first()) " | " + viewModel.page.pageName else ""
            MainComposeAUD("اعدادات المتجر$s",viewModel.stateController, { finish() },) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (viewModel.page.pageId == 0)
                        MainPage()
                    else if (viewModel.page.pageId == 1)
                        StoreTimePage()

                }
                if (viewModel.isShowUpdateStoreTime) modalUpdateStoreTime()
                if (viewModel.isShowUpdateDeliveryPrice) modalUpdateDeliveryPrice()
                if (viewModel.isShowAddCurrency) modalShowCurrencyOfStore()
            }
        }
    }

    private fun LazyListScope.MainPage() {
        item {
            CustomCard2(modifierBox = Modifier) {
                Column(Modifier.selectableGroup()) {
                    Text("الرئيسية", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                    SettingItem("الموقع"){ gotoStoreLocation()}
                    SettingItem("اوقات الدوام"){ viewModel.page = viewModel.pages[1]}
                }
            }
        }
        item {
            CustomCard2(modifierBox = Modifier) {
                Column(Modifier.selectableGroup()) {
                    Text("العملات والتسعير", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                    SettingItem("عملات المتجر"){ viewModel.getStoreCurrencies()}
                    SettingItem("سعر التوصيل"){ viewModel.isShowUpdateDeliveryPrice = true}
                }
                }
        }
        item {
            CustomCard2(modifierBox = Modifier) {
                Column(Modifier.selectableGroup()) {
                    Text("الاشتراكات والنقاط والتطبيق", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                    SettingItem("الاشتراكات"){gotoStoreSubdcriptions(ProductTypeEnum.SUBS.name)}
                    SettingItem("النقاط"){
                        gotoStoreSubdcriptions(ProductTypeEnum.INAPP.name)
                    }
                    if (viewModel.selectedStore.app != null){
                        val t = if ( viewModel.selectedStore.app!!.hasServiceAccount) "تعديل" else "اضافة"
                        SettingItem("$t اعدادات الخدمة للتطبيق "){
                            getContentServiceAccount.launch("application/json")}
                    }
                }
            }
        }

        if (viewModel.selectedStore.typeId == 1)
            item {
                CustomCard2(modifierBox = Modifier
                    .fillMaxSize()
                    .clickable {

                    }) {
                    CustomRow {
                        Text("وضع التعديل")
                        Switch(
                            checked = viewModel.isEditMode,
                            onCheckedChange = { viewModel.editMode(it) },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        if (viewModel.isSharedStore()) {
            if ((viewModel.appSession.categories != viewModel.selectedStore.storeConfig!!.categories||
                        viewModel.appSession.sections != viewModel.selectedStore.storeConfig!!.sections ||
                        viewModel.appSession.nestedSection != viewModel.selectedStore.storeConfig!!.nestedSections ||
                        viewModel.appSession.products != viewModel.selectedStore.storeConfig!!.products)
                && viewModel.isEditMode
            ) {
                item {
                    CustomCard2(modifierBox = Modifier
                        .fillMaxSize()
                        .clickable {
//                            SingletonHome.updateStoreConfig()
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


    @OptIn(ExperimentalFoundationApi::class)
    private fun LazyListScope.StoreTimePage(){
        if (viewModel.storeTime.isEmpty()) viewModel.getStoreTime()

        itemsIndexed(viewModel.storeTime){index, item ->
            CustomCard2(modifierBox = Modifier
                .fillMaxSize()
                .clickable {
                    viewModel.selectedStoreTime = item
                    viewModel.isShowUpdateStoreTime = true
                }) {
                CustomRow2 {
                    Text(item.day)
                    if (item.storeTime == null){
                        Button(onClick = {
                            viewModel.updateStoreTime((index+1).toString(),null)
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
                        Text(formatTime(item.storeTime.openAt)?.get(1)
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

    private fun gotoStoreLocation() {
        val intent = Intent(this, LocationStoreActivity1::class.java)
        intent.putExtra("mode", LocationUpdateMode.STORE.name)
        startActivity(intent)
    }

    private fun gotoStoreSubdcriptions(type:String) {
        val intent = Intent(this, SubscriptionsStoreActivity1::class.java)
        intent.putExtra("product_type", type)
        startActivity(intent)
    }
    fun saveFileToCache() {
        val cacheFile = File(cacheDir, "service.json")
        try {
            val fos = FileOutputStream(cacheFile)
            contentResolver.openInputStream(viewModel.uriFile!!)?.use { input ->
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
                viewModel.file = fileContents
                return fileContents
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    val getContentFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null){
            viewModel.uriFile = uri
        }
    }
    private fun backHandler() {
        if (viewModel.page.pageId != 0) {
            viewModel.page = viewModel.pages.first()
        } else
            finish()
    }
    @Composable
    private fun BackHand() {
        BackHandler {
            backHandler()
        }
    }

    val getContentServiceAccount = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null){
            viewModel.uriServiceAccount = uri

            confirmDialog2(this) {

                if (viewModel.uriServiceAccount != null){
                    val requestBodyFile = object : RequestBody() {
                        val mediaType = "json".toMediaTypeOrNull()
                        override fun contentType(): MediaType? {
                            return mediaType
                        }

                        override fun writeTo(sink: BufferedSink) {
                            contentResolver.openInputStream(viewModel.uriServiceAccount!!)?.use { input ->
                                val buffer = ByteArray(4096)
                                var bytesRead: Int
                                while (input.read(buffer).also { bytesRead = it } != -1) {
                                    sink.write(buffer, 0, bytesRead)
                                }
                            }
                        }
                    }
                    viewModel.updateServiceAccount(it,requestBodyFile)
                }

                Log.e("ssss",it.toString())

            }

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalShowCurrencyOfStore() {
        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowAddCurrency = false }) {
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
                             viewModel.storeCurrencies.forEach { text ->
                                 Row(
                                     Modifier
                                         .fillMaxWidth()
                                         .height(56.dp)
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
                                                 confirmDialog(this@SettingsStoreActivity1, withTextField = false){
                                                     viewModel.updateDefaultCurrency(text.id.toString())
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
            onDismissRequest = { viewModel.isShowUpdateDeliveryPrice = false }) {
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

                        val selected  = viewModel.selectedStore.storeCurrencies.find { it.isSelected == 1 }
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
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            onClick = {
                                                if (enabled){
                                                    confirmDialog(this@SettingsStoreActivity1, withTextField = false){
                                                        viewModel.updateDeliveryPrice(deliveryPrice.toString(),freeDeliveryPrice.toString(),lessCartPrice.toString())
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
            onDismissRequest = { viewModel.isShowUpdateStoreTime = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                var storeTime by remember { mutableStateOf(viewModel.selectedStoreTime!!) }

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
                                        enabled = viewModel.selectedStoreTime != storeTime,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        onClick = {
                                            viewModel.updateStoreTime(storeTime.storeTime!!.day.toString(),storeTime.storeTime)
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
                             val name:String,
                             val price:Double,
                             val productId:String,
                             val isPending:Boolean,
    )

@Serializable
data class StoreTime(val day:Int,val openAt:String, val closeAt:String, val isOpen:Int)
@Serializable
data class StoreTime2(val day:String, val storeTime: StoreTime?)
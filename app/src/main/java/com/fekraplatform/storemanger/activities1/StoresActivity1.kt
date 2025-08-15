package com.fekraplatform.storemanger.activities1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.models.Store
import com.fekraplatform.storemanger.shared.AToken1
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomException
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomImageView1
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.ServerConfig
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.storage.MyAppStorage1
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import javax.inject.Inject

@HiltViewModel
class StoresViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    private val serverConfig: ServerConfig,
    private val appSession: AppSession,
    private val myAppStorage: MyAppStorage1,
    private val builder: FormBuilder,
    private val aToken: AToken1
):ViewModel()
{

    var stores by mutableStateOf<List<Store>>(appSession.stores)
    val stateController = StateController()
    val isShowUpdateStore = mutableStateOf(false)
    var uriLogo=mutableStateOf<Uri?>(null)
    var uriCover =mutableStateOf<Uri?>(null)
    fun baseImageLogoUrl() = appSession.remoteConfig.BASE_IMAGE_URL+appSession.remoteConfig.SUB_FOLDER_STORE_LOGOS
    fun baseImageCoverUrl() = appSession.remoteConfig.BASE_IMAGE_URL+appSession.remoteConfig.SUB_FOLDER_STORE_COVERS

    var selectedStore by mutableStateOf<Store?>(null)
        private set

//    fun updateSelectedStore(newStore: Store) {
//     // إذا أردت حفظه أيضاً في AppSession
//    }


    init {
        initApp()
    }

    var shouldExit by mutableStateOf(false)

    suspend fun checkAccessToken() {
        val isSet = aToken.isSetAccessToken()
        if (!isSet) {
            shouldExit = true // ✅ التعديل يكون على الـ private MutableStateFlow
        }
        else{
            read()
        }

    }

     fun initApp(){
         viewModelScope.launch {
             stateController.startRead()
             try {
                 if (!requestServer.isInternetAvailable()){
                     throw CustomException(0,"No Internet")
                 }
                 if (!serverConfig.isSetSubscribeApp()){
                     val appId = "1"
                     val success = serverConfig.subscribeToTopicSuspend(appId)
                     if (success) {
                         serverConfig.setSubscribeApp(appId)
                         Log.d("Topic", "Subscribed successfully")
                     } else {
                         Log.d("Topic", "Subscription failed")
                     }
                 }
                 else{
                     Log.e("App Sub Stored ","Done")
                 }
                 serverConfig.getFcmTokenSuspend()

                 if (serverConfig.isSetRemoteConfig()){
                     Log.e("Remote Config V",appSession.remoteConfig.toString())
                 }else{
                     requestServer.initVarConfig()
                 }
                 //
                 myAppStorage.processLanguage()
                 checkAccessToken()
             }catch (e: CustomException){
                 Log.e("Custom Error init server conige",e.message)
                 stateController.errorStateRead(e.message)
             }
         }

    }

    private suspend fun read() {
        stateController.startRead()
        try {
            val body = builder.sharedBuilderForm()
            val data = requestServer.request(body, "getStores")
            stores = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
            appSession.stores = stores
            if (stores.isNotEmpty())
            selectStore(stores.first())
            stateController.successState()
        } catch (e: Exception) {
            Log.e("UUURRRL3",e.message.toString())
            stateController.errorStateRead(e.message.toString())
        }
    }

    fun updateStore(name: String,requestBodyIcon:RequestBody?,requestBodyCover:RequestBody?) {
        stateController.startAud()
        val body = builder.sharedBuilderFormWithStoreId()
            .addFormDataPart("name",name)

        if (requestBodyIcon != null){
            body.addFormDataPart("logo", "file1.jpg", requestBodyIcon)
        }
        if (requestBodyCover!= null){
            body.addFormDataPart("cover", "file2.jpg", requestBodyCover)
        }

        viewModelScope.launch {
            try {
                val data = requestServer.request(body, "updateStore")
                val result: Store = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                appSession.stores = appSession.stores.map {
                    if (result.id == it.id){
                        it.copy(
                            name = result.name,
                            logo = result.logo,
                            cover = result.cover,
                        )
                    }else
                        it
                }
                appSession.selectedStore = stores.first()
                uriLogo.value = null
                uriCover.value = null
                isShowUpdateStore.value = false
                stateController.successStateAUD("تمت   بنجاح")
            } catch (e: Exception) {
                Log.e("UUURRRL3",e.message.toString())
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

    fun selectStore(store: Store){
        selectedStore = store
        appSession.selectedStore = store
    }
}

@AndroidEntryPoint
class StoresActivity1 : ComponentActivity() {
    val viewModel : StoresViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            if (viewModel.shouldExit){
                gotoLogin()
            }
            StoreMangerTheme {

                    MainCompose1 (
                        0.dp, viewModel.stateController, this,
                        { viewModel.initApp() },
                    ) {
                        LazyColumn(Modifier.safeDrawingPadding()) {
                            stickyHeader {
                                CustomCard2(modifierBox = Modifier){
                                    Column {
                                        DropDownDemo()
                                    }
                                }
                            }
                            if (viewModel.selectedStore != null){
                                item {
                                    CustomCard2(modifierBox = Modifier) {
                                        CustomImageView1(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .padding(8.dp)
                                                .clickable {
                                                    viewModel.isShowUpdateStore.value = true
                                                },
                                            imageUrl =viewModel.baseImageLogoUrl()+viewModel.selectedStore!!.logo,
                                        )

                                        Row (verticalAlignment = Alignment.CenterVertically){
                                            CustomImageView1(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .padding(8.dp)
                                                    .clickable {
                                                     viewModel.isShowUpdateStore.value= true
                                                    },
                                                imageUrl =viewModel.baseImageCoverUrl() + viewModel.selectedStore!!.cover,
                                            )
                                            Text(viewModel.selectedStore!!.storeMainCategory.storeMainCategoryName)
                                        }


                                    }
                                }

                                item {
                                    Button(onClick = {
                                        goToStore()

                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("عرض المتجر")
                                    }
                                }


                                item {
                                    Button(onClick = {
                                        gotoStoreOrders()

                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("ادارة الطلبات")
                                    }
                                }
                                item {
                                    Button(onClick = {
                                        gotoStoreAds()
                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("الاعلانات")
                                    }
                                    Button(onClick = {
                                        gotoStoreDeliveryMen()
                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("الموصلين")
                                    }
                                }
                                item {
                                    Button(
                                        enabled = viewModel.selectedStore!!.app != null,
                                        onClick = {
                                        gotoStoreNotifications()
                                    },
                                        modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("الاشعارات")
                                    }
                                }
                                item {
                                    Button(onClick = {
                                        gotoStoreCoupons()
                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("إدارة الكوبونات")
                                    }
                                }

                                item {
                                    Button(onClick = {
                                        gotoStoreSettings()
                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("اعدادات المتجر")
                                    }
                                }
                            }
                            else if(viewModel.stores.isEmpty()){
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "أنشئ متجرك الالكتروني وابدأ في تحقيق أرباحك",
                                            style = MaterialTheme.typography.titleMedium,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )

                                        Button(
                                            onClick = { gotoAddStore() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                        ) {
                                            Text("إضافة متجر جديد")
                                        }
                                    }
                                }
                            }
                        }
//                        if (viewModel.isShowUpdateStore.value) modalUpdateStore()

                    }
                }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalUpdateStore() {


        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowUpdateStore.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                var storeName by remember { mutableStateOf(viewModel.selectedStore!!.name) }
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Button(onClick = {
                            var requestBodyIcon : RequestBody? = null
                            if (viewModel.uriLogo.value != null){
                                 requestBodyIcon = object : RequestBody() {
                                    val mediaType = "image/jpeg".toMediaTypeOrNull()
                                    override fun contentType(): MediaType? {
                                        return mediaType
                                    }

                                    override fun writeTo(sink: BufferedSink) {
                                        contentResolver.openInputStream(viewModel.uriLogo.value!!)?.use { input ->
                                            val buffer = ByteArray(4096)
                                            var bytesRead: Int
                                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                                sink.write(buffer, 0, bytesRead)
                                            }
                                        }
                                    }
                                }
                            }
                            var requestBodyCover : RequestBody? = null
                            if (viewModel.uriCover.value != null){
                                 requestBodyCover= object : RequestBody() {
                                    val mediaType = "image/jpeg".toMediaTypeOrNull()
                                    override fun contentType(): MediaType? {
                                        return mediaType
                                    }

                                    override fun writeTo(sink: BufferedSink) {
                                        contentResolver.openInputStream(viewModel.uriCover.value!!)?.use { input ->
                                            val buffer = ByteArray(4096)
                                            var bytesRead: Int
                                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                                sink.write(buffer, 0, bytesRead)
                                            }
                                        }
                                    }
                            }

                            }
                           viewModel.updateStore(storeName,requestBodyIcon,requestBodyCover)
                        }, modifier = Modifier.padding(8.dp)) { Text("حفظ التعديلات") }

                    }
                    item {

                        Card(Modifier.padding(8.dp)){

                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                OutlinedTextField(
                                    modifier = Modifier.padding(8.dp),
                                    value = storeName,
                                    label = {
                                        Text("اسم المتجر")
                                    },
                                    onValueChange = {
                                        storeName = it
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            Modifier
                                .size(80.dp)
                                .padding(8.dp)
                                .clickable {
                                    getContentlogo.launch("image/*")
                                }) {
//                            if (){
                                CustomImageViewUri(
                                    modifier =  Modifier.fillMaxWidth(),
                                    imageUrl = if (viewModel.uriLogo.value != null) viewModel.uriLogo.value!! else viewModel.baseImageLogoUrl() + viewModel.selectedStore!!.logo,
                                    contentScale = ContentScale.Crop
                                )
//                            }
                        }
                    }

                    item {
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clickable {
                                    getContentCover.launch("image/*")
                                }) {

                            CustomImageViewUri(
                                modifier =  Modifier.fillMaxWidth(),
                                imageUrl = if (viewModel.uriCover.value != null) viewModel.uriCover.value!! else viewModel.baseImageCoverUrl() + viewModel.selectedStore!!.cover,
                                contentScale = ContentScale.Crop
                            )
//
                        }
                    }

                }
            }
        }
    }

    @Composable
    fun DropDownDemo() {

        val isDropDownExpanded = remember {
            mutableStateOf(false)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable {
                            isDropDownExpanded.value = true
                        }
                ) {
                    CustomIcon(Icons.Default.Settings, border = true) {
                        gotoSettings()
                    }
                    if (viewModel.selectedStore!= null){
                        Text(text = viewModel.selectedStore!!.name )
                        Text(text = "عرض الكل" )
                    }
                }
                DropdownMenu(
                    expanded = isDropDownExpanded.value,
                    onDismissRequest = {
                        isDropDownExpanded.value = false
                    }) {
                    viewModel.stores.forEachIndexed { index, store ->
                        DropdownMenuItem(text = {
                           Row {
                               Text(text = store.name)
                               Text(" نوع المتجر: ")
                               if (store.typeId == 1)
                                   Text("مشترك")
                               if (store.typeId == 2)
                                   Text("  vip مخصص")
                           }
                        },
                            onClick = {
//                                SingletonHome.isEditMode.value = false
                                viewModel.selectStore(store)
//                                viewModel .selectedStore=store
                                isDropDownExpanded.value = false
                            })
                    }
                    DropdownMenuItem(text = {
                        Row {
                           Text("+")
                        }
                    },
                        onClick = {
                            isDropDownExpanded.value = false
                            gotoAddStore()
//                            isShowAddCatgory.value = true
                        })
                }
            }

        }
    }

    //// Logic Function




    //goto
    private fun gotoLogin() {
        val intent = Intent(this, LoginActivity1::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
    private fun gotoSettings() {
        val intent = Intent(this, SettingsActivity1::class.java)
        startActivity(intent)
    }
    private fun gotoAddStore() {
        val intent = Intent(this, AddStoreActivity1::class.java)
        launcherAddStore.launch(intent)
    }
    private fun gotoStoreOrders() {
        val intent = Intent(
            this,
            StoreOrdersActivity1::class.java
        )
        startActivity(intent)
    }
    private fun gotoStoreAds() {
        val intent = Intent(this, AdsActivity1::class.java)
        startActivity(intent)
    }
    private fun gotoStoreNotifications() {
        val intent = Intent(
            this,
            StoreNotificationsActivity1::class.java
        )
        startActivity(intent)
    }
    private fun gotoStoreDeliveryMen() {
        val intent = Intent(
            this,
            StoreDeliveryMenActivity1::class.java
        )
        startActivity(intent)
    }
    private fun gotoStoreSettings() {
        val intent = Intent(
            this,
            SettingsStoreActivity1::class.java
        )
        startActivity(intent)
    }
    private fun gotoStoreCoupons() {
        val intent = Intent(
            this,
            StoreCouponsActivity::class.java
        )
        startActivity(intent)
    }
    private fun goToStore() {
        val intent = Intent(
            this,
            StoreCategoriesActivity1::class.java
        )
//        intent.putExtra("store", MyJson.MyJson.encodeToString(store))
        startActivity(intent)
    }
    ////Callback
    val getContentlogo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null){
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val sizeInBytes = inputStream?.available() ?: 0
                val sizeInKB = sizeInBytes / 1024
                if (sizeInKB > 100) {
                    viewModel.stateController.errorStateAUD("حجم الصورة يجب أن يكون أقل من 100 كيلوبايت")
                    viewModel.uriLogo.value = null
                }else{
                    viewModel.uriLogo.value = uri
                }
            } catch (e: Exception) {
                viewModel.stateController.errorStateAUD("فشل في تحميل الصورة: ${e.message}")
            }
        }
        }
    val getContentCover = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null){
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val sizeInBytes = inputStream?.available() ?: 0
                val sizeInKB = sizeInBytes / 1024
                if (sizeInKB > 100) {
                    viewModel.stateController.errorStateAUD("حجم الصورة يجب أن يكون أقل من 100 كيلوبايت")
                    viewModel.uriCover.value = null
                }else{
                    viewModel. uriCover.value = uri
                }
            } catch (e: Exception) {
                viewModel.stateController.errorStateAUD("فشل في تحميل الصورة: ${e.message}")
            }
        }
        }
    val launcherAddStore = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val storeJson = result.data?.getStringExtra("store")
            val store = storeJson?.let { MyJson.IgnoreUnknownKeys.decodeFromString<Store>(it) }

            if (store != null){
                viewModel.stores += store
                viewModel.selectStore(store)
            }

            // استخدم store هنا
        }
    }
}




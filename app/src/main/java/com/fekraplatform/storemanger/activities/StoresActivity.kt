package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.models.Store
import com.fekraplatform.storemanger.shared.CustomCard
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomImageView
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm3
import com.fekraplatform.storemanger.storage.AppDatabase
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink


class StoresActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)
    ////
//    var CustomSingleton.selectedStore by mutableStateOf<Store?>(null)
    val isShowAddCatgory = mutableStateOf(false)
    val isShowUpdateStore = mutableStateOf(false)
    private lateinit var storeToUpdate: Store
    var uriLogo=mutableStateOf<Uri?>(null)
    var uriCover =mutableStateOf<Uri?>(null)

//    var usrs by mutableStateOf<List<User>>(emptyList())



    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val db = Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java, "database-name"
//        ).build()


        read()
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {

                    MainCompose1 (
                        0.dp, stateController, this,
                        { read() },
                    ) {
                        LazyColumn(Modifier.safeDrawingPadding()) {
                            stickyHeader {
                                CustomCard(modifierBox = Modifier){
                                    Column {
                                        DropDownDemo()
                                    }
                                }
                            }
                            if (CustomSingleton.selectedStore!=null){



                                item {
                                    Button(onClick = {
                                        SelectedStore.store.value = CustomSingleton.selectedStore!!
                                        goToStore(CustomSingleton.selectedStore!!)

                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("عرض المتجر")
                                    }
                                }

                                item {
                                    CustomImageView(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .padding(8.dp)
                                            .clickable {

                                                storeToUpdate = CustomSingleton.selectedStore!!
                                                isShowUpdateStore.value = true
                                            },
                                        context = this@StoresActivity,
                                        imageUrl = requestServer.serverConfig.getRemoteConfig().BASE_IMAGE_URL+requestServer.serverConfig.getRemoteConfig().SUB_FOLDER_STORE_LOGOS+CustomSingleton.selectedStore!!.logo,
                                        okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                                    )
                                }
                                item {
                                    Button(onClick = {
                                        SelectedStore.store.value = CustomSingleton.selectedStore!!
                                        gotoStoreOrders()

                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("ادارة الطلبات")
                                    }
                                }
                                item {
                                    Button(onClick = {
                                        gotoStorePosts()
                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("المنشورات")
                                    }
                                    Button(onClick = {
                                        SelectedStore.store.value = CustomSingleton.selectedStore!!
                                        gotoStoreDeliveryMen()
                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("الموصلين")
                                    }
                                }
                                item {
                                    Button(onClick = {
                                        gotoStoreNotifications()
                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("الاشعارات")
                                    }
                                    Button(onClick = {
//                                        SelectedStore.store.value = CustomSingleton.selectedStore!!
//                                        gotoStoreDeliveryMen()
                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("الاعلانات")
                                    }
                                }
                                item {
                                    Button(onClick = {
                                        SelectedStore.store.value = CustomSingleton.selectedStore!!
                                        gotoStoreSettings(CustomSingleton.selectedStore!!)
                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("اعدادات المتجر")
                                    }
                                }
                            }
                        }
                        if (isShowAddCatgory.value) modalAddMyCategory()
                        if (isShowUpdateStore.value) modalUpdateStore()

                    }
                }
        }
    }



    ///Modals
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddMyCategory() {


        ModalBottomSheet(
            onDismissRequest = { isShowAddCatgory.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    item {
                        var storeName by remember { mutableStateOf("") }
                        Card(Modifier.padding(8.dp)){
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                OutlinedTextField(
                                    modifier = Modifier.padding(8.dp),
                                    value = storeName,
                                    onValueChange = {
                                        storeName = it
                                    }
                                )
                                IconButton(onClick = {
                                    addStore(storeName)
//                                    addCategory(categoryName,{
//                                        categoryName = ""
//                                        categories.value += it
//                                    })

                                }) {
                                    Icon(
                                        modifier =
                                        Modifier
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(
                                                    16.dp
                                                )
                                            )
                                            .clip(
                                                RoundedCornerShape(
                                                    16.dp
                                                )
                                            ),
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = ""
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            Modifier
                                .size(50.dp)
                                .clickable {
                                    getContentlogo.launch("image/*")
                                }) {
                            if (uriLogo.value != null){
                                CustomImageViewUri(
                                    modifier = Modifier.fillMaxWidth(),
                                    imageUrl = uriLogo.value!!,
                                )
                            }
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
                            if (uriCover.value != null){
                                CustomImageViewUri(
                                    modifier = Modifier.fillMaxWidth(),
                                    imageUrl = uriCover.value!!,
                                )
                            }
                        }
                    }

                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalUpdateStore() {


        ModalBottomSheet(
            onDismissRequest = { isShowUpdateStore.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                var storeName by remember { mutableStateOf(CustomSingleton.selectedStore!!.name) }
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
//                    if (CustomSingleton.selectedStore!!.name != storeName )
                    item {
                        Button(onClick = {
                            updateStore(storeToUpdate.id.toString(),storeName)
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
                                    imageUrl = if (uriLogo.value != null) uriLogo.value!! else CustomSingleton.getStoreLogo(),
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
                                imageUrl = if (uriCover.value != null) uriCover.value!! else CustomSingleton.getStoreCover(),
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

                    if (CustomSingleton.selectedStore != null){
                        Text(text = CustomSingleton.selectedStore!!.name )
                        Text(text = "عرض الكل" )
                    }

                }
                DropdownMenu(
                    expanded = isDropDownExpanded.value,
                    onDismissRequest = {
                        isDropDownExpanded.value = false
                    }) {
                    CustomSingleton.stores.forEachIndexed { index, store ->
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
                                SingletonHome.isEditMode.value = false
                                CustomSingleton.selectedStore=store
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
                            isShowAddCatgory.value = true
                        })
                }
            }

        }
    }

    //// Logic Function
    private fun read() {
        stateController.startRead()

        val body = builderForm3()
            .build()

        requestServer.request2(body, "getStores", { code, fail ->
            stateController.errorStateRead(fail)
        }
        ) { data ->
            CustomSingleton.stores =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )
            if (CustomSingleton.stores.isNotEmpty()){
                CustomSingleton.selectedStore = CustomSingleton.stores.first()
            }

            stateController.successState()
        }
    }
    private fun addStore(name: String) {
        stateController.startAud()

        val requestBodyIcon = object : RequestBody() {
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            override fun contentType(): MediaType? {
                return mediaType
            }

            override fun writeTo(sink: BufferedSink) {
                contentResolver.openInputStream(uriLogo.value!!)?.use { input ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        sink.write(buffer, 0, bytesRead)
                    }
                }
            }
        }

        val requestBodyCover= object : RequestBody() {
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            override fun contentType(): MediaType? {
                return mediaType
            }

            override fun writeTo(sink: BufferedSink) {
                contentResolver.openInputStream(uriCover.value!!)?.use { input ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        sink.write(buffer, 0, bytesRead)
                    }
                }
            }
        }


        val body = builderForm3()
            .addFormDataPart("typeId","2")
            .addFormDataPart("name",name)
            .addFormDataPart("logo", "file1.jpg", requestBodyIcon)
            .addFormDataPart("cover", "file2.jpg", requestBodyCover)
            .build()

        requestServer.request2(body,"addStore",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result: Store =  MyJson.IgnoreUnknownKeys.decodeFromString(it)

            CustomSingleton.stores += result
//            homeStorage.setHome(MyJson.IgnoreUnknownKeys.encodeToString(SingletonHome.home.value!!),SingletonStoreConfig.storeId)
            isShowAddCatgory.value = false
            stateController.successStateAUD("تمت الاضافه  بنجاح")
        }
    }
    private fun updateStore(storeId:String,name: String) {
        stateController.startAud()






        val body = builderForm3()
            .addFormDataPart("storeId",storeId)
            .addFormDataPart("typeId","2")
            .addFormDataPart("name",name)

            if (uriLogo.value != null){
                val requestBodyIcon = object : RequestBody() {
                    val mediaType = "image/jpeg".toMediaTypeOrNull()
                    override fun contentType(): MediaType? {
                        return mediaType
                    }

                    override fun writeTo(sink: BufferedSink) {
                        contentResolver.openInputStream(uriLogo.value!!)?.use { input ->
                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                sink.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                }
                   body.addFormDataPart("logo", "file1.jpg", requestBodyIcon)
            }
        if (uriCover.value != null){
            val requestBodyCover= object : RequestBody() {
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            override fun contentType(): MediaType? {
                return mediaType
            }

            override fun writeTo(sink: BufferedSink) {
                contentResolver.openInputStream(uriCover.value!!)?.use { input ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        sink.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
            body.addFormDataPart("cover", "file2.jpg", requestBodyCover)
        }



        requestServer.request2(body.build(),"updateStore",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result: Store =  MyJson.IgnoreUnknownKeys.decodeFromString(it)

            CustomSingleton.stores = CustomSingleton.stores.map {
                if (result.id == it.id){
                    it.copy(
                        name = result.name,
                        logo = result.logo,
                        cover = result.cover,
                    )
                }else
                it
            }
            CustomSingleton.selectedStore = CustomSingleton.stores.find { it.id == CustomSingleton.selectedStore!!.id }
            uriLogo.value = null
            uriCover.value = null
//            homeStorage.setHome(MyJson.IgnoreUnknownKeys.encodeToString(SingletonHome.home.value!!),SingletonStoreConfig.storeId)
            isShowUpdateStore.value = false
            stateController.successStateAUD("تمت   بنجاح")

        }
    }
    private fun deleteStores(ids:List<Int>,onDone:()->Unit) {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("ids",ids.toString())
            .build()

        requestServer.request2(body,"deleteStores",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            onDone()
            stateController.successStateAUD()
        }
    }
    //goto
    private fun gotoLogin() {
        val intent =
            Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
    private fun gotoSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    private fun gotoStoreOrders() {
        val intent = Intent(
            this,
            StoreOrdersActivity::class.java
        )
        startActivity(intent)
    }
    private fun gotoStorePosts() {
        val intent = Intent(
            this,
            StoreOrdersActivity::class.java
        )
        startActivity(intent)
    }
    private fun gotoStoreNotifications() {
        val intent = Intent(
            this,
            StoreNotificationsActivity::class.java
        )
        startActivity(intent)
    }
    private fun gotoStoreDeliveryMen() {
        val intent = Intent(
            this,
            StoreDeliveryMenActivity::class.java
        )
        startActivity(intent)
    }
    private fun gotoStoreSettings(store: Store) {
        val intent = Intent(
            this,
            SettingsStoreActivity::class.java
        )
        intent.putExtra("store", MyJson.MyJson.encodeToString(store))
//        val intent =
//            Intent(this, SettingsStoreActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
//        finish()
    }
    private fun goToStore(store: Store) {
        val intent = Intent(
            this,
            StoreCategoriesActivity::class.java
        )
        intent.putExtra("store", MyJson.MyJson.encodeToString(store))
        startActivity(intent)
    }
    ////Callback
    val getContentlogo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null){
                uriLogo.value = uri
            }
        }
    val getContentCover = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null){
                uriCover.value = uri
            }
        }
}
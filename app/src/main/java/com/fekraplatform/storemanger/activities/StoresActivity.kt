package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.models.Currency
import com.fekraplatform.storemanger.models.CustomOption
import com.fekraplatform.storemanger.models.Store
import com.fekraplatform.storemanger.shared.AToken
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomImageView1
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.storage.MyAppStorage
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink


class StoresActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)
    val isShowAddCatgory = mutableStateOf(false)
    val isShowUpdateStore = mutableStateOf(false)
    private lateinit var storeToUpdate: Store
    var uriLogo=mutableStateOf<Uri?>(null)
    var uriCover =mutableStateOf<Uri?>(null)
    var isHaveMore by mutableStateOf(false)
    var mainCategories by mutableStateOf<List<MainCategory>>(emptyList())

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainInit()

        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {

                    MainCompose1 (
                        0.dp, stateController, this,
                        { mainInit() },
                    ) {
                        LazyColumn(Modifier.safeDrawingPadding()) {
                            stickyHeader {
                                CustomCard2(modifierBox = Modifier){
                                    Column {
                                        DropDownDemo()
                                    }
                                }
                            }
                            if (CustomSingleton.selectedStore!=null){

                                item {
                                    CustomCard2(modifierBox = Modifier) {
                                        CustomImageView1(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .padding(8.dp)
                                                .clickable {

                                                    storeToUpdate = CustomSingleton.selectedStore!!
                                                    isShowUpdateStore.value = true
                                                },
                                            imageUrl = requestServer.serverConfig.getRemoteConfig().BASE_IMAGE_URL+requestServer.serverConfig.getRemoteConfig().SUB_FOLDER_STORE_LOGOS+CustomSingleton.selectedStore!!.logo,
                                        )
                                    }

                                }

                                item {
                                    Button(onClick = {
                                        SelectedStore.store.value = CustomSingleton.selectedStore!!
                                        goToStore(CustomSingleton.selectedStore!!)

                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("عرض المتجر")
                                    }
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
                                        gotoStoreAds()
                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("الاعلانات")
                                    }
                                    Button(onClick = {
                                        SelectedStore.store.value = CustomSingleton.selectedStore!!
                                        gotoStoreDeliveryMen()
                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("الموصلين")
                                    }
                                }
                                item {
                                    Button(
                                        enabled = CustomSingleton.selectedStore!!.app != null,
                                        onClick = {
                                        gotoStoreNotifications()
                                    },
                                        modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("الاشعارات")
                                    }
                                }
                                item {
                                    Button(onClick = {
                                        SelectedStore.store.value = CustomSingleton.selectedStore!!
                                        gotoStoreSettings()
                                    },Modifier.fillMaxWidth().padding(8.dp)) {
                                        Text("اعدادات المتجر")
                                    }
                                }
                            }
                            else if(CustomSingleton.stores.isEmpty()){
                                item {
                                    Text("أنشئ متجرك الالكتروني وابدأ في تحقيق ارباحك")
                                    Button(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        onClick = {
                                        isShowAddCatgory.value = true
                                    }) {
                                        Text("اضافة متجر جديد")
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

        if (mainCategories.isEmpty())readMainCategories()

        ModalBottomSheet(
            onDismissRequest = { isShowAddCatgory.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                var storeName by remember { mutableStateOf("") }
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {

                    item {
                        CustomCard2(modifierBox =  Modifier) {
                            OutlinedTextField(
                                modifier = Modifier.padding(8.dp),
                                value = storeName,
                                onValueChange = {
                                    storeName = it
                                },
                                label = {
                                    Text("اسم المتجر")
                                }
                            )
                            Text("شعار المتجر", modifier = Modifier.padding(12.dp) )
                            Card(
                                Modifier
                                    .size(100.dp)
                                    .padding(8.dp)
                                    .clickable {
                                        getContentlogo.launch("image/*")
                                    }) {
                    //                            if (){
                                if (uriLogo.value != null)
                                    CustomImageViewUri(
                                        modifier =  Modifier.fillParentMaxSize(),
                                        imageUrl =  uriLogo.value!!,
                                        contentScale = ContentScale.Inside
                                    )else{
                                    CustomImageViewUri(
                                        imageUrl =  R.drawable.baseline_add_card_24,
                                        contentScale = ContentScale.Crop
                                    )
                                }
                    //                            }
                            }

                            Text("صورة غلاف المتجر", modifier = Modifier.padding(12.dp) )
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(8.dp)
                                    .clickable {
                                        getContentCover.launch("image/*")
                                    }) {

                                if (uriCover.value != null){
                                    CustomImageViewUri(
                                        modifier =  Modifier.fillParentMaxSize(),
                                        imageUrl =  uriCover.value!! ,
                                        contentScale = ContentScale.Inside
                                    )
                                }else{
                                    CustomImageViewUri(
                                        imageUrl =  R.drawable.baseline_add_card_24,
                                        contentScale = ContentScale.Crop
                                    )
                                }

                    //
                            }

                            val radioOptions = listOf(
                                CustomOption(1,"متجر مشترك"),
                                CustomOption(2,"متجر مخصص"),
                            )
                            var selectedOption by remember { mutableStateOf<CustomOption?>(null) }

                            Text("نوع المتجر", modifier = Modifier.padding(14.dp))
                            radioOptions.forEach { text ->
                                Row(
                                    Modifier.fillMaxWidth().height(56.dp)
                                        .selectable(
                                            selected = (text == selectedOption),
                                            onClick = {
                                                selectedOption = text
                    //                                    if (selectedOption.id == 2 ){
                    ////                                        selectedLocation = null
                    //                                    }
                    //
                    //                                    onOptionSelected(text)

                                            },
                                            role = Role.RadioButton
                                        ).padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                )
                                {
                                    RadioButton(selected = (text == selectedOption), onClick = null)
                                    Text(text = text.name,style = MaterialTheme. typography. bodyLarge,modifier = Modifier. padding(start = 16.dp))
                                }
                            }
                            Text("اختر الفئة الملائمة لمتجرك ")
                            var selectedCategory by remember { mutableStateOf<MainCategory?>(null) }
                            LazyHorizontalGrid (
                                rows = GridCells.Fixed(2),
                                modifier = Modifier
                                    .height(220.dp)
                                    .padding(8.dp)
                                    .background(Color.White)
                            ) {

                                itemsIndexed(mainCategories){index: Int, item: MainCategory ->
                                    Column (horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                                        .padding(2.dp)
                                        .width(100.dp)
                                        .background(if (selectedCategory == item) Color.Gray else Color.Transparent)
                                        .clickable { selectedCategory = item }){
                                        CustomImageView1(
                                            modifier = Modifier
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.primary, CircleShape
                                                )
                                                .clip(CircleShape)
                                                .size(50.dp)
                                            ,
                                            imageUrl = item.image,
                                            contentScale = ContentScale.Crop
                                        )
                                        Text(item.name, textAlign = TextAlign.Center, fontSize = 12.sp , overflow = TextOverflow.Ellipsis, softWrap = true, modifier = Modifier.height(50.dp))

                                    }
                                }
                            }

                            Button(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                enabled = storeName.length > 5 && uriLogo.value != null && uriCover.value != null && selectedOption != null && selectedCategory != null ,
                                onClick = {
                                    addStore(storeName,selectedOption!!.id.toString(),selectedCategory!!.id.toString())
                                }) {
                                Text("اضافة")
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
                            gotoAddStore()
//                            isShowAddCatgory.value = true
                        })
                }
            }

        }
    }

    //// Logic Function
    private fun read() {
        stateController.startRead()

        val body = builderForm2()
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
    private fun readMainCategories() {
        stateController.startAud()

//        val requestBodyIcon = object : RequestBody() {
//            val mediaType = "image/jpeg".toMediaTypeOrNull()
//            override fun contentType(): MediaType? {
//                return mediaType
//            }
//
//            override fun writeTo(sink: BufferedSink) {
//                contentResolver.openInputStream(uriLogo.value!!)?.use { input ->
//                    val buffer = ByteArray(4096)
//                    var bytesRead: Int
//                    while (input.read(buffer).also { bytesRead = it } != -1) {
//                        sink.write(buffer, 0, bytesRead)
//                    }
//                }
//            }
//        }

//        val requestBodyCover= object : RequestBody() {
//            val mediaType = "image/jpeg".toMediaTypeOrNull()
//            override fun contentType(): MediaType? {
//                return mediaType
//            }
//
//            override fun writeTo(sink: BufferedSink) {
//                contentResolver.openInputStream(uriCover.value!!)?.use { input ->
//                    val buffer = ByteArray(4096)
//                    var bytesRead: Int
//                    while (input.read(buffer).also { bytesRead = it } != -1) {
//                        sink.write(buffer, 0, bytesRead)
//                    }
//                }
//            }
//        }


        val body = builderForm2()
//            .addFormDataPart("typeId",storeTypeId)
//            .addFormDataPart("name",name)
//            .addFormDataPart("logo", "file1.jpg", requestBodyIcon)
//            .addFormDataPart("cover", "file2.jpg", requestBodyCover)
            .build()

        requestServer.request2(body,"getMainCategories",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
           mainCategories =  MyJson.IgnoreUnknownKeys.decodeFromString(it)

//            CustomSingleton.stores += result
//            CustomSingleton.selectedStore = result
//            homeStorage.setHome(MyJson.IgnoreUnknownKeys.encodeToString(SingletonHome.home.value!!),SingletonStoreConfig.storeId)
//            isShowAddCatgory.value = false
            stateController.successStateAUD()
        }
    }
    private fun addStore(name: String,storeTypeId:String,mainCategoryId: String) {
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


        val body = builderForm2()
            .addFormDataPart("typeId",storeTypeId)
            .addFormDataPart("name",name)
            .addFormDataPart("logo", "file1.jpg", requestBodyIcon)
            .addFormDataPart("cover", "file2.jpg", requestBodyCover)
            .addFormDataPart("mainCategoryId", mainCategoryId)
            .build()

        requestServer.request2(body,"addStore",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result: Store =  MyJson.IgnoreUnknownKeys.decodeFromString(it)

            CustomSingleton.stores += result
            CustomSingleton.selectedStore = result
//            homeStorage.setHome(MyJson.IgnoreUnknownKeys.encodeToString(SingletonHome.home.value!!),SingletonStoreConfig.storeId)
            isShowAddCatgory.value = false
            stateController.successStateAUD("تمت الاضافه  بنجاح")
        }
    }
    private fun updateStore(storeId:String,name: String) {
        stateController.startAud()






        val body = builderForm2()
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
    private fun gotoAddStore() {
        val intent = Intent(this, AddStoreActivity::class.java)
        startActivity(intent)
    }
    private fun gotoStoreOrders() {
        val intent = Intent(
            this,
            StoreOrdersActivity::class.java
        )
        startActivity(intent)
    }
    private fun gotoStoreAds() {
        val intent = Intent(this, AdsActivity::class.java)
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
    private fun gotoStoreSettings() {
        val intent = Intent(
            this,
            SettingsStoreActivity::class.java
        )
        startActivity(intent)
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

    ///
    private fun mainInit() {
        val myAppStorage = MyAppStorage()
        Log.e("mm",myAppStorage.getLang().code)
        if (myAppStorage.getLang().code != getAppLanguage(this) && myAppStorage.getLang().name.isEmpty() ){
            setLocale(this,myAppStorage.getLang().code)
//            recreate()
        }else{
            setLocale(this,myAppStorage.getLang().code)
        }
        if (!requestServer.serverConfig.isSetSubscribeApp()) {
            subscribeToAppTopic()
        }
        if (!requestServer.serverConfig.isSetRemoteConfig()) {
            stateController.startRead()
            requestServer.initVarConfig({
                stateController.errorStateRead("enable get remote config")
            }) {
                CustomSingleton.remoteConfig = requestServer.serverConfig.getRemoteConfig()
                checkTokenToRead()
            }
        } else {
            CustomSingleton.remoteConfig = requestServer.serverConfig.getRemoteConfig()
            checkTokenToRead()
        }
    }
    private fun subscribeToAppTopic() {
        val appId = "1"
        Firebase.messaging.subscribeToTopic(appId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    requestServer.serverConfig.setSubscribeApp(appId)
                    Log.e("subsecribed",appId)
                }
            }
    }
    private fun checkTokenToRead() {
        if (!AToken().isSetAccessToken()) {
            gotoLogin()
        } else {
            read()
        }
    }
}

@Serializable
data class RemoteConfigModel(
    val TYPE_STORE_MANAGER: String,
    val SUB_FOLDER_STORE_COVERS: String,
    val SUB_FOLDER_PRODUCT: String,
    val BASE_IMAGE_URL: String,
    val BASE_URL: String,
    val SUB_FOLDER_STORE_LOGOS: String,
    val SUB_FOLDER_USERS_LOGOS: String,
    val VERSION:String = "v1"
)

@Serializable
data class MainCategory(val id:Int,val name :String,val image:String)


@Serializable
data class MainData(val mainCategories:List<MainCategory>,val currencies :List<Currency>)

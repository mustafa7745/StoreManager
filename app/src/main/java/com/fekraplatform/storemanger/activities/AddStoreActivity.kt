package com.fekraplatform.storemanger.activities

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.models.Currency
import com.fekraplatform.storemanger.models.CustomOption
import com.fekraplatform.storemanger.models.MainCategory
import com.fekraplatform.storemanger.models.MainData
import com.fekraplatform.storemanger.models.PageModel
import com.fekraplatform.storemanger.models.Store
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomImageView1
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.CustomRow2
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PinConfig
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink

class AddStoreActivity : ComponentActivity() {
    val requestServer = RequestServer(this)
    val stateController = StateController()
    val myLocationManager = MyLocationManager(this)
    var isCurrentLocation by mutableStateOf(true)
    val pages = listOf(
        PageModel("",0),
        PageModel("موقع المتجر على الخريطه",1),
        )
    var page by mutableStateOf(pages.first())

    var uriLogo=mutableStateOf<Uri?>(null)
    var uriCover =mutableStateOf<Uri?>(null)

    var  mainCategories by mutableStateOf<List<MainCategory>>(emptyList())
    var  currencies by mutableStateOf<List<Currency>>(emptyList())
    var storeLocation by mutableStateOf<LatLng?>(null)

    var storeName by mutableStateOf("")
    var selectedCategory by  mutableStateOf<MainCategory?>(null)
    var selectedOption by  mutableStateOf<CustomOption?>(null)
    var selectedCurrency by  mutableStateOf<Currency?>(null)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateController.startRead()
        myLocationManager.initLocation{
            readMainCategories()
        }

//        stateController.startRead()
//        initLocation()
//        getCurrentLocation{
////            Log.e("ffffdf567",it.toString())
////            readMainCategories()
//        }
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme  {
//                if (myLocationManager.isLoading){
//                    stateController.startRead()
//                }else{
//                    if (myLocationManager.isSuccess){
//                        stateController.successStateAUD()
//                        stateController.successState()
//                    }else{
//                        stateController.errorStateAUD(myLocationManager.messageLocation)
//                        stateController.errorStateRead(myLocationManager.messageLocation)
//                    }
//                }
                BackHand()


                    MainCompose1(0.dp,stateController,this@AddStoreActivity,{
                        myLocationManager.initLocation{
                            if (mainCategories.isEmpty())
                                readMainCategories()
                        }

                    }) {
                        Column(Modifier.safeDrawingPadding()) {
                            CustomCard2(modifierBox = Modifier) {
                                CustomRow2 {
                                    CustomIcon(Icons.AutoMirrored.Default.ArrowBack, border = true) {
                                        backHandler()
                                    }
                                    Row {
                                        Text("اضافة متجر جديد")
                                        if (page != pages.first()){
                                            Text(" | ")
                                            Text(page.pageName)
                                        }
                                    }
                                }
                            }

                        if (myLocationManager.location != null){
                            if (page.pageId == 0)
                                SettingsList()
                            if (page.pageId == 1)
                                ComposeMapp()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ComposeMapp() {
        var location = myLocationManager.location!!
        val markerState = rememberMarkerState(position = location)

        var cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(location, 16f)
        }

        val pinConfig = PinConfig.builder().build()

// Checking if camera is moving and editMode is enabled
        if (cameraPositionState.isMoving && !isCurrentLocation) {
            // Update location based on camera's current target position
            val updatedLatLng = LatLng(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude
            )

            // Set new position to markerState and location
            location = updatedLatLng
            markerState.position = updatedLatLng
        }

        Button(
            onClick = {
                storeLocation = LatLng(cameraPositionState.position.target.latitude, cameraPositionState.position.target.longitude)
             page = pages[0]

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "حفظ", color = Color.White, fontSize = 18.sp)
        }




        Box(
            Modifier.fillMaxSize(),
        ) {
            GoogleMap(
                Modifier.fillMaxWidth().height(400.dp).align(Alignment.TopCenter),
                cameraPositionState = cameraPositionState
            ) {
                AdvancedMarker(
                    state = markerState,
                    pinConfig = pinConfig
                )
            }
            Column (Modifier.fillMaxWidth().align(Alignment.TopCenter)){


                if (!isCurrentLocation){
                    Text(modifier = Modifier.fillMaxWidth().background(Color.LightGray), textAlign = TextAlign.Center, text = "قم بتحريك المؤشر للموقع الذي تريد")
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        modifier =
                        Modifier.weight(1f)
                            .padding(8.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            isCurrentLocation = true
                            stateController.startAud()
                            myLocationManager.initLocation()
                            cameraPositionState.move(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(location, 16f)))
                            markerState.position = myLocationManager.location!!
                        }
                    ) {
                        Row {
                            Text("الموقع الحالي")
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = "",
                                tint = Color.White
                            )
                        }

                    }
                    Button(
                        modifier =
                        Modifier.weight(1f)
                            .padding(8.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            isCurrentLocation = false
//                    stateController.successStateAUD("")
//                    stateController.startAud()
//                    myLocationManager.initLocation()
//                    cameraPositionState.move(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(location, 16f)))
//                    markerState.position = myLocationManager.location!!
                        }
                    ) {
                        Row {
                            Text("موقع اخر")
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = "",
                                tint = Color.White
                            )
                        }

                    }
                }
            }
        }
    }
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun SettingsList() {

        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {

            stickyHeader {

                Row (Modifier.fillMaxWidth().background(Color.White)) {
                    Button(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        enabled = storeName.length > 5 && uriLogo.value != null && uriCover.value != null && selectedOption != null && selectedCategory != null && storeLocation != null && selectedCurrency != null,
                        onClick = {
                            addStore(storeName,selectedOption!!.id.toString(),selectedCategory!!.id.toString(),selectedCurrency!!.id.toString())
                        }) {
                        Text("اضافة")
                    }
                }

            }

            item {
                CustomCard2(modifierBox = Modifier) {
                    Column(Modifier.selectableGroup()) {
                        Text("بيانات الموقع", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                        AddItem ("موقع المتجر على الخريطه",storeLocation != null){page = pages[1] }
                    }
                }
            }

            item {
                CustomCard2(modifierBox =  Modifier) {

                    val isNameTooShort = storeName.length < 10

                    Column(modifier = Modifier.padding(8.dp)) {
                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("اسم المتجر") },
                            isError = isNameTooShort, // ✅ يجعل الحدود حمراء إذا هناك خطأ
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        if (isNameTooShort) {
                            Text(
                                text = "يجب أن يكون اسم المتجر 10 أحرف أو أكثر",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }

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


                    CustomCard2(modifierBox = Modifier) {
                        Column(Modifier.selectableGroup()) {
                            Text("اختر نوع المتجر", modifier = Modifier.padding(14.dp))
                            radioOptions.forEach { text ->
                                Row(
                                    Modifier.fillMaxWidth().height(56.dp)
                                        .selectable(
                                            selected = (text == selectedOption),
                                            onClick = {

                                                selectedCategory = null
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
                        }
                    }
//                    Text("نوع المتجر", modifier = Modifier.padding(14.dp))

                    CustomCard2(modifierBox = Modifier) {
                        Column(Modifier.selectableGroup()) {
                            Text("اختر عملة المتجر", modifier = Modifier.padding(14.dp))
                            currencies.forEach { text ->
                                Row(
                                    Modifier.fillMaxWidth().height(56.dp)
                                        .selectable(
                                            selected = (text == selectedCurrency),
                                            onClick = {
//                                            if (selectedOption.id == 2 ){
//                                                selectedLocation = null
//                                            }
                                                selectedCurrency = text
                                            },
                                            role = Role.RadioButton
                                        ).padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                )
                                {
                                    RadioButton(selected = (text == selectedCurrency), onClick = null)
                                    Text(text = text.name,style = MaterialTheme. typography. bodyLarge,modifier = Modifier. padding(start = 16.dp))
                                }
                            }
                        }
                    }
//                    Button(
//                        modifier = Modifier.fillMaxWidth().padding(8.dp) .background(if(storeLocation != null) Color.Gray else Color.Magenta),
//                        onClick = {
//                            page = pages[1]
////                        }
////                            addStore(storeName,selectedOption!!.id.toString(),selectedCategory!!.id.toString())
//                        }) {
//                        Text("اختر الموقع الجغرافي لمتجرك")
//                    }
                    Text("اختر الفئة الملائمة لمتجرك ")

                    LazyHorizontalGrid (
                        rows = GridCells.Fixed(2),
                        modifier = Modifier
                            .height(220.dp)
                            .padding(8.dp)
                            .background(Color.White)
                    ) {
                        itemsIndexed(    if ((selectedOption?.id ?: -1) == 1)
                            mainCategories.filter { it.sharedableStores.isNotEmpty() }
                        else
                            mainCategories ){ index: Int, item: MainCategory ->
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
                }
            }

        }
    }
    @Composable
    private fun BackHand() {
        BackHandler {
            backHandler()
        }
    }
    private fun backHandler() {
        if (page.pageId != 0) {
            page = pages.first()
        } else
            finish()
    }

    val getContentlogo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null){
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val sizeInBytes = inputStream?.available() ?: 0
                val sizeInKB = sizeInBytes / 1024
                if (sizeInKB > 100) {
                    stateController.errorStateAUD("حجم الصورة يجب أن يكون أقل من 100 كيلوبايت")
                    uriLogo.value = null
                }else{
                    uriLogo.value = uri
                }
            } catch (e: Exception) {
                stateController.errorStateAUD("فشل في تحميل الصورة: ${e.message}")
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
                    stateController.errorStateAUD("حجم الصورة يجب أن يكون أقل من 100 كيلوبايت")
                    uriCover.value = null
                }else{
                    uriCover.value = uri
                }
            } catch (e: Exception) {
                stateController.errorStateAUD("فشل في تحميل الصورة: ${e.message}")
            }
        }
    }
    private fun addStore(name: String,storeTypeId:String,mainCategoryId: String,currenctId:String) {
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
            .addFormDataPart("latitude", storeLocation!!.latitude.toString())
            .addFormDataPart("longitude", storeLocation!!.longitude.toString())
            .addFormDataPart("currencyId", currenctId)
            .build()

        requestServer.request2(body,"addStore",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result: Store =  MyJson.IgnoreUnknownKeys.decodeFromString(it)

            CustomSingleton.stores += result
            CustomSingleton.selectedStore = result

            stateController.successStateAUD("تمت الاضافه  بنجاح")
            finish()
        }
    }

    private fun readMainCategories() {
        stateController.startRead()
        val body = builderForm2()
            .build()

        requestServer.request2(body,"getMainData1",{code,fail->
            stateController.errorStateRead(fail)
        }
        ){it->
            val result: MainData =  MyJson.IgnoreUnknownKeys.decodeFromString(it)

            mainCategories = result.mainCategories
            currencies = result.currencies
            stateController.successState()
        }
    }

    @Composable
    private fun AddItem(text:String , isDone : Boolean,onClick:()-> Unit) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable {
                    onClick()
                }
                .border( 1.dp,if (isDone) Color. Green else Color.Transparent,)
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

}
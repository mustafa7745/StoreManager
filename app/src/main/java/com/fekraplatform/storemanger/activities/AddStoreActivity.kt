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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.models.Currency
import com.fekraplatform.storemanger.models.CustomOption
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

    val pages = listOf(
        PageModel("",0),
        PageModel("اختيار موقع المتجر",1),
        )
    var page by mutableStateOf(pages.first())

    var uriLogo=mutableStateOf<Uri?>(null)
    var uriCover =mutableStateOf<Uri?>(null)

    var  mainCategories by mutableStateOf<List<MainCategory>>(emptyList())
    var  currencies by mutableStateOf<List<Currency>>(emptyList())
    var storeLocation by mutableStateOf<LatLng?>(null)
    val pinConfig = PinConfig.builder().build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateController.startRead()
        initLocation()
        getCurrentLocation{
//            Log.e("ffffdf567",it.toString())
//            readMainCategories()
        }
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme  {
                BackHand()
                if (location != null)
                    LaunchedEffect(1) {
                        readMainCategories()
                    }
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
                    MainCompose1(0.dp,stateController,this@AddStoreActivity,{
                            getCurrentLocation{
                            readMainCategories()
                        }
                    }) {
                        if (page.pageId == 0)
                            SettingsList()
                        if (page.pageId == 1)
                            ComposeMapp()
                    }
                }
            }
        }
    }

    @Composable
    private fun ComposeMapp() {
        var mapLocation = location!!
        val markerState = rememberMarkerState(position = mapLocation)
        var cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(mapLocation, 16f)
        }


        if (cameraPositionState.isMoving) {
            mapLocation = LatLng(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude
            )
            markerState.position = LatLng(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude
            )
        }

        Box(
            Modifier.fillMaxSize(),
        ) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                AdvancedMarker(
                    state = markerState,
                    pinConfig = pinConfig
                )
            }
//            Button(
//                onClick = {
//                    isSet1 = 1
//                },
//                modifier = Modifier
//                    .align(Alignment.TopStart)
//                    .padding(8.dp)
//                    .height(100.dp),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text(
//                    text = "LatLng1" + (latLong1?.latitude ?: "0") + "," + (latLong1?.longitude
//                        ?: "0"), color = Color.White, fontSize = 8.sp
//                )
//            }
//            Button(
//                onClick = {
//
//                    isSet1 = 2
//                },
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .padding(8.dp)
//                    .height(100.dp),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text(
//                    text = "LatLng2" + (latLong2?.latitude ?: "0") + "," + (latLong2?.longitude
//                        ?: "0"), color = Color.White, fontSize = 8.sp
//                )
//            }
//            Text(if (isSet1 == 1) "1" else "2", Modifier.align(Alignment.TopCenter))


            Button(
                onClick = {

                    storeLocation = LatLng(cameraPositionState.position.target.latitude,cameraPositionState.position.target.longitude)
                    Log.e("ffdfder", storeLocation.toString())
                    page = pages.first()
//                    long.value = cameraPositionState.position.target.longitude
//                    lat.value = cameraPositionState.position.target.latitude
//
//                    Log.e("lat", lat.value.toString())
//                    Log.e("long", long.value.toString())
//                    val l = LatLng(lat.value, long.value)
//                    if (isSet1 == 1) {
//                        latLong1 = l
//                    } else {
//                        latLong2 = l
//                    }
//
//                    if (latLong1 != null && latLong2 != null) {
//                        val result = isPointWithinCircle(latLong1!!, latLong2!!, 100.0)
//                        if (result) {
//                            println("The target point is within 100 meters of the center.")
//                        } else {
//                            println("The target point is outside the 100-meter radius of the center.")
//                        }
//                    }


                    //                            fetchStreetNameFromCoordinates(LatLng(lat.value,long.value),{})
                    //                            long.value = cameraPositionState.position.target.longitude.toString()
                    //                            lat.value = cameraPositionState.position.target.latitude.toString()
                    //
                    //                            val data1 = Intent()
                    //                            data1.putExtra("lat",lat.value)
                    //                            data1.putExtra("long",long.value)
                    //                            setResult(RESULT_OK,data1)
                    //                            finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .height(70.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "حفظ", color = Color.White, fontSize = 18.sp)
                //                            ,fontFamily = FontFamily(
                ////                            Font(R.font.bukra_bold)
                //
                //                        ))
            }
        }
    }
    @Composable
    private fun SettingsList() {
        var storeName by remember { mutableStateOf("") }
        var selectedCurrency by remember { mutableStateOf<Currency?>(null) }
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
                    Button(
                        modifier = Modifier.fillMaxWidth().padding(8.dp) .background(if(storeLocation != null) Color.Gray else Color.Magenta),
                        onClick = {
                            page = pages[1]
//                        }
//                            addStore(storeName,selectedOption!!.id.toString(),selectedCategory!!.id.toString())
                        }) {
                        Text("اختر الموقع الجغرافي لمتجرك")
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
                        enabled = storeName.length > 5 && uriLogo.value != null && uriCover.value != null && selectedOption != null && selectedCategory != null && storeLocation != null && selectedCurrency != null,
                        onClick = {
                            addStore(storeName,selectedOption!!.id.toString(),selectedCategory!!.id.toString(),selectedCurrency!!.id.toString())
                        }) {
                        Text("اضافة")
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
            uriLogo.value = uri
        }
    }
    val getContentCover = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null){
            uriCover.value = uri
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
            val result:MainData =  MyJson.IgnoreUnknownKeys.decodeFromString(it)

            mainCategories = result.mainCategories
            currencies = result.currencies
            stateController.successState()
        }
    }


    /// GPS FUNCTIONS AND CALLBACKES AND VARS
    // 1) VARS
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var settingsClient: SettingsClient
    private var isGpsEnabled by mutableStateOf(false)
    private var isLoadingStateLocation by mutableStateOf<Boolean>(false)
    private var isSuccessStateLocation by mutableStateOf<Boolean?>(null)
    private var messageLocation by mutableStateOf("للحصول على تجربة مميزة فعل الموقع")
    var location by mutableStateOf<LatLng?>(null)
    // 2) Functions
    private fun requestPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    private fun getCurrentLocation(onSuccess:(LatLng)->Unit = {}) {
        if (!isGpsEnabled){
            Log.e("f2",isGpsEnabled.toString())
            requestEnableGPS()
            Log.e("f3",isGpsEnabled.toString())
            return
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
            return
        }
        Log.e("sddd3", "11")

        fusedLocationClient. requestLocationUpdates(locationRequest,locationCallback{
            isSuccessStateLocation = true
            Log.e("ffffdf2",location.toString())
            onSuccess(it)
        }, null)
//        GlobalScope.launch {
//            fusedLocationClient.lastLocation
//                .addOnSuccessListener { l ->
//                    Log.e("sddd3", "55")
//                    if (l != null) {
//                        Log.e("sddd3", "669")
//                        Log.e("loc", location.toString())
//                        Log.e("sddd3", "669")
//                        location = LatLng(l.latitude, l.longitude)
//                        onSuccess(location!!)
//                        isSuccessStateLocation = true
//                    } else {
//                        messageLocation = "Unable to get location Try Again"
//                        // Handle the case where the location is not available
////                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                .addOnFailureListener {
//                    // Handle failure in location retrieval
////                MyToast(this,"Failed to get location")
//                }
//        }



    }
    private fun requestEnableGPS() {
        // Create a LocationSettingsRequest to check GPS and other location settings
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
        // Check the settings
        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(locationSettingsRequest)
        task.addOnSuccessListener(this, OnSuccessListener<LocationSettingsResponse> {
            // If GPS is enabled, proceed to get the current location
            Log.d("GPS", "GPS is enabled.")
            isGpsEnabled = true
            getCurrentLocation()
        })


        task.addOnFailureListener { exception ->

            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution)
                            .build()//Create the request prompt
                    gpsActivityResultLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }


    }
    private fun initLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        settingsClient = LocationServices.getSettingsClient(this)
        locationRequest =
            LocationRequest.Builder(10000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
    }
    // 3)Callbacks
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            stateController.errorStateRead( "Permission GPS denied")
        }
    }
    private val gpsActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.e("result ", result.toString())
            isGpsEnabled = true
            getCurrentLocation()
        }else{
            stateController.errorStateRead("يجب تفعيل ال GPS")
        }
    }
    private fun locationCallback(onSuccess: (LatLng) -> Unit): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val locations = locationResult.locations
                for (l in locations) {

                    // Pass each location to the provided callback

                    location = LatLng(l.latitude, l.longitude)
                    onSuccess(location!!)
                    Log.e("ffffdf",location.toString())
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }
    }
}
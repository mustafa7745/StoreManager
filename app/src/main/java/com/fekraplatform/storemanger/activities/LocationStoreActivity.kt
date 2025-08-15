package com.fekraplatform.storemanger.activities

import android.Manifest
import android.app.Activity
import android.app.Activity.LOCATION_SERVICE
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomIcon2
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.MyHeader
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.convertStringToLatLng
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
import okhttp3.MultipartBody


class LocationStoreActivity : ComponentActivity() {
    val requestServer = RequestServer(this)
    private val stateController = StateController()
    //
    val myLocationManager = MyLocationManager(this)
    var street by mutableStateOf("")
    var isCurrentLocation by mutableStateOf(true)

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myLocationManager.initLocation()

        setContent {
            StoreMangerTheme {
                if (myLocationManager.isLoading){
                    stateController.startRead()
                }else{
                    if (myLocationManager.isSuccess){
                        stateController.successStateAUD()
                        stateController.successState()
                    }else{
                        stateController.errorStateAUD(myLocationManager.messageLocation)
                        stateController.errorStateRead(myLocationManager.messageLocation)
                    }
                }

                MainCompose1(0.dp,stateController,this,{
                    myLocationManager.initLocation()
                }){
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally) {


                        MyHeader({
                            finish()
                        },{

                        }) {
                            Text("اضافة موقع للمتجر")
                        }

                        ComposeMapp()
                    }
                }

            }
        }

    }

    //    var markerState by mutableStateOf(MarkerState(location, "Initial Marker"))
    @Composable
    private fun ComposeMapp() {
        var location = if (SelectedStore.store.value!!.latLng == null) myLocationManager.location!! else  convertStringToLatLng(SelectedStore.store.value!!.latLng!!)!!
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
                updateLocation(cameraPositionState.position.target.latitude.toString(), cameraPositionState.position.target.longitude.toString())

//                addLocation( cameraPositionState.position.target.latitude.toString()+ "," + cameraPositionState.position.target.longitude.toString(),)
//                        updateLocation()
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

    fun updateLocation(lat:String,long:String) {
        val latLong = "$lat,$long"
        stateController.startAud()
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("latLng",latLong)
            .addFormDataPart("storeId", SelectedStore.store.value!!.id.toString())
            .addFormDataPart("latitude", lat)
            .addFormDataPart("longitude",long)
            .build()

        requestServer.request2(body, "updateStoreLocation", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
//            val result: Store =
//                MyJson.IgnoreUnknownKeys.decodeFromString(
//                    data
//                )
            SelectedStore.store.value!! .latLng = latLong


            stateController.successStateAUD("تم بنجاح")
            finish()
        }
    }

}

class MyLocationManager(private val activity: ComponentActivity){
    private var countReShow = 0;
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var settingsClient: SettingsClient
    private var isGpsEnabled by mutableStateOf(false)
    var isLoading by mutableStateOf<Boolean>(false)
    var isSuccess by mutableStateOf<Boolean>(false)
    var messageLocation by mutableStateOf("للحصول على تجربة مميزة فعل الموقع")
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
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
            return
        }
        Log.e("sddd3", "11")

        fusedLocationClient. requestLocationUpdates(locationRequest,locationCallback{
//            CustomSingleton.location = it
            isSuccess = true
            onSuccess(it)
            isLoading = false
            isSuccess = true
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
        task.addOnSuccessListener(activity, OnSuccessListener<LocationSettingsResponse> {
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
    fun initLocation(onSuccess: () -> Unit = {}) {
        isLoading = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        val locationManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        settingsClient = LocationServices.getSettingsClient(activity)
        locationRequest = LocationRequest.Builder(10000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
        getCurrentLocation{
            onSuccess()
        }
    }
    private val requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            val message = "Permission Denied"
            messageLocation = message
            Toast.makeText(activity,message,Toast.LENGTH_SHORT)
            isLoading = false
            isSuccess = false
        }
    }
    private val gpsActivityResultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.e("result ", result.toString())
            isGpsEnabled = true
            getCurrentLocation()
        }else{
            countReShow ++
            if (countReShow <2){
                getCurrentLocation()
            }else{
                val message = "يجب تفعيل ال GPS"
                messageLocation = message
                Toast.makeText(activity,message,Toast.LENGTH_SHORT)
                isLoading = false
                isSuccess = false
            }
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
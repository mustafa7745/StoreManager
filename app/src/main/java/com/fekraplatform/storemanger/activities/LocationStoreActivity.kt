package com.fekraplatform.storemanger.activities

import android.Manifest
import android.app.Activity
import android.content.Context
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomIcon2
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.MyToast
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
    val stateController = StateController()
    val requestServer = RequestServer(this)
//    lateinit var la: Store
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    private lateinit var settingsClient: SettingsClient
    var isGpsEnabled by mutableStateOf(false)
    var location by mutableStateOf<LatLng?>(null)
    var editMode by mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stateController.startRead()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        settingsClient = LocationServices.getSettingsClient(this)
        locationRequest = LocationRequest.Builder(10000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
        ///
        getCurrentLocation{
            Log.e("ffffff",location.toString(),)
            Log.e("selected",CustomSingleton.selectedStore.toString(),)
            location = convertStringToLatLng( if (CustomSingleton.selectedStore!!.latLng != null)  CustomSingleton.selectedStore!!.latLng!! else "0.0,0.0" )
//            location = latLng
            Log.e("llllll",location.toString(),)
        }

        setContent {
            StoreMangerTheme {
                MainCompose2(
                    0.dp, stateController, this,

                ) {
                    Column (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        if (location != null){
                            Row {
                                Button(onClick = {

                                    editMode = !editMode
                                }) {
                                    Text(if (editMode) "view mode" else "editMode")
                                }
                                if (editMode) Button(onClick = {
                                    updateLocation()
                                }) { Text("save") }
                            }

                            HorizontalDivider()
                            ComposeMapp()
                        }
//                        item {


//                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ComposeMapp() {
//        var location = LatLng(lat, long)
        val markerState = rememberMarkerState(position = location!!)
        markerState.position = location!!

        var cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(location!!, 17f)
        }

        val pinConfig = PinConfig.builder().build()

// Checking if camera is moving and editMode is enabled
        if (cameraPositionState.isMoving) {
            // Update location based on camera's current target position
            val updatedLatLng = LatLng(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude
            )

            // Set new position to markerState and location
            location = updatedLatLng
            markerState.position = updatedLatLng
        }




        Box(
            Modifier.fillMaxWidth().height(400.dp),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxWidth().fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                AdvancedMarker(
                    state = markerState,
                    pinConfig = pinConfig
                )
            }


            CustomIcon2(Icons.Outlined.Place, modifierIcon = Modifier.align(Alignment.TopEnd).padding(8.dp),border = true) {
                getCurrentLocation{ ll->
                    markerState.position = ll
                    val cameraUpdate = CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(location!!, 17f))
                    cameraPositionState.move(cameraUpdate)
                }
            }
        }
    }

//    @Composable
//    private fun ComposeMapp() {
////        var location = LatLng(lat.value, long.value)
////        val markerState = rememberMarkerState(position = location)
////        var cameraPositionState = rememberCameraPositionState {
////            position = CameraPosition.fromLatLngZoom(location, 16f)
////        }
////        val pinConfig = PinConfig.builder().build()
////
////
////        if (cameraPositionState.isMoving && editMode) {
////
////
////            location = LatLng(
////                cameraPositionState.position.target.latitude,
////                cameraPositionState.position.target.longitude
////            )
////            markerState.position = LatLng(
////                cameraPositionState.position.target.latitude,
////                cameraPositionState.position.target.longitude
////            )
////            lat.value =  cameraPositionState.position.target.latitude
////            long.value =    cameraPositionState.position.target.longitude
////        }
//        var location = LatLng(lat.value, long.value)
//
//        val markerState = rememberMarkerState(position = location)
//
//        var cameraPositionState = rememberCameraPositionState {
//            position = CameraPosition.fromLatLngZoom(location, 16f)
//        }
//
//        val pinConfig = PinConfig.builder().build()
//
//// Checking if camera is moving and editMode is enabled
//        if (cameraPositionState.isMoving && editMode) {
//            // Update location based on camera's current target position
//            val updatedLatLng = LatLng(
//                cameraPositionState.position.target.latitude,
//                cameraPositionState.position.target.longitude
//            )
//
//            // Set new position to markerState and location
//            location = updatedLatLng
//            markerState.position = updatedLatLng
//
//            // Update the lat and long state variables
//            lat.value = updatedLatLng.latitude
//            long.value = updatedLatLng.longitude
//        }
//
//
//        GoogleMap(
//            modifier = Modifier.fillMaxWidth().height(400.dp),
//            cameraPositionState = cameraPositionState
//        ) {
//            AdvancedMarker(
//                state = markerState,
//                pinConfig = pinConfig
//            )
//        }
//
//        Box(
//            Modifier.fillMaxSize(),
//        ) {
//            if (editMode)
//            Button(
//                onClick = {
//updateLocation()
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(8.dp)
//                    .height(100.dp),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text(text = "حفظ", color = Color.White, fontSize = 18.sp)
//                //                            ,fontFamily = FontFamily(
//                ////                            Font(R.font.bukra_bold)
//                //
//                //                        ))
//            }
//        }
//    }
    private fun requestPermissions() {
    // Launch the request permission dialog
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, now we can get the location
                getCurrentLocation()
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(
                    this,
                    "Location permission is required to fetch country name",
                    Toast.LENGTH_SHORT
                ).show()
            }
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

//        fusedLocationClient. requestLocationUpdates(locationRequest,locationCallback{
//            stateController.successState()
//            onSuccess(it)
//        }, null)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { l ->
                if (l != null) {
                    location = LatLng(l.latitude, l.longitude)
                    stateController.successState()
                    onSuccess(location!!)
                } else {
                    stateController.errorStateRead("Unable to get location Try Again")
                    // Handle the case where the location is not available
//                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                // Handle failure in location retrieval
//                MyToast(this,"Failed to get location")
            }

    }

    fun updateLocation() {
        val latiLng = location!!.latitude.toString() + "," + location!!.longitude.toString()
        stateController.startAud()
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("latLng",latiLng)
            .addFormDataPart("storeId", SelectedStore.store.value!!.id.toString())
            .build()

        requestServer.request2(body, "updateStoreLocation", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
//            val result: Store =
//                MyJson.IgnoreUnknownKeys.decodeFromString(
//                    data
//                )
            SelectedStore.store.value!! .latLng = latiLng
            MyToast(this,"تم بنجاح")
            stateController.successStateAUD()
            finish()
        }
    }
    private fun requestEnableGPS() {
//        // Create a LocationRequest for high accuracy
//        val locationRequest: LocationRequest = LocationRequest.Builder(10000)
////            .setIntervalMillis(10000)  // Set interval to 10 seconds
////                .setFastestIntervalMillis(5000)  // Set fastest interval to 5 seconds
//            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)  // High accuracy for GPS
//            .build()
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//            interval = 10000  // Update location every 10 seconds
//        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//        gpsActivityResultLauncher.launch(intent)
        //
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
                }
            }
        }
    }
}
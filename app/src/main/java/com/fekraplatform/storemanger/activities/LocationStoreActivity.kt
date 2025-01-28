package com.fekraplatform.storemanger.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.MyToast
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.convertStringToLatLng
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PinConfig
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import okhttp3.MultipartBody


class LocationStoreActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)
//    lateinit var la: Store
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var editMode by mutableStateOf(false)
    var  latLng by mutableStateOf<LatLng?>(null)



    var lat = mutableDoubleStateOf(0.0)
    var long = mutableDoubleStateOf(0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val intent = intent
        val str =  intent.getStringExtra("latLng")
        Log.e("str",str.toString())
        if (str != null){
            Log.e("str2",str.toString())
            latLng = convertStringToLatLng(str)

            lat.value = latLng!!.latitude
            long.value = latLng!!.longitude

            Log.e("str5",latLng.toString())
            Log.e("latt", latLng!!.latitude.toString())
            Log.e("longg",latLng!!.longitude.toString())
        }
        //
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
        } else {
            getCurrentLocation()
        }


//        if (str != null) {
//            try {
//                latLng = MyJson.IgnoreUnknownKeys.decodeFromString(str)
//            } catch (e: Exception) {
//                finish()
//            }
//
//        } else {
//            finish()
//        }

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
                        if (latLng != null){
                            Button(onClick = {

                                editMode = !editMode
                            }) {
                                Text(if (editMode) "view mode" else "editMode")
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
//        var location = LatLng(lat.value, long.value)
//        val markerState = rememberMarkerState(position = location)
//        var cameraPositionState = rememberCameraPositionState {
//            position = CameraPosition.fromLatLngZoom(location, 16f)
//        }
//        val pinConfig = PinConfig.builder().build()
//
//
//        if (cameraPositionState.isMoving && editMode) {
//
//
//            location = LatLng(
//                cameraPositionState.position.target.latitude,
//                cameraPositionState.position.target.longitude
//            )
//            markerState.position = LatLng(
//                cameraPositionState.position.target.latitude,
//                cameraPositionState.position.target.longitude
//            )
//            lat.value =  cameraPositionState.position.target.latitude
//            long.value =    cameraPositionState.position.target.longitude
//        }
        var location = LatLng(lat.value, long.value)

        val markerState = rememberMarkerState(position = location)

        var cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(location, 16f)
        }

        val pinConfig = PinConfig.builder().build()

// Checking if camera is moving and editMode is enabled
        if (cameraPositionState.isMoving && editMode) {
            // Update location based on camera's current target position
            val updatedLatLng = LatLng(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude
            )

            // Set new position to markerState and location
            location = updatedLatLng
            markerState.position = updatedLatLng

            // Update the lat and long state variables
            lat.value = updatedLatLng.latitude
            long.value = updatedLatLng.longitude
        }


        GoogleMap(
            modifier = Modifier.fillMaxWidth().height(400.dp),
            cameraPositionState = cameraPositionState
        ) {
            AdvancedMarker(
                state = markerState,
                pinConfig = pinConfig
            )
        }

        Box(
            Modifier.fillMaxSize(),
        ) {
            if (editMode)
            Button(
                onClick = {
updateLocation()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .height(100.dp),
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
    private fun requestPermissions() {
        // Launch the request permission dialog
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
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
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("sddd","null")
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        Log.e("sddd3","11")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                Log.e("sddd3","55")
                if (location != null) {
                    Log.e("sddd3","669")
                    latLng  = LatLng(location.latitude,location.longitude)
                    lat.value = latLng!!.latitude
                    long.value = latLng!!.longitude
                } else {
                    // Handle the case where the location is not available
                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                // Handle failure in location retrieval
                MyToast(this,"Failed to get location")
            }
    }

    fun updateLocation() {
        val latiLng = lat.value.toString() + "," + long.value.toString()
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

}
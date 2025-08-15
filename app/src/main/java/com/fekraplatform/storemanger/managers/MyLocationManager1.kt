package com.fekraplatform.storemanger.managers

import android.Manifest
import android.app.Activity
import android.app.Activity.LOCATION_SERVICE
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task


class MyLocationManager1(private val activity: ComponentActivity){
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

        // 1. Try last known location first
        fusedLocationClient.lastLocation.addOnSuccessListener { l ->
            if (l != null) {
                val quickLocation = LatLng(l.latitude, l.longitude)
                location = quickLocation
                onSuccess(quickLocation) // Show map instantly
                isSuccess = true
                isLoading = false
            }
        }
        // 2. Request fresh update for accuracy
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback {
                onSuccess(it)
                isSuccess = true
                isLoading = false
            },
            null
        )
//        fusedLocationClient. requestLocationUpdates(locationRequest,locationCallback{
////            CustomSingleton.location = it
//            isSuccess = true
//            onSuccess(it)
//            isLoading = false
//            isSuccess = true
//        }, null)

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
        task.addOnSuccessListener(activity, {
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
        locationRequest = LocationRequest.Builder(5000).setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY).build()
        getCurrentLocation{
            onSuccess()
        }
    }
    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            val message = "Permission Denied"
            messageLocation = message
            Toast.makeText(activity,message, Toast.LENGTH_SHORT)
            isLoading = false
            isSuccess = false
        }
    }
    private val gpsActivityResultLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()) { result ->
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
                Toast.makeText(activity,message, Toast.LENGTH_SHORT)
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
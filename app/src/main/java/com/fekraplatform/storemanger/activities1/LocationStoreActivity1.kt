package com.fekraplatform.storemanger.activities1

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
import androidx.activity.viewModels
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
import androidx.compose.material3.TextField
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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.managers.MyLocationManager1
import com.fekraplatform.storemanger.models.OrderDelivery
import com.fekraplatform.storemanger.models.Store
import com.fekraplatform.storemanger.shared.AToken1
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomIcon2
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.MainComposeRead
import com.fekraplatform.storemanger.shared.MyHeader
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.ServerConfig
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.convertStringToLatLng
import com.fekraplatform.storemanger.storage.MyAppStorage1
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
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import okhttp3.MultipartBody
import javax.inject.Inject

enum class LocationUpdateMode {
    STORE,
    ORDER
    // ممكن تضيف أنواع أخرى لاحقاً
}


@HiltViewModel
class LocationStoreViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    val appSession: AppSession,
    private val builder: FormBuilder,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
): ViewModel(){
    val selectedStore = appSession.selectedStore
    val stateController = StateController()
    var isCurrentLocation by mutableStateOf(true)
    var shouldExit by mutableStateOf(false)
    var street by mutableStateOf("")
    var mode: String   = savedStateHandle["mode"]?:""
    fun updateLocation(lat:String,long:String) {
        viewModelScope.launch {

            stateController.startAud()
            try {
                val latLong = "$lat,$long"
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("latLng",latLong)
                    .addFormDataPart("latitude", lat)
                    .addFormDataPart("longitude",long)

                when (mode) {
                    LocationUpdateMode.STORE.name -> {
                        requestServer.request(body, "updateStoreLocation")
                        appSession.setStoreAndUpdateStores(appSession.selectedStore.copy(latLng = latLong))
                    }
                    LocationUpdateMode.ORDER.name -> {
                        if (appSession.orderComponent == null) return@launch
                        body.addFormDataPart("orderId", appSession.orderComponent!!.orderDetail.id.toString())
                        body.addFormDataPart("street", street)
                        body.addFormDataPart("userId", appSession.orderComponent!!.orderDetail.userId.toString())
                        val data = requestServer.request(body, "updateOrderLocation")
                        val result: OrderDelivery = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                        appSession.orderComponent = appSession.orderComponent!!.updateOrderDelivery(result)
                        stateController.successStateAUD("تم الحفظ بنجاح")
                        shouldExit = true
                    }
                }

                stateController.successStateAUD("تم الحفظ بنجاح")
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
}
@AndroidEntryPoint
class LocationStoreActivity1 : ComponentActivity() {
    val viewModel : LocationStoreViewModel by viewModels()
    val myLocationManager = MyLocationManager1(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myLocationManager.initLocation()

        setContent {
            StoreMangerTheme {
                if (viewModel.shouldExit){
                    finish()
                }
                if (myLocationManager.isLoading){
                    viewModel.stateController.startRead()
                }else{
                    if (myLocationManager.isSuccess){
                        viewModel.stateController.successStateAUD()
                        viewModel. stateController.successState()
                    }else{
                        viewModel.stateController.errorStateAUD(myLocationManager.messageLocation)
                        viewModel.stateController.errorStateRead(myLocationManager.messageLocation)
                    }
                }


                val text = if(viewModel.mode == LocationUpdateMode.STORE.name) "موقع المتجر" else if(viewModel.mode == LocationUpdateMode.ORDER.name) "موقع الطلب" else ""
                MainComposeRead (text,viewModel.stateController,{finish()},{
                    myLocationManager.initLocation()
                }){
                    ComposeMapp()
                }

            }
        }

    }

    //    var markerState by mutableStateOf(MarkerState(location, "Initial Marker"))
    @Composable
    private fun ComposeMapp() {
        var location = if ( viewModel.mode == LocationUpdateMode.STORE.name && viewModel.selectedStore.latLng != null) convertStringToLatLng(viewModel.selectedStore.latLng!!)!! else if( viewModel.mode == LocationUpdateMode.ORDER.name && viewModel.appSession.orderComponent!!.orderDelivery != null) convertStringToLatLng(viewModel.appSession.orderComponent!!.orderDelivery!!.latLng)!! else  myLocationManager.location!!
        val markerState = rememberMarkerState(position = location)

        var cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(location, 16f)
        }

        val pinConfig = PinConfig.builder().build()

// Checking if camera is moving and editMode is enabled

        if (cameraPositionState.isMoving && !viewModel.isCurrentLocation) {
            // Update location based on camera's current target position
            val updatedLatLng = LatLng(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude
            )

            // Set new position to markerState and location
            location = updatedLatLng
            markerState.position = updatedLatLng
        }

        if (LocationUpdateMode.ORDER.name == viewModel.mode)
            TextField(viewModel.street,{
                viewModel.street = it
            }, label = {
                Text("وصف الموقع")
            })

        Button(
            onClick = {
              viewModel. updateLocation(cameraPositionState.position.target.latitude.toString(), cameraPositionState.position.target.longitude.toString())
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
                cameraPositionState = cameraPositionState,

            ) {
                AdvancedMarker(
                    state = markerState,
                    pinConfig = pinConfig
                )
            }
            Column (Modifier.fillMaxWidth().align(Alignment.TopCenter)){


                if (!viewModel.isCurrentLocation){
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
                            viewModel. isCurrentLocation = true
                            viewModel.stateController.startAud()
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
                            viewModel.isCurrentLocation = false
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
}


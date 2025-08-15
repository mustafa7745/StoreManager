package com.fekraplatform.storemanger.activities1

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.models.StoreDeliveryMan
import com.fekraplatform.storemanger.repositories.BillingRepository
import com.fekraplatform.storemanger.shared.AToken1
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.MainComposeRead
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.ServerConfig
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.storage.MyAppStorage1
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreDeliveryMenViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    private val serverConfig: ServerConfig,
    val appSession: AppSession,
    private val myAppStorage: MyAppStorage1,
    private val builder: FormBuilder,
    private val aToken: AToken1,
    private val billingRepository: BillingRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
): ViewModel(){
    val stateController = StateController()
    var deliveryMen by mutableStateOf<List<StoreDeliveryMan>>(listOf())
    var isShowAddDelivery by mutableStateOf(false)
    fun read() {

        viewModelScope.launch {
            stateController.startRead()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                val data = requestServer.request(body, "getDeliveryMen")
                deliveryMen = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                stateController.successState()
            } catch (e: Exception) {
                stateController.errorStateRead(e.message.toString())
            }
        }
    }
    fun addDeliveryManToStore(phone: String) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId() .addFormDataPart("phone", phone)
                val data = requestServer.request(body, "addDeliveryManToStore")
                val result: StoreDeliveryMan = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                deliveryMen += result
                isShowAddDelivery = false
                stateController.successStateAUD("تمت الاضافه  بنجاح")
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    init {
        read()
    }
}

@AndroidEntryPoint
class StoreDeliveryMenActivity1 : ComponentActivity() {
   val viewModel:StoreDeliveryMenViewModel by viewModels()


    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StoreMangerTheme {
                MainComposeRead (
                    "ادارة الموصلين",viewModel.stateController, { finish() },{
                        viewModel.read()
                    }

                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        item {
                            Button(onClick = {
                               viewModel.isShowAddDelivery = true
                            }) { Text("ADD") }
                        }
                        itemsIndexed(viewModel.deliveryMen){ index, order ->
                            CustomCard2( modifierBox = Modifier
                                .fillMaxSize()
                                .clickable {

                                }) {
                                Column {
                                    Text( " اسم الموصل : "+order.firstName.toString(),Modifier.padding(8.dp))
                                    Text(  " رقم الموصل : "+ order.phone.toString(),Modifier.padding(8.dp))
                                }
                            }
                        }
                    }
                    if (viewModel.isShowAddDelivery) modalAddDelivery()
                }
            }
        }
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddDelivery() {


        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowAddDelivery = false }) {
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
                        var phone by remember { mutableStateOf("") }
                        Card(Modifier.padding(8.dp)){
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                CompositionLocalProvider(LocalTextStyle provides TextStyle(textDirection = TextDirection.Ltr)){
                                    OutlinedTextField(
                                        modifier = Modifier.padding(8.dp),
                                        value = phone,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        label = {
                                            Text("رقم هاتف الموصل")
                                        },
                                        onValueChange = {
                                            phone = it
                                        }
                                    )
                                }

                                IconButton(onClick = {
                                   viewModel.addDeliveryManToStore(phone)
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
                }
            }
        }
    }

}
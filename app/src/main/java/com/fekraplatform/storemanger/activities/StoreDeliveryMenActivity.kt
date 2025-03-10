package com.fekraplatform.storemanger.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
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
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.models.DeliveryMan
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyHeader
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme


class StoreDeliveryMenActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)
    private var deliveryMen by mutableStateOf<List<DeliveryMan>>(listOf())

    var isShowAddDelivery by mutableStateOf(false)

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        read()
        setContent {
            StoreMangerTheme {
                MainCompose1(
                    0.dp, stateController, this,{
                         read()
                    }

                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        stickyHeader {
                            MyHeader({
                                finish()
                            },{
//                                if (!CustomSingleton.isSharedStore()){
                                    CustomIcon(Icons.Default.Add,true) {
                                        isShowAddDelivery = true
//                                    }
                                }

                            }) {
                                Text("ادارة الموصلين")
                            }
                        }
                        itemsIndexed(deliveryMen){ index, order ->
                            CustomCard2( modifierBox = Modifier.fillMaxSize().clickable {

                            }) {
                                Column {
                                    Text( " اسم الموصل : "+order.firstName.toString(),Modifier.padding(8.dp))
                                    Text(  " رقم الموصل : "+ order.phone.toString(),Modifier.padding(8.dp))
                                }
                            }
                        }
                    }
                    if (isShowAddDelivery) modalAddDelivery()
                }
            }
        }
    }

    fun read() {
        stateController.startRead()

        val body = builderForm2()
            .addFormDataPart("storeId",SelectedStore.store.value!!.id.toString())
            .build()

        requestServer.request2(body, "getDeliveryMen", { code, fail ->
            stateController.errorStateRead(fail)
        }
        ) { data ->
            deliveryMen =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            stateController.successState()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddDelivery() {


        ModalBottomSheet(
            onDismissRequest = { isShowAddDelivery = false }) {
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
                                    addStore(phone)
//                                    addStore(storeName)
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
                }
            }
        }
    }
    private fun addStore(phone: String) {
        stateController.startAud()

        val body = builderForm2()
            .addFormDataPart("storeId",SelectedStore.store.value!!.id.toString())
            .addFormDataPart("phone", phone)
            .build()

        requestServer.request2(body,"addDeliveryManToStore",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result: DeliveryMan =  MyJson.IgnoreUnknownKeys.decodeFromString(it)
            deliveryMen += result
            isShowAddDelivery = false
            stateController.successStateAUD("تمت الاضافه  بنجاح")
        }
    }
}
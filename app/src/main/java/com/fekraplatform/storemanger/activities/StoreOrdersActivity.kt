package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.models.CustomOption
import com.fekraplatform.storemanger.models.Order
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.CustomSingleton2
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyHeader
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.shared.formatPrice
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class StoreOrdersActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    var fromDate by mutableStateOf(getCurrentDate().withDayOfMonth(1) .format(formatter).toString())
    var toDate by mutableStateOf(getCurrentDate().format(formatter).toString())

    var isFrom by mutableStateOf(false)
    var isShowSelectDate by mutableStateOf(false)

    var selectedCustomOption by mutableStateOf<CustomOption?>(null)

    var isLoadingMore by mutableStateOf(false)
    var isHaveMore by mutableStateOf(false)

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        readSituations()

        setContent {
            StoreMangerTheme {
                MainCompose1(
                    0.dp, stateController, this,{
                        readSituations()
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
                            }, {
                            CustomIcon(Icons.Default.MoreVert) {

                            }
                            }) {
                                DropDownDemo()
                            }
                            //
                            CustomCard2(modifierBox = Modifier) {
                                Row (Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ){
                                    Text("من: $fromDate", modifier = Modifier.clickable { isShowSelectDate = true
                                        isFrom = true
                                    })
                                    Text("الى: $toDate", modifier = Modifier.clickable { isShowSelectDate = true
                                        isFrom = false
                                    })
                                }
                            }
//                                Card(Modifier.fillMaxWidth().height(100.dp).clickable {
//
//                                }) {
//                                    CustomImageView(
//                                        modifier = Modifier.fillMaxWidth()
//                                            .height(80.dp)
//                                            .padding(8.dp)
//                                            .clickable {
//
//                                            },
//                                        context = this@StoreCategoriesActivity,
//                                        imageUrl = requestServer.serverConfig.getRemoteConfig().BASE_IMAGE_URL+requestServer.serverConfig.getRemoteConfig().SUB_FOLDER_STORE_COVERS+CustomSingleton.selectedStore!!.cover,
//                                        okHttpClient = requestServer.createOkHttpClientWithCustomCert()
//                                    )
//                                }
                        }


                        itemsIndexed(CustomSingleton2.storeOrders!!.orders){index, order ->
                            CustomCard2( modifierBox = Modifier
                                .fillMaxSize()
                                .clickable {

                                }) {
                                Column {
                                    Text( " رقم الطلب: " + order.id.toString(),Modifier.padding(8.dp))
                                    Text( "الحالة : " + order.situation.toString(),Modifier.padding(8.dp))
                                    Text( " اسم المستخدم : "+order.userName.toString(),Modifier.padding(8.dp))
                                    Text( "تاريخ الطلب : "+order.createdAt.toString(),Modifier.padding(8.dp))
                                    Text(  " رقم المستخدم : "+ order.userPhone.toString(),
                                        Modifier
                                            .padding(8.dp)
                                            .clickable {
                                                intentFunUrl("tel:${order.userPhone}")
                                            })
                                    Text(
                                        text = order.amounts.joinToString(
                                            separator = " و "
                                        ) { formatPrice(it.amount)  +" "+ it.currencyName },
                                        fontSize = 14.sp,
                                    )
                                    Button(onClick = {
                                        CustomSingleton2.selectedStoreOrder = order
                                        gotoOrderProducts()
                                    }, modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxSize()) {
                                        Text("الاطلاع على الطلب")
                                    }
                                }

                            }
                        }

                        item {
                            if (isLoadingMore) CircularProgressIndicator()
                            else{
                                if (isHaveMore) Button(onClick = {
                                    isLoadingMore = true
                                    read(listOf(selectedCustomOption!!.id).toString()){}
                                }){ Text("عرض المزيد") }
                            }
                        }

                    }
                    if (isShowSelectDate)DatePickerModal()
                //                    DatePickerDialog(onDismissRequest = {
//                        isShowSelectDate = false
//                    }) { }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DatePickerModal(
    ) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            modifier = Modifier.padding(16.dp),
            onDismissRequest = { isShowSelectDate = false },
            confirmButton = {
                TextButton(onClick = {
                    if (datePickerState.selectedDateMillis != null){
                        if (isFrom)fromDate = convertMillisToDate(datePickerState.selectedDateMillis!!)
                        else
                            toDate = convertMillisToDate(datePickerState.selectedDateMillis!!)
                        isShowSelectDate = false
                    }

//                    datePickerState.selectedDateMillis
//                    onDateSelected(datePickerState.selectedDateMillis)
//                    onDismiss()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { isShowSelectDate = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
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
                    if (CustomSingleton.selectedStore != null){
                        Text("الطلبات"+" | "+selectedCustomOption!!.name)
//                        Text(text = CustomSingleton.selectedStore!!.name )
                       Icon(Icons.Default.ArrowDropDown, contentDescription = "")
                    }
                }
                DropdownMenu(
                    expanded = isDropDownExpanded.value,
                    onDismissRequest = {
                        isDropDownExpanded.value = false
                    }) {
                    DropdownMenuItem(text = {
                        Row {
                            Text(text = "مكتملة")

                        }
                    },
                        onClick = {
                            isDropDownExpanded.value = false
                            read (listOf(2,3).toString()){
                                selectedCustomOption = CustomOption(0,"مكتملة")
                            }
                        })

                    DropdownMenuItem(text = {
                        Row {
                            Text(text = "قيد المعالجة")

                        }
                    },
                        onClick = {
                            isDropDownExpanded.value = false
                            read (listOf(1,4,5,6,7).toString()){
                                selectedCustomOption = CustomOption(0,"قيد المعالجة")
                            }
                        })

                    CustomSingleton2.storeOrders!!.situations.forEachIndexed { index, store ->
                        DropdownMenuItem(text = {
                            Row {
                                Text(text = store.name)

                            }
                        },
                            onClick = {
                                isDropDownExpanded.value = false
                                read (listOf(store.id).toString()){
                                    selectedCustomOption = store
                                }
                            })
                    }
                }
            }

        }
    }

    fun read(situationId:String, onSuccess:()->Unit) {
//        if (!isLoadingMore)
        stateController.startAud()

        val body = builderForm2()
            .addFormDataPart("storeId",SelectedStore.store.value!!.id.toString())
            .addFormDataPart("situationId",situationId)
            .addFormDataPart("fromDate",fromDate)
            .addFormDataPart("toDate",toDate)
            .addFormDataPart("from", if  (isLoadingMore)CustomSingleton2.storeOrders!!.orders.size.toString() else "0")
            .build()

        requestServer.request2(body, "getOrders", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
          val  res:List<Order> =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )
            if (isLoadingMore){
                CustomSingleton2.storeOrders = CustomSingleton2.storeOrders!!.copy(orders = CustomSingleton2.storeOrders!!.orders + res)
            }
                else{
                CustomSingleton2.storeOrders = CustomSingleton2.storeOrders!!.copy(orders = res)
            }


            if (res.size >= 7){
                isHaveMore = true
            }
            else{
                isHaveMore = false
            }
            isLoadingMore = false
            onSuccess()
            stateController.successStateAUD()
        }
    }

    fun readSituations() {
        stateController.startRead()

        val body = builderForm2()
            .addFormDataPart("storeId",SelectedStore.store.value!!.id.toString())
            .addFormDataPart("fromDate",fromDate)
            .addFormDataPart("toDate",toDate)
            .addFormDataPart("from","0")
            .build()

        requestServer.request2(body, "getOrderSituations", { code, fail ->
            stateController.errorStateRead(fail)
        }
        ) { data ->
            CustomSingleton2.storeOrders =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )
            selectedCustomOption = CustomSingleton2.storeOrders!!.situations.first()

            if (CustomSingleton2.storeOrders!!.orders.size >= 7){
                isHaveMore = true
            }
            else{
                isHaveMore = false
            }
//            read()
            stateController.successState()
        }
    }
    private fun gotoOrderProducts() {
        val intent = Intent(this, OrderProductsActivity::class.java)
//        intent.putExtra("order", MyJson.MyJson.encodeToString(order))
        startActivity(intent)
    }
    private fun intentFunUrl(uri:String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(uri)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {

        }
    }
    fun convertMillisToDate(millis: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd",Locale.US)
        return formatter.format(Date(millis))
    }



}
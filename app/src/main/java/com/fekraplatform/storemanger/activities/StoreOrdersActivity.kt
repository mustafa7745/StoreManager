package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                    0.dp, stateController, this, {
                        readSituations()
                    }
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        stickyHeader {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                MyHeader({
                                    finish()
                                }, {
                                    CustomIcon(Icons.Default.MoreVert) {
                                    }
                                }) {
                                    DropDownDemo()
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            modifier = Modifier.clickable { 
                                                isShowSelectDate = true
                                                isFrom = true 
                                            }
                                        ) {
                                            Text(
                                                "من",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                fromDate,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        
                                        Divider(
                                            modifier = Modifier
                                                .height(24.dp)
                                                .width(1.dp)
                                        )
                                        
                                        Column(
                                            modifier = Modifier.clickable { 
                                                isShowSelectDate = true
                                                isFrom = false 
                                            }
                                        ) {
                                            Text(
                                                "الى",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                toDate,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        itemsIndexed(CustomSingleton2.storeOrders!!.orders) { index, order ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { },
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "رقم الطلب: ${order.id}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        Surface(
                                            color = when(order.situation) {
                                                "مكتمل" -> Color(0xFF4CAF50)
                                                "قيد المعالجة" -> Color(0xFFFFA000)
                                                else -> MaterialTheme.colorScheme.secondary
                                            },
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(
                                                order.situation.toString(),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }

                                    Divider()

                                    Text(
                                        "اسم المستخدم: ${order.userName}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    Text(
                                        "تاريخ الطلب: ${order.createdAt}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "رقم المستخدم: ${order.userPhone}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        
                                        IconButton(
                                            onClick = { intentFunUrl("tel:${order.userPhone}") }
                                        ) {
                                            Icon(
                                                Icons.Default.Phone,
                                                contentDescription = "Call",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Text(
                                        text = order.amounts.joinToString(separator = " و ") { 
                                            formatPrice(it.amount) + " " + it.currencyName 
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Button(
                                        onClick = {
                                            CustomSingleton2.selectedStoreOrder = order
                                            gotoOrderProducts()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "الاطلاع على الطلب",
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            if (isLoadingMore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else if (isHaveMore) {
                                Button(
                                    onClick = {
                                        isLoadingMore = true
                                        read(listOf(selectedCustomOption!!.id).toString()) {}
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("عرض المزيد")
                                }
                            }
                        }
                    }
                    
                    if (isShowSelectDate) DatePickerModal()
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

        Log.e("from",fromDate.toString())
        Log.e("to",toDate.toString())

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
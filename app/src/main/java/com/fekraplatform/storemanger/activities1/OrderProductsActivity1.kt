package com.fekraplatform.storemanger.activities1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.AddLocation
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.models.Location
import com.fekraplatform.storemanger.models.StoreDeliveryMan
import com.fekraplatform.storemanger.models.OrderComponent
import com.fekraplatform.storemanger.models.OrderDelivery
import com.fekraplatform.storemanger.models.OrderDetail
import com.fekraplatform.storemanger.models.OrderProduct
import com.fekraplatform.storemanger.models.OrderStatus
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomRow
import com.fekraplatform.storemanger.shared.MainComposeRead
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.ServerConfig
import com.fekraplatform.storemanger.shared.Situations
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.confirmDialog
import com.fekraplatform.storemanger.shared.confirmDialog3
import com.fekraplatform.storemanger.shared.formatPrice
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

@HiltViewModel
class OrderProductsViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    private val serverConfig: ServerConfig,
    val appSession: AppSession,
    private val builder: FormBuilder,
    savedStateHandle: SavedStateHandle
): ViewModel(){


    var locations by mutableStateOf<List<Location>>(emptyList())
    var order by mutableStateOf<OrderEntity>(MyJson.IgnoreUnknownKeys.decodeFromString(savedStateHandle["order"] ?: ""))
    val stateController = StateController()
    var isShowReadLocations by mutableStateOf(false)
    var storeDeliveryMen by mutableStateOf<List<StoreDeliveryMan>>(listOf())
    lateinit var orderProductO: OrderProduct

    var isShowControllProduct by mutableStateOf(false)
    var isShowChooseDeliveryMan by mutableStateOf(false)
    fun read() {
        viewModelScope.launch {
            stateController.startRead()
            try {
                val body = builder.sharedBuilderFormWithStoreId().addFormDataPart("orderId",order.id.toString())

                val data = requestServer.request(body, "getOrderProducts")
                appSession.orderComponent = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                stateController.successState()
            } catch (e: Exception) {
                stateController.errorStateRead(e.message.toString())
            }
        }
    }
    fun updateLocation(usersStoreLocationId:Int) {
        viewModelScope.launch {

            stateController.startAud()
            try {

                val body = builder.sharedBuilderFormWithStoreId()

                    .addFormDataPart("userStoreLocationId", usersStoreLocationId.toString())
                    .addFormDataPart("orderId",order.id.toString())
                    .addFormDataPart("userId",appSession.orderComponent!!.orderDetail.userId.toString())

                if (appSession.orderComponent == null) return@launch

                val data = requestServer.request(body, "updateOrderLocation")
                val result: OrderDelivery = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                appSession.orderComponent = appSession.orderComponent!!.updateOrderDelivery(result)
                stateController.successStateAUD("تم الحفظ بنجاح")

            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun readUserLocation() {
        if (locations.isNotEmpty()){
            isShowReadLocations = true
            return
        }
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("userId",appSession.orderComponent!!.orderDetail.userId.toString())
                val data = requestServer.request(body, "getUserLocations")
                locations = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)

                stateController.successStateAUD()
                isShowReadLocations = true
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }


    }
    fun changeQuantity(product: OrderProduct) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("orderId",order.id.toString())
                    .addFormDataPart("id",product.id.toString())
                    .addFormDataPart("qnt",product.quantity.toString())

                val data = requestServer.request(body, "updateOrderProductQuantity")
                val orderProduct:OrderProduct = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                appSession.orderComponent  =    appSession.orderComponent !!.updateOrderProduct(orderProduct)

                val newAmount = order.amounts.map {
                    if (it.currencyId == product.currencyId){
//                        if (orderComponent!!.orderCoupon != null){
//
//                        }
                        val s = product.quantity - orderProductO.quantity
                        val am = it.amount.toDouble()
                        val n = (am + (s * product.price)).toString()
                        Log.e("ffrr",n)
                        it.copy(amount = n )
                    }else it
                }
                order = order.copy(
                    amounts = newAmount
                )
                updateOrders()
                isShowControllProduct = false
                stateController.successStateAUD()

            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    private fun updateOrders() {
        val newOrders = appSession.storeOrders2!!.orders.map { inorder ->
            if (inorder.id == order.id)
                order
            else inorder
        }
        appSession.storeOrders2 = appSession.storeOrders2!!.copy(orders = newOrders)
    }
    fun chooseDeliveryMan(id:String) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("orderId",order.id.toString())
                    .addFormDataPart("storeDeliveryManId",id)
                val data = requestServer.request(body, "updateOrderDeliveryMan")
                val orderDelivery:OrderDelivery = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                appSession.orderComponent  = appSession.orderComponent !!.updateOrderDelivery(orderDelivery)
                isShowChooseDeliveryMan = false
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun updateSystemOrderNumber(systemOrderNumber:String) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("orderId",order.id.toString())
                    .addFormDataPart("systemOrderNumber",systemOrderNumber)
                val data = requestServer.request(body, "updateSystemOrderNumber")
                val result: JsonObject = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)

                val orderDetail: OrderDetail = MyJson.IgnoreUnknownKeys.decodeFromString(
                    result["orderDetail"].toString()
                )
                val orderStatusList: List<OrderStatus> = MyJson.IgnoreUnknownKeys.decodeFromString(
                    result["orderStatusList"].toString()
                )

                appSession.orderComponent  = appSession.orderComponent !!.updateOrderDetail(orderDetail)
                appSession.orderComponent  = appSession.orderComponent !!.updateOrderStatusList(orderStatusList)
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun updateStatus(situationId:Int,causeCancel:String? = null) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("orderId",order.id.toString())
                    .addFormDataPart("situationId",situationId.toString())
                if (causeCancel != null){
                    body.addFormDataPart("causeCancel",causeCancel)
                }

                val data = requestServer.request(body, "updateOrderStatus")
                val result: JsonObject = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)

                val orderDetail: OrderDetail = MyJson.IgnoreUnknownKeys.decodeFromString(
                    result["orderDetail"].toString()
                )
                val orderStatusList: List<OrderStatus> = MyJson.IgnoreUnknownKeys.decodeFromString(
                    result["orderStatusList"].toString()
                )

                appSession.orderComponent  = appSession.orderComponent !!.updateOrderDetail(orderDetail)
                appSession.orderComponent  = appSession.orderComponent !!.updateOrderStatusList(orderStatusList)
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun deleteOrderProducts(ids:List<Int>,onDone:()->Unit) {

        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("orderId",order.id.toString())
                    .addFormDataPart("ids",ids.toString())
                val data = requestServer.request(body, "deleteOrderProducts")
                val result: StoreDeliveryMan = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                storeDeliveryMen += result
                appSession.orderComponent  =  appSession.orderComponent !!.filterProduct(ids)
                onDone()
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun readDeliveryMen() {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                val data = requestServer.request(body, "getDeliveryMen")
                val result: List<StoreDeliveryMan> = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                storeDeliveryMen = result
                stateController.successStateAUD()
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
class OrderProductsActivity1 : ComponentActivity() {

    val viewModel:OrderProductsViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StoreMangerTheme {
                MainComposeRead (
                    "رقم الطلب: ${viewModel.order.id}", viewModel.stateController,
                    { finish() }, {
                        viewModel.read()
                    }
                ) {
                    val orderComponent = viewModel.appSession.orderComponent  ?: return@MainComposeRead
                    var ids by remember { mutableStateOf<List<Int>>(emptyList()) }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            val situation = viewModel.appSession.storeOrders2!!.situations.find { it.id == viewModel.order.situationId }

                            if (situation != null)
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(4.dp),
                                color =
                                  MaterialTheme.colorScheme.secondary
                                ,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        situation.name.toString(),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        viewModel.order.createdAt,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color =  Color.White
                                    )
                                }
                            }
                        }
                        stickyHeader {
                            if (viewModel.order.situationId !in listOf(Situations.CANCELED, Situations.COMPLETED)) {
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "اسم المستخدم: ${viewModel.order.userName}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        IconButton(
                                            onClick = {
//                                                    intentFunUrl("tel:${viewModel.order.userPhone}")
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Phone,
                                                contentDescription = "Call",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {

                                                   },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("الغاء الطلب")
                                            }
                                            
                                            Button(
                                                onClick = {

                                                    },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("تسليم الطلب")
                                            }
                                        }
                                    }
                                }
                            }
                        }
//
//                        item {
//                            ElevatedCard(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clip(RoundedCornerShape(12.dp)),
//                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
//                            ) {
//                                Column(
//                                    modifier = Modifier.padding(16.dp),
//                                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                                ) {
//                                    Row(
//                                        modifier = Modifier.fillMaxWidth(),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically
//                                    ) {
//                                        Text(
//                                            "معلومات الدفع",
//                                            style = MaterialTheme.typography.titleLarge,
//                                            fontWeight = FontWeight.Bold
//                                        )
//
//                                        AsyncImage(
//                                            model = R.drawable.epay,
//                                            contentDescription = null,
//                                            modifier = Modifier
//                                                .size(50.dp)
//                                                .clip(RoundedCornerShape(16.dp))
//                                                .border(
//                                                    1.dp,
//                                                    MaterialTheme.colorScheme.primary,
//                                                    RoundedCornerShape(16.dp)
//                                                )
//                                        )
//                                    }
//
//                                    HorizontalDivider()
//
//                                    Row(
//                                        modifier = Modifier.fillMaxWidth(),
//                                        horizontalArrangement = Arrangement.SpaceBetween
//                                    ) {
//                                        Text(
//                                            "طريقة الدفع:",
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                                        )
//                                        Text(
//                                            if (viewModel.orderComponent!!.orderDetail.paid == 0) "عند التوصيل" else "الكترونيا",
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//                                    }
//
//                                    if (viewModel.orderComponent!!.orderPayment != null) {
//                                        Column(
//                                            verticalArrangement = Arrangement.spacedBy(8.dp)
//                                        ) {
//                                            Row(
//                                                modifier = Modifier.fillMaxWidth(),
//                                                horizontalArrangement = Arrangement.SpaceBetween,
//                                                verticalAlignment = Alignment.CenterVertically
//                                            ) {
//                                                Text(
//                                                    "معلومات الدفع الالكتروني:",
//                                                    style = MaterialTheme.typography.bodyMedium
//                                                )
//                                                AsyncImage(
//                                                    modifier = Modifier
//                                                        .size(50.dp)
//                                                        .padding(10.dp),
//                                                    model = viewModel.orderComponent!!.orderPayment!!.paymentImage,
//                                                    contentDescription = null
//                                                )
//                                            }
//
//                                            Row(
//                                                modifier = Modifier.fillMaxWidth(),
//                                                horizontalArrangement = Arrangement.SpaceBetween
//                                            ) {
//                                                Text(
//                                                    "الدفع عن طريق:",
//                                                    style = MaterialTheme.typography.bodyMedium,
//                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                                                )
//                                                Text(
//                                                    viewModel.orderComponent!!.orderPayment!!.paymentName,
//                                                    style = MaterialTheme.typography.bodyMedium
//                                                )
//                                            }
//
//                                            Row(
//                                                modifier = Modifier.fillMaxWidth(),
//                                                horizontalArrangement = Arrangement.SpaceBetween
//                                            ) {
//                                                Text(
//                                                    "تاريخ الدفع:",
//                                                    style = MaterialTheme.typography.bodyMedium,
//                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                                                )
//                                                Text(
//                                                    viewModel.orderComponent!!.orderPayment!!.createdAt,
//                                                    style = MaterialTheme.typography.bodyMedium
//                                                )
//                                            }
//                                        }
//                                    } else {
//                                        Text(
//                                            if (viewModel.orderComponent!!.orderDetail.paid != 0) "لم يتم ادخال كود الشراء بعد" else "لم يتم الدفع بعد",
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            color = MaterialTheme.colorScheme.error
//                                        )
//                                    }
//                                }
//                            }
//                        }
//
//                        item {
//                            ElevatedCard(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clip(RoundedCornerShape(12.dp)),
//                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
//                            ) {
//                                Column(
//                                    modifier = Modifier.padding(16.dp),
//                                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                                ) {
//                                    Row(
//                                        modifier = Modifier.fillMaxWidth(),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically
//                                    ) {
//                                        Text(
//                                            "معلومات التوصيل",
//                                            style = MaterialTheme.typography.titleLarge,
//                                            fontWeight = FontWeight.Bold
//                                        )
//
//                                        AsyncImage(
//                                            model = R.drawable.delivery,
//                                            contentDescription = null,
//                                            modifier = Modifier
//                                                .size(50.dp)
//                                                .clip(RoundedCornerShape(16.dp))
//                                                .border(
//                                                    1.dp,
//                                                    MaterialTheme.colorScheme.primary,
//                                                    RoundedCornerShape(16.dp)
//                                                )
//                                        )
//                                    }
//
//                                    HorizontalDivider()
//
//                                    if (viewModel.orderComponent!!.orderDelivery != null) {
//                                        Column(
//                                            verticalArrangement = Arrangement.spacedBy(8.dp)
//                                        ) {
//                                            if (viewModel.orderComponent!!.orderDelivery!!.deliveryMan != null) {
//                                                Row(
//                                                    modifier = Modifier.fillMaxWidth(),
//                                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                                    verticalAlignment = Alignment.CenterVertically
//                                                ) {
//                                                    Text(
//                                                        "موصل الطلب:",
//                                                        style = MaterialTheme.typography.bodyMedium,
//                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                                                    )
//                                                    Text(
//                                                        viewModel.orderComponent!!.orderDelivery!!.deliveryMan!!.firstName,
//                                                        style = MaterialTheme.typography.bodyMedium,
//                                                        modifier = Modifier.clickable {
//                                                            viewModel.isShowChooseDeliveryMan = true
//                                                        }
//                                                    )
//                                                }
//                                            } else {
//                                                Button(
//                                                    onClick = { viewModel.isShowChooseDeliveryMan = true },
//                                                    modifier = Modifier.fillMaxWidth(),
//                                                    shape = RoundedCornerShape(8.dp)
//                                                ) {
//                                                    Text("اختيار موصل الطلب")
//                                                }
//                                            }
//
//                                            Text(
//                                                "موقع توصيل الطلب:",
//                                                style = MaterialTheme.typography.bodyMedium,
//                                                fontWeight = FontWeight.Medium
//                                            )
//
//                                            Row(
//                                                modifier = Modifier.fillMaxWidth(),
//                                                horizontalArrangement = Arrangement.SpaceBetween
//                                            ) {
//                                                Text(
//                                                    "شارع:",
//                                                    style = MaterialTheme.typography.bodyMedium,
//                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                                                )
//                                                Text(
//                                                    viewModel.orderComponent!!.orderDelivery!!.street,
//                                                    style = MaterialTheme.typography.bodyMedium
//                                                )
//                                            }
//
//                                            Row(
//                                                modifier = Modifier.fillMaxWidth(),
//                                                horizontalArrangement = Arrangement.SpaceBetween,
//                                                verticalAlignment = Alignment.CenterVertically
//                                            ) {
//                                                Text(
//                                                    "الموقع على الخريطه:",
//                                                    style = MaterialTheme.typography.bodyMedium,
//                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                                                )
//                                                IconButton(
//                                                    onClick = {
//                                                        val googleMapsUrl = "https://www.google.com/maps?q=${viewModel.orderComponent!!.orderDelivery!!.latLng}"
//                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUrl))
//                                                        startActivity(intent)
//                                                    }
//                                                ) {
//                                                    Icon(
//                                                        Icons.Default.Place,
//                                                        contentDescription = "View on Map",
//                                                        tint = MaterialTheme.colorScheme.primary
//                                                    )
//                                                }
//                                            }
//                                        }
//                                    } else {
//                                        Text(
//                                            "هذا الطلب بدون توصيل",
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                                        )
//                                    }
//                                }
//                            }
//                        }

                        stickyHeader {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("المنتجات")
//                                    IconButton(
//                                        onClick = { /* TODO */ }
//                                    ) {
//                                        Icon(
//                                            Icons.Default.Add,
//                                            contentDescription = "Add",
//                                            tint = MaterialTheme.colorScheme.primary
//                                        )
//                                    }
                                    
                                    IconButton(
                                        enabled = ids.isNotEmpty(),
                                        onClick = {
                                            confirmDialog(this@OrderProductsActivity1,"",false){
                                                viewModel.deleteOrderProducts(ids) { ids = emptyList() } }
                                            }


                                    ) {
                                        Icon(
                                            Icons.Outlined.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = if (ids.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        val column0Weight = 0.07f // 30%
                        val column1Weight = 0.44f // 30%
                        val column2Weight = 0.12f // 70%
                        val column3Weight = 0.15f // 30%
                        val column4Weight = 0.22f // 70%
                        item {
                            Row(Modifier.background(Color.Gray)) {
                                TableCellHeader(text = "#", weight = column0Weight)
                                TableCellHeader(text = "الصنف", weight = column1Weight)
                                TableCellHeader(text = "الكمية", weight = column2Weight)
                                TableCellHeader(text = "السعر", weight = column3Weight)
                                TableCellHeader(text = "الاجمالي", weight = column4Weight)
                            }
                        }
                            ///
                        itemsIndexed( viewModel.appSession.orderComponent !!.orderProducts) { index, orderProduct ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(if (ids.contains(orderProduct.id)) Color.Red else Color.White)
                                        .combinedClickable(
                                        onClick = {
                                            if (ids.isEmpty()) {
                                                viewModel.orderProductO = orderProduct
                                                viewModel.isShowControllProduct = true
                                            } else {
                                                val isSelected = ids.contains(orderProduct.id)
                                                val newIds = if (isSelected) {
                                                    ids - orderProduct.id
                                                } else {
                                                    ids + orderProduct.id
                                                }

                                                val totalProducts = viewModel.appSession.orderComponent ?.orderProducts?.size ?: 0
                                                if (totalProducts <= 1 || newIds.size == totalProducts) {
                                                    viewModel.stateController.showMessage("لا يمكن حذف آخر منتج من الطلب")
                                                    return@combinedClickable
                                                }

                                                ids = newIds
                                            }
                                        },
                                onLongClick = {
                                    if (ids.isEmpty()) {
                                        val newIds = ids + orderProduct.id

                                        val totalProducts = viewModel.appSession.orderComponent ?.orderProducts?.size ?: 0
                                        if (totalProducts <= 1 || newIds.size == totalProducts) {
                                            viewModel.stateController.showMessage("لا يمكن حذف آخر منتج من الطلب")
                                            return@combinedClickable
                                        }

                                        ids = newIds
                                    }
                                }
                            ))

                            {

                                TableCell(
                                    text = (index + 1).toString(), weight = column0Weight
                                )
                                TableCell(
                                    text = orderProduct.productName, weight = column1Weight
                                )
                                TableCell(
                                    text = orderProduct.quantity.toString(),
                                    weight = column2Weight
                                )
                                TableCell(
                                    text = formatPrice(orderProduct.price.toString()) , weight = column3Weight
                                )
                                TableCell(
                                    text = formatPrice ((orderProduct.price * orderProduct.quantity).toString()),
                                    weight = column4Weight
                                )

                            }
                        }

                        if (viewModel.appSession.orderComponent !!.orderCoupon != null) {
                            val coupon = viewModel.appSession.orderComponent !!.orderCoupon!!
                            val index = viewModel.appSession.orderComponent !!.orderProducts.size + 1
                            val currencyName = viewModel.appSession.selectedStore.storeCurrencies.find { it.currencyId == coupon.currencyId }?.currencyName
                            val discountText = if (coupon.type == 1) {
                                "${coupon.amount}% خصم"
                            } else {
                                "${formatPrice(coupon.amount.toString())} $currencyName خصم"
                            }

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = false) {}, // غير قابل للنقر حاليًا
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TableCell(
                                        text = index.toString(),
                                        weight = column0Weight
                                    )
                                    TableCell(
                                        text = "خصم",
                                        weight = column1Weight
                                    )
                                    TableCell(
                                        text = discountText,
                                        weight = 0.49f
                                    )
                                }
                            }
                        }

                        if (viewModel.appSession.orderComponent !!.orderDelivery != null){
                            val delivery =viewModel.appSession.orderComponent !!.orderDelivery!!
                            var index = viewModel.appSession.orderComponent !!.orderProducts.size + 1
                            if (viewModel.appSession.orderComponent !!.orderCoupon != null){
                                index +2;
                            }
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = false) {}, // غير قابل للنقر حاليًا
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TableCell(
                                        text = index.toString(),
                                        weight = column0Weight
                                    )
                                    TableCell(
                                        text = "توصيل الطلب",
                                        weight = column1Weight
                                    )
                                    TableCell(
                                        text = formatPrice(delivery.deliveryPrice.toString()) + " "+ delivery.currencyName,
                                        weight = 0.49f
                                    )
                                }
                            }
                        }


                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F8E9)) // أخضر فاتح جداً
                                    .border(1.dp, Color(0xFF81C784), RoundedCornerShape(12.dp)) // حدود بلون أخضر لطيف
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {

                                    Text(
                                        text = viewModel.order.amounts.joinToString(
                                            separator = " و "
                                        ) {
                                            "${formatPrice(it.amount.toString())} ${it.currencyName}"
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = Color(0xFF2E7D32),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }


                        stickyHeader {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("تسلسل حالات الطلب")
                                }
                            }
                        }

                        val ordersHome = viewModel.appSession.storeOrders2
                        val idsDisabled = listOf(1, 2)

                        if (ordersHome != null) {
                                itemsIndexed(
                                    ordersHome.situations
                                        .filter { it.id !in listOf(9997, 9998,1) }
                                        .sortedBy { it.id }
                                ) { index, item ->
                                    val status = viewModel.appSession.orderComponent ?.orderStatusList?.find { it.situationId == item.id }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )

                                            if (item.id !in idsDisabled) {
                                                if (item.id == 3){
                                                    if (orderComponent.orderDelivery != null){
                                                        Switch(
                                                            enabled = status == null,
                                                            checked = status != null,
                                                            onCheckedChange = {
                                                               viewModel.isShowChooseDeliveryMan = true
                                                            }
                                                        )
                                                    }
                                                    else{
                                                        Icon(
                                                            modifier = Modifier.clickable {  viewModel.readUserLocation()   },
                                                            imageVector = Icons.Outlined.AddLocation,
                                                            contentDescription = "Location"
                                                        )
                                                    }
                                                }else{
                                                    Switch(
                                                        enabled = status == null || item.id == Situations.SYSTEM_ORDER_READY,
                                                        checked = status != null,
                                                        onCheckedChange = {
                                                            if (item.id == Situations.SYSTEM_ORDER_READY)
                                                            confirmDialog3(this@OrderProductsActivity1,"","Enter system Order here"){
                                                                if (it.trim().isNotEmpty())
                                                               viewModel.updateSystemOrderNumber(it)
                                                            }
                                                            else if(item.id == Situations.CANCELED){
                                                                confirmDialog3(this@OrderProductsActivity1,"سبب الغاء الطلب","السبب") {
                                                                    val cause = it.trim()
                                                                    if (cause.length > 5)
                                                                    viewModel.updateStatus(item.id,it)
                                                                    else
                                                                        viewModel.stateController.showMessage("not enoupgh Cause")
                                                                }
                                                            }
                                                            else
                                                                confirmDialog3(this@OrderProductsActivity1,"",null) {
                                                                    viewModel.updateStatus(item.id)
                                                                }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        if (item.id == 3 && orderComponent.orderDelivery != null) {
                                            if (orderComponent.orderDelivery.storeDeliveryMan != null)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp) // مسافة بين العناصر
                                            ) {
                                                orderComponent.orderDelivery.storeDeliveryMan.let {
                                                    Text("${it.firstName} ${it.lastName}")
                                                }
                                                Icon(
                                                    modifier = Modifier.clickable { viewModel.isShowChooseDeliveryMan = true },
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit DeliveryMan"
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp)) // مسافة بين الصفوف

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                orderComponent.orderDelivery.let {
                                                    Icon(
                                                        modifier = Modifier.clickable { openMap(it.latLng) },
                                                        imageVector = Icons.Outlined.Map,
                                                        contentDescription = "Location"
                                                    )

                                                    Column {
                                                        Text(
                                                            text = orderComponent.orderDelivery.street,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color.Gray,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                        Text("المسافة:" +orderComponent.orderDelivery.distance ,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color.Gray,
                                                            modifier = Modifier.padding(top = 4.dp))
                                                        Text("الوقت:" +orderComponent.orderDelivery.duration ,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color.Gray,
                                                            modifier = Modifier.padding(top = 4.dp))
                                                    }


                                                }
                                                Icon(
                                                    modifier = Modifier.clickable { viewModel.readUserLocation() },
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Location"
                                                )
                                            }
                                        }

                                        if (item.id == Situations.SYSTEM_ORDER_READY && orderComponent.orderDetail.systemOrderNumber != null){
                                            Text(
                                                text = "رقم الفاتورة: ${orderComponent.orderDetail.systemOrderNumber}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }

                                        if (status != null) {
                                            Text(
                                                text = "تم في: ${status.createdAt}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                        )
                                    }
                                }
                            }




//                        itemsIndexed(viewModel.orderComponent!!.orderProducts) { index, orderProduct ->
//                            ElevatedCard (
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clip(RoundedCornerShape(12.dp)),
//                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
//                            ) {
//                                Column(
//                                    modifier = Modifier.padding(16.dp),
//                                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                                ) {
//                                    Row(
//                                        modifier = Modifier.fillMaxWidth(),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically
//                                    ) {
//                                        Column {
//                                            Text(
//                                                orderProduct.productName,
//                                                style = MaterialTheme.typography.titleMedium,
//                                                fontWeight = FontWeight.Bold
//                                            )
//                                            Text(
//                                                orderProduct.optionName,
//                                                style = MaterialTheme.typography.bodyMedium,
//                                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                                            )
//                                        }
//
//                                        Row(
//                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
//                                            verticalAlignment = Alignment.CenterVertically
//                                        ) {
//                                            IconButton (
//                                                onClick = {
//                                                    viewModel.orderProductO = orderProduct
//                                                    viewModel.isShowControllProduct = true
//                                                }
//                                            ) {
//                                                Icon(
//                                                    Icons.Outlined.MoreVert,
//                                                    contentDescription = "Options",
//                                                    tint = MaterialTheme.colorScheme.primary
//                                                )
//                                            }
//
//                                            Checkbox(
//                                                checked = ids.contains(orderProduct.id),
//                                                onCheckedChange = { checked ->
//                                                    ids = if (checked) ids + orderProduct.id else ids - orderProduct.id
//                                                }
//                                            )
//                                        }
//                                    }
//
//                                    HorizontalDivider()
//
//                                    Row(
//                                        modifier = Modifier.fillMaxWidth(),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically
//                                    ) {
//                                        Text(
//                                            "السعر: ${formatPrice(orderProduct.price.toString())}",
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//
//                                        Text(
//                                            "الكمية: ${orderProduct.quantity}",
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//
//                                        Text(
//                                            "المجموع: ${formatPrice((orderProduct.price * orderProduct.quantity).toString())} ${orderProduct.currencyName}",
//                                            style = MaterialTheme.typography.titleMedium,
//                                            fontWeight = FontWeight.Bold,
//                                            color = MaterialTheme.colorScheme.primary
//                                        )
//                                    }
//                                }
//                            }
//                        }
                    }

                    if (viewModel.isShowControllProduct) modalControll()
                    if (viewModel.isShowChooseDeliveryMan) modalChooesDeliveryMan()
                    if (viewModel.isShowReadLocations)modalShowLocations()
                }
            }
        }
    }

    private fun openMap(latLong:String){
        val googleMapsUrl = "https://www.google.com/maps?q=${latLong}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUrl))
        startActivity(intent)
    }




    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalControll() {
        var product by remember { mutableStateOf(viewModel.orderProductO) }
        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowControllProduct = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){

                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Row(
                            modifier = Modifier
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomIcon(Icons.Default.Add) {
                                product =   product.copy(quantity = product.quantity + 1)

                            }

                            Text(product.quantity.toString())

                            CustomIcon(Icons.Default.KeyboardArrowDown) {
                                if (product.quantity >1)
                             product =   product.copy(quantity = product.quantity -1)

                            }

                            if (product.quantity != viewModel.orderProductO.quantity)
                                    Button(onClick = {

                                        viewModel.changeQuantity(product)
                                    }) { Text("حفظ") }
                        }
                    }
                }
                }
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalChooesDeliveryMan() {
        if (viewModel.storeDeliveryMen.isEmpty())viewModel.readDeliveryMen()
        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowChooseDeliveryMan = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){

                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    itemsIndexed(viewModel.storeDeliveryMen.filter {
                        it.id != (viewModel.appSession.orderComponent !!.orderDelivery?.storeDeliveryMan?.id ?: -1)
                    }){ index, item ->
                        CustomCard2( modifierBox = Modifier
                            .fillMaxSize()
                            .clickable {

                            }) {
                            Column {
                                CustomRow {
                                    Text( " اسم الموصل : "+item.firstName.toString(),Modifier.padding(8.dp))
                                    Button(onClick = {
                                        confirmDialog(this@OrderProductsActivity1,"",false){
                                            viewModel.chooseDeliveryMan(item.id.toString())
                                        }
                                    }) {
                                        Text("اختيار")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalShowLocations() {

        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowReadLocations = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Button(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            onClick = {
                             gotoLocationUpdate()

                            }) { Text("اضافة") }
                    }

                    itemsIndexed(viewModel.locations){index,location->
                        Card(Modifier.padding(8.dp)) {
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Text(location.street)
                                Button(
                                    onClick = {

                                        viewModel.updateLocation(location.id)
                                        viewModel.isShowReadLocations = false
                                    }) {
                                    Text("اختيار") }
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun RowScope.TableCellHeader(
        text: String, weight: Float
    ) {
        Text(
            modifier = Modifier
                .border(1.dp, Color.Black)
                .weight(weight)
                .padding(8.dp),
            text = text,
            fontSize = 10.sp,
            color = Color.White,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }

    @Composable
    fun RowScope.TableCell(
        text: String, weight: Float
    )
    {
        Text(

            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.Black)
                .weight(weight),
            textAlign=TextAlign.Center,

            text = text,
            fontSize = 10.sp,
            overflow = TextOverflow.Ellipsis, // Allow overflow to be visible
            maxLines = Int.MAX_VALUE, // Allow multiple lines
        )

    }

    private fun gotoLocationUpdate() {
        val intent = Intent(this, LocationStoreActivity1::class.java)
        intent.putExtra("mode", LocationUpdateMode.ORDER.name)
        startActivity(intent)
    }
}
package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.models.StoreDeliveryMan
import com.fekraplatform.storemanger.models.OrderComponent
import com.fekraplatform.storemanger.models.OrderDelivery
import com.fekraplatform.storemanger.models.OrderProduct
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomRow
import com.fekraplatform.storemanger.shared.CustomSingleton2
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.Situations
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.shared.builderForm3
import com.fekraplatform.storemanger.shared.formatPrice
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme


class OrderProductsActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)
    private var orderComponent by mutableStateOf<OrderComponent?>(null)
    private var deliveryMen by mutableStateOf<List<StoreDeliveryMan>>(listOf())
//    lateinit var order: Order
    lateinit var orderProductO: OrderProduct

    private var isShowControllProduct by mutableStateOf(false)
    private var isShowChooseDeliveryMan by mutableStateOf(false)

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val intent = intent
//        val str = intent.getStringExtra("order")
//        if (str != null) {
//            try {
//                order = MyJson.IgnoreUnknownKeys.decodeFromString(str)
//            }catch (e:Exception){
//                finish()
//            }
//        } else {
//            finish()
//        }

        read()

        setContent {
            StoreMangerTheme {
                MainCompose1(
                    0.dp, stateController, this, {
                        read()
                    }
                ) {
                    var ids by remember { mutableStateOf<List<Int>>(emptyList()) }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        stickyHeader {
                            if (CustomSingleton2.selectedStoreOrder!!.situationId !in listOf(Situations.CANCELED, Situations.COMPLETED)) {
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "رقم الطلب: ${CustomSingleton2.selectedStoreOrder!!.id}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        Surface(
                                            color = when(CustomSingleton2.selectedStoreOrder!!.situation) {
                                                "مكتمل" -> Color(0xFF4CAF50)
                                                "قيد المعالجة" -> Color(0xFFFFA000)
                                                else -> MaterialTheme.colorScheme.secondary
                                            },
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(
                                                CustomSingleton2.selectedStoreOrder!!.situation.toString(),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }

                                        Text(
                                            "اسم المستخدم: ${CustomSingleton2.selectedStoreOrder!!.userName}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        
                                        Text(
                                            "تاريخ الطلب: ${CustomSingleton2.selectedStoreOrder!!.createdAt}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "رقم المستخدم: ${CustomSingleton2.selectedStoreOrder!!.userPhone}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            
                                            IconButton(
                                                onClick = {
//                                                    intentFunUrl("tel:${CustomSingleton2.selectedStoreOrder!!.userPhone}")
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Phone,
                                                    contentDescription = "Call",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Text(
                                            text = CustomSingleton2.selectedStoreOrder!!.amounts.joinToString(
                                                separator = " و "
                                            ) { formatPrice(it.amount) + " " + it.currencyName },
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { cancelOrder() },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("الغاء الطلب")
                                            }
                                            
                                            Button(
                                                onClick = { completeOrder() },
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

                        item {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "معلومات الدفع",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        AsyncImage(
                                            model = R.drawable.epay,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    RoundedCornerShape(16.dp)
                                                )
                                        )
                                    }

                                    HorizontalDivider()

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "طريقة الدفع:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            if (orderComponent!!.orderDetail.paid == 0) "عند التوصيل" else "الكترونيا",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    if (orderComponent!!.orderPayment != null) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "معلومات الدفع الالكتروني:",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                AsyncImage(
                                                    modifier = Modifier
                                                        .size(50.dp)
                                                        .padding(10.dp),
                                                    model = orderComponent!!.orderPayment!!.paymentImage,
                                                    contentDescription = null
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    "الدفع عن طريق:",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    orderComponent!!.orderPayment!!.paymentName,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    "تاريخ الدفع:",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    orderComponent!!.orderPayment!!.createdAt,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            if (orderComponent!!.orderDetail.paid != 0) "لم يتم ادخال كود الشراء بعد" else "لم يتم الدفع بعد",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "معلومات التوصيل",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        AsyncImage(
                                            model = R.drawable.delivery,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    RoundedCornerShape(16.dp)
                                                )
                                        )
                                    }

                                    HorizontalDivider()

                                    if (orderComponent!!.orderDelivery != null) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (orderComponent!!.orderDelivery!!.storeDeliveryMan != null) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "موصل الطلب:",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        orderComponent!!.orderDelivery!!.storeDeliveryMan!!.firstName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.clickable {
                                                            isShowChooseDeliveryMan = true
                                                        }
                                                    )
                                                }
                                            } else {
                                                Button(
                                                    onClick = { isShowChooseDeliveryMan = true },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("اختيار موصل الطلب")
                                                }
                                            }

                                            Text(
                                                "موقع توصيل الطلب:",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    "شارع:",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    orderComponent!!.orderDelivery!!.street,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "الموقع على الخريطه:",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                IconButton(
                                                    onClick = {
                                                        val googleMapsUrl = "https://www.google.com/maps?q=${orderComponent!!.orderDelivery!!.latLng}"
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUrl))
                                                        startActivity(intent)
                                                    }
                                                ) {
                                                    Icon(
                                                        Icons.Default.Place,
                                                        contentDescription = "View on Map",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            "هذا الطلب بدون توصيل",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
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
                                    IconButton(
                                        onClick = { /* TODO */ }
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = { deleteOrderProducts(ids) { ids = emptyList() } },
                                        enabled = ids.isNotEmpty()
                                    ) {
                                        Icon(
                                            Icons.Outlined.MoreVert,
                                            contentDescription = "Delete",
                                            tint = if (ids.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        itemsIndexed(orderComponent!!.orderProducts) { index, orderProduct ->
                            ElevatedCard (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
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
                                        Column {
                                            Text(
                                                orderProduct.productName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                orderProduct.optionName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton (
                                                onClick = {
                                                    orderProductO = orderProduct
                                                    isShowControllProduct = true
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Outlined.MoreVert,
                                                    contentDescription = "Options",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            
                                            Checkbox(
                                                checked = ids.contains(orderProduct.id),
                                                onCheckedChange = { checked ->
                                                    ids = if (checked) ids + orderProduct.id else ids - orderProduct.id
                                                }
                                            )
                                        }
                                    }

                                    HorizontalDivider()

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "السعر: ${formatPrice(orderProduct.price.toString())}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        
                                        Text(
                                            "الكمية: ${orderProduct.quantity}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        
                                        Text(
                                            "المجموع: ${formatPrice((orderProduct.price * orderProduct.quantity).toString())} ${orderProduct.currencyName}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isShowControllProduct) modalControll()
                    if (isShowChooseDeliveryMan) modalChooesDeliveryMan()
                }
            }
        }
    }

    fun read() {
        stateController.startRead()

        val body = builderForm3()
            .addFormDataPart("orderId",CustomSingleton2.selectedStoreOrder!!.id.toString())
            .build()

        requestServer.request2(body, "getOrderProducts", { code, fail ->
            stateController.errorStateRead(fail)
        }
        ) { data ->
            orderComponent =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )
            if (CustomSingleton2.selectedStoreOrder!!.situationId == Situations.NEW){
                CustomSingleton2.selectedStoreOrder = CustomSingleton2.selectedStoreOrder!!.copy(situationId = Situations.VIEWED, situation = "تم الاطلاع")
                updateOrders()
            }

            stateController.successState()
        }
    }

    fun changeQuantity(product: OrderProduct) {
        stateController.startAud()

        val body = builderForm3()
            .addFormDataPart("orderId",CustomSingleton2.selectedStoreOrder!!.id.toString())
            .addFormDataPart("id",product.id.toString())
            .addFormDataPart("qnt",product.quantity.toString())
            .build()

        requestServer.request2(body, "updateOrderProductQuantity", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val orderProduct:OrderProduct =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

        orderComponent =    orderComponent!!.updateOrderProduct(orderProduct)


            val newAmount = CustomSingleton2.selectedStoreOrder!!.amounts.map {
                if (it.currencyId == product.currencyId){
                    val s = product.quantity - orderProductO.quantity
                    val am = it.amount.toDouble()
                    val n = (am + (s * product.price)).toString()
                    Log.e("ffrr",n)
                    it.copy(amount = n )
                }else it
            }
                CustomSingleton2.selectedStoreOrder = CustomSingleton2.selectedStoreOrder!!.copy(
                    amounts = newAmount
                )

            updateOrders()


//          orderComponent!!.copy(
//
//
//          ) =  orderComponent!!.orderProducts .map {
//                if (it.id == orderProduct.id){
//                    orderProduct
//                }else
//                    it
//            }

            isShowControllProduct = false
            stateController.successStateAUD()
        }
    }

    private fun updateOrders() {
        val newOrders = CustomSingleton2.storeOrders!!.orders.map { order ->
            if (order.id == CustomSingleton2.selectedStoreOrder!!.id)
                CustomSingleton2.selectedStoreOrder!!
            else order
        }
        CustomSingleton2.storeOrders = CustomSingleton2.storeOrders!!.copy(orders = newOrders)
    }

    fun cancelOrder() {
        stateController.startAud()

        val body = builderForm3()
            .addFormDataPart("orderId",CustomSingleton2.selectedStoreOrder!!.id.toString())
            .build()

        requestServer.request2(body, "cancelOrder", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            CustomSingleton2.selectedStoreOrder = CustomSingleton2.selectedStoreOrder!!.copy(situationId = Situations.CANCELED, situation = "تم الغاء الطلب")
            updateOrders()
            stateController.successStateAUD()
        }
    }
    fun completeOrder() {
        stateController.startAud()

        val body = builderForm3()
            .addFormDataPart("orderId",CustomSingleton2.selectedStoreOrder!!.id.toString())
            .build()

        requestServer.request2(body, "completeOrder", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            CustomSingleton2.selectedStoreOrder = CustomSingleton2.selectedStoreOrder!!.copy(situationId = Situations.COMPLETED, situation = "تم الغاء الطلب")
            updateOrders()
            stateController.successStateAUD()
        }
    }
    fun chooseDeliveryMan(id:String) {
        stateController.startAud()

        val body = builderForm3()
            .addFormDataPart("orderId",CustomSingleton2.selectedStoreOrder!!.id.toString())
            .addFormDataPart("deliveryManId",id)
            .build()

        requestServer.request2(body, "updateOrderDeliveryMan", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val orderDelivery:OrderDelivery =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            orderComponent = orderComponent!!.updateOrderDelivery(orderDelivery)
//          orderComponent!!.copy(
//
//
//          ) =  orderComponent!!.orderProducts .map {
//                if (it.id == orderProduct.id){
//                    orderProduct
//                }else
//                    it
//            }

            isShowChooseDeliveryMan = false
            stateController.successStateAUD()
        }
    }
    fun deleteOrderProducts(ids:List<Int>,onDone:()->Unit) {
        stateController.startAud()

        val body = builderForm3()
            .addFormDataPart("orderId",CustomSingleton2.selectedStoreOrder!!.id.toString())
            .addFormDataPart("ids",ids.toString())
            .build()

        requestServer.request2(body, "deleteOrderProducts", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
//            val orderProduct:OrderProduct =
//                MyJson.IgnoreUnknownKeys.decodeFromString(
//                    data
//                )



            orderComponent =  orderComponent!!.filterProduct(ids)
            onDone()
            stateController.successStateAUD()
        }
    }

    fun readDeliveryMen() {
        stateController.startAud()

        val body = builderForm2()
            .addFormDataPart("storeId", SelectedStore.store.value!!.id.toString())

// Check if deliveryMan is not null and add the corresponding part to the form data
        if (orderComponent!!.orderDelivery!= null && orderComponent!!.orderDelivery!!.storeDeliveryMan != null) {
            body.addFormDataPart("deliveryManId", orderComponent!!.orderDelivery!!.storeDeliveryMan!!.id.toString())
        }

//        body.build()

        requestServer.request2(body.build(), "getDeliveryMen", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            deliveryMen = MyJson.IgnoreUnknownKeys.decodeFromString(data)
//            if (deliveryMen)
            stateController.successStateAUD()
        }
    }




    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalControll() {
        var product by remember { mutableStateOf(orderProductO) }
        ModalBottomSheet(
            onDismissRequest = { isShowControllProduct = false }) {
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

                            if (product.quantity != orderProductO.quantity)
                                    Button(onClick = {

                                        changeQuantity(product)
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
        if (deliveryMen.isEmpty())readDeliveryMen()
        ModalBottomSheet(
            onDismissRequest = { isShowChooseDeliveryMan = false }) {
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

                    itemsIndexed(deliveryMen){index, item ->
                        CustomCard2( modifierBox = Modifier
                            .fillMaxSize()
                            .clickable {

                            }) {
                            Column {
                                CustomRow {
                                    Text( " اسم الموصل : "+item.firstName.toString(),Modifier.padding(8.dp))
                                    Button(onClick = {

                                        chooseDeliveryMan(item.id.toString())
                    //                                        isShowChooseDeliveryMan = true
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


}
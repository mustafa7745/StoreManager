package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.models.DeliveryMan
import com.fekraplatform.storemanger.models.OrderComponent
import com.fekraplatform.storemanger.models.OrderDelivery
import com.fekraplatform.storemanger.models.OrderProduct
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomIcon2
import com.fekraplatform.storemanger.shared.CustomRow
import com.fekraplatform.storemanger.shared.CustomRow2
import com.fekraplatform.storemanger.shared.CustomSingleton2
import com.fekraplatform.storemanger.shared.IconDelete
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
    private var deliveryMen by mutableStateOf<List<DeliveryMan>>(listOf())
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
                    0.dp, stateController, this,{
                         read()
                    }

                ) {
                    var ids by remember { mutableStateOf<List<Int>>(emptyList()) }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        stickyHeader {
                            if (CustomSingleton2.selectedStoreOrder!!.situationId !in listOf(Situations.CANCELED,Situations.COMPLETED) )
                                CustomCard2(modifierBox = Modifier.fillMaxWidth().padding(8.dp)) {

                                    Text( " رقم الطلب: " + CustomSingleton2.selectedStoreOrder!! .id.toString(),Modifier.padding(8.dp))
                                    Text( "الحالة : " + CustomSingleton2.selectedStoreOrder!!.situation.toString(),Modifier.padding(8.dp))
                                    Text( " اسم المستخدم : "+CustomSingleton2.selectedStoreOrder!!.userName.toString(),Modifier.padding(8.dp))
                                    Text( "تاريخ الطلب : "+CustomSingleton2.selectedStoreOrder!!.createdAt.toString(),Modifier.padding(8.dp))
                                    Text(  " رقم المستخدم : "+ CustomSingleton2.selectedStoreOrder!!.userPhone.toString(),
                                        Modifier
                                            .padding(8.dp)
                                            .clickable {
                            //                                                intentFunUrl("tel:${CustomSingleton2.selectedStoreOrder!!.userPhone}")
                                            })
                                    Text(
                                        text = CustomSingleton2.selectedStoreOrder!!.amounts.joinToString(
                                            separator = " و "
                                        ) { formatPrice(it.amount)  +" "+ it.currencyName },
                                        fontSize = 14.sp,
                                    )


                                    Button(onClick = {

                                        cancelOrder()
                                    }) { Text("الغاء الطلب") }
                                }
                        }
                        item {
                            CustomCard2(
                                modifierBox = Modifier
                                    .fillMaxSize()
                                    .clickable {

                                    }
                            ){
                                Column {
                                    CustomRow  {
                                        Text("معلومات الدفع", fontSize = 20.sp)
                                        Image(
                                            painter = rememberImagePainter(R.drawable.epay),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .padding(bottom = 8.dp)
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
                                                    ))
                                        )
                                    }
                                    HorizontalDivider(Modifier.padding(8.dp))

                                    CustomRow2 {
                                        Text(" طريقة الدفع : ")
                                        Text(if (orderComponent!!.orderDetail.paid == 0) "عند التوصيل" else "الكترونيا")
                                    }

                                    if (orderComponent!!.orderPayment != null){
                                        CustomRow {
                                            Text("معلومات الدفع الالكتروني:")
                                            AsyncImage(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .padding(10.dp),
                                                model = orderComponent!!.orderPayment!!.paymentImage,
                                                contentDescription = null
                                            )
                                        }

                                        CustomRow2 {
                                            Text("الدفع عن طريق: ")
                                            Text(orderComponent!!.orderPayment!!.paymentName)
                                        }
                                        CustomRow2 {
                                            Text("تاريخ الدفع: ")
                                            Text(orderComponent!!.orderPayment!!.createdAt)
                                        }
                                    } else{
                                        Text(  if (orderComponent!!.orderDetail.paid != 0) "لم يتم ادخال كود الشراء بعد" else "لم يتم الدفع بعد" ,Modifier.padding(8.dp))
                                    }


                                }


                            }
                            HorizontalDivider()
                        }
                        item {
                            CustomCard2(
                                modifierBox = Modifier
                                    .fillMaxSize()
                                    .clickable {

                                    }
                            ){
                                Column {
                                    CustomRow  {
                                        Text("معلومات التوصيل", fontSize = 20.sp)
                                        Image(
                                            painter = rememberImagePainter(R.drawable.delivery),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .padding(bottom = 8.dp)
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
                                                    ))
                                        )
                                    }
                                    HorizontalDivider(Modifier.padding(8.dp))
                                    if (orderComponent!!.orderDelivery != null){

                                        if (orderComponent!!.orderDelivery!!.deliveryMan != null){
                                            CustomRow2 {
                                                Text("موصل الطلب: ")
                                                Text(orderComponent!!.orderDelivery!!.deliveryMan!!.firstName,
                                                    Modifier
                                                        .padding(8.dp)
                                                        .clickable {
                                                            isShowChooseDeliveryMan = true
                                                        })
                                            }

                                        }else{
                                            CustomRow {
                                                Text("موصل الطلب: ")
                                                Button(onClick = {
                                                    isShowChooseDeliveryMan =true
                                                }) {
                                                    Text("اختيار موصل الطلب")
                                                }
                                            }
                                        }
                                        Text("موقع توصيل الطلب: ")
                                        CustomRow2 {
                                            Text("شارع: ")
                                            Text(orderComponent!!.orderDelivery!!.street)

                                        }
                                        CustomRow2 {
                                            Text("الموقع على الخريطه: ")
                                            CustomIcon(Icons.Default.Place,true) {
                                                val googleMapsUrl = "https://www.google.com/maps?q=${orderComponent!!.orderDelivery!!.latLng}"
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUrl))
                                                startActivity(intent)}
                                        }

                                    }else{
                                        Text("هذا الطلب بدون توصيل")
                                    }
                                }


                            }
                            HorizontalDivider()

                        }
                        stickyHeader {
                            CustomCard2(modifierBox = Modifier) {
                                CustomRow {
                                    CustomIcon2(Icons.Default.Add, modifierIcon = Modifier,true) {

                                    }
                                    IconDelete(ids) {
                                        deleteOrderProducts(ids){
                                            ids = emptyList()
                                        }
                                    }
                                }
                            }
                        }
                        itemsIndexed(orderComponent!!.orderProducts) { index: Int, orderProduct:OrderProduct ->

                            CustomCard2(
                                modifierBox = Modifier
                                    .fillMaxSize()
                                    .clickable {

                                    }
                            ) {


                                Column {

                        //                                    Log.e(
                        //                                        "image", SingletonRemoteConfig.remoteConfig.BASE_IMAGE_URL +
                        //                                                SingletonRemoteConfig.remoteConfig.SUB_FOLDER_PRODUCT +
                        //                                                cartProduct.product.images.first()
                        //                                    )
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(orderProduct.productName)
                                        Text(orderProduct.optionName)
                                        CustomIcon(Icons.Outlined.MoreVert,true) {
                                            orderProductO =orderProduct
                                            isShowControllProduct = true
                                        }
                                        Checkbox(checked = ids.find { it == orderProduct.id } != null, onCheckedChange = {
                                            val itemC = ids.find { it == orderProduct.id}
                                            if (itemC == null) {
                                                ids = ids + orderProduct.id
                                            }else{
                                                ids = ids - orderProduct.id
                                            }
                                        })
                        //                                            Text(
                        //                                                modifier = Modifier.padding(8.dp),
                        //                                                text = formatPrice(orderProduct.price.toString()) +" "+ orderProduct.currencyName,
                        //                                                fontWeight = FontWeight.Bold,
                        ////                                                color = MaterialTheme.colorScheme.primary
                        //                                            )
                        //                                            ADControll(
                        //                                                orderProduct.product,
                        //                                                option.productOption
                        //                                            )
                                    }
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(formatPrice((orderProduct.price).toString()))
                                        Text(orderProduct.quantity.toString())
                                        Text(
                                            modifier = Modifier.padding(8.dp),
                                            text = formatPrice((orderProduct.price * orderProduct.quantity).toString()) +" "+ orderProduct.currencyName,
                                            fontWeight = FontWeight.Bold,
                        //                                                color = MaterialTheme.colorScheme.primary
                                        )
                        //                                            ADControll(
                        //                                                orderProduct.product,
                        //                                                option.productOption
                        //                                            )
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
                CustomSingleton2.selectedStoreOrder = CustomSingleton2.selectedStoreOrder!!.copy(situationId = Situations.VIEWD, situation = "تم الاطلاع")
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
        if (orderComponent!!.orderDelivery!= null && orderComponent!!.orderDelivery!!.deliveryMan != null) {
            body.addFormDataPart("deliveryManId", orderComponent!!.orderDelivery!!.deliveryMan!!.id.toString())
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
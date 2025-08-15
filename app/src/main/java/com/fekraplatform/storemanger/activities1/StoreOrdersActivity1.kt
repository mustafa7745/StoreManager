package com.fekraplatform.storemanger.activities1

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.activities.getCurrentDate
import com.fekraplatform.storemanger.models.CustomOption
import com.fekraplatform.storemanger.models.Order
import com.fekraplatform.storemanger.models.OrderAmount
import com.fekraplatform.storemanger.models.OrdersHome
import com.fekraplatform.storemanger.models.StoreOrders
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.MainCompose
import com.fekraplatform.storemanger.shared.MyHeader
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.ServerConfig
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.formatPrice
import com.fekraplatform.storemanger.storage.MyAppStorage1
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StoreOrdersViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    private val serverConfig: ServerConfig,
    val appSession: AppSession,
    private val myAppStorage: MyAppStorage1,
    private val builder: FormBuilder,
    private val situationDao: OrderSituationDao,
    private val ordersDao: OrdersDao): ViewModel() {


    val stateController = StateController()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    var fromDate by mutableStateOf(getCurrentDate().withDayOfMonth(1) .format(formatter).toString())
    var toDate by mutableStateOf(getCurrentDate().format(formatter).toString())
    var isFrom by mutableStateOf(false)
    var isShowSelectDate by mutableStateOf(false)
    var situationsIds = emptyList<Int>()



    var selectedCustomOption by mutableStateOf<OrderSituationEntity?>(null)

    var isLoadingMore by mutableStateOf(false)
    var isHaveMore by mutableStateOf(false)

    fun read(situtionIds:String = selectedCustomOption?.let { listOf(it.id).toString() }.toString(), onSuccess:()->Unit={}) {
//        if (!isLoadingMore)
        stateController.startAud()

        val body = builder.sharedBuilderFormWithStoreId()
            .addFormDataPart("situationId",situtionIds )
            .addFormDataPart("fromDate",fromDate)
            .addFormDataPart("toDate",toDate)
            .addFormDataPart("from", if  (isLoadingMore)appSession.storeOrders!!.orders.size.toString() else "0")

        viewModelScope.launch {
            try {
                val data = requestServer.request(body, "getOrders")
                val result:List<Order> = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                if (isLoadingMore){
                    appSession.storeOrders =  appSession.storeOrders!!.copy(orders = appSession.storeOrders!!.orders + result)
                }
                else{
                    appSession.storeOrders = appSession.storeOrders!!.copy(orders = result)
                }


                if (result.size >= 7){
                    isHaveMore = true
                }
                else{
                    isHaveMore = false
                }
                isLoadingMore = false
                stateController.successStateAUD()
                onSuccess()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun readSituations() {
        stateController.startRead()

        val body = builder.sharedBuilderFormWithStoreId()
            .addFormDataPart("fromDate",fromDate)
            .addFormDataPart("toDate",toDate)
            .addFormDataPart("from","0")
        viewModelScope.launch {
            try {
                val data = requestServer.request(body, "getOrderSituations")
                appSession.storeOrders = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
//                selectedCustomOption = appSession.storeOrders!!.situations.first()
//
                if (appSession.storeOrders!!.orders.size >= 7){
                    isHaveMore = true
                }
                else{
                    isHaveMore = false
                }
                stateController.successState()
            } catch (e: Exception) {
                stateController.errorStateRead(e.message.toString())
            }
        }
    }

    fun readOrdersHome() {
        stateController.startRead()
        val zoneId = ZoneId.of(appSession.selectedStore.timezone)
        val toDateInnerStr = LocalDate
            .parse(toDate)
            .atTime(23, 59, 59)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val fromDateInnerStr = LocalDate
            .parse(fromDate)
            .atTime(0, 0, 0)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))



        val addList = listOf(OrderSituationEntity(9997,"قيد المعالجة","",""),OrderSituationEntity(9998,"مكتملة","",""))
        selectedCustomOption = addList.first()
        situationsIds = listOf(1,4,5,6,7)
        viewModelScope.launch {
            try {
                // 1. جلب البيانات القديمة من قاعدة البيانات
                val oldSituations = situationDao.getAll()
                Log.e("orderSituationsBefore", oldSituations.toString())

                val from = 0
                val oldOrders = ordersDao.getAll(appSession.selectedStore.id,situationsIds,fromDateInnerStr,toDateInnerStr,from)
                Log.e("orderBefore", oldOrders.toString())

                // 2. تحضير البيانات لإرسالها إلى السيرفر
                val orderSituationUpdatedAt = getOldUpdatedAt(oldSituations.map { it.updatedAt })
                println("ff"+orderSituationUpdatedAt)
                val orderOrdersUpdatedAt = getOldUpdatedAt(oldOrders.map { it.updatedAt })
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("orderSituationUpdatedAt", orderSituationUpdatedAt)
                    .addFormDataPart("ordersUpdatedAt", orderOrdersUpdatedAt)
                    .addFormDataPart("situationIds",situationsIds.toString() )
                    .addFormDataPart("fromDate",fromDateInnerStr)
                    .addFormDataPart("toDate",toDateInnerStr)
                    .addFormDataPart("from",from.toString())

                // 3. تنفيذ الطلب
                val response = requestServer.request(body, "getOrdersHome")
                val result = MyJson.IgnoreUnknownKeys.decodeFromString<OrdersHome>(response as String)
                Log.e("result", result.toString())


                result.situations.forEach { situationDao.insert(it) }
                result.orders.forEach { ordersDao.insert(it) }

                val updatedSituations = addList + situationDao.getAll()
                println(fromDateInnerStr)
                println(toDateInnerStr)
                val updatedOrders = ordersDao.getAll(appSession.selectedStore.id,situationsIds,fromDateInnerStr,toDateInnerStr,0)
                Log.e("orderSituationsAfter", updatedSituations.toString())

//                selectedCustomOption =addList + updatedSituations.first()

                appSession.storeOrders2 = OrdersHome(situations = updatedSituations, orders = updatedOrders, pendingIds = result.pendingIds, completedIds = result.completedIds)
                // 6. نجاح
                stateController.successState()
            } catch (e: Exception) {
                // 7. فشل
                Log.e("readOrdersHomeError", e.toString(), e)
                stateController.errorStateRead(e.message ?: "Unknown error")
            }
        }
    }
    fun readOrdersHomeNoDB() {
        stateController.startRead()
        val zoneId = ZoneId.of(appSession.selectedStore.timezone)
        val toDateInnerStr = LocalDate
            .parse(toDate)
            .atTime(23, 59, 59)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val fromDateInnerStr = LocalDate
            .parse(fromDate)
            .atTime(0, 0, 0)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))



        val addList = listOf(OrderSituationEntity(9997,"قيد المعالجة","",""),OrderSituationEntity(9998,"مكتملة","",""))
        selectedCustomOption = addList.first()

        situationsIds = listOf(1,4,5,6,7)
        viewModelScope.launch {
            try {
               val from = 0
                val orderSituationUpdatedAt = getOldUpdatedAt(emptyList())
                println("ff"+orderSituationUpdatedAt)
                val orderOrdersUpdatedAt = getOldUpdatedAt(emptyList())
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("orderSituationUpdatedAt", orderSituationUpdatedAt)
                    .addFormDataPart("ordersUpdatedAt", orderOrdersUpdatedAt)
                    .addFormDataPart("situationIds",situationsIds.toString() )
                    .addFormDataPart("fromDate",fromDateInnerStr)
                    .addFormDataPart("toDate",toDateInnerStr)
                    .addFormDataPart("from",from.toString())

                // 3. تنفيذ الطلب
                val response = requestServer.request(body, "getOrdersHome")
                val result = MyJson.IgnoreUnknownKeys.decodeFromString<OrdersHome>(response as String)
                Log.e("result", result.toString())


                appSession.storeOrders2 = OrdersHome(situations = addList + result.situations, orders = result.orders, pendingIds = result.pendingIds, completedIds = result.completedIds)
                // 6. نجاح
                stateController.successState()
            } catch (e: Exception) {
                // 7. فشل
                Log.e("readOrdersHomeError", e.toString(), e)
                stateController.errorStateRead(e.message ?: "Unknown error")
            }
        }
    }


    fun getOldUpdatedAt(list: List<String>): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // صيغة التاريخ القادمة من MySQL

        val oldest = "1970-01-01 00:00:00"

        return list
            .mapNotNull {
                runCatching {
                    val parsed = LocalDateTime.parse(it, inputFormatter)
                    parsed to parsed.format(inputFormatter)
                }.getOrNull()
            }
            .maxByOrNull { it.first }  // ترتيب حسب التاريخ الفعلي
            ?.second ?: oldest         // أرجع التنسيق المطلوب أو أقدم تاريخ
    }
    init {

//        readSituations()
//        readOrdersHome()
        readOrdersHomeNoDB()
    }
}

@Serializable
@Entity(tableName = "ordersSituations")
data class OrderSituationEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val createdAt: String,
    val updatedAt: String,
)


@Dao
interface OrderSituationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(orderSituationEntity: OrderSituationEntity)

    @Query("SELECT * FROM ordersSituations order by id desc")
    suspend fun getAll(): List<OrderSituationEntity>

//    @Query("SELECT * FROM billing_items WHERE purchaseToken = :token LIMIT 1")
//    suspend fun getByToken(token: String): BillingEntity?
//
//    @Delete
//    suspend fun delete(billing: BillingEntity)
}

@Serializable
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: Int,
    val userName: String,
    val situationId: Int,
    val storeId: Int,
    val withApp: Int,
    val paid: Int,
    val inStore:Int,
    val systemOrderNumber: String?,
    val updatedAt: String,
    val createdAt: String,
    val userPhone: String,
    val amounts:List<OrderAmount>
)


@Dao
interface OrdersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(orderEntity: OrderEntity)

    @Query("SELECT * FROM orders WHERE storeId = :storeId AND situationId IN (:situationIds) AND (createdAt BETWEEN :fromDate AND :toDate) order by createdAt desc LIMIT 7 OFFSET :offset")
    suspend fun getAll(
        storeId: Int,
        situationIds: List<Int>,
        fromDate: String,
        toDate: String,
        offset: Int
    ): List<OrderEntity>

//    @Query("SELECT * FROM billing_items WHERE purchaseToken = :token LIMIT 1")
//    suspend fun getByToken(token: String): BillingEntity?
//
//    @Delete
//    suspend fun delete(billing: BillingEntity)
}

@AndroidEntryPoint
class StoreOrdersActivity1 : ComponentActivity() {
    val viewModel: StoreOrdersViewModel by viewModels()





    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StoreMangerTheme {
                MainCompose(
                    viewModel.stateController, {
                        if (viewModel.appSession.storeOrders2 != null) {
                            viewModel.read()
                        } else {
                            viewModel.readSituations()
                        }
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
                                                viewModel.isShowSelectDate = true
                                                viewModel.isFrom = true
                                            }
                                        ) {
                                            Text(
                                                "من",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                viewModel.fromDate,
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
                                                viewModel.isShowSelectDate = true
                                                viewModel.isFrom = false
                                            }
                                        ) {
                                            Text(
                                                "الى",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                viewModel.toDate,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        itemsIndexed(viewModel.appSession.storeOrders2!!.orders) { index, order ->
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
                                        val situation = viewModel.appSession.storeOrders2!!.situations.find { it.id == order.situationId }

                                        if (situation != null)
                                            Surface(
                                                modifier = Modifier.fillMaxWidth().padding(4.dp),
                                                color =
                                                MaterialTheme.colorScheme.secondary
                                                ,
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Text(
                                                    situation.name.toString(),
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            }

//                                        Surface(
//                                            color = when (order.situation) {
//                                                "مكتمل" -> Color(0xFF4CAF50)
//                                                "قيد المعالجة" -> Color(0xFFFFA000)
//                                                else -> MaterialTheme.colorScheme.secondary
//                                            },
//                                            shape = RoundedCornerShape(16.dp)
//                                        ) {
//                                            Text(
//                                                order.situation.toString(),
//                                                modifier = Modifier.padding(
//                                                    horizontal = 12.dp,
//                                                    vertical = 4.dp
//                                                ),
//                                                color = Color.White,
//                                                style = MaterialTheme.typography.labelMedium
//                                            )
//                                        }
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
                                    Text(
                                        "تاريخ التحديث: ${order.updatedAt}",
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
//                                            viewModel.selectedStoreOrder = order
                                            gotoOrderProducts(order)
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
                            if (viewModel.isLoadingMore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else if (viewModel.isHaveMore) {
                                Button(
                                    onClick = {
                                        viewModel.isLoadingMore = true
                                        viewModel.read()
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

                    if (viewModel.isShowSelectDate) DatePickerModal()
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
                onDismissRequest = { viewModel.isShowSelectDate = false },
                confirmButton = {
                    TextButton(onClick = {
                        if (datePickerState.selectedDateMillis != null){
                            if (viewModel.isFrom)viewModel.fromDate = convertMillisToDate(datePickerState.selectedDateMillis!!)
                            else
                                viewModel.toDate = convertMillisToDate(datePickerState.selectedDateMillis!!)
                            viewModel.isShowSelectDate = false
                        }

//                    datePickerState.selectedDateMillis
//                    onDateSelected(datePickerState.selectedDateMillis)
//                    onDismiss()
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.isShowSelectDate = false }) {
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
                            Text("الطلبات"+" | "+viewModel.selectedCustomOption!!.name)
//                        Text(text = CustomSingleton.selectedStore!!.name )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "")
                    }
                    DropdownMenu(
                        expanded = isDropDownExpanded.value,
                        onDismissRequest = {
                            isDropDownExpanded.value = false
                        }) {
//                        DropdownMenuItem(text = {
//                            Row {
//                                Text(text = "مكتملة")
//
//                            }
//                        },
//                            onClick = {
////                                isDropDownExpanded.value = false
////                                viewModel.read (listOf(2,3).toString()){
////                                    viewModel.selectedCustomOption = CustomOption(0,"مكتملة")
////                                }
//                            })
//
//                        DropdownMenuItem(text = {
//                            Row {
//                                Text(text = "قيد المعالجة")
//
//                            }
//                        },
//                            onClick = {
////                                isDropDownExpanded.value = false
////                                viewModel.read (listOf(1,4,5,6,7).toString()){
////                                    viewModel.selectedCustomOption = CustomOption(0,"قيد المعالجة")
////                                }
//                            })

                        viewModel.appSession.storeOrders2!!.situations.forEachIndexed { index, situationEntity ->
                            DropdownMenuItem(text = {
                                Row {
                                    Text(text = situationEntity.name)
                                }
                            },
                                onClick = {
                                    isDropDownExpanded.value = false
                                    viewModel.read (listOf(situationEntity.id).toString()){
                                        viewModel.selectedCustomOption = situationEntity
                                    }
                                })
                        }
                    }
                }

            }
        }


        private fun gotoOrderProducts(order: OrderEntity) {
            val intent = Intent(this, OrderProductsActivity1::class.java)
        intent.putExtra("order", MyJson.MyJson.encodeToString(order))
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
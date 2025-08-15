package com.fekraplatform.storemanger.activities1

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.models.Coupon
import com.fekraplatform.storemanger.models.StoreCurrency
import com.fekraplatform.storemanger.shared.MainComposeRead
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.confirmDialog
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreCouponsViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    private val builder: FormBuilder,
    val appSession: AppSession,
    @ApplicationContext private val context: Context
): ViewModel(){
    val stateController = StateController()
    var isShowAddDelivery by mutableStateOf(false)
    fun read() {
        viewModelScope.launch {
            stateController.startRead()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                val data = requestServer.request(body, "getCoupons")
                appSession.coupons = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                stateController.successState()
            } catch (e: Exception) {
                stateController.errorStateRead(e.message.toString())
            }
        }
    }
    fun add(
        countUsed: Int?,
             discountType: Int,
             currencyId: Int?,
             amount: Double) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                    if (countUsed != null){
                        body.addFormDataPart("countUsed", countUsed.toString())
                    }
                body.addFormDataPart("discountType", discountType.toString())
                body.addFormDataPart("currencyId", currencyId.toString())
                body.addFormDataPart("amount", amount.toString())

                val data = requestServer.request(body, "addCoupon")
                val result: Coupon = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                appSession.coupons += result
                isShowAddDelivery = false
                stateController.successStateAUD("تمت الاضافه  بنجاح")
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

    fun stop(id: String) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                body.addFormDataPart("id", id.toString())

                val data = requestServer.request(body, "stopCoupon")
//                val result: Coupon = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                appSession.coupons  = appSession.coupons.map {
                    if (it.id.toString() == id) it.copy(isActive = 0) else it
                }
                stateController.successStateAUD("تمت الايقاف  بنجاح")
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
class StoreCouponsActivity : ComponentActivity() {
   val viewModel:StoreCouponsViewModel by viewModels()


    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StoreMangerTheme {
                MainComposeRead (
                    "ادارة الكوبونات",viewModel.stateController, { finish() },{
                        viewModel.read()
                    }

                ) {
                    val clipboardManager = LocalClipboardManager.current
                    val context = LocalContext.current

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Button(
                                onClick = { viewModel.isShowAddDelivery = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text("إضافة كوبون جديد")
                            }
                        }

                        itemsIndexed(viewModel.appSession.coupons) { index, order ->
                            val currency = viewModel.appSession.selectedStore.storeCurrencies
                                .find { it.currencyId == order.currencyId }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("🧾 الكود: ${order.code ?: "-"}", fontWeight = FontWeight.Bold)
                                        IconButton(onClick = {
                                            order.code.let {
                                                clipboardManager.setText(AnnotatedString(it))
                                                Toast.makeText(context, "تم نسخ الكود", Toast.LENGTH_SHORT).show()
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "نسخ الكود"
                                            )
                                        }
                                    }

                                    Text("النوع: ${if (order.type == 1) "نسبة مئوية" else "مبلغ نقدي"}")
                                    Text("قيمة الخصم: ${order.amount} ${if (order.type == 1) "%" else currency?.currencyName ?: ""}")
                                    Text("العملة: ${currency?.currencyName ?: order.currencyId}")
                                    Text("تم استخدامه: ${order.used}")
                                    if (order.countUsed != null) {
                                        Text("عدد مرات الاستخدام المسموح بها: ${order.countUsed}")
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text("نشط", modifier = Modifier.padding(end = 8.dp))
                                        Switch(
                                            checked = order.isActive == 1,
                                            onCheckedChange = {
                                                confirmDialog(this@StoreCouponsActivity,"",false){
                                                    viewModel.stop(order.id.toString())
                                                }
                                                },
                                            enabled = order.isActive == 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (viewModel.isShowAddDelivery) modalAdd()
                }
            }
        }



    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAdd() {

        var countUsedEnabled by remember { mutableStateOf(false) }
        var discountType by remember { mutableStateOf(1) } // 0 = نقدي, 1 = مئوي
        var selectedCurrency by remember { mutableStateOf<StoreCurrency?>(null) }
        var countUsed by remember { mutableIntStateOf(0) }
        var amount by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowAddDelivery = false }
        ) {
            LazyColumn (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                item {
                    // ✅ عدد مرات الاستخدام
                    CompositionLocalProvider(LocalTextStyle provides TextStyle(textDirection = TextDirection.Ltr)) {
                        OutlinedTextField(
                            leadingIcon = {
                                Switch(
                                    checked = countUsedEnabled,
                                    onCheckedChange = { countUsedEnabled = it },
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            },
                            enabled = countUsedEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            value = countUsed.toString(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("مرات الاستخدام") },
                            onValueChange = {
                                countUsed = it.toIntOrNull() ?: 0
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ✅ اختيار نوع الخصم
                    Column(Modifier.selectableGroup()) {
                        Text("نوع الخصم", modifier = Modifier.padding(vertical = 8.dp))

                        listOf(0, 1).forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = (discountType == type),
                                        onClick = { discountType = type },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = discountType == type, onClick = null)
                                Text(
                                    text = if (type == 1) "مئوي" else "نقدي",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ✅ اختيار العملة
                    Column(Modifier.selectableGroup()) {
                        val currencies = viewModel.appSession.selectedStore.storeCurrencies
                        Text("عملة الخصم", modifier = Modifier.padding(vertical = 8.dp))

                        currencies.forEach { currency:StoreCurrency ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = selectedCurrency == currency,
                                        onClick = { selectedCurrency = currency },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedCurrency == currency, onClick = null)
                                Text(
                                    text = currency.currencyName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ✅ كمية الخصم
                    // ✅ كمية الخصم
                    CompositionLocalProvider(LocalTextStyle provides TextStyle(textDirection = TextDirection.Ltr)) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = amount,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = {
                                Text(if (discountType == 1) "نسبة الخصم (%)" else "قيمة الخصم")
                            },
                            onValueChange = {
                                val newValue = it.toDoubleOrNull()

                                if (newValue != null) {
                                    amount = if (discountType == 1) {
                                        // خصم مئوي: لا يزيد عن 100
                                        if (newValue <= 100) it else "100"
                                    } else {
                                        // خصم نقدي: أي قيمة مسموح بها
                                        it
                                    }
                                } else {
                                    // لو دخل قيمة غير صالحة (مثل نص)، نسمح بالقيمة ليتعامل معها المستخدم
                                    amount = it
                                }
                            }
                        )
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    // ✅ زر التأكيد
                    Button(
                        enabled = selectedCurrency != null && (amount.toDoubleOrNull() ?: 0.0) > 0,
                        onClick = {
                            // استخدم القيم:
                            // countUsedEnabled, countUsed, discountType, selectedCurrency, amount
                        viewModel.add(
                            countUsed = if(countUsedEnabled && countUsed > 0) countUsed else null,
                            discountType = discountType,
                            currencyId = selectedCurrency!!.currencyId,
                            amount = amount.toDoubleOrNull() ?: 0.0
                        )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ الخصم")
                    }
                }

            }
        }
    }


}
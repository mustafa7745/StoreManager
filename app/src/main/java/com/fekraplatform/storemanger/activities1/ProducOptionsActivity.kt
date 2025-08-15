package com.fekraplatform.storemanger.activities1

import android.annotation.SuppressLint
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.models.HomeProduct
import com.fekraplatform.storemanger.models.PrimaryProduct
import com.fekraplatform.storemanger.models.PrimaryStoreProduct
import com.fekraplatform.storemanger.models.ProductImage
import com.fekraplatform.storemanger.models.StoreCurrency
import com.fekraplatform.storemanger.models.StoreNestedSection
import com.fekraplatform.storemanger.models.StoreProduct
import com.fekraplatform.storemanger.models.StoreProductView
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomImageView
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.MainComposeAUD
import com.fekraplatform.storemanger.shared.MainComposeRead
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.confirmDialog
import com.fekraplatform.storemanger.shared.formatPrice
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class ProductOptionsViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    val appSession: AppSession,
    private val builder: FormBuilder,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
): ViewModel()
{
    val stateController = StateController()
    var isShowProducts by mutableStateOf(false)
    var isShowUpdate by mutableStateOf(false)
    var isShowAdd by mutableStateOf(false)
    var productId: Int =  MyJson.IgnoreUnknownKeys.decodeFromString(savedStateHandle["productId"]?:"")
    var storeNestedSectionId: Int =  MyJson.IgnoreUnknownKeys.decodeFromString(savedStateHandle["storeNestedSectionId"]?:"")
    var storeProductViewId: Int =  MyJson.IgnoreUnknownKeys.decodeFromString(savedStateHandle["storeProductViewId"]?:"")
    fun getString(@StringRes resId: Int): String = context.getString(resId)
    lateinit var selectedStoreProduct:PrimaryStoreProduct
    var updatedFields by mutableStateOf(mapOf<String, Any>())
    ///
    fun updateStoreProduct() {
        Log.e("DAAA",updatedFields.map { it.toPair() }.toString())

        val homeStoreProduct = appSession.homeStoreProduct

        if (homeStoreProduct != null){
            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("id",selectedStoreProduct.id.toString())
                .apply {
                    updatedFields.forEach { (key, value) ->
                        val valueToSend = if (key == "info") {
                            MyJson.IgnoreUnknownKeys.encodeToString(ListSerializer(String.serializer()), value as List<String>)
                        } else {
//                            MyJson.IgnoreUnknownKeys.encodeToString(String.serializer(), value as String)
                            value.toString()
                        }
                        Log.e("DDDDD",valueToSend)

                        addFormDataPart(key, valueToSend)
                    }
                }


            viewModelScope.launch {
                stateController.startAud()
                try {
                    val data = requestServer.request(body, "updateStoreProduct")
                    val result: PrimaryStoreProduct = MyJson.IgnoreUnknownKeys.decodeFromString(data.toString())
                    val updatedStoreProducts = homeStoreProduct.storeProducts.map {
                        if (it.id == result.id) result else it
                    }

                    appSession.homeStoreProduct = homeStoreProduct.copy(storeProducts = updatedStoreProducts)
                    stateController.successStateAUD(getString(R.string.success_update))
                    updatedFields = mapOf()
                    isShowUpdate = false
                } catch (e: Exception) {
                    stateController.errorStateAUD(e.message.toString())
                }
            }
        }
    }
    fun addStoreProduct(name:String,description: String,info:String,price:String,prePrice:String,currencyId:String) {
        Log.e("DAAA",updatedFields.map { it.toPair() }.toString())

        val homeStoreProduct = appSession.homeStoreProduct

        if (homeStoreProduct != null){
            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("productId",productId.toString())
                .addFormDataPart("storeNestedSectionId",storeNestedSectionId.toString())
                .addFormDataPart("storeProductViewId",storeProductViewId.toString())
                .addFormDataPart("name",name)
                .addFormDataPart("currencyId",currencyId)
                .addFormDataPart("description",description)
                .addFormDataPart("info",info)
                .addFormDataPart("price",price)
                .addFormDataPart("prePrice",prePrice)


            viewModelScope.launch {
                stateController.startAud()
                try {
                    val data = requestServer.request(body, "addStoreProduct")
                    val result: PrimaryStoreProduct = MyJson.IgnoreUnknownKeys.decodeFromString(data.toString())
                    appSession.homeStoreProduct = homeStoreProduct.copy(storeProducts = homeStoreProduct.storeProducts + result)
                    stateController.successStateAUD(getString(R.string.success_update))
                    isShowAdd = false
                } catch (e: Exception) {
                    stateController.errorStateAUD(e.message.toString())
                }
            }
        }
    }
    fun deleteStoreProduct(id:String) {

        val homeStoreProduct = appSession.homeStoreProduct
        if (homeStoreProduct != null){

            val storeProduct = homeStoreProduct.storeProducts.firstOrNull { it.id.toString() == id }
            if ( storeProduct == null) return
            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("id",id.toString())


            viewModelScope.launch {
                stateController.startAud()
                try {
                    val data = requestServer.request(body, "deleteStoreProduct")
//                    val result: ProductImage = MyJson.IgnoreUnknownKeys.decodeFromString(data.toString())
                    appSession.homeStoreProduct = homeStoreProduct.copy(storeProducts = homeStoreProduct.storeProducts - storeProduct)
                    stateController.successStateAUD(getString(R.string.success_delete))
                } catch (e: Exception) {
                    stateController.errorStateAUD(e.message.toString())
                }
            }
        }
    }
}

@AndroidEntryPoint
class ProductOptionsActivity : ComponentActivity() {
    val viewModel:ProductOptionsViewModel by viewModels()
    @OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StoreMangerTheme {  val homeStoreProduct =  viewModel.appSession.homeStoreProduct
                if (homeStoreProduct != null){
                    val product = homeStoreProduct.products.find { it.id == viewModel.productId }
                    if (product != null){
                    MainComposeAUD (
                        "خيارات المنتج"+ " " + product.name,
                        viewModel.stateController,
                        { finish() }) {
                        LazyColumn {
                            item {

                                    val productImages =
                                        homeStoreProduct
                                            .productsImages
                                            .filter { it.productId == product.id }
                                    val pagerStateImage = rememberPagerState(pageCount = { productImages.size })

                                    HorizontalPager(
                                        state = pagerStateImage,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                    ) { i ->
                                        Box {
                                            CustomImageViewUri(
                                                imageUrl = viewModel.appSession.remoteConfig.BASE_IMAGE_URL +
                                                        viewModel.appSession.remoteConfig.SUB_FOLDER_PRODUCT +
                                                        productImages[i].image,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }

                                    val storeProductOptions = homeStoreProduct.storeProducts.filter { it.productId == viewModel.productId}.sortedBy { it.orderNo }


                                    storeProductOptions.forEach { primaryStoreProduct ->
                                        val currencyName = viewModel.appSession.selectedStore.storeCurrencies
                                            .firstOrNull { it.currencyId == primaryStoreProduct.currencyId }
                                            ?.currencyName ?: ""

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp, horizontal = 12.dp)
                                                .combinedClickable(onClick = {
                                                    viewModel.selectedStoreProduct =
                                                        primaryStoreProduct
                                                    viewModel.isShowUpdate = true
                                                }, onLongClick = {

                                                    confirmDialog(this@ProductOptionsActivity, getString(R.string.confirm_delete) ,false){
                                                        viewModel.deleteStoreProduct(primaryStoreProduct.id.toString())
                                                    }

                                                }),
                                            elevation = CardDefaults.cardElevation(4.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {

                                                // اسم المنتج

                                                if (primaryStoreProduct.name.isNotEmpty()){
                                                    Text(
                                                        text = primaryStoreProduct.name,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                }




                                                // الوصف
                                                if (primaryStoreProduct.description.isNotEmpty()) {
                                                    Text(
                                                        text = primaryStoreProduct.description,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.Gray
                                                    )

                                                    Spacer(modifier = Modifier.height(4.dp))
                                                }

                                                // info أفقية
//                                                if (primaryStoreProduct.info.isNotEmpty()) {
//                                                    Row(
//                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                                                        verticalAlignment = Alignment.CenterVertically,
//                                                        modifier = Modifier
//                                                            .fillMaxWidth()
//                                                            .padding(vertical = 4.dp)
//                                                    ) {
//                                                        primaryStoreProduct.info.forEach { item ->
//                                                            Surface(
//                                                                shape = RoundedCornerShape(8.dp),
//                                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
//                                                            ) {
//                                                                Row(
//                                                                    verticalAlignment = Alignment.CenterVertically,
//                                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//                                                                ) {
//                                                                    Icon(
//                                                                        imageVector = Icons.Default.Star,
//                                                                        contentDescription = null,
//                                                                        tint = MaterialTheme.colorScheme.primary,
//                                                                        modifier = Modifier.size(14.dp)
//                                                                    )
//                                                                    Spacer(modifier = Modifier.width(4.dp))
//                                                                    Text(
//                                                                        text = item,
//                                                                        style = MaterialTheme.typography.bodySmall,
//                                                                        color = MaterialTheme.colorScheme.primary
//                                                                    )
//                                                                }
//                                                            }
//                                                        }
//                                                    }
//                                                }

                                                FlowRow(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 10.dp)
                                                ) {
                                                    primaryStoreProduct.info.forEach { item ->
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Star,
                                                                    contentDescription = null,
                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = item,
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                // السعر
//                                                Text(
//                                                    text = "${formatPrice(primaryStoreProduct.price.toString())} $currencyName",
//                                                    style = MaterialTheme.typography.bodyLarge,
//                                                    fontWeight = FontWeight.SemiBold,
//                                                    color = MaterialTheme.colorScheme.primary
//                                                )
//                                                if (primaryStoreProduct.prePrice != 0.0 && primaryStoreProduct.price > primaryStoreProduct.prePrice){
//                                                    Text(
//                                                        text = "${formatPrice(primaryStoreProduct.prePrice.toString())} $currencyName",
//                                                        style = MaterialTheme.typography.bodyLarge,
//                                                        fontWeight = FontWeight.SemiBold,
//                                                        color = MaterialTheme.colorScheme.primary
//                                                    )
//                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {

                                                    if (primaryStoreProduct.prePrice != 0.0 && primaryStoreProduct.prePrice > primaryStoreProduct.price) {
                                                        Text(
                                                            text = "${formatPrice(primaryStoreProduct.prePrice.toString())} $currencyName",
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                textDecoration = TextDecoration.LineThrough,
                                                                color = Color.Gray
                                                            )
                                                        )
                                                    }

                                                    // السعر الحالي
                                                    Text(
                                                        text = "${formatPrice(primaryStoreProduct.price.toString())} $currencyName",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                            }
                                        }
                                    }

                                OutlinedButton(
                                    onClick = {

                                        viewModel.isShowAdd = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp).padding(8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = "إضافة")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("إضافة")
                                }

                            }
                        }

                        if (viewModel.isShowUpdate) modalUpdate()
                        if (viewModel.isShowAdd) modalAdd()
                            }
                        }
                    }
                }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun modalUpdate() {
        val originalStoreProduct = viewModel.selectedStoreProduct

        var name by remember { mutableStateOf(originalStoreProduct.name) }
        var description by remember { mutableStateOf(originalStoreProduct.description) }
        var price by remember { mutableDoubleStateOf(originalStoreProduct.price) }
        var prePrice by remember { mutableDoubleStateOf(originalStoreProduct.prePrice) }
        var infos = remember { originalStoreProduct.info.toMutableStateList() }

        fun processChanges() {
            val newUpdates = mutableMapOf<String, Any>()

            if (name != originalStoreProduct.name) newUpdates["name"] = name
            if (description != originalStoreProduct.description) newUpdates["description"] = description
            if (price != originalStoreProduct.price) newUpdates["price"] = price
            if (prePrice != originalStoreProduct.prePrice) newUpdates["prePrice"] = prePrice

//            val cleanedInfos = infos.filter { it.isNotEmpty() }
            val cleanedInfos = infos.map { it.trim() }.filter { it.isNotEmpty() }
            val originalCleaned = originalStoreProduct.info.map { it.trim() }.filter { it.isNotEmpty() }

            if (cleanedInfos != originalCleaned) {
                newUpdates["info"] = cleanedInfos
            }

            viewModel.updatedFields = newUpdates
            Log.e("UpdatedFields", newUpdates.toString())
        }

        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowUpdate = false
               viewModel.updatedFields = mapOf()
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                item {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                processChanges()
                            },
                            label = { Text("وصف قصير") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                description = it
                                processChanges()
                            },
                            label = { Text("وصف آخر") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        Spacer(Modifier.height(8.dp))
                        Text("معلومات المنتج", style = MaterialTheme.typography.titleMedium)

                        infos.forEachIndexed { index, info ->
                            val text = remember(infos[index]) { mutableStateOf(info) }

                            OutlinedTextField(
                                value = text.value,
                                onValueChange = {
                                    text.value = it
                                    infos[index] = it
                                    processChanges()
                                },
                                label = { Text("معلومة ${index + 1}") },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        infos.removeAt(index)
                                        processChanges()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = "حذف",
                                            tint = Color.Red
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }


                        OutlinedButton(
                            onClick = {
                                infos += ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "إضافة معلومة")
                            Spacer(Modifier.width(8.dp))
                            Text("إضافة معلومة أخرى")
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = price.toString(),
                            onValueChange = {
                                it.toDoubleOrNull()?.let { v ->
                                    price = v
                                    processChanges()
                                }
                            },
                            label = { Text("السعر الجديد") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
                        )

                        OutlinedTextField(
                            value = prePrice.toString(),
                            onValueChange = {
                                it.toDoubleOrNull()?.let { v ->
                                    prePrice = v
                                    processChanges()
                                }
                            },
                            label = { Text("السعر السابق الجديد") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
                        )

                        OutlinedTextField(
                            value = originalStoreProduct.price.toString(),
                            enabled = false,
                            onValueChange = {},
                            label = { Text("السعر الحالي") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        OutlinedTextField(
                            value = originalStoreProduct.prePrice.toString(),
                            enabled = false,
                            onValueChange = {},
                            label = { Text("السعر السابق الحالي") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }

                if (viewModel.updatedFields.isNotEmpty()) {
                    stickyHeader {
                        OutlinedButton(
                            onClick = {
                             viewModel.updateStoreProduct()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "حفظ التعديلات")
                            Spacer(Modifier.width(8.dp))
                            Text("حفظ التعديلات")
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun modalAdd() {

        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedCurrency by remember { mutableStateOf<StoreCurrency?>(null) }
        var price by remember { mutableDoubleStateOf(0.0) }
        var prePrice by remember { mutableDoubleStateOf(0.0) }
        var infos = remember {  emptyList<String>().toMutableStateList() }


        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowAdd = false }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                item {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                            },
                            label = { Text("وصف قصير") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                description = it
                            },
                            label = { Text("وصف آخر") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        Spacer(Modifier.height(8.dp))
                        Text("معلومات المنتج", style = MaterialTheme.typography.titleMedium)

                        infos.forEachIndexed { index, info ->
                            val text = remember(infos[index]) { mutableStateOf(info) }

                            OutlinedTextField(
                                value = text.value,
                                onValueChange = {
                                    text.value = it
                                    infos[index] = it
                                },
                                label = { Text("معلومة ${index + 1}") },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        infos.removeAt(index)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = "حذف",
                                            tint = Color.Red
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }


                        OutlinedButton(
                            onClick = {
                               infos += ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "إضافة معلومة")
                            Spacer(Modifier.width(8.dp))
                            Text("إضافة معلومة")
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = price.toString(),
                            onValueChange = {
                                it.toDoubleOrNull()?.let { v ->
                                    price = v
                                }
                            },
                            label = { Text("السعر الحالي") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
                        )





                        OutlinedTextField(
                            value = prePrice.toString(),
                            enabled = true,
                            onValueChange = {
                                it.toDoubleOrNull()?.let { v ->
                                    prePrice = v
                                }
                            },
                            label = { Text("السعر السابق ") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                                    textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
                        )


                    }
                }
                item {
                    Column(Modifier.selectableGroup()) {
                        val currencies = viewModel.appSession.selectedStore.storeCurrencies
                        Text("عملة المنتج", modifier = Modifier.padding(14.dp))
                        currencies.forEach { text ->
                            Row(
                                Modifier.fillMaxWidth().height(56.dp)
                                    .selectable(
                                        selected = (text == selectedCurrency ),
                                        onClick = {
                                           selectedCurrency = text
                                        },
                                        role = Role.RadioButton
                                    ).padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            )
                            {
                                RadioButton(selected = (text == selectedCurrency), onClick = null)
                                Text(text = text.currencyName,style = MaterialTheme. typography. bodyLarge,modifier = Modifier. padding(start = 16.dp))
                            }
                        }
                    }
                }

                if (price > 0 && selectedCurrency != null && name.length > 4) {
                    stickyHeader {
                        OutlinedButton(
                            onClick = {
                                viewModel.addStoreProduct(name,description, MyJson.IgnoreUnknownKeys.encodeToString(ListSerializer(String.serializer()), infos as List<String>),price.toString(),prePrice.toString(),selectedCurrency!!.currencyId.toString())
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "حفظ ")
                            Spacer(Modifier.width(8.dp))
                            Text("حفظ ")
                        }
                    }
                }
            }
        }
    }

}


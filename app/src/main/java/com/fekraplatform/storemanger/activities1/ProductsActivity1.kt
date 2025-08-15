package com.fekraplatform.storemanger.activities1

import android.content.ClipDescription
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.activities.StringResult
import com.fekraplatform.storemanger.models.HomeProduct
import com.fekraplatform.storemanger.models.Option
import com.fekraplatform.storemanger.models.PrimaryProduct
import com.fekraplatform.storemanger.models.Product2
import com.fekraplatform.storemanger.models.ProductImage
import com.fekraplatform.storemanger.models.StoreNestedSection
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.IconDelete
import com.fekraplatform.storemanger.shared.MainComposeRead
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.shared.confirmDialog
import com.fekraplatform.storemanger.shared.confirmDialog2
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    val appSession: AppSession,
    private val builder: FormBuilder,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
): ViewModel(){
    val stateController = StateController()
    var storeNestedSection: StoreNestedSection  =  MyJson.IgnoreUnknownKeys.decodeFromString(savedStateHandle["storeNestedSection"]?:"")
    val uri = mutableStateOf<Uri?>(null)
    val isShowAddImage = mutableStateOf(false)
    var textValue by mutableStateOf("")
    var isAddImageMode by mutableStateOf(true)
    var isUpdateName by mutableStateOf(true)
    lateinit var selectedProduct:PrimaryProduct
    lateinit var selectedImage:ProductImage
    val isShowUpdateText = mutableStateOf(false)
    val isShowAdd = mutableStateOf(false)
    fun getString(@StringRes resId: Int): String = context.getString(resId)

    fun addImage(file: InputStream?) {
        val homeStoreProduct = appSession.homeStoreProduct
        if (homeStoreProduct != null){

            //
            val requestBody = object : RequestBody() {
                val mediaType = "image/jpeg".toMediaTypeOrNull()
                override fun contentType(): MediaType? {
                    return mediaType
                }

                override fun writeTo(sink: BufferedSink) {
                    file?.use { input ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            sink.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }

            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("productId",selectedProduct.id.toString())
                .addFormDataPart("image", "file.jpg", requestBody)


            viewModelScope.launch {
                stateController.startAud()
                try {
                    val data = requestServer.request(body, "addProductImage")
                    val result: ProductImage = MyJson.IgnoreUnknownKeys.decodeFromString(data.toString())
                    appSession.homeStoreProduct = homeStoreProduct.copy(productsImages = homeStoreProduct.productsImages + result)
                    stateController.successStateAUD(getString(R.string.success_add))
                    isShowAddImage.value = false
                    uri.value = null
                    file?.close()
                } catch (e: Exception) {
                    stateController.errorStateAUD(e.message.toString())
                }
            }
        }
    }
    fun updateImage(file: InputStream) {
        val homeStoreProduct = appSession.homeStoreProduct
        if (homeStoreProduct != null){

            //
            val requestBody = object : RequestBody() {
                val mediaType = "image/jpeg".toMediaTypeOrNull()
                override fun contentType(): MediaType? {
                    return mediaType
                }

                override fun writeTo(sink: BufferedSink) {
                    file?.use { input ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            sink.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }

            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("id",selectedImage.id.toString())
                .addFormDataPart("image", "file.jpg", requestBody)


            viewModelScope.launch {
                stateController.startAud()
                try {
                    val data = requestServer.request(body, "updateProductImage")
                    val result: ProductImage = MyJson.IgnoreUnknownKeys.decodeFromString(data.toString())
                    val updatedImages = homeStoreProduct.productsImages.map {
                        if (it.id == result.id) result else it
                    }

                    appSession.homeStoreProduct = homeStoreProduct.copy(productsImages = updatedImages)
                    stateController.successStateAUD(getString(R.string.success_update))
                    isShowAddImage.value = false
                    uri.value = null
                    file.close()
                } catch (e: Exception) {
                    stateController.errorStateAUD(e.message.toString())
                }
            }
        }

//        val request = Request.Builder()
//            .url("https://user2121.greenland-rest.com/public/api/v1/upload-image")  // Replace with your server URL
//            .post(body)
//            .build()
//        lifecycleScope.launch {
//            withContext(Dispatchers.IO) {
////                Log.e("fdfdf",file.name)
//                val response = OkHttpClient.Builder().build().newCall(request).execute()
//                val data = response.body!!.string()
//                url.value = data
//                Log.e("loooog",data)
//                Log.e("loooog",response.code.toString())
//                }
//            }

//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                println("Upload failed: ${e.message}")
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) {
//                    println("Upload successful: ${response.body?.string()}")
//                } else {
//                    println("Upload failed: ${response.message}")
//                }
//            }
//        })
    }
    fun deleteImage() {
        val homeStoreProduct = appSession.homeStoreProduct
        if (homeStoreProduct != null){
            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("id",selectedImage.id.toString())


            viewModelScope.launch {
                stateController.startAud()
                try {
                    val data = requestServer.request(body, "deleteProductImage")
//                    val result: ProductImage = MyJson.IgnoreUnknownKeys.decodeFromString(data.toString())
                    appSession.homeStoreProduct = homeStoreProduct.copy(productsImages = homeStoreProduct.productsImages - selectedImage)
                    stateController.successStateAUD(getString(R.string.success_delete))
                } catch (e: Exception) {
                    stateController.errorStateAUD(e.message.toString())
                }
            }
        }
    }
    ////
    fun addProduct(name: String,description: String) {
        val homeStoreProduct = appSession.homeStoreProduct
        if (homeStoreProduct != null){
            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("description",description)
                .addFormDataPart("nestedSectionId", storeNestedSection.nestedSectionId.toString())
                .addFormDataPart("name", name)


            viewModelScope.launch {
                stateController.startAud()
                try {
                    val data = requestServer.request(body, "addProduct")
                    val result: PrimaryProduct = MyJson.IgnoreUnknownKeys.decodeFromString(data.toString())
                    appSession.homeStoreProduct = homeStoreProduct.copy(products = homeStoreProduct.products + result )
                    stateController.successStateAUD(getString(R.string.success_add))
                    isShowAdd.value = false
                } catch (e: Exception) {
                    stateController.errorStateAUD(e.message.toString())
                }
            }
        }
    }
    fun updateProductName(value: String) {
        val homeStoreProduct = appSession.homeStoreProduct
        if (homeStoreProduct != null){
            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("productId",selectedProduct.id.toString())
                .addFormDataPart("productName", value)


            viewModelScope.launch {
                stateController.startAud()
                try {
                    requestServer.request(body, "updateProductName")
                    val updatedProducts= homeStoreProduct.products.map {
                        if (it.id == selectedProduct.id) selectedProduct.copy(name = value) else it
                    }
                    appSession.homeStoreProduct = homeStoreProduct.copy(products = updatedProducts)
                    stateController.successStateAUD(getString(R.string.success_update))
                } catch (e: Exception) {
                    stateController.errorStateAUD(e.message.toString())
                }
            }
        }
    }
    fun updateProductDescription(value: String) {
        val homeStoreProduct = appSession.homeStoreProduct
        if (homeStoreProduct != null){
            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("productId",selectedProduct.id.toString())
                .addFormDataPart("description", value)


            viewModelScope.launch {
                stateController.startAud()
                try {
                    requestServer.request(body, "updateProductDescription")
                    val updatedProducts= homeStoreProduct.products.map {
                        if (it.id == selectedProduct.id) selectedProduct.copy(description = value) else it
                    }
                    appSession.homeStoreProduct = homeStoreProduct.copy(products = updatedProducts)
                    stateController.successStateAUD(getString(R.string.success_update))
                } catch (e: Exception) {
                    stateController.errorStateAUD(e.message.toString())
                }
            }
        }
    }
    fun deleteProducts() {
        val homeStoreProduct = appSession.homeStoreProduct
        if (homeStoreProduct != null){
            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("ids", listOf(selectedProduct.id).toString())


            viewModelScope.launch {
                stateController.startAud()
                try {
                    val data = requestServer.request(body, "deleteProducts")
//                    val result: ProductImage = MyJson.IgnoreUnknownKeys.decodeFromString(data.toString())
                    appSession.homeStoreProduct = homeStoreProduct.copy(products = homeStoreProduct.products - selectedProduct)
                    stateController.successStateAUD(getString(R.string.success_delete))
                } catch (e: Exception) {
                    stateController.errorStateAUD(e.message.toString())
                }
            }
        }
    }

    fun readProducts() {
        val homeStoreProduct = appSession.homeStoreProduct
        val homeProduct = appSession.homeProduct

        if (homeStoreProduct != null){
            if (homeProduct != null){
                stateController.successState()
                return
            }

            viewModelScope.launch {
                stateController.startRead()

                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("nestedSectionId", storeNestedSection.nestedSectionId.toString())
                    .addFormDataPart("notIds", homeStoreProduct.products.map { it.id }.toString())

                try {
                    val data = requestServer.request(body, "getProducts")
                    val result:HomeProduct = MyJson.IgnoreUnknownKeys.decodeFromString(data.toString())
                    Log.e("CC",result.products.size.toString())
                    appSession.homeProduct = result
                    appSession.homeStoreProduct = homeStoreProduct.copy(products = homeStoreProduct.products + result.products, productsImages = homeStoreProduct.productsImages + result.productsImages)
                    stateController.successState()
                } catch (e: Exception) {
                    stateController.errorStateRead(e.message.toString())
                }
            }
        }

    }
    init {
        readProducts()
    }

}

@AndroidEntryPoint
class ProductsActivity : ComponentActivity() {
    val viewModel:ProductsViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StoreMangerTheme {
                MainComposeRead("المنتجات الرئيسية",viewModel.stateController,{finish()},{viewModel.readProducts()}) {
                    var ids by remember { mutableStateOf<List<Int>>(emptyList()) }
                    LazyColumn {

                        viewModel.appSession.homeStoreProduct?.products?.forEach {product->
                            item {
                                val productImages =
                                    viewModel.appSession.homeStoreProduct!!
                                        .productsImages
                                        .filter { it.productId == product.id }
                                val pagerStateImage = rememberPagerState(pageCount = { productImages.size })
//                                val scope = rememberCoroutineScope()

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp).combinedClickable(onClick = {},onLongClick = {
                                            confirmDialog(this@ProductsActivity,"تاكيد حذف المنتج",false){
                                                if (product in viewModel.appSession.homeStoreProduct!!.products){
                                                    if (product.id in productImages.map { it.productId }){
                                                        viewModel.selectedProduct = product
                                                        viewModel.deleteProducts()
                                                    }else{
                                                        viewModel.stateController.showMessage("لايمكن حذف منتج لديه صور مرتبطه به")
                                                    }
                                                }else{
                                                    viewModel.stateController.showMessage("لايمكن حذف منتج مرتبط بالمتجر")
                                                }

                                            }
//                                            ids += product.id
                                        }),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {

                                        // 🟢 اسم المنتج
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = product.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            IconButton(onClick = {
                                                viewModel.selectedProduct = product
                                                viewModel.isUpdateName = true
                                                viewModel.isShowUpdateText.value = true
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "تعديل الاسم")
                                            }
                                        }

                                        // 🟢 وصف المنتج
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = product.description ?: "بدون وصف",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            IconButton(onClick = {
                                                viewModel.selectedProduct = product
                                                viewModel.isUpdateName = false
                                                viewModel.isShowUpdateText.value = true
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "تعديل الوصف")
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // 🖼️ الصور
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

                                                IconButton(
                                                    onClick = {
                                                        viewModel.isAddImageMode = false
                                                        viewModel.selectedImage = productImages[i]
                                                        getContent.launch("image/*")
                                                    },
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(8.dp)
                                                        .background(
                                                            color = Color.Black.copy(alpha = 0.4f),
                                                            shape = CircleShape
                                                        )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "تعديل الصورة",
                                                        tint = Color.White
                                                    )
                                                }

                                                IconButton(
                                                    onClick = {
                                                        confirmDialog(this@ProductsActivity,"Confirm Delete",false){
                                                            viewModel.selectedImage = productImages[i]
                                                            viewModel.deleteImage()
                                                        }
                                                         },
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .padding(8.dp)
                                                        .background(
                                                            color = Color.Black.copy(alpha = 0.4f),
                                                            shape = CircleShape
                                                        )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteForever,
                                                        contentDescription = null,
                                                        tint = Color.Red
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // ➕ زر إضافة صورة
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.isAddImageMode = true
                                                getContent.launch("image/*")
                                                viewModel.selectedProduct = product
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.AddAPhoto, contentDescription = "إضافة صورة")
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("إضافة صورة")
                                        }
                                    }
                                }
                            }



                        }

                        item {
                            OutlinedButton(
                                onClick = {

                                    viewModel.isShowAdd.value =true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AddCard, contentDescription = "")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("إضافة منتج جديد")
                            }
                        }
                    }

                    if (viewModel.isShowAddImage.value) modalAddImage()
                    if (viewModel.isShowUpdateText.value) modalUpdateText()
                    if (viewModel.isShowAdd.value) modalAddNewProduct()
                }
            }
        }
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddImage() {
        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowAddImage.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                LazyColumn {
                    item {

                        if (viewModel.uri.value != null) {

                            CustomImageViewUri(
                                modifier = Modifier.fillMaxWidth(),
                                imageUrl = viewModel.uri.value!!,
                            )
                            if (viewModel.uri.value != null) {
                                val type = contentResolver.getType(viewModel.uri.value!!)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(text = "PNG", modifier = Modifier.padding(end = 8.dp))

                                    Icon(
                                        imageVector = if (type == "image/png") Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (type == "image/png") Color.Green else Color.Red
                                    )
                                }
                            }


                            Button(
                                onClick = {
                                    val uriVal = viewModel.uri.value ?: return@Button

                                    val type = contentResolver.getType(uriVal)
                                    if (type != "image/png") {
                                        viewModel.stateController.errorStateAUD("الصورة يجب أن تكون بصيغة PNG")
                                        return@Button
                                    }

                                    try {
                                        val inputStream = contentResolver.openInputStream(uriVal)
                                        if (inputStream != null){
                                            val sizeInBytes = inputStream.available() ?: 0
                                            val sizeInKB = sizeInBytes / 1024

                                            val size = 300
                                            if (sizeInKB > size) {
                                                viewModel.stateController.errorStateAUD("حجم الصورة يجب أن يكون أقل من $size كيلوبايت ")
                                                return@Button
                                            }

                                            if (viewModel.isAddImageMode)
                                                viewModel.addImage(inputStream)
                                            else
                                                viewModel.updateImage(inputStream)
                                        }


//                                        inputStream?.close()
                                    } catch (e: Exception) {
                                        viewModel.stateController.errorStateAUD("فشل في تحميل الصورة: ${e.message}")
                                    }
                                }
                                ,
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                if (viewModel.isAddImageMode)
                                Text("تأكيد أضافة الصورة",)
                                else
                                Text("تأكيد تحديث الصورة",)

                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalUpdateText() {
        var value by remember { mutableStateOf(if (viewModel.isUpdateName) viewModel.selectedProduct.name else viewModel.selectedProduct.description.toString()) }

        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowUpdateText.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        item {
                            TextField(value = value, onValueChange = {
                                value = it
                            })
                            Button(onClick = {
                                if (viewModel.isUpdateName){

                                    viewModel.updateProductName(value)
                                }else{
                                    viewModel.updateProductDescription(value)
                                }
                            }) {
                                Text("حفظ")
                            }
                        }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddNewProduct() {

        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowAdd.value = false }) {
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
                        var productName by remember { mutableStateOf("") }
                        var productDescription by remember { mutableStateOf("") }
                        Card(Modifier.padding(8.dp)){
                            Row (Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                Column {
                                    OutlinedTextField(
                                        modifier = Modifier.padding(8.dp),
                                        value = productName,
                                        label = {
                                            Text("الاسم")
                                        },
                                        onValueChange = {
                                            productName = it
                                        }
                                    )
                                    OutlinedTextField(
                                        modifier = Modifier.padding(8.dp),
                                        value = productDescription,
                                        label = {
                                            Text("الوصف")
                                        },
                                        onValueChange = {
                                            productDescription = it
                                        }
                                    )
                                }
                                IconButton(onClick = {
                                    if (productName.length > 3 && productDescription.length > 5)
                                    viewModel.addProduct(productName,productDescription)
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

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri2: Uri? ->
            if (uri2 != null) {
              viewModel.uri.value = uri2
                viewModel.isShowAddImage.value = true
            } else {
                viewModel.isShowAddImage.value = false
            }
        }

}


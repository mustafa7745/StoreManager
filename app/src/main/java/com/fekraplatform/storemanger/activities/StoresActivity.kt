package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.models.Store
import com.fekraplatform.storemanger.shared.CustomImageView
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm3
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import kotlinx.serialization.encodeToString
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink


class StoresActivity : ComponentActivity() {
    private val stores = mutableStateOf<List<Store>>(listOf())
    val stateController = StateController()
    val requestServer = RequestServer(this)

    val isShowAddCatgory = mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        read()
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {

                    MainCompose1 (
                        0.dp, stateController, this,
                        { read() },
                    ) {
                        LazyColumn {
                            item {
                                Button(onClick = {
                                    isShowAddCatgory.value = true
                                }) {
                                    Text("add")
                                }
                            }
                            itemsIndexed(stores.value){index, item ->

                                Card(
                                    Modifier.fillMaxWidth().height(100.dp).padding(8.dp)
                                ) {
                                    Box (
                                        Modifier.fillMaxSize().clickable {
                                            SelectedStore.store.value = item
                                            goToStores(item)
                                        }
                                    ){
                                        Row (Modifier.fillMaxSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                            ){
                                            Text(item.name)
                                            CustomImageView(
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .padding(8.dp)
                                                    .clickable {

                                                    },
                                                context = this@StoresActivity,
                                                imageUrl = requestServer.serverConfig.getRemoteConfig().BASE_IMAGE_URL+requestServer.serverConfig.getRemoteConfig().SUB_FOLDER_STORE_LOGOS+item.logo,
                                                okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                                            )
                                        }

                                    }
                                }
                            }
                            item {
                                Card(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .padding(8.dp)
                                ) {
                                    Box (
                                        Modifier
                                            .fillMaxSize()
                                            .clickable {
                                                isShowAddCatgory.value = true
                                            }
                                    ){
                                        Text("+", modifier = Modifier.align(Alignment.Center))
                                    }
                                }
                            }
                        }
                        if (isShowAddCatgory.value) modalAddMyCategory()

                    }
                }
//               LazyColumn {
//                   item {
//                       Spacer(Modifier.height(50.dp))
//                   }
//
//                   item {
//                       Card(
//                           Modifier.fillMaxWidth().height(50.dp).padding(8.dp) .clickable {
//                               goToProducts()
//                           }
//                       ) {
//                           Text("منتجات المتجر")
//                       }
//                       Card(
//                           Modifier.fillMaxWidth().height(50.dp).padding(8.dp) .clickable {
//                               goToProducts()
//                           }
//                       ) {
//                           Text("فئات المتجر")
//                       }
//                       Card(
//                           Modifier.fillMaxWidth().height(50.dp).padding(8.dp) .clickable {
//                               goToMyCategories()
//                           }
//                       ) {
//                           Text( "فئاتي")
//                       }
//                       Card(
//                           Modifier.fillMaxWidth().height(50.dp).padding(8.dp) .clickable {
//                               goToMyProducts()
//                           }
//                       ) {
//                           Text("منتجاتي")
//                       }
//                   }
//               }
        }
    }
//    private fun goToProducts() {
//        val intent = Intent(
//            this,
//            ProductsActivity::class.java
//        )
//        startActivity(intent)
//    }
//    private fun goToMyProducts() {
//        val intent = Intent(
//            this,
//            MyProductsActivity::class.java
//        )
//        startActivity(intent)
//    }
//    private fun goToMyCategories() {
//        val intent = Intent(
//            this,
//            MyCategoriesActivity::class.java
//        )
//        startActivity(intent)
//    }
private fun goToStores(store: Store) {
    val intent = Intent(
        this,
        StoreCategoriesActivity::class.java
    )
    intent.putExtra("store", MyJson.MyJson.encodeToString(store))
    startActivity(intent)
}
fun read() {
    stateController.startRead()

    val body = builderForm3()
        .build()

    requestServer.request2(body, "getStores", { code, fail ->
        stateController.errorStateRead(fail)
    }
    ) { data ->
        stores.value =
            MyJson.IgnoreUnknownKeys.decodeFromString(
                data
            )

        stateController.successState()
    }
}

//private fun add(storeId: String,categoryId:String) {
//
//        stateController.startAud()
//
//        val body = MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("storeId",storeId)
//            .addFormDataPart("categoryId",categoryId)
//            .build()
//
//        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/addStoreCategory",{code,fail->
//            stateController.errorStateAUD(fail)
//        }
//        ){it->
//            val result: StoreCategory1 =  MyJson.IgnoreUnknownKeys.decodeFromString(
//                it
//            )
//
//            storeCategories.value += result
//            isShowAddCatgory.value = false
//            stateController.successStateAUD("تمت الاضافه  بنجاح")
//        }
//    }

//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    private fun modalAddMyCategory() {
//        var category by remember { mutableStateOf<Category?>(null) }
//        ModalBottomSheet(
//            onDismissRequest = { isShowAddCatgory.value = false }) {
//            Box(
//                Modifier
//                    .fillMaxSize()
//                    .padding(bottom = 10.dp)
//            ){
//                LazyColumn(
//                    Modifier.fillMaxSize(),
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    item {
//                        var expanded by remember { mutableStateOf(false) }
//                        Card(Modifier.padding(8.dp)) {
//                            Row (Modifier.fillMaxWidth().padding(8.dp).clickable {
//                                if (categories.value.isEmpty()){
//                                    readCategories()
//                                }
//                                expanded = !expanded
//                            },
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ){
//                                Text(category?.name ?: "اختر فئة")
//                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
//                            }
//                            if (expanded)
//                                categories.value.filterNot { categoryItem ->
//                                    storeCategories.value.any { storeCategory ->
//                                        storeCategory.categoryId == categoryItem.id // Compare by the 'name' field
//                                    }
//                                }.forEach { item ->
//                                    DropdownMenuItem(onClick = {
//                                        category = item
//                                        expanded = false // Close the dropdown after selection
//                                    }, text = {
//                                        Text(item.name)
//                                    })
//                                }
//
//                        }
//                        if (category != null )
//                            Button(onClick = {
//                                add("1",category!!.id.toString())
//                            }) {
//                                Text("حفظ")
//                            }
//                    }
//                }
//            }
//        }
//    }

    var uriLogo=mutableStateOf<Uri?>(null)
    var uriCover =mutableStateOf<Uri?>(null)

    val getContentlogo =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null){
                uriLogo.value = uri
            }
        }
    val getContentCover =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null){
                uriCover.value = uri
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddMyCategory() {


        ModalBottomSheet(
            onDismissRequest = { isShowAddCatgory.value = false }) {
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
                        var storeName by remember { mutableStateOf("") }
                        Card(Modifier.padding(8.dp)){
                            Row (Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                OutlinedTextField(
                                    modifier = Modifier.padding(8.dp),
                                    value = storeName,
                                    onValueChange = {
                                        storeName = it
                                    }
                                )
                                IconButton(onClick = {
                                    addStore(storeName)
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

                    item {
                        Card(Modifier.size(50.dp).clickable {
                            getContentlogo.launch("image/*")
                        }) {
                            if (uriLogo.value != null){
                                CustomImageViewUri(
                                    modifier = Modifier.fillMaxWidth(),
                                    imageUrl = uriLogo.value!!,
                                )
                            }
                        }
                    }

                    item {
                        Card(Modifier.fillMaxWidth().height(100.dp).clickable {
                            getContentCover.launch("image/*")
                        }) {
                            if (uriCover.value != null){
                                CustomImageViewUri(
                                    modifier = Modifier.fillMaxWidth(),
                                    imageUrl = uriCover.value!!,
                                )
                            }
                        }
                    }

                }
            }
        }
    }

    private fun addStore(name:String,) {
        stateController.startAud()

        val requestBodyIcon = object : RequestBody() {
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            override fun contentType(): MediaType? {
                return mediaType
            }

            override fun writeTo(sink: BufferedSink) {
                contentResolver.openInputStream(uriLogo.value!!)?.use { input ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        sink.write(buffer, 0, bytesRead)
                    }
                }
            }
        }

        val requestBodyCover= object : RequestBody() {
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            override fun contentType(): MediaType? {
                return mediaType
            }

            override fun writeTo(sink: BufferedSink) {
                contentResolver.openInputStream(uriCover.value!!)?.use { input ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        sink.write(buffer, 0, bytesRead)
                    }
                }
            }
        }


        val body = builderForm3()
            .addFormDataPart("typeId","2")
            .addFormDataPart("name",name)
            .addFormDataPart("logo", "file1.jpg", requestBodyIcon)
            .addFormDataPart("cover", "file2.jpg", requestBodyCover)
            .build()

        requestServer.request2(body,"addStore",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result: Store =  MyJson.IgnoreUnknownKeys.decodeFromString(it)

            stores.value += result
//            homeStorage.setHome(MyJson.IgnoreUnknownKeys.encodeToString(SingletonHome.home.value!!),SingletonStoreConfig.storeId)
            isShowAddCatgory.value = false
            stateController.successStateAUD("تمت الاضافه  بنجاح")
        }
    }


//    fun updateImage(file: InputStream?, id:Int) {
//        stateController.startAud()
//        //
//        val requestBody = object : RequestBody() {
//            val mediaType = "image/jpeg".toMediaTypeOrNull()
//            override fun contentType(): MediaType? {
//                return mediaType
//            }
//
//            override fun writeTo(sink: BufferedSink) {
//                file?.use { input ->
//                    val buffer = ByteArray(4096)
//                    var bytesRead: Int
//                    while (input.read(buffer).also { bytesRead = it } != -1) {
//                        sink.write(buffer, 0, bytesRead)
//                    }
//                }
//            }
//        }
//
//        val body = MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("id",id.toString())
//            .addFormDataPart("image", "file.jpg", requestBody)
//            .build()
//
////        val urli = "https://user2121.greenland-rest.com/public/api/v1/upload-image"
//
//
//        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/updateProductImage",{code,fail->
//            stateController.errorStateAUD(fail)
//        }
//        ){it->
//            val result: ProductImage =  MyJson.IgnoreUnknownKeys.decodeFromString(
//                it
//            )
//            selectedImage = result
////            val pre = storeProducts.value.map { storeProduct ->
////                // Update the products of each store product
////                val updatedProducts = storeProduct.products.map { product ->
////
////                    // Remove the image with the matching ID
////                    val filteredImages = product.images.filterNot { image -> image.id == result.id }
////
////                    // Return a new product with the filtered images (removing the unwanted image)
//////                    product.copy(images = filteredImages)
////                    product.copy(productName = "mustafafa")
////                }
////
////                // Return a new StoreProduct with the updated products
////                storeProduct.copy(products = updatedProducts)
////            }
////            Log.e("pre",pre.toString())
////            storeProducts.value = pre
////            Log.e("post",storeProducts.value.toString())
//
//
//            storeProducts.value = storeProducts.value.map { product ->
//                // Update the images of each product
//                val updatedImages = product.images.map { image ->
//                    // Check if the image ID matches the result and update the image
//                    if (image.id == result.id) {
//                        Log.e("11jiamge", result.toString())
//                        // Replace image with the new URL (result.image)
//                        image.copy(image = result.image)
//                    } else {
//                        image // Leave the image unchanged
//                    }
//                }
//                // Return a new product with the updated images
//                product.copy(images = updatedImages)
//            }
//
//
//            Log.e("jiamge",result.toString())
//
//            isShowUpdateImage.value = false
//            uri.value = null
//            stateController.successStateAUD("تم تعديل الصورة بنجاح")
//        }
//
//
//
////        val request = Request.Builder()
////            .url("https://user2121.greenland-rest.com/public/api/v1/upload-image")  // Replace with your server URL
////            .post(body)
////            .build()
////        lifecycleScope.launch {
////            withContext(Dispatchers.IO) {
//////                Log.e("fdfdf",file.name)
////                val response = OkHttpClient.Builder().build().newCall(request).execute()
////                val data = response.body!!.string()
////                url.value = data
////                Log.e("loooog",data)
////                Log.e("loooog",response.code.toString())
////                }
////            }
//
////        client.newCall(request).enqueue(object : Callback {
////            override fun onFailure(call: Call, e: IOException) {
////                println("Upload failed: ${e.message}")
////            }
////
////            override fun onResponse(call: Call, response: Response) {
////                if (response.isSuccessful) {
////                    println("Upload successful: ${response.body?.string()}")
////                } else {
////                    println("Upload failed: ${response.message}")
////                }
////            }
////        })
//    }
}
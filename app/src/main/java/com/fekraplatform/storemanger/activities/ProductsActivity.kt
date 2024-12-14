package com.fekraplatform.storemanger.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.fekraplatform.storemanger.models.StoreCategory
import com.fekraplatform.storemanger.models.Option
import com.fekraplatform.storemanger.models.Product
import com.fekraplatform.storemanger.models.ProductToSelect
import com.fekraplatform.storemanger.models.StoreProduct
import com.fekraplatform.storemanger.models.ProductImage
import com.fekraplatform.storemanger.models.ProductOption
import com.fekraplatform.storemanger.models.StoreNestedSection
import com.fekraplatform.storemanger.shared.CustomImageView
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.U1R
import com.fekraplatform.storemanger.shared.builderForm3
import com.fekraplatform.storemanger.storage.ProductsStorageDBManager
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import kotlinx.serialization.Serializable
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.InputStream
import java.time.Duration


class ProductsActivity : ComponentActivity() {
    val productsStorageDBManager = ProductsStorageDBManager(this)

    val stateController = StateController()
    val requestServer = RequestServer(this)
    private val storeProducts = mutableStateOf<List<StoreProduct>>(listOf())
    private val storeCategories = mutableStateOf<List<StoreCategory>>(listOf())

//        private val products = mutableStateOf<List<Product>>(listOf())
    private val productsToSelect = mutableStateOf<List<ProductToSelect>>(listOf())

   //    lateinit var selectedProduct: Product

    val UPDATE_PRODUCT_NAME = 1
    val UPDATE_PRODUCT_DESCRIPTION = 2
    val UPDATE_OPTION_NAME = 3
    val UPDATE_OPTION_PRICE = 4

    var SELECTED_UPDATE  = -1

    val uri = mutableStateOf<Uri?>(null)

    val isShowUpdateImage = mutableStateOf(false)
    val isShowAddImage = mutableStateOf(false)
    val isShowDeleteImage = mutableStateOf(false)
    val isShowUpdateText = mutableStateOf(false)
    val isShowAddProductOption = mutableStateOf(false)
    val isShowAddProductStore = mutableStateOf(false)
    lateinit var selectedImage : ProductImage
    lateinit var selectedStoreProduct:StoreProduct
    lateinit var selectedProductOption:ProductOption
    var selectedOption:Option? = null
    private val options = mutableStateOf<List<Option>>(listOf())
    lateinit var storeNestedSection: StoreNestedSection


    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val str = intent.getStringExtra("storeNestedSection")
        if (str != null) {
            try {
                storeNestedSection = MyJson.IgnoreUnknownKeys.decodeFromString(str)
            }catch (e:Exception){
                finish()
            }
        } else {
            finish()
        }

        val stId=  if (SingletonStoreConfig.isSharedStore()) SingletonStoreConfig.storeIdReference else SingletonStoreConfig.storeId

        if (productsStorageDBManager.isSet(stId,storeNestedSection.id.toString())){
            val diff =
                Duration.between(productsStorageDBManager.getDate(stId,storeNestedSection.id.toString()), getCurrentDate()).toMinutes()
            if (diff <= 1){

//                val storeProduct : List<StoreProduct>

                val res = productsStorageDBManager.getStoreProducts(stId,storeNestedSection.id.toString())
                res.forEach {
                    Log.e("res",it.options.joinToString(","){it.storeProductId.toString() })
                }

                storeProducts.value = res
                stateController.successState()
//
            }else{
                read(stId)
            }
        }else{
            read(stId)
        }




        setContent {
            StoreMangerTheme{
                MainCompose1(
                    0.dp, stateController ,this,
                    { read(stId) },
                ){

                    LazyColumn(Modifier.fillMaxSize()) {

                        item {
                            SingletonStoreConfig.EditModeCompose()
                        }


                        item {
                            Button(onClick = {
                                if (!SingletonStoreConfig.isSharedStore())
                                isShowAddProductStore.value = true

                            }) {
                                Text("add")
                            }
                            HorizontalDivider()
                        }
//                        val r = storeProducts.value
//                        val prods = if (SingletonStoreConfig.isSharedStore()){
//                            if (SingletonHome.isEditMode.value)
//                                r
//                             else
//                            r.filterNot { it.storeProductId in SingletonStoreConfig.products.value }
//                        }
//                        else r

//                        if (SingletonStoreConfig.isSharedStore()){
//                            if (SingletonHome.isEditMode.value) {
//                                storeProducts.value
//                            }
//                            else {
//                                // In non-edit mode, filter products based on the options
//                                storeProducts.value.filter { product ->
//                                    // Ensure product has non-empty options and at least one option matches storeProductId in SingletonStoreConfig.products.value
//                                    product.options.isNotEmpty() && product.options.any { option ->
//                                        option.storeProductId in SingletonStoreConfig.products.value
//                                    }
//                                }
//                            }
//                        }
//                        else{
//                            storeProducts.value
//                        }



                        itemsIndexed(storeProducts.value){index, storeProduct ->

                                Card (
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(5.dp)
                                        .clickable {
//
//                                    selectedProduct = product
//                                    isShowSubProduct.value = true
////                                    goToAddToCart(product)
                                        }){
                                    var isExpanded by remember { mutableStateOf(true) }
                                    Row (
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ){
                                        Text(storeProduct.productName,
                                            Modifier
                                                .padding(8.dp)
                                                .clickable {
                                                    selectedStoreProduct = storeProduct
                                                    SELECTED_UPDATE = UPDATE_PRODUCT_NAME
                                                    isShowUpdateText.value = true
//
                                                }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        IconButton(onClick = {
                                           isExpanded = !isExpanded
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
                                                imageVector = if(isExpanded) Icons.Outlined.ArrowDropDown else Icons.Outlined.KeyboardArrowUp ,
                                                contentDescription = ""
                                            )
                                        }
                                    }
                                    if (!isExpanded){
                                        var optionIds by remember { mutableStateOf<List<Int>>(emptyList()) }
                                        Text(storeProduct.productDescription.toString(),Modifier.clickable {  }, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Row (Modifier.fillMaxWidth()
                                            , verticalAlignment = Alignment.CenterVertically
                                        ){
                                            Text("الخيارات")
                                            IconButton(onClick = {
                                                if (!SingletonStoreConfig.isSharedStore()){
                                                    selectedStoreProduct = storeProduct
                                                    isShowAddProductOption.value = true
                                                }
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
                                            if (optionIds.isNotEmpty()){
                                                IconButton(onClick = {
                                                    if (!SingletonStoreConfig.isSharedStore())
                                                    deleteProductOptions(optionIds,{
                                                        optionIds = emptyList()
                                                    })
//                                                    selectedProduct = product
//                                                    isShowAddProductOption.value = true
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
                                                        imageVector = Icons.Outlined.Delete,
                                                        contentDescription = ""
                                                    )
                                                }
                                            }
                                        }

//                                        val opts = if (SingletonStoreConfig.isSharedStore()){
//                                            if (SingletonHome.isEditMode.value) {
//                                                Log.e("st1",storeProduct.options.toString())
//                                                storeProduct.options
//                                            }
//                                            else {
//                                                Log.e("st2",storeProduct.options.filterNot { it.storeProductId in SingletonStoreConfig.products.value }.toString())
//                                                storeProduct.options.filterNot { it.storeProductId in SingletonStoreConfig.products.value }
//
//                                            }
//                                        }
//                                        else{
//                                            Log.e("st3",storeProduct.options.toString())
//                                            storeProduct.options
//                                        }

//                                        isShow = if (SingletonStoreConfig.isSharedStore()) {
//                                            opts.isNotEmpty()
//                                        } else {
//                                            true // Assuming you want to show all products if it's not a shared store
//                                        }


                                       storeProduct.options.forEach { productOption ->
                                            Row (
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(5.dp)
                                                , verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ){
                                                Text(productOption.name,Modifier.clickable {
                                                    if (!SingletonStoreConfig.isSharedStore()){
                                                        selectedProductOption = productOption
                                                        selectedStoreProduct = storeProduct
                                                        SELECTED_UPDATE = UPDATE_OPTION_NAME
                                                        isShowUpdateText.value = true
                                                    }

                                                })
                                                Text(productOption.price,Modifier.clickable {
                                                    if (!SingletonStoreConfig.isSharedStore()){
                                                        selectedProductOption = productOption
                                                        selectedStoreProduct = storeProduct
                                                        SELECTED_UPDATE = UPDATE_OPTION_PRICE
                                                        isShowUpdateText.value = true
                                                    }
                                                })
                                                Checkbox(checked = optionIds.find { it == productOption.storeProductId } != null, onCheckedChange = {
                                                    val itemC = optionIds.find { it == productOption.storeProductId }
                                                    if (itemC == null) {
                                                        optionIds = optionIds + productOption.storeProductId
                                                    }else{
                                                        optionIds = optionIds - productOption.storeProductId
                                                    }
                                                })
                                            }
                                            if (SingletonHome.isEditMode.value && SingletonStoreConfig.isSharedStore()){
                                                if (SingletonStoreConfig.products.value.any { number -> number == productOption.storeProductId }){
                                                    if (! SingletonHome. products.value.any { it == productOption.storeProductId }) {
                                                        Text(
                                                            "تمت الاضافة بانتظار التأكيد",
                                                            Modifier

                                                                .clickable {
                                                                    SingletonHome. products.value +=productOption.storeProductId
                                                                })
                                                    }
                                                    else{
                                                        Text(
                                                            "اضافة",
                                                            Modifier
//                                                                .align(Alignment.BottomEnd)
                                                                .clickable {
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())
                                                                    SingletonHome.products.value -= productOption.storeProductId
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())

                                                                })
                                                    }
                                                }
                                                else{
                                                    if ( SingletonHome. products.value.any { it == productOption.storeProductId }){
                                                        Text("تمت الحذف بانتظار التأكيد",
                                                            Modifier
//                                                                .align(Alignment.BottomEnd)
                                                                .clickable {
                                                                    SingletonHome.products.value -= productOption.storeProductId
                                                                })
                                                    }else{
                                                        Text("حذف",
                                                            Modifier
                                                                .clickable {
                                                                    SingletonHome. products.value+=productOption.storeProductId
                                                                })
                                                    }

                                                }
                                            }
                                            HorizontalDivider(Modifier.padding(8.dp))
                                        }


                                        if (!SingletonStoreConfig.isSharedStore()){
                                            Row (Modifier.fillMaxWidth()
                                                , verticalAlignment = Alignment.CenterVertically
                                            ){

                                                IconButton(onClick = {
                                                    getContent.launch("image/*")
                                                    selectedStoreProduct = storeProduct
                                                    isShowAddImage.value = true
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
                                                Text("الصور")
                                            }
                                            LazyRow(
                                                Modifier.height(120.dp)
                                            ) {
                                                itemsIndexed(storeProduct.images){ index: Int, item: ProductImage ->
                                                    Log.e("urlfff",U1R.BASE_IMAGE_URL+item.image)
                                                    Box (Modifier.size(100.dp)){
                                                        CustomImageView(
                                                            modifier = Modifier
                                                                .size(100.dp)
                                                                .padding(8.dp)
                                                                .clickable {
                                                                    selectedImage = item
                                                                    isShowUpdateImage.value = true

//                                        if (inputStream != null)
//                                        uploadImage(inputStream,item.id)
                                                                },
                                                            context = this@ProductsActivity,
                                                            imageUrl = U1R.BASE_IMAGE_URL+U1R.SUB_FOLDER_PRODUCT+item.image,
                                                            okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                                                        )
                                                        IconButton(onClick = {
                                                            selectedImage = item
                                                            isShowDeleteImage.value = true
                                                        }
                                                            ,
                                                            Modifier
                                                                .align(Alignment.BottomEnd)
                                                                .padding(5.dp)
                                                        ) {
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
                                                                imageVector = Icons.Outlined.Delete,
                                                                contentDescription = ""
                                                            )
                                                        }
                                                    }

//                                                Button(onClick = {
//                                                    selectedImage = item
//                                                    isShowDeleteImage.value = true
//
//                                                }) {
//                                                    Text("حذف")
//                                                }
//                                if (inputStream != null)
//                                    Button(onClick = {
//
//
//                                    }) {
//                                        Text("save")
//                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }
//                            Column {
//
//                            }
                    }


                    if (isShowUpdateImage.value)modalUpdateImage()
                    if (isShowAddImage.value)modalAddImage()
                    if (isShowDeleteImage.value)modalDeleteImage()
                    if (isShowUpdateText.value)modalUpdateText()
                    if(isShowAddProductOption.value)modalAddProductOption()
                    if(isShowAddProductStore.value)modalAddProductStore()

                }


            }
        }
    }
    fun read(storeId:String){
        stateController.startRead()

        Log.e("sec",storeNestedSection.toString())
        val body = builderForm3()
            .addFormDataPart("storeNestedSectionId", storeNestedSection.id.toString())
            .addFormDataPart("storeId",storeId)
            .build()

        requestServer.request2(body,"readMain",{code,fail->
            stateController.errorStateRead(fail)
        }
        ){data->

            val result:List<StoreProduct> =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            productsStorageDBManager.clearAllData(storeNestedSection.id.toString())

            productsStorageDBManager.addStoreProducts(result,
               if (SingletonStoreConfig.isSharedStore()) SingletonStoreConfig.storeIdReference else SingletonStoreConfig.storeId
                ,storeNestedSection.id)

            result.forEach {
                Log.e("res0",it.options.joinToString(","){it.storeProductId.toString() })
            }

            storeProducts.value = result

            stateController.successState()
        }
    }

//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    private fun modalProduct(
//
//    ) {
//
//        ModalBottomSheet(
//            onDismissRequest = { isShowSubProduct.value = false }) {
//            Box(
//                Modifier
//                    .fillMaxSize()
//                    .padding(bottom = 10.dp)
//            ){
//
//                LazyColumn {
//                    item {
//                        Text(selectedProduct.productName)
//                        Text(selectedProduct.productDescription)
//                    }
//                    item {
//                        Text("الخيارات")
//                        selectedProduct.options.forEach { productOption ->
//                            Text(productOption.name)
//                            Text(productOption.price)
//                            HorizontalDivider()
//                        }
//                    }
//                    item {
//                        Row {
//                            Text("الصور")
//
//                            Button(onClick = {
//                                getContent.launch("image/*")
//                            }) {
//                                Text("choose")
//                            }
//                        }
//                        LazyRow(
//                            Modifier.height(120.dp)
//                        ) {
//                            itemsIndexed(selectedProduct.images){index: Int, item: ProductImage ->
//                                Log.e("urlfff",U1R.BASE_IMAGE_URL+item.image)
//                                CustomImageView(
//                                    modifier = Modifier.size(100.dp).clickable {
//                                        selectedImage = item
//                                        isShowUpdateImage.value = true
//
////                                        if (inputStream != null)
////                                        uploadImage(inputStream,item.id)
//                                    },
//                                    context = this@ProductsActivity,
//                                    imageUrl = U1R.BASE_IMAGE_URL+U1R.SUB_FOLDER_PRODUCT+item.image,
//                                    okHttpClient = requestServer.createOkHttpClientWithCustomCert()
//                                )
////                                if (inputStream != null)
////                                    Button(onClick = {
////
////
////                                    }) {
////                                        Text("save")
////                                    }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun modalAddProductStore(

    ) {

        var price by remember { mutableStateOf("") }
        var option by remember { mutableStateOf<Option?>(null) }
        var storeCategory by remember { mutableStateOf<StoreCategory?>(null) }
        var product by remember { mutableStateOf<ProductToSelect?>(null) }
        ModalBottomSheet(
            onDismissRequest = { isShowAddProductStore.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                LazyColumn(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    item {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = price , onValueChange = {
                                price = it
                            },
                            label = {
                                Text("السعر")
                            }
                        )
                    }
                    item {
                        var expanded by remember { mutableStateOf(false) }
                        Card(Modifier.padding(8.dp)) {
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        if (productsToSelect.value.isEmpty()) {
                                            readProducts()
                                        }
                                        expanded = !expanded
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Text(product?.name ?: "اختر منتج")
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                            if (expanded)
                                productsToSelect.value.filterNot { product1 ->
                                    storeProducts.value.any { it.productId == product1.id }
                                    // Compare by the 'name' field

                                }.forEach { item ->
                                    DropdownMenuItem(onClick = {
                                        product = item
                                        expanded = false // Close the dropdown after selection
                                    }, text = {
                                        Text(item.name)
                                    })
                                }

                        }
                    }
//                    item {
//                        var expanded by remember { mutableStateOf(false) }
//                        Card(Modifier.padding(8.dp)) {
//                            Row (Modifier.fillMaxWidth().padding(8.dp).clickable {
//                                if (storeCategories.value.isEmpty()){
//                                    readStoreCategories()
//                                }
//                                expanded = !expanded
//                            },
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ){
//                                Text(storeCategory?.categoryName ?: "اختر فئة")
//                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
//                            }
//                            if (expanded)
//                                storeCategories.value.forEach { item ->
//                                    DropdownMenuItem(onClick = {
//                                        storeCategory = item
//                                        expanded = false // Close the dropdown after selection
//                                    }, text = {
//                                        Text(item.categoryName)
//                                    })
//                                }
//
//                        }
//                    }
                    item {
                        var expanded by remember { mutableStateOf(false) }
                        Card(Modifier.padding(8.dp)) {
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        if (options.value.isEmpty()) {
                                            readOptions()
                                        }
                                        expanded = !expanded
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Text(option?.name ?: "اختر الخيار")
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                            if (expanded)
                                options.value.forEach { item ->
                                    DropdownMenuItem(onClick = {
                                        option = item
                                        expanded = false // Close the dropdown after selection
                                    }, text = {
                                        Text(item.name)
                                    })
                                }

                        }
                    }
                    item {
                        if (option != null && product != null && price.length > 0)
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    addproductOption(price, option!!.id ,product!!.id)
                                }) { Text("حفظ") }
                    }


                    // State to manage the dropdown visibility


//                    item {
//                        var expanded by remember { mutableStateOf(false) }
//                        Column {
//                            // Button to show the dropdown menu
//                            OutlinedTextField(
//                                value = (option?.name ?: ""),
//                                onValueChange = {},
////                                label = { Text("Select Item") },
////                            trailingIcon = {
////                                Icon(
////                                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.ArrowDropDown,
////                                    contentDescription = null
////                                )
////                            },
//                                readOnly = true,
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clickable { expanded = !expanded } // Toggle dropdown
//                            )
//
//                            // Dropdown Menu
//                            ExposedDropdownMenuBox(
//                                expanded = expanded,
//                                onExpandedChange = { expanded = it }
//                            ) {
//                                ExposedDropdownMenu(
//                                    expanded = expanded,
//                                    onDismissRequest = { expanded = false } // Close dropdown
//                                ) {
//                                    options.value.forEach { item ->
//                                        DropdownMenuItem(onClick = {
//                                            option = item
//                                            expanded = false // Close the dropdown after selection
//                                        }, text = {
//                                            Text(item.name)
//                                        })
//                                    }
//                                }
//                            }
//                        }
//                    }


//                        itemsIndexed(options.value){index: Int, item: Option ->
//                            Button(
//                                onClick = {
//                                    option = item
//                                }
//                            ) { Text(item.name) }
//                        }
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun modalAddProductOption() {

        var price by remember { mutableStateOf("") }
        var option by remember { mutableStateOf<Option?>(null) }
        var storeCategory by remember { mutableStateOf<StoreCategory?>(null) }
        ModalBottomSheet(
            onDismissRequest = { isShowAddProductOption.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                LazyColumn(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    item {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = price , onValueChange = {
                            price = it
                        },
                            label = {
                                Text("السعر")
                            }
                            )
                    }
                    item {
                        var expanded by remember { mutableStateOf(false) }
                        Card(Modifier.padding(8.dp)) {
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        if (options.value.isEmpty()) {
                                            readOptions()
                                        }
                                        expanded = !expanded
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Text(option?.name ?: "اختر الخيار")
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                            if (expanded)
                                options.value.filterNot { option1 ->
                                    selectedStoreProduct.options.any { option2 ->
                                        option2.optionId == option1.id
                                        // Compare by the 'name' field
                                    }
                                }.forEach { item ->
                                    DropdownMenuItem(onClick = {
                                        option = item
                                        expanded = false // Close the dropdown after selection
                                    }, text = {
                                        Text(item.name)
                                    })
                                }

                        }
                    }
//                    item {
//                        var expanded by remember { mutableStateOf(false) }
//                        Card(Modifier.padding(8.dp)) {
//                            Row (Modifier.fillMaxWidth().padding(8.dp).clickable {
//                                expanded = !expanded
//                            },
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ){
//                                Text(category?.categoryName ?: "اختر الفئة")
//                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
//                            }
//                            if (expanded)
//                                storeCategories.value.forEach { item ->
//                                    DropdownMenuItem(onClick = {
//                                        category = item
//                                        expanded = false // Close the dropdown after selection
//                                    }, text = {
//                                        Text(item.categoryName)
//                                    })
//                                }
//
//                        }
//                    }
                    item {
                        if (option != null && price.length > 0)
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                addproductOption(price, option!!.id ,selectedStoreProduct.productId)
                        }) { Text("حفظ") }
                    }


                    // State to manage the dropdown visibility


//                    item {
//                        var expanded by remember { mutableStateOf(false) }
//                        Column {
//                            // Button to show the dropdown menu
//                            OutlinedTextField(
//                                value = (option?.name ?: ""),
//                                onValueChange = {},
////                                label = { Text("Select Item") },
////                            trailingIcon = {
////                                Icon(
////                                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.ArrowDropDown,
////                                    contentDescription = null
////                                )
////                            },
//                                readOnly = true,
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clickable { expanded = !expanded } // Toggle dropdown
//                            )
//
//                            // Dropdown Menu
//                            ExposedDropdownMenuBox(
//                                expanded = expanded,
//                                onExpandedChange = { expanded = it }
//                            ) {
//                                ExposedDropdownMenu(
//                                    expanded = expanded,
//                                    onDismissRequest = { expanded = false } // Close dropdown
//                                ) {
//                                    options.value.forEach { item ->
//                                        DropdownMenuItem(onClick = {
//                                            option = item
//                                            expanded = false // Close the dropdown after selection
//                                        }, text = {
//                                            Text(item.name)
//                                        })
//                                    }
//                                }
//                            }
//                        }
//                    }


//                        itemsIndexed(options.value){index: Int, item: Option ->
//                            Button(
//                                onClick = {
//                                    option = item
//                                }
//                            ) { Text(item.name) }
//                        }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalUpdateText(

    ) {
        var value by remember { mutableStateOf("") }
        var type = -1
        var function = {}
        when(SELECTED_UPDATE){
            UPDATE_PRODUCT_NAME->{
                value = selectedStoreProduct.productName
                type = 1
            }
            UPDATE_PRODUCT_DESCRIPTION->{
                value = selectedStoreProduct.productDescription!!
                type = 1
            }
            UPDATE_OPTION_PRICE->{
                value = selectedProductOption.price
                type = 2
            }
            UPDATE_OPTION_NAME->{
                readOptions()
                value = selectedProductOption.name
                type = 1
            }
        }

        ModalBottomSheet(
            onDismissRequest = { isShowUpdateText.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (SELECTED_UPDATE != UPDATE_OPTION_NAME)

                    item {
                        TextField(value = value , onValueChange = {
                         value = it
                        })
                        Button(onClick = {
                            when(SELECTED_UPDATE){
                                UPDATE_PRODUCT_NAME->{
                                     updateProductName(value)
                                }
                                UPDATE_PRODUCT_DESCRIPTION->{

                             updateProductDescription(value)
                                }
                                UPDATE_OPTION_PRICE->{
                                    updateProductOptionPrice(value)
                                }
                            }


                        }) {
                            Text("حفظ")
                        }
                    }
                    if (SELECTED_UPDATE == UPDATE_OPTION_NAME)
                    itemsIndexed(options.value){index: Int, item: Option ->
                        Button(
                            onClick = {
                                updateProductOptionName(item.id.toString())
                            }
                        ) { Text(item.name) }
                    }

                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalUpdateImage() {
        ModalBottomSheet(
            onDismissRequest = { isShowUpdateImage.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                LazyColumn {
                    item {

                        if (uri.value != null){
                            CustomImageViewUri(
                                modifier = Modifier.fillMaxWidth(),
                                context = this@ProductsActivity,
                                imageUrl = uri.value!!,
                            )
//                        if (inputStream != null){
                            Button(onClick = {
                                val inputStream = contentResolver.openInputStream(uri.value!!)
                                updateImage(inputStream,selectedImage.id)
                            },
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)) {
                                Text("تأكيد تعديل الصورة",)
                            }
                        }else{
                            CustomImageView(
                                modifier = Modifier.fillMaxWidth(),
                                context = this@ProductsActivity,
                                imageUrl = U1R.BASE_IMAGE_URL+U1R.SUB_FOLDER_PRODUCT+selectedImage.image,
                                okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                            )
                            Button(onClick = {
                                getContent.launch("image/*")
                            },
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)) {
                                Text("تعديل الصورة")
                            }
                        }
                    }
                }
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddImage() {
        ModalBottomSheet(
            onDismissRequest = { isShowAddImage.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                LazyColumn {
                    item {

                        if (uri.value != null){
                            CustomImageViewUri(
                                modifier = Modifier.fillMaxWidth(),
                                context = this@ProductsActivity,
                                imageUrl = uri.value!!,
                            )
//                        if (inputStream != null){
                            Button(onClick = {
                                val inputStream = contentResolver.openInputStream(uri.value!!)
                                addImage(inputStream)
                            },
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)) {
                                Text("تأكيد أضافة الصورة",)
                            }
                        }
                    }
                }
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalDeleteImage() {
        ModalBottomSheet(
            onDismissRequest = { isShowDeleteImage.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                LazyColumn {
                    item {

                            CustomImageView(
                                modifier = Modifier.fillMaxWidth(),
                                context = this@ProductsActivity,
                                imageUrl = U1R.BASE_IMAGE_URL+U1R.SUB_FOLDER_PRODUCT+selectedImage.image,
                                okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                            )
                            Button(onClick = {

                                deleteImage()
                            },
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)) {
                                Text("حذف الصورة")
                            }

                    }
                }
            }
        }
    }
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri2: Uri? ->
            if (uri2 != null){
                uri.value = uri2
            }
            else{
                isShowAddImage.value = false
            }
        }
    fun updateImage(file: InputStream?, id:Int) {
        stateController.startAud()
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

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("id",id.toString())
            .addFormDataPart("image", "file.jpg", requestBody)
            .build()

//        val urli = "https://user2121.greenland-rest.com/public/api/v1/upload-image"


        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/updateProductImage",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result:ProductImage =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            selectedImage = result
//            val pre = storeProducts.value.map { storeProduct ->
//                // Update the products of each store product
//                val updatedProducts = storeProduct.products.map { product ->
//
//                    // Remove the image with the matching ID
//                    val filteredImages = product.images.filterNot { image -> image.id == result.id }
//
//                    // Return a new product with the filtered images (removing the unwanted image)
////                    product.copy(images = filteredImages)
//                    product.copy(productName = "mustafafa")
//                }
//
//                // Return a new StoreProduct with the updated products
//                storeProduct.copy(products = updatedProducts)
//            }
//            Log.e("pre",pre.toString())
//            storeProducts.value = pre
//            Log.e("post",storeProducts.value.toString())


            storeProducts.value = storeProducts.value.map { product ->
                    // Update the images of each product
                    val updatedImages = product.images.map { image ->
                        // Check if the image ID matches the result and update the image
                        if (image.id == result.id) {
                            Log.e("11jiamge", result.toString())
                            // Replace image with the new URL (result.image)
                            image.copy(image = result.image)
                        } else {
                            image // Leave the image unchanged
                        }
                    }
                    // Return a new product with the updated images
                    product.copy(images = updatedImages)
            }


            Log.e("jiamge",result.toString())

            isShowUpdateImage.value = false
            uri.value = null
            stateController.successStateAUD("تم تعديل الصورة بنجاح")
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
        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("id",selectedImage.id.toString())
            .build()

//        val urli = "https://user2121.greenland-rest.com/public/api/v1/upload-image"


        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/deleteProductImage",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){
            storeProducts.value = storeProducts.value.map { product ->
                val updatedImages = product.images.toMutableList()  // Convert to mutable list to add new images
                updatedImages.remove(selectedImage)

                // Return a new product with the updated images
                product.copy(images = updatedImages)
            }


            isShowDeleteImage.value = false
            uri.value = null
            stateController.successStateAUD("تم حذف الصورة بنجاح")
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
    fun addImage(file: InputStream?) {
        stateController.startAud()
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

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("productId",selectedStoreProduct.productId.toString())
            .addFormDataPart("image", "file.jpg", requestBody)
            .build()

//        val urli = "https://user2121.greenland-rest.com/public/api/v1/upload-image"


        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/addProductImage",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result:ProductImage =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            selectedImage = result
//            val pre = storeProducts.value.map { storeProduct ->
//                // Update the products of each store product
//                val updatedProducts = storeProduct.products.map { product ->
//
//                    // Remove the image with the matching ID
//                    val filteredImages = product.images.filterNot { image -> image.id == result.id }
//
//                    // Return a new product with the filtered images (removing the unwanted image)
////                    product.copy(images = filteredImages)
//                    product.copy(productName = "mustafafa")
//                }
//
//                // Return a new StoreProduct with the updated products
//                storeProduct.copy(products = updatedProducts)
//            }
//            Log.e("pre",pre.toString())
//            storeProducts.value = pre
//            Log.e("post",storeProducts.value.toString())


            storeProducts.value = storeProducts.value.map { product ->
                    // Update the images of each product
                    val updatedImages = product.images.map { image ->
                        // Check if the image ID matches the result and update the image
                        if (image.id == result.id) {
                            Log.e("11jiamge", result.toString())
                            // Replace image with the new URL (result.image)
                            image.copy(image = result.image)
                        } else {
                            image // Leave the image unchanged
                        }
                    }.toMutableList()  // Convert to mutable list to add new images

//                    updatedImages.add(result)
//                    // If the image with result.id is not found, add the new image
                    if (selectedStoreProduct.productId == product.productId) {
                        updatedImages.add(result)
                         // Add the new image to the list
                    }


                    // Return a new product with the updated images
                    product.copy(images = updatedImages)
            }


            Log.e("jiamge",result.toString())

            isShowAddImage.value = false
            uri.value = null
            stateController.successStateAUD("تم اضافة الصورة بنجاح")
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
    private fun addproductOption(price: String, optionId: Int,productId:Int) {

        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("productId",productId.toString())
            .addFormDataPart("optionId", optionId.toString())
            .addFormDataPart("CsPsSCRId", storeNestedSection.id.toString())
            .addFormDataPart("price",price.toString())
            .build()

//        val urli = "https://user2121.greenland-rest.com/public/api/v1/upload-image"


        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/addProductOption",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result:ProductOption =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )

            storeProducts.value = storeProducts.value.map { product ->
                    // Check if the productId matches the selectedProduct's productId
                    val updatedOptions = product.options.toMutableList()  // Convert options to a mutable list

                    if (productId == product.productId) {
                        // Only add the result if the productId matches
                        updatedOptions.add(result)  // Add the new image (result) to the options
                    }

                    // Return a new product with the updated options (images)
                    product.copy(options = updatedOptions)
            }



            Log.e("jiamge",result.toString())

            isShowAddProductOption.value = false
            stateController.successStateAUD("تم اضافة الصورة بنجاح")
        }
    }

    fun updateProductName(value:String) {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("productId",selectedStoreProduct.productId.toString())
            .addFormDataPart("productName",value)
            .build()

        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/updateProductName",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result:StringResult =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            storeProducts.value = storeProducts.value.map { product ->
                // Update the products of each store product
                    if (selectedStoreProduct.productId == product.productId){
                        product.copy(productName = result.result)
                    }
                    else product

            }

            isShowUpdateText.value = false
            stateController.successStateAUD("تم التحديث بنجاح")
        }
    }
    fun updateProductDescription(value:String) {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("productId",selectedStoreProduct.productId.toString())
            .addFormDataPart("description",value)
            .build()

        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/updateProductDescription",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result:StringResult =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            storeProducts.value = storeProducts.value.map { product ->
                // Update the products of each store product
                    if (selectedStoreProduct.productId == product.productId){
                        product.copy(productDescription = result.result)
                    }
                    else product
                }

            }

            isShowUpdateText.value = false
            stateController.successStateAUD("تم التحديث بنجاح")

    }
    fun updateProductOptionName(value:String) {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeProductId",selectedProductOption.storeProductId.toString())
            .addFormDataPart("optionId", value.toString())
            .build()

        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/updateProductOptionName",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result:StringResult =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            storeProducts.value = storeProducts.value.map { product ->
                    val updatedOptions = product.options.map { option ->
                        if (selectedProductOption.optionId == option.optionId){
                            option.copy(name = result.result)
                        }else{
                            option
                        }
                    }
                    product.copy(options = updatedOptions)

            }

            isShowUpdateText.value = false
            stateController.successStateAUD("تم التحديث بنجاح")
        }
    }
    fun updateProductOptionPrice(value:String) {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeProductId",selectedStoreProduct.productId.toString())
            .addFormDataPart("price",value)
            .build()

        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/updateProductOptionPrice",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result:StringResult =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            storeProducts.value = storeProducts.value.map { product ->
                // Update the products of each store product
                    val updatedOptions = product.options.map { option ->
                        if (selectedProductOption.optionId == option.optionId){
                            option.copy(price = result.result)
                        }else{
                            option
                        }
                    }
                    product.copy(options = updatedOptions)

            }

            isShowUpdateText.value = false
            stateController.successStateAUD("تم التحديث بنجاح")
        }
    }
    fun readOptions() {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("d","e")
            .build()

        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/readOptions",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            options.value =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            stateController.successStateAUD()
        }
    }
    fun readStoreCategories() {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("d","e")
            .build()

        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/readStoreCategories",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            storeCategories.value =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            stateController.successStateAUD()
        }
    }
    fun deleteProductOptions(ids:List<Int>,onDone:()->Unit) {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("ids",ids.toString())
            .build()

        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/deleteProductOptions",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            storeProducts.value = storeProducts.value.map { product ->
                // Update the products of each store product
                    // Update the images of each product
                    val updatedOptions = product.options.filterNot { it1 -> it1.storeProductId in ids }
                    // Use filterNot to remove options with storeProductId in ids

                    // Return a new product with the updated options
                    product.copy(options = updatedOptions)

            }

            onDone()

            stateController.successStateAUD()
        }
    }

    fun readProducts() {
        stateController.startAud()
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("nestedSectionId", storeNestedSection.nestedSectionId.toString())
            .addFormDataPart("storeId", "1")
            .build()

        requestServer.request(body, "${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/getProducts", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->

            productsToSelect.value =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            stateController.successStateAUD()
        }
    }
}

@Serializable
data class StringResult(val result:String)


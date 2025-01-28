package com.fekraplatform.storemanger.activities

import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.room.Room
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.models.Currency
import com.fekraplatform.storemanger.models.StoreCategory
import com.fekraplatform.storemanger.models.Option
import com.fekraplatform.storemanger.models.Product2
import com.fekraplatform.storemanger.models.StoreProduct
import com.fekraplatform.storemanger.models.ProductImage
import com.fekraplatform.storemanger.models.ProductOption
import com.fekraplatform.storemanger.models.NativeProductView
import com.fekraplatform.storemanger.models.Product
import com.fekraplatform.storemanger.models.ProductView
import com.fekraplatform.storemanger.models.StoreNestedSection
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomImageView
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.IconDelete
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.StoredProducts
import com.fekraplatform.storemanger.shared.U1R
import com.fekraplatform.storemanger.shared.builderForm3
import com.fekraplatform.storemanger.shared.formatPrice
import com.fekraplatform.storemanger.storage.AppDatabase
import com.fekraplatform.storemanger.storage.Date
import com.fekraplatform.storemanger.storage.Images
import com.fekraplatform.storemanger.storage.ProductViews
import com.fekraplatform.storemanger.storage.ProductsStorageDBManager
import com.fekraplatform.storemanger.storage.StoreProducts
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.InputStream
import java.time.Duration
import java.time.LocalDateTime


class ProductsActivity : ComponentActivity() {
    val productsStorageDBManager = ProductsStorageDBManager(this)

    val stateController = StateController()
    val requestServer = RequestServer(this)
    private val productViews = mutableStateOf<List<ProductView>>(listOf())
    private var nativeProductViews by mutableStateOf<List<NativeProductView>>(listOf())
    private val storeCategories = mutableStateOf<List<StoreCategory>>(listOf())

    var isShowChooseProductView by mutableStateOf(false)

    //        private val products = mutableStateOf<List<Product>>(listOf())
    private val products = mutableStateOf<List<Product2>>(listOf())
    private val currencies = mutableStateOf<List<Currency>>(listOf())

    //    lateinit var selectedProduct: Product

    val UPDATE_PRODUCT_NAME = 1
    val UPDATE_PRODUCT_DESCRIPTION = 2
    val UPDATE_OPTION_NAME = 3
    val UPDATE_OPTION_PRICE = 4

    var SELECTED_UPDATE = -1

    val uri = mutableStateOf<Uri?>(null)

    val isShowUpdateImage = mutableStateOf(false)
    val isShowAddImage = mutableStateOf(false)
    val isShowDeleteImage = mutableStateOf(false)
    val isShowUpdateText = mutableStateOf(false)
    val isShowAddProductOption = mutableStateOf(false)
    val isShowAddProductStore = mutableStateOf(false)
    lateinit var selectedImage: ProductImage
    lateinit var selectedStoreProduct: StoreProduct
    lateinit var selectedProductOption: ProductOption
    lateinit var selectedProduct: Product2
    private val options = mutableStateOf<List<Option>>(listOf())
    lateinit var storeNestedSection: StoreNestedSection
    val isShowAddCatgory = mutableStateOf(false)
    var isShowUpdateProductOrder by mutableStateOf(false)
    var isShowChooseCurrencies by mutableStateOf(false)
    val isShowChooseOptionAndPrice = mutableStateOf(false)

    lateinit var storeId: String

    var isOption = false


    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val str = intent.getStringExtra("storeNestedSection")
        if (str != null) {
            try {
                storeNestedSection = MyJson.IgnoreUnknownKeys.decodeFromString(str)
            } catch (e: Exception) {
                finish()
            }
        } else {
            finish()
        }

//        storeId=  if (CustomSingleton.isSharedStore()) CustomSingleton.getCustomStoreId().toString()Reference else CustomSingleton.getCustomStoreId().toString()

        storeId = CustomSingleton.getCustomStoreId().toString()

        processAndRead()


//        if (productsStorageDBManager.isSet(storeId,storeNestedSection.id.toString())){
//            val diff =
//                Duration.between(productsStorageDBManager.getDate(storeId,storeNestedSection.id.toString()), getCurrentDate()).toMinutes()
//            if (diff <= 1){
//
////                val storeProduct : List<StoreProduct>
//
//                val res = productsStorageDBManager.getStoreProducts(storeId,storeNestedSection.id.toString())
//                res.forEach {
//                    Log.e("res",it.options.joinToString(","){it.storeProductId.toString() })
//                }
//
//                if (CustomSingleton.isSharedStore()){
//                    productViews.value = res.map {
//                        val newOptions = it.options.filterNot {
//                            it.storeProductId in CustomSingleton.selectedStore!!.storeConfig!!.products
//                        }
//                        it.copy(options = newOptions)
//                    }
//                }else{
//                    productViews.value = res
//                }
//
//
//                productViews.value = res
//                stateController.successState()
////
//            }else{
//                read()
//            }
//        }else{
//            read()
//        }


        setContent {
            StoreMangerTheme {
                MainCompose1(
                    0.dp, stateController, this,
                    { processAndRead() },
                ) {
                    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
                    var offsetY by remember { mutableStateOf(0f) }
                    LazyColumn(Modifier.fillMaxSize()) {

                        item {
                            SingletonStoreConfig.EditModeCompose()
                        }


//                        val r = storeProducts.value
//                        val prods = if (CustomSingleton.isSharedStore()){
//                            if (SingletonHome.isEditMode.value)
//                                r
//                             else
//                            r.filterNot { it.storeProductId in SingletonStoreConfig.products.value }
//                        }
//                        else r

//                        if (CustomSingleton.isSharedStore()){
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

                        productViews.value.forEach { productView ->

                            item {
                                Text(productView.name)
                                HorizontalDivider(Modifier.padding(8.dp))
                            }
                            itemsIndexed(productView.products) { index, storeProduct ->
                                var isExpanded by remember { mutableStateOf(true) }
                                Card(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(5.dp)
//                                        .pointerInput(Unit) {
//                                            detectDragGestures { _, dragAmount ->
//                                                val currentIndex = productView.products.indexOf(storeProduct)
//                                                val newOffsetY = offsetY + dragAmount.y
//                                                offsetY = newOffsetY
//
//                                                // Determine new position of the dragged item based on the vertical movement
//                                                val newIndex = (currentIndex + (newOffsetY / 100).toInt()).coerceIn(0, productView.products.size - 1)
//
//                                                if (newIndex != currentIndex) {
//                                                    val newProducts = productViews.value.find { it.id == productView.id }!!.products.toMutableList()
//
//                                                    newProducts.apply {
//                                                        removeAt(currentIndex)
//                                                        add(newIndex, storeProduct)
//                                                    }
//
//                                                        productViews.value = productViews.value.map {
//                                                            it.copy(products = newProducts)
//                                                        }
//                                                }
//                                            }
//                                        }

                                        .clickable {
                                            isExpanded = !isExpanded
                                        }

                                ) {


                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(storeProduct.product.productName,
                                            Modifier
                                                .padding(8.dp)
                                                .clickable {
                                                    if (!CustomSingleton.isSharedStore()){
                                                        selectedStoreProduct = storeProduct
                                                        SELECTED_UPDATE = UPDATE_PRODUCT_NAME
                                                        isShowUpdateText.value = true
                                                    }
                                                }, fontWeight = FontWeight.Bold, fontSize = 16.sp
                                        )
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
                                                imageVector = if (isExpanded) Icons.Outlined.ArrowDropDown else Icons.Outlined.KeyboardArrowUp,
                                                contentDescription = ""
                                            )
                                        }
                                    }
                                    if (!isExpanded) {
                                        var ids by remember { mutableStateOf<List<Int>>(emptyList()) }
                                        Text(
                                            storeProduct.product.productDescription.toString(),
                                            Modifier.clickable {
                                                if (!CustomSingleton.isSharedStore()){
                                                    selectedStoreProduct = storeProduct
                                                    SELECTED_UPDATE = UPDATE_PRODUCT_DESCRIPTION
                                                    isShowUpdateText.value = true
                                                }
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )

                                        Text(productView.name,
                                            Modifier
                                                .padding(8.dp)
                                                .clickable {
                                                    if (!CustomSingleton.isSharedStore()) {
                                                        selectedStoreProduct = storeProduct
                                                        if (nativeProductViews.isEmpty())
                                                            readProductViews()
                                                        else {
                                                            isShowChooseProductView = true
                                                        }
                                                    }
                                                })

                                        if (!CustomSingleton.isSharedStore())
                                            CustomIcon(Icons.Default.Menu) {
                                                isOption = false
                                                selectedStoreProduct = storeProduct
                                                isShowUpdateProductOrder = true
                                            }
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("الخيارات")
                                            if (!CustomSingleton.isSharedStore()){
                                                CustomIcon(Icons.Outlined.Add,) {
                                                    selectedStoreProduct = storeProduct
                                                    isShowAddProductOption.value = true
                                                }
                                                IconDelete(ids) {
                                                    deleteProductOptions(ids, {
                                                        ids = emptyList()
                                                    })
                                                }
                                            }
                                        }

//                                        val opts = if (CustomSingleton.isSharedStore()){
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

//                                        isShow = if (CustomSingleton.isSharedStore()) {
//                                            opts.isNotEmpty()
//                                        } else {
//                                            true // Assuming you want to show all products if it's not a shared store
//                                        }


                                        storeProduct.options.forEach { productOption ->
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(productOption.name, Modifier.clickable {
                                                    if (!CustomSingleton.isSharedStore()) {
                                                        selectedProductOption = productOption
                                                        selectedStoreProduct = storeProduct
                                                        SELECTED_UPDATE = UPDATE_OPTION_NAME
                                                        isShowUpdateText.value = true
                                                    }

                                                })
                                                Text(formatPrice(productOption.price), Modifier.clickable {
                                                    if (!CustomSingleton.isSharedStore()) {
                                                        selectedProductOption = productOption
                                                        selectedStoreProduct = storeProduct
                                                        SELECTED_UPDATE = UPDATE_OPTION_PRICE
                                                        isShowUpdateText.value = true
                                                    }
                                                })
                                                Text(
                                                    modifier = Modifier.padding(8.dp).clickable {
                                                        if (!CustomSingleton.isSharedStore()){
                                                            selectedProductOption = productOption
                                                            selectedStoreProduct = storeProduct
                                                            isShowChooseCurrencies = true
                                                        }
                                                    },
                                                    text =  productOption.currency.name,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )

                                                if (!CustomSingleton.isSharedStore()) {
                                                    CustomIcon(Icons.Default.Menu) {
                                                        isOption = true
                                                        selectedProductOption = productOption
                                                        isShowUpdateProductOrder = true
                                                    }
                                                    Checkbox(checked = ids.find { it == productOption.storeProductId } != null,
                                                        onCheckedChange = {
                                                            val itemC =
                                                                ids.find { it == productOption.storeProductId }
                                                            if (itemC == null) {
                                                                ids =
                                                                    ids + productOption.storeProductId
                                                            } else {
                                                                ids =
                                                                    ids - productOption.storeProductId
                                                            }
                                                        })
                                                }

                                            }
                                            if (SingletonHome.isEditMode.value && CustomSingleton.isSharedStore()) {
                                                if (CustomSingleton.selectedStore!!.storeConfig!!.products.any { number -> number == productOption.storeProductId }) {
                                                    if (!SingletonHome.products.value.any { it == productOption.storeProductId }) {
                                                        Text(
                                                            "تمت الاضافة بانتظار التأكيد",
                                                            Modifier

                                                                .clickable {
                                                                    SingletonHome.products.value += productOption.storeProductId
                                                                })
                                                    } else {
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
                                                } else {
                                                    if (SingletonHome.products.value.any { it == productOption.storeProductId }) {
                                                        Text("تمت الحذف بانتظار التأكيد",
                                                            Modifier
//                                                                .align(Alignment.BottomEnd)
                                                                .clickable {
                                                                    SingletonHome.products.value -= productOption.storeProductId
                                                                })
                                                    } else {
                                                        Text("حذف",
                                                            Modifier
                                                                .clickable {
                                                                    SingletonHome.products.value += productOption.storeProductId
                                                                })
                                                    }

                                                }
                                            }
                                            HorizontalDivider(Modifier.padding(8.dp))
                                        }


                                        if (!CustomSingleton.isSharedStore()) {
                                            Row(
                                                Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (!CustomSingleton.isSharedStore())
                                                    CustomIcon(Icons.Default.Add, true) {
                                                        getContent.launch("image/*")
                                                        selectedStoreProduct = storeProduct
                                                        isShowAddImage.value = true
                                                    }

                                                Text("الصور")
                                            }
                                            LazyRow(
                                                Modifier.height(120.dp)
                                            ) {
                                                itemsIndexed(storeProduct.product.images) { index: Int, item: ProductImage ->
                                                    Log.e(
                                                        "urlfff",
                                                        requestServer.serverConfig.getRemoteConfig().BASE_IMAGE_URL + requestServer.serverConfig.getRemoteConfig().SUB_FOLDER_PRODUCT + item.image
                                                    )
                                                    Box(Modifier.size(100.dp)) {
                                                        CustomImageView(
                                                            modifier = Modifier
                                                                .size(100.dp)
                                                                .padding(8.dp)
                                                                .clickable {
                                                                    if (!CustomSingleton.isSharedStore()) {
                                                                        selectedImage = item
                                                                        isShowUpdateImage.value =
                                                                            true
                                                                    }
                                                                },
                                                            context = this@ProductsActivity,
                                                            imageUrl = requestServer.serverConfig.getRemoteConfig().BASE_IMAGE_URL + requestServer.serverConfig.getRemoteConfig().SUB_FOLDER_PRODUCT + item.image,
                                                            okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                                                        )

                                                        if (!CustomSingleton.isSharedStore())
                                                            CustomIcon(Icons.Outlined.Delete) {
                                                                selectedImage = item
                                                                isShowDeleteImage.value = true
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
                        }

                        if (!CustomSingleton.isSharedStore())
                            item {
                                Card(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .padding(8.dp)
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .clickable {
                                                isShowAddCatgory.value = true
                                            }
                                    ) {
                                        Text("+", modifier = Modifier.align(Alignment.Center))
                                    }
                                }
                            }
//                            Column {
//
//                            }
                    }


                    if (isShowUpdateImage.value) modalUpdateImage()
                    if (isShowAddImage.value) modalAddImage()
                    if (isShowDeleteImage.value) modalDeleteImage()
                    if (isShowUpdateText.value) modalUpdateText()
                    if (isShowAddProductOption.value) modalAddProductOption()
                    if (isShowAddProductStore.value) modalAddProductStore()
                    if (isShowAddCatgory.value) modalAddNewProduct()
                    if (isShowChooseOptionAndPrice.value) modalChooseOptionAndPrice()
                    if (isShowChooseProductView) modalChooseProductView()
                    if (isShowUpdateProductOrder) modalUpdateProductOrder()
                    if (isShowChooseCurrencies) modalChooseCurrencies()
                }
            }
        }
    }

    private fun processAndRead() {
        val s =
            CustomSingleton.storedProducts.find { it.storeId == storeId.toInt() && it.storeNestedSectionId == storeNestedSection.id }
        if (s != null) {
            val diff = Duration.between(s.storeAt, getCurrentDate()).toSeconds()
            if (diff <= 30) {
                productViews.value = s.productViews
                ///
//                if (CustomSingleton.isSharedStore()){
//                    productViews.value = productViews.value.map { view->
//                        val newProductsStore = view.products.map {  product->
//                            val newOptions = product.options.filterNot { it.storeProductId in CustomSingleton.selectedStore!!.storeConfig!!.products }
//                            product.copy(options = newOptions)
//                        }
//                        view.copy(products = newProductsStore)
//                    }
//                }
                if (CustomSingleton.isSharedStore() && !SingletonHome.isEditMode.value) {
                    productViews.value = productViews.value.map { productView ->
                        var newProducts = productView.products.map { product ->
                            val newOptions = product.options.filterNot { option ->
                                option.storeProductId in CustomSingleton.selectedStore!!.storeConfig!!.products
                            }
                            product.copy(options = newOptions)
                        }
                        productView.copy(products = newProducts.filterNot { it.options.isEmpty() })
                    }
                }

                stateController.successState()
            } else read {
                updateStoredProducts()
            }
        } else
            read {
                CustomSingleton.storedProducts += storedProducts()
            }
    }

    private fun updateStoredProducts() {
        CustomSingleton.storedProducts = CustomSingleton.storedProducts.map {
            if (it.storeId == storeId.toInt() && it.storeNestedSectionId == storeNestedSection.id) {
                it.copy(productViews = productViews.value, storeAt = getCurrentDate())
            } else
                it
        }
    }

    private fun storedProducts() = StoredProducts(
        storeId.toInt(), storeNestedSection.id, productViews.value,
        getCurrentDate()
    )

    fun getProductViews(db: AppDatabase): List<ProductView> {
        val storeProducts =
            db.storeProductsDao().loadAllByIds(storeId.toInt(), storeNestedSection.id)
        val productIds = mutableListOf<Int>()
        storeProducts.forEach {
            productIds.add(it.productId)
        }
        val images = db.imagesDao().loadAllByIds(productIds.toIntArray())

        val storeProducts0 = getStoreProducts(storeProducts, images)

        val productViews0 = db.productViewDao().loadDate()
        var productViews1: List<ProductView> = emptyList()

        productViews0.forEach {
            val prods = emptyList<StoreProduct>().toMutableList()
            storeProducts0.forEach { storeProduct ->
                if (it.id == storeProduct.product.productViewId) {
                    prods += storeProduct
                }
            }
            productViews1 += ProductView(it.id, it.name, prods.toList())
        }
        return productViews1
    }

    private fun processData() {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "dbb"
        ).build()


        CoroutineScope(Dispatchers.IO).launch {
            val insertedDate = db.dateDao().loadDate()

            if (insertedDate != null) {
                val date = LocalDateTime.parse(insertedDate)
                val diff = Duration.between(date, getCurrentDate()).toSeconds()
                if (diff <= 30) {
                    productViews.value = getProductViews(db)
                } else {
                    AddData(db)
                }
            } else {
                read {
                    AddData(db)
                }
            }

        }

    }

    private fun getStoreProducts(
        storeProducts: List<StoreProducts>,
        images: List<Images>
    ): List<StoreProduct> {
        var result: MutableList<StoreProduct> = mutableListOf()


        storeProducts.forEach { storeProduct ->
            if (!result.any { it.product.productId == storeProduct.productId }) {
                val imgs = images.filter { it.productId == storeProduct.productId }
                val newImgs = mutableListOf<ProductImage>()
                imgs.forEach {
                    newImgs.add(ProductImage(it.id, it.image))
                }

                result +=
                    StoreProduct(
                        product = Product(
                            storeProduct.productId,
                            storeProduct.productViewId,
                            storeProduct.ProductName,
                            storeProduct.productDescription,
                            newImgs
                        ),
                        storeNestedSectionId = storeNestedSection.id,
                        options = emptyList()
                    )

            }

            result = result.map { r ->
                if (r.product.productId == storeProduct.productId) {
                    var newOption = r.options
                    newOption += ProductOption(
                        storeProduct.optionId,
                        Currency(
                            storeProduct.currencyId,
                            storeProduct.currencyName,
                            storeProduct.currencySign
                        ), storeProduct.id, storeProduct.optionName, storeProduct.price.toString()

                    )
                    r.copy(options = newOption)
                } else
                    r

            }.toMutableList()

        }
        return result
    }


    private fun AddData(db: AppDatabase) {
        val storeProducts = mutableListOf<StoreProducts>()
        val productViews0 = db.productViewDao().loadDate()
        val images = mutableListOf<Images>()
        productViews.value.forEach { productView ->
            if (!productViews0.any { it.id == productView.id }) {
                db.productViewDao().insert(ProductViews(productView.id, productView.name))
            }

            productView.products.forEach { product ->
                product.product.images.forEach {
                    images.add(Images(it.id, it.image, product.product.productId))
                }
                product.options.forEach { option ->
                    val storeProduct = StoreProducts(
                        option.storeProductId,
                        product.product.productId,
                        product.product.productName,
                        product.product.productDescription.toString(),
                        storeNestedSection.id,
                        option.price.toDouble(),
                        storeId.toInt(),
                        option.optionId,
                        option.name,
                        option.currency.id,
                        option.currency.name,
                        option.currency.sign,
                        productView.id,
                        productView.name
                    )
                    storeProducts.add(storeProduct)
                }
            }
        }

        storeProducts.forEach {
            db.storeProductsDao().insertAll(it)
        }
        images.forEach {
            db.imagesDao().insert(it)
        }
        db.dateDao().insertOrUpdate(Date(1, getCurrentDate().toString()))
    }

    fun read(onDone: () -> Unit) {
        stateController.startRead()

        Log.e("sec", storeNestedSection.toString())
        val body = builderForm3()
            .addFormDataPart("storeNestedSectionId", storeNestedSection.id.toString())
            .addFormDataPart("storeId", CustomSingleton.getCustomStoreId().toString())
            .build()

        Log.e("ff", storeNestedSection.id.toString())
        Log.e("ff2", storeId.toString())

        requestServer.request2(body, "getMain", { code, fail ->
            stateController.errorStateRead(fail)
        }
        ) { data ->

            val result: List<ProductView> =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

//            productsStorageDBManager.clearAllData(storeNestedSection.id.toString())
//
//            productsStorageDBManager.addStoreProducts(result,
//               CustomSingleton.getCustomStoreId().toString()
//                ,storeNestedSection.id)


//            Log.e("storePP",CustomSingleton.selectedStore!!.storeConfig!!.products.toString())
//            Log.e("storeResPP",result.joinToString (","){ it.productId.toString() })
//            result.forEach {
//                Log.e("res0",it.options.joinToString(","){it.storeProductId.toString() })
//            }

//            if (CustomSingleton.isSharedStore()){
//                productViews.value = productViews.value.map { view->
//                    val newProductsStore = view.products.map {  product->
//                        val newOptions = product.options.filterNot { it.storeProductId in CustomSingleton.selectedStore!!.storeConfig!!.products }
//                        product.copy(options = newOptions)
//                    }
//                    view.copy(products = newProductsStore)
//                }
//            }

            if (CustomSingleton.isSharedStore() && !SingletonHome.isEditMode.value) {
                productViews.value = result.map { productView ->
                    var newProducts = productView.products.map { product ->
                        val newOptions = product.options.filterNot { option ->
                            option.storeProductId in CustomSingleton.selectedStore!!.storeConfig!!.products
                        }
                        product.copy(options = newOptions)
                    }
                    productView.copy(products = newProducts.filterNot { it.options.isEmpty() })
                }
            } else {
                productViews.value = result
            }

            onDone()
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
//
//        var price by remember { mutableStateOf("") }
//        var option by remember { mutableStateOf<Option?>(null) }
//        var storeCategory by remember { mutableStateOf<StoreCategory?>(null) }
//        var product by remember { mutableStateOf<Product2?>(null) }
//        ModalBottomSheet(
//            onDismissRequest = { isShowAddProductStore.value = false }) {
//            Box(
//                Modifier
//                    .fillMaxSize()
//                    .padding(bottom = 10.dp)
//            ){
//                LazyColumn(
//                    Modifier.fillMaxSize(),
//                    horizontalAlignment = Alignment.Start
//                ) {
//                    item {
//                        TextField(
//                            modifier = Modifier.fillMaxWidth(),
//                            value = price , onValueChange = {
//                                price = it
//                            },
//                            label = {
//                                Text("السعر")
//                            }
//                        )
//                    }
//                    item {
//                        var expanded by remember { mutableStateOf(false) }
//                        Card(Modifier.padding(8.dp)) {
//                            Row (
//                                Modifier
//                                    .fillMaxWidth()
//                                    .padding(8.dp)
//                                    .clickable {
//                                        if (products.value.isEmpty()) {
//                                            readProducts()
//                                        }
//                                        expanded = !expanded
//                                    },
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ){
//                                Text(product?.name ?: "اختر منتج")
//                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
//                            }
//                            val productIds= emptyList<Int>().toMutableList()
//                            productViews.value.forEach {
//                                it.products.forEach {
//                                    productIds.add(it.productId)
//                                }
//                            }
//
//                            if (expanded)
//                                products.value.filterNot { product1 ->
//                                   productIds.any { it == product1.id }
//                                    // Compare by the 'name' field
////                                    it.products.any { it.productId ==  }
//                                }.forEach { item ->
//                                    DropdownMenuItem(onClick = {
//                                        product = item
//                                        expanded = false // Close the dropdown after selection
//                                    }, text = {
//                                        Text(item.name)
//                                    })
//                                }
//
//                        }
//                    }
////                    item {
////                        var expanded by remember { mutableStateOf(false) }
////                        Card(Modifier.padding(8.dp)) {
////                            Row (Modifier.fillMaxWidth().padding(8.dp).clickable {
////                                if (storeCategories.value.isEmpty()){
////                                    readStoreCategories()
////                                }
////                                expanded = !expanded
////                            },
////                                verticalAlignment = Alignment.CenterVertically,
////                                horizontalArrangement = Arrangement.SpaceBetween
////                            ){
////                                Text(storeCategory?.categoryName ?: "اختر فئة")
////                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
////                            }
////                            if (expanded)
////                                storeCategories.value.forEach { item ->
////                                    DropdownMenuItem(onClick = {
////                                        storeCategory = item
////                                        expanded = false // Close the dropdown after selection
////                                    }, text = {
////                                        Text(item.categoryName)
////                                    })
////                                }
////
////                        }
////                    }
//                    item {
//                        var expanded by remember { mutableStateOf(false) }
//                        Card(Modifier.padding(8.dp)) {
//                            Row (
//                                Modifier
//                                    .fillMaxWidth()
//                                    .padding(8.dp)
//                                    .clickable {
//                                        if (options.value.isEmpty()) {
//                                            readOptions()
//                                        }
//                                        expanded = !expanded
//                                    },
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ){
//                                Text(option?.name ?: "اختر الخيار")
//                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
//                            }
//                            if (expanded)
//                                options.value.forEach { item ->
//                                    DropdownMenuItem(onClick = {
//                                        option = item
//                                        expanded = false // Close the dropdown after selection
//                                    }, text = {
//                                        Text(item.name)
//                                    })
//                                }
//
//                        }
//                    }
//                    item {
//                        if (option != null && product != null && price.length > 0)
//                            Button(
//                                modifier = Modifier.fillMaxWidth(),
//                                onClick = {
//                                    addproductOption(price, option!!.id ,product!!.id)
//                                }) { Text("حفظ") }
//                    }
//
//
//                    // State to manage the dropdown visibility
//
//
////                    item {
////                        var expanded by remember { mutableStateOf(false) }
////                        Column {
////                            // Button to show the dropdown menu
////                            OutlinedTextField(
////                                value = (option?.name ?: ""),
////                                onValueChange = {},
//////                                label = { Text("Select Item") },
//////                            trailingIcon = {
//////                                Icon(
//////                                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.ArrowDropDown,
//////                                    contentDescription = null
//////                                )
//////                            },
////                                readOnly = true,
////                                modifier = Modifier
////                                    .fillMaxWidth()
////                                    .clickable { expanded = !expanded } // Toggle dropdown
////                            )
////
////                            // Dropdown Menu
////                            ExposedDropdownMenuBox(
////                                expanded = expanded,
////                                onExpandedChange = { expanded = it }
////                            ) {
////                                ExposedDropdownMenu(
////                                    expanded = expanded,
////                                    onDismissRequest = { expanded = false } // Close dropdown
////                                ) {
////                                    options.value.forEach { item ->
////                                        DropdownMenuItem(onClick = {
////                                            option = item
////                                            expanded = false // Close the dropdown after selection
////                                        }, text = {
////                                            Text(item.name)
////                                        })
////                                    }
////                                }
////                            }
////                        }
////                    }
//
//
////                        itemsIndexed(options.value){index: Int, item: Option ->
////                            Button(
////                                onClick = {
////                                    option = item
////                                }
////                            ) { Text(item.name) }
////                        }
//                }
//            }
//        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun modalChooseProductView() {
        ModalBottomSheet(
            onDismissRequest = { isShowChooseProductView = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {

                    itemsIndexed(nativeProductViews) { index, item ->
                        Card(Modifier.padding(8.dp)) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.name)
                                Button(
                                    onClick = {
                                        updateProductView(item)
                                    }) {
                                    Text("اختيار")
                                }
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
        var currency by remember { mutableStateOf<Currency?>(null) }
        ModalBottomSheet(
            onDismissRequest = { isShowAddProductOption.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    item {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = price, onValueChange = {
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
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        if (currencies.value.isEmpty()) {
                                            readCurrencies()
                                        }
                                        expanded = !expanded
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(currency?.name ?: "اختر العملة")
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                            if (expanded)
                                currencies.value.forEach { item ->
                                    DropdownMenuItem(onClick = {
                                        currency = item
                                        expanded = false // Close the dropdown after selection
                                    }, text = {
                                        Text(item.name)
                                    })
                                }

                        }
                    }
                    item {
                        var expanded by remember { mutableStateOf(false) }
                        Card(Modifier.padding(8.dp)) {
                            Row(
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
                            ) {
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
                                 if (option != null && currency != null && price.isNotEmpty())
                                addProductOption(price, option!!.id ,currency!!.id)
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
    private fun modalUpdateProductOrder() {
        ModalBottomSheet(
            onDismissRequest = { isShowUpdateProductOrder = false }) {
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
                        var value by remember { mutableStateOf("") }
                        TextField(value = value, onValueChange = {
                            value = it
                        })
                        Button(onClick = {
                            if (value.isNotEmpty())
                                updateOrder(value)
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
    private fun modalUpdateText() {
        var value by remember { mutableStateOf("") }
        var type = -1
        var function = {}
        when (SELECTED_UPDATE) {
            UPDATE_PRODUCT_NAME -> {
                value = selectedStoreProduct.product.productName
                type = 1
            }

            UPDATE_PRODUCT_DESCRIPTION -> {
                value = selectedStoreProduct.product.productDescription!!
                type = 1
            }

            UPDATE_OPTION_PRICE -> {
                value = selectedProductOption.price
                type = 2
            }

            UPDATE_OPTION_NAME -> {
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
            ) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (SELECTED_UPDATE != UPDATE_OPTION_NAME)

                        item {
                            TextField(value = value, onValueChange = {
                                value = it
                            })
                            Button(onClick = {
                                when (SELECTED_UPDATE) {
                                    UPDATE_PRODUCT_NAME -> {
                                        updateProductName(value)
                                    }

                                    UPDATE_PRODUCT_DESCRIPTION -> {

                                        updateProductDescription(value)
                                    }

                                    UPDATE_OPTION_PRICE -> {
                                        updateProductOptionPrice(value)
                                    }
                                }


                            }) {
                                Text("حفظ")
                            }
                        }
                    if (SELECTED_UPDATE == UPDATE_OPTION_NAME)
                        itemsIndexed(options.value) { index: Int, item: Option ->
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
            ) {
                LazyColumn {
                    item {

                        if (uri.value != null) {
                            CustomImageViewUri(
                                modifier = Modifier.fillMaxWidth(),
                                imageUrl = uri.value!!,
                            )
//                        if (inputStream != null){
                            Button(
                                onClick = {
                                    val inputStream = contentResolver.openInputStream(uri.value!!)
                                    updateImage(inputStream, selectedImage.id)
                                },
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text("تأكيد تعديل الصورة",)
                            }
                        } else {
                            CustomImageView(
                                modifier = Modifier.fillMaxWidth(),
                                context = this@ProductsActivity,
                                imageUrl = U1R.BASE_IMAGE_URL + U1R.SUB_FOLDER_PRODUCT + selectedImage.image,
                                okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                            )
                            Button(
                                onClick = {
                                    getContent.launch("image/*")
                                },
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
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
            ) {
                LazyColumn {
                    item {

                        if (uri.value != null) {
//                            var imageSize by remember { mutableStateOf<Pair<Int, Int>?>(null) } // To store image width and height
//                            var imageFileSizeInMB by remember { mutableStateOf<Float?>(null) } // To store image size in MB

//                            val bitmap = inputStream?.let { BitmapFactory.decodeStream(it) }
//                            bitmap?.let {
//                                imageSize = Pair(it.width, it.height) // Store the image resolution
//                            }
////                            Log.e("inputss",inputStream!!. .toString())
//                            // Get the image file size in MB
//                            val file = File(uri.value!!.path!!)
//                            Log.e("ffff",file.path .toString())
//                            Log.e("ffff1",file.length() .toString())
//                            Log.e("ffff2",file.toString())
//                            val fileSizeInBytes = file.length()
//                            Log.e("sssii",fileSizeInBytes.toString())
//                            imageFileSizeInMB = (fileSizeInBytes / (1024 * 1024)).toFloat()
//                            Log.e("sssiiMM",imageFileSizeInMB.toString())

                            CustomImageViewUri(
                                modifier = Modifier.fillMaxWidth(),
                                imageUrl = uri.value!!,
                            )
//                            imageSize?.let { size ->
//                                Text(
//                                    text = "Resolution: ${size.first}x${size.second}",
//                                )
//                            }
//
//                            imageFileSizeInMB?.let { size ->
//                                Text(
//                                    text = "Size: %.2f MB".format(size),
//                                )
//                            }

//                        if (inputStream != null){
                            Button(
                                onClick = {
                                    val inputStream = contentResolver.openInputStream(uri.value!!)
                                    addImage(inputStream)
                                },
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
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
            ) {
                LazyColumn {
                    item {

                        CustomImageView(
                            modifier = Modifier.fillMaxWidth(),
                            context = this@ProductsActivity,
                            imageUrl = U1R.BASE_IMAGE_URL + U1R.SUB_FOLDER_PRODUCT + selectedImage.image,
                            okHttpClient = requestServer.createOkHttpClientWithCustomCert()
                        )
                        Button(
                            onClick = {

                                deleteImage()
                            },
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text("حذف الصورة")
                        }

                    }
                }
            }
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri2: Uri? ->
            if (uri2 != null) {
                uri.value = uri2
            } else {
                isShowAddImage.value = false
            }
        }

    fun updateImage(file: InputStream?, id: Int) {
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
        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/updateProductImage",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result:ProductImage =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            selectedImage = result
            productViews.value = productViews.value.map { productView->
                val newProducts =  productView.products.map { storeProduct ->
                    // Update the images of each product
                    val updatedImages = storeProduct.product.images.map { image ->
                        // Check if the image ID matches the result and update the image
                        if (image.id == result.id) {
                            Log.e("11jiamge", result.toString())
                            // Replace image with the new URL (result.image)
                            image.copy(image = result.image)
                        } else {
                            image // Leave the image unchanged
                        }
                    }
                    val product = storeProduct.product.copy(images = updatedImages)

                    // Return a new product with the updated images
                    storeProduct.copy(product = product)
                }
                productView.copy(products=newProducts)
            }




            Log.e("jiamge",result.toString())

            isShowUpdateImage.value = false
            uri.value = null
            updateStoredProducts()
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

            productViews.value = productViews.value.map { view ->
                val p=    view.products.map {  storeProduct ->
                    val updatedImages = storeProduct.product.images.toMutableList()  // Convert to mutable list to add new images
                    updatedImages.remove(selectedImage)

                    val product = storeProduct.product.copy(images = updatedImages)

                    // Return a new product with the updated images
                    storeProduct.copy(product = product)
                }
                view.copy(products = p)

            }


            isShowDeleteImage.value = false
            uri.value = null
            updateStoredProducts()
            stateController.successStateAUD("تم حذف الصورة بنجاح")
        }
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
            .addFormDataPart("productId",selectedStoreProduct.product.productId.toString())
            .addFormDataPart("image", "file.jpg", requestBody)
            .build()

        requestServer.request2(body,"addProductImage",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result:ProductImage =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            selectedImage = result
            productViews.value = productViews.value.map { view->
                val p=    view.products.map {  storeProduct ->
                    // Update the images of each product
                    val updatedImages = storeProduct.product.images.map { image ->
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
                    if (selectedStoreProduct.product.productId == storeProduct.product.productId) {
                        updatedImages.add(result)
                        // Add the new image to the list
                    }

                    val product = storeProduct.product.copy(images = updatedImages)

                    // Return a new product with the updated images
                    storeProduct.copy(product = product)
                }

                view.copy(products = p)
            }
            isShowAddImage.value = false
            uri.value = null
            updateStoredProducts()
            stateController.successStateAUD("تم اضافة الصورة بنجاح")
        }
    }
    fun readProductViews() {
        stateController.startAud()
//
        val body = builderForm3().build()
        requestServer.request2(body, "getProductViews", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) {
            nativeProductViews = MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            stateController.successStateAUD()
            isShowChooseProductView = true
        }
    }



    private fun addProductOption(
        price: String,
        optionId: Int,
        currencyId: Int,
        getWithProduct: Boolean = false
    ) {

        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("productId", if (getWithProduct) selectedProduct.id.toString() else selectedStoreProduct.product.productId.toString())
            .addFormDataPart("optionId", optionId.toString())
            .addFormDataPart("storeNestedSectionId", storeNestedSection.id.toString())
            .addFormDataPart("price", price.toString())
            .addFormDataPart("storeId", storeId)
            .addFormDataPart("currencyId", currencyId.toString())
            .addFormDataPart("getWithProduct", if (getWithProduct) "1" else "0")
            .build()

        requestServer.request2(body,"addProductOption",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
//
//
            if (getWithProduct){
                val result:StoreProduct =  MyJson.IgnoreUnknownKeys.decodeFromString(
                    it
                )
                productViews.value = productViews.value.map { productView->

                    val newProducts = productView.products.toMutableList()
                    if (productView.id == result.product.productViewId){
                        newProducts.add(result)
                    }
                    productView.copy(products = newProducts)
                }

            }
            else{
                val result:ProductOption =  MyJson.IgnoreUnknownKeys.decodeFromString(
                    it
                )

                productViews.value = productViews.value.map { productView->
                    val products = productView.products.map { product->
                        val updatedOptions = product.options.toMutableList()
                        if (selectedStoreProduct.product.productId == product.product.productId){
                            updatedOptions += result
                        }
                        product.copy(options=updatedOptions)
                    }
                    productView.copy(products = products)
                }
                updateStoredProducts()
//
//                productViews.value = productViews.value.map { product ->
//                    // Check if the productId matches the selectedProduct's productId
//                    val updatedOptions = product.options.toMutableList()  // Convert options to a mutable list
//
//                    if (productId == product.productId) {
//                        // Only add the result if the productId matches
//                        updatedOptions.add(result)  // Add the new image (result) to the options
//                        productsStorageDBManager.addProductOption2(
//                            result.storeProductId.toString(),
//                            result.optionId.toString(),storeNestedSection.id,product.productId,result.name,result.price)
//
//                    }
//
//                    // Return a new product with the updated options (images)
//                    product.copy(options = updatedOptions)
//                }
            }





//            Log.e("jiamge",result.toString())

            isShowAddCatgory.value = false
            isShowChooseOptionAndPrice.value = false
            isShowAddProductOption.value = false
            stateController.successStateAUD("تمت الاضافة  بنجاح")
        }
    }

    fun updateProductName(value: String) {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("productId",selectedStoreProduct.product.productId.toString())
            .addFormDataPart("productName", value)
            .build()

        requestServer.request2(body, "updateProductName", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { it ->
            val result: StringResult = MyJson.IgnoreUnknownKeys.decodeFromString(it)
            productViews.value = productViews.value.map { view ->
                val products = view.products.map { product ->
                    if (selectedStoreProduct.product.productId == product.product.productId) {
                        val p = product.product.copy(productName = result.result)
                        product.copy(product = p)
                    } else product
                }
                view.copy(products = products)
            }
            isShowUpdateText.value = false
            stateController.successStateAUD("تم التحديث بنجاح")
            updateStoredProducts()
        }
    }

    fun updateProductView(value: NativeProductView) {
        stateController.startAud()
        //
        val body = builderForm3()
            .addFormDataPart("productId", selectedStoreProduct.product.productId.toString())
            .addFormDataPart("storeId", SelectedStore.store.value!!.id.toString())
            .addFormDataPart("productViewId", value.id.toString())
            .build()

        requestServer.request2(body, "updateProductView", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { it ->

            val newStoreProduct =
                selectedStoreProduct.copy(product = selectedStoreProduct.product.copy(productViewId = value.id))
            var newProductViews: MutableList<ProductView> = mutableListOf()

            productViews.value.forEach {
                val p = it.products.toMutableList()
                if (it.id == value.id) {
                    if (selectedStoreProduct !in p) {
                        p += newStoreProduct
                    }
                } else {
                    if (selectedStoreProduct in p) {
                        p -= selectedStoreProduct
                    }
                }
                newProductViews.add(ProductView(it.id, it.name, p))
            }

            productViews.value = newProductViews
            updateStoredProducts()
//                productViews.value.map { productView ->
//
//                val products: MutableList<StoreProduct> = mutableListOf()
//
//                productView.products.map { storeProduct ->
//                    if (productView.id == value.id){
//
//                        if (storeProduct !in products){
//                            products+= storeProduct
//                        }
//                    }
//
//                    storeProduct
////                    if (selectedStoreProduct == storeProduct){
////
////                    }
//                }
//                productView
//            }
//            val result:ProductView =  MyJson.IgnoreUnknownKeys.decodeFromString(it)
//            productViews.value =  productViews.value.map { productView ->
//                when {
//                    // If this is the source ProductView that contains the product to move, remove the product
//                    productView.products.any { it.product.productId == selectedStoreProduct.product.productId } -> {
//                        if (productView.id == selectedStoreProduct.product.productViewId ) {
//                            // Create a new ProductView with the updated product list (without the moved product)
//                            productView.products.removeIf { it.productId == product.productId }
//                        }
//                        // If this is the target ProductView (matching the new id), add the product
//                        if (productView.id == value.id) {
//                            productView.products.add(product)
//                        }
//                    }
//                    // Otherwise, leave the product view as is
//                    else -> productView
//                }
//            }

//            productViews.value.map { productView ->
//                when {
//                    // If this is the source ProductView containing the product to move, remove the product
//                    productView.id == selectedStoreProduct.product.productId && productView.products.any { it.product.productId == selectedStoreProduct.product.productId } -> {
//                        // Create a new ProductView with the updated product list (without the moved product)
//                        productView.copy(
//                            products = productView.products.filter { it.product.productId != selectedStoreProduct.product.productId }
//                        )
//                    }
//                    // If this is the target ProductView (matching the new ppid), add the product
//                    productView.id == value.id -> {
//                        // Create a new ProductView with the new product added to its list
//                        productView.copy(
//                            products = productView.products + selectedStoreProduct
//                        )
//                    }
//                    // Otherwise, return the product view as is
//                    else -> productView
//                }
//            }

//            productViews.value
//
//            val newProductView = mutableListOf<ProductView>()
//            productViews.value.forEach { view ->
//
//                view.products.forEach { product ->
//                    if (value.id == view.id && selectedStoreProduct == product){
//
//                    }
//                }
//
//            }
//
//
//                productViews.value.map { view ->
//                // Update the products of each store product
//                val products = view.products.toMutableList()
//
//                    view.products.forEach { product->
//                        if (view.id == value.id){
//                                products += selectedStoreProduct.copy(product = product.copy(productViewId = value.id))
//                        }else{
//                            if (selectedStoreProduct == product){
//                                products -= product
//                            }
//                        }
//                    }
//                view.copy(products = products)
//            }

            isShowChooseProductView = false
            stateController.successStateAUD("تم التحديث بنجاح")
        }
    }

    fun updateOrder(value: String) {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("orderNo", value)

        if (isOption) {
            body.addFormDataPart("storeProductId", selectedProductOption.storeProductId.toString())
        } else {
            body.addFormDataPart("productId", selectedStoreProduct.product.productId.toString())
        }

        requestServer.request2(
            body.build(),
            if (isOption) "updateStoreProductOrder" else "updateProductOrder",
            { code, fail ->
                stateController.errorStateAUD(fail)
            }
        ) { it ->
            read {
                updateStoredProducts()
                isShowUpdateProductOrder = false
                stateController.successStateAUD("تم التحديث بنجاح")
            }
        }

    }

    fun updateProductDescription(value: String) {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("productId",selectedStoreProduct.product.productId.toString())
            .addFormDataPart("description", value)
            .build()

        requestServer.request2(body, "updateProductDescription", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { it ->
            val result: StringResult = MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            productViews.value = productViews.value.map { view ->
                val products = view.products.map { product ->
                    if (selectedStoreProduct.product.productId == product.product.productId) {
                        val p = product.product.copy(productDescription = result.result)
                        product.copy(product = p)
                    } else product
                }
                view.copy(products = products)
            }

            isShowUpdateText.value = false
            stateController.successStateAUD("تم التحديث بنجاح")
            updateStoredProducts()

        }
    }

   fun updateProductOptionName(value: String) {
            stateController.startAud()
            //
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("storeProductId", selectedProductOption.storeProductId.toString())
                .addFormDataPart("optionId", value.toString())
                .build()

//        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/updateProductOptionName",{code,fail->
//            stateController.errorStateAUD(fail)
//        }
//        ){it->
//            val result:StringResult =  MyJson.IgnoreUnknownKeys.decodeFromString(
//                it
//            )
//            productViews.value = productViews.value.map { product ->
//                    val updatedOptions = product.options.map { option ->
//                        if (selectedProductOption.optionId == option.optionId){
//                            option.copy(name = result.result)
//                        }else{
//                            option
//                        }
//                    }
//                    product.copy(options = updatedOptions)
//
//            }
//
//            isShowUpdateText.value = false
//            stateController.successStateAUD("تم التحديث بنجاح")
//        }
        }

   fun updateProductOptionPrice(value: String) {
            stateController.startAud()
            //
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("storeProductId", selectedProductOption.storeProductId.toString())
                .addFormDataPart("price", value)
                .build()

            requestServer.request2(body, "updateProductOptionPrice", { code, fail ->
                stateController.errorStateAUD(fail)
            }
            ) { it ->
                val result: StringResult = MyJson.IgnoreUnknownKeys.decodeFromString(
                    it
                )
                productViews.value = productViews.value.map { view ->
                    val products = view.products.map { product ->
                        // Update the products of each store product
                        val updatedOptions = product.options.map { option ->
                            if (selectedProductOption.optionId == option.optionId) {
                                option.copy(price = result.result)
                            } else {
                                option
                            }
                        }
                        product.copy(options = updatedOptions)
                    }
                    view.copy(products = products)
                }

                isShowUpdateText.value = false
                stateController.successStateAUD("تم التحديث بنجاح")
                updateStoredProducts()
            }
        }
    fun updateCurrency(value: String) {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeProductId", selectedProductOption.storeProductId.toString())
            .addFormDataPart("currencyId", value)
            .build()

        requestServer.request2(body, "updateCurrency", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { it ->
            val result: Currency = MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            productViews.value = productViews.value.map { view ->
                val products = view.products.map { product ->
                    // Update the products of each store product
                    val updatedOptions = product.options.map { option ->
                        if (selectedProductOption.optionId == option.optionId) {
                            option.copy(currency = result)
                        } else {
                            option
                        }
                    }
                    product.copy(options = updatedOptions)
                }
                view.copy(products = products)
            }

            isShowUpdateText.value = false
            stateController.successStateAUD("تم التحديث بنجاح")
            updateStoredProducts()
            isShowChooseCurrencies = false
        }
    }

        fun readOptions() {
            stateController.startAud()
            //
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("d", "e")
                .build()

            requestServer.request2(body, "getOptions", { code, fail ->
                stateController.errorStateAUD(fail)
            }
            ) { it ->
                options.value = MyJson.IgnoreUnknownKeys.decodeFromString(
                    it
                )
                stateController.successStateAUD()
            }
        }
    fun readCurrencies() {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("d", "e")
            .build()

        requestServer.request2(body, "getCurrencies", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { it ->
            currencies.value = MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )
            stateController.successStateAUD()
            isShowChooseCurrencies = true
        }
    }

        fun deleteProductOptions(ids: List<Int>, onDone: () -> Unit) {
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
            productViews.value = productViews.value.map {view->
             val p =   view.products.map {  product ->
                    // Update the products of each store product
                    // Update the images of each product
                    val updatedOptions = product.options.filterNot { it1 -> it1.storeProductId in ids }
                    // Use filterNot to remove options with storeProductId in ids

                    // Return a new product with the updated options
                    product.copy(options = updatedOptions)

                }
                view.copy(products=p)
            }
            onDone()

            stateController.successStateAUD()
        }
        }

        fun deleteProducts(ids: List<Int>, onDone: () -> Unit) {
            stateController.startAud()
            //
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("ids", ids.toString())
                .build()

            requestServer.request2(body, "deleteProducts", { code, fail ->
                stateController.errorStateAUD(fail)
            }
            ) { it ->
                onDone()
                stateController.successStateAUD()
            }
        }

        fun readProducts() {
            stateController.startAud()
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("nestedSectionId", storeNestedSection.nestedSectionId.toString())
                .addFormDataPart("storeId", CustomSingleton.getCustomStoreId().toString())
                .build()

            requestServer.request2(body, "getProducts", { code, fail ->
                stateController.errorStateAUD(fail)
            }
            ) { data ->

                products.value =
                    MyJson.IgnoreUnknownKeys.decodeFromString(
                        data
                    )

                stateController.successStateAUD()
            }
        }


        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        private fun modalAddNewProduct() {
        var ids by remember { mutableStateOf<List<Int>>(emptyList()) }
        if (products.value.isEmpty()) {
            readProducts()
        }
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
                                    addProduct(productName,productDescription,{
                                        productName = ""
                                        productDescription = ""
                                        products.value += it
                                    })

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
                            IconDelete(ids){
                                if (!CustomSingleton.isSharedStore())
                                deleteProducts(ids, {

                                    products.value = products.value.filterNot { it.id in ids }
                                    isShowAddCatgory.value = false

                                     productViews.value.forEach { view ->

                                         view.products.forEach {storeProduct->
                                             if (storeProduct.product .productId in ids){
//                                                 productViews.value -= storeProduct
                                             }
                                         }

                                    }
                                    ids = emptyList()
                                })
                            }
                        }
                    }

                    itemsIndexed(products.value){index,product->
                        Card(Modifier.padding(8.dp)) {
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Checkbox(checked = ids.find { it == product.id } != null, onCheckedChange = {
                                    val itemC = ids.find { it == product.id}
                                    if (itemC == null) {
                                        ids = ids + product.id
                                    }else{
                                        ids = ids - product.id
                                    }
                                })
                                Text(product.name)
                                Button(
                                    enabled = !productViews.value.any {  it.products.any { it.product.productId == product.id  } } && product.acceptedStatus != 0,
                                    onClick = {
                                        selectedProduct = product
                                        isShowChooseOptionAndPrice.value = true
//                                        addProduct(product.id.toString())
                                    }) { Text(if (product.acceptedStatus == 0) "بانتظار الموافقة" else if (!productViews.value.any { it.products.any { it.product.productId == product.id  }  }) "اضافة" else "تمت الاضافة") }

                            }
                        }
                    }

                }
            }
        }
        }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalChooseOptionAndPrice() {
        if (options.value.isEmpty()) {
            readOptions()
        }
        var option by remember { mutableStateOf<Option?>(null) }
        ModalBottomSheet(
            onDismissRequest = { isShowChooseOptionAndPrice.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ) {
                var currency by remember { mutableStateOf<Currency?>(null) }
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    item {
                        var price by remember { mutableStateOf("") }
                        Card(Modifier.padding(8.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    OutlinedTextField(
                                        modifier = Modifier.padding(8.dp),
                                        value = price,
                                        label = {
                                            Text("السعر")
                                        },
                                        onValueChange = {
                                            price = it
                                        }
                                    )

                                        var expanded by remember { mutableStateOf(false) }
                                        Card(Modifier.padding(8.dp)) {
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                                    .clickable {
                                                        if (currencies.value.isEmpty()) {
                                                            readCurrencies()
                                                        }
                                                        expanded = !expanded
                                                    },
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(currency?.name ?: "اختر العملة")
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                            }
                                            if (expanded)
                                                currencies.value.forEach { item ->
                                                    DropdownMenuItem(onClick = {
                                                        currency = item
                                                        expanded = false // Close the dropdown after selection
                                                    }, text = {
                                                        Text(item.name)
                                                    })
                                                }

                                        }


                                    OutlinedTextField(
                                        modifier = Modifier.padding(8.dp),
                                        value = if (option != null) option!!.name else "لم يتم اختيار نوع بعد",
                                        enabled = false,
                                        label = {
                                            Text("الخيار")
                                        },
                                        onValueChange = {
//                                            price = it
                                        }
                                    )

                                    IconButton(onClick =
                                    {
                                        if (option != null && currency != null)
                                            addProductOption(
                                                price,
                                                option!!.id,
                                                currency!!.id,
                                                true
                                            )

                                    })
                                    {
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

                    itemsIndexed(options.value) { index, ops ->
                        Card(Modifier.padding(8.dp)) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(ops.name)
                                Button(
                                    onClick = {
                                        option = ops
//                                        addProduct(product.id.toString())
                                    }) { Text("اختيار") }

                            }
                        }
                    }

                }
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalChooseCurrencies() {
        if (currencies.value.isEmpty()) {
            readCurrencies()
        }
        ModalBottomSheet(
            onDismissRequest = { isShowChooseCurrencies = false }) {
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
                    itemsIndexed(currencies.value) { index, ops ->
                        Card(Modifier.padding(8.dp)) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(ops.name)
                                Button(
                                    onClick = {
                                        updateCurrency(ops.id.toString())
//                                        addProduct(product.id.toString())
                                    }) { Text("اختيار") }

                            }
                        }
                    }

                }
            }
        }
    }
    fun addProduct(name: String, description: String, onSuccess: (data: Product2) -> Unit) {
        stateController.startAud()
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("name", name)
            .addFormDataPart("nestedSectionId", storeNestedSection.nestedSectionId.toString())
            .addFormDataPart("description", description)
            .addFormDataPart("storeId", CustomSingleton.getCustomStoreId().toString())
            .build()

        requestServer.request2(body, "addProduct", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val result: Product2 =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )
            onSuccess(result)
            stateController.successStateAUD()
        }
    }
}
@Serializable
data class StringResult(val result:String)


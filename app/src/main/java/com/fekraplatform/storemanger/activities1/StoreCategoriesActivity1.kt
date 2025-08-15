package com.fekraplatform.storemanger.activities1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.models.Category
import com.fekraplatform.storemanger.models.Home
import com.fekraplatform.storemanger.models.StoreCategory
import com.fekraplatform.storemanger.models.StoreConfig
import com.fekraplatform.storemanger.models.Subscription
import com.fekraplatform.storemanger.repositories.BillingRepository
import com.fekraplatform.storemanger.shared.AToken1
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.IconDelete
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyHeader
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.ServerConfig
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.storage.HomeStorage
import com.fekraplatform.storemanger.storage.MyAppStorage1
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import okhttp3.MultipartBody
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject


fun getCurrentDate(): LocalDateTime {
    return LocalDateTime.now()
}
@HiltViewModel
class StoreCategoriesViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    val appSession: AppSession,
    private val builder: FormBuilder
): ViewModel()
{
    val categories = mutableStateOf<List<Category>>(listOf())
    val stateController = StateController()
    val isShowAddCatgory = mutableStateOf(false)
    var selectMode by mutableStateOf(false)
    fun deleteCategories(ids: List<Int>, onDone: () -> Unit) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                val data = requestServer.request(body, "deleteCategories")
                val result: Subscription = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                onDone()
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

    fun deleteStoreCategories(ids: List<Int>, onDone: () -> Unit) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("ids", ids.toString())
                val data = requestServer.request(body, "deleteStoreCategories")
                onDone()
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

    fun add(categoryId: String) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("categoryId", categoryId)


                val data = requestServer.request(body, "addStoreCategory")
                val result: StoreCategory = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                appSession.home.value!!.storeCategories += result
//                homeStorage.setHome(
//                    MyJson.IgnoreUnknownKeys.encodeToString(SingletonHome.home.value!!),
//                    CustomSingleton.getCustomStoreId().toString()
//                )
                isShowAddCatgory.value = false
                stateController.successStateAUD("تمت الاضافه بنجاح")
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

    fun readCategories() {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderForm().addFormDataPart("storeId", appSession.getCustomStoreId().toString())

                val data = requestServer.request(body, "getCategories")
                categories.value = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

    fun addCategory(name: String, onSuccess: (data: Category) -> Unit) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("name", name)


                val data = requestServer.request(body, "addCategory")
                val result: Category = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                onSuccess(result)
//                homeStorage.setHome(
//                    MyJson.IgnoreUnknownKeys.encodeToString(result),
//                    CustomSingleton.selectedStore!!.id.toString()
//                )
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

     fun read(storeId: String) {
        viewModelScope.launch {
            stateController.startRead()
            try {
                val body = builder.sharedBuilderForm().addFormDataPart("storeId",storeId)
                val data = requestServer.request(body, "getHome")
                val result: Home = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                appSession.home.value = result

                if (appSession.isSharedStore()){
                    appSession.categories = appSession.selectedStore.storeConfig!!.categories
                    appSession.sections = appSession.selectedStore.storeConfig!!.sections
                    appSession.nestedSection = appSession.selectedStore.storeConfig!!.nestedSections
                    appSession.products = appSession.selectedStore.storeConfig!!.products
                }

                stateController.successState()
            } catch (e: Exception) {
                stateController.errorStateRead(e.message.toString())
            }
        }
    }
    init {
        read(appSession.getCustomStoreId().toString())
//        SingletonHome.initHome(CustomSingleton.getCustomStoreId().toString())

    }
}


@AndroidEntryPoint
class StoreCategoriesActivity1 : ComponentActivity() {
    val viewModel:StoreCategoriesViewModel by viewModels()




    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        }

        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {

//                SingletonHome.categories.value = SingletonStoreConfig.categories.value
//                SingletonHome.sections.value = SingletonStoreConfig.sections.value
//                SingletonHome.nestedSection.value = SingletonStoreConfig.nestedSection.value
//                SingletonHome.products.value = SingletonStoreConfig.products.value

                    MainCompose1 (
                        0.dp,  viewModel.stateController, this,
                        {
                            viewModel.read(viewModel.appSession.getCustomStoreId().toString())
                        }
                    ) {

                        var ids by remember { mutableStateOf<List<Int>>(emptyList()) }

                        LazyColumn(Modifier.safeDrawingPadding()) {
                            stickyHeader {
                                MyHeader({
                                    if (!viewModel.selectMode){
                                        finish()
                                    }else{
                                        viewModel.selectMode = false
                                    }

                                },{
                                    if (!viewModel.appSession.isSharedStore()){
                                        IconDelete(ids) {
                                            if (!viewModel.appSession.isSharedStore())
                                               viewModel. deleteStoreCategories(ids){
                                                    ids = emptyList()
//                                    nestedSections.value.forEach {
//                                        if (it.id in ids){
//                                            nestedSections.value -= it
//                                        }
//                                    }
                                                }
                                        }
                                        CustomIcon(Icons.Default.Add,true) {
                                            viewModel. isShowAddCatgory.value = true
                                        }
                                    }

                                }) {
                                    Text("الفئات الرئيسية")
                                }
//                                Card(Modifier.fillMaxWidth().height(100.dp).clickable {
//
//                                }) {
//                                    CustomImageView(
//                                        modifier = Modifier.fillMaxWidth()
//                                            .height(80.dp)
//                                            .padding(8.dp)
//                                            .clickable {
//
//                                            },
//                                        context = this@StoreCategoriesActivity,
//                                        imageUrl = requestServer.serverConfig.getRemoteConfig().BASE_IMAGE_URL+requestServer.serverConfig.getRemoteConfig().SUB_FOLDER_STORE_COVERS+CustomSingleton.selectedStore!!.cover,
//                                        okHttpClient = requestServer.createOkHttpClientWithCustomCert()
//                                    )
//                                }
                            }

//                            if ()

//                            item {
//                             Button(onClick = {
//                                 isShowAddCatgory.value = true
//                             }) {
//                                 Text("add")
//                             }
//                            }

//                            item {
//                                Button(onClick = {
//                                    isShowAddCatgory.value = true
//                                }) {
//                                    Text("add")
//                                }
//                            }
//                            item {
//                                LazyRow(Modifier.fillMaxWidth().height(100.dp).padding(8.dp)) {

                            val cats = if (viewModel.appSession.isSharedStore()){
                                if (viewModel.appSession.isEditMode) viewModel.appSession.home.value!!.storeCategories else viewModel.appSession.home.value!!.storeCategories.filterNot { it.id in viewModel.appSession.selectedStore.storeConfig!!.categories }
                            }
                            else viewModel.appSession.home.value!!.storeCategories
                                    itemsIndexed(cats ){index, category ->

                                        Card(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                                .padding(8.dp)
                                        ) {
                                            Box (
                                                Modifier
                                                    .fillMaxSize()
                                                    .combinedClickable (
                                                        onClick = {
                                                            if (! viewModel.appSession.categories.any { it == category.id }) goToSections(category)

                                                        },
                                                        onLongClick = {
                                                            viewModel.selectMode = ! viewModel.selectMode
                                                        }
                                                    )

                                            ){
                                                if (!viewModel.appSession.isSharedStore() &&  viewModel.selectMode)
                                                Checkbox(  modifier = Modifier.align(Alignment.CenterStart),checked = ids.find { it == category.id } != null, onCheckedChange = {
                                                    val itemC = ids.find { it == category.id}
                                                    if (itemC == null) {
                                                        ids = ids + category.id
                                                    }else{
                                                        ids = ids - category.id
                                                    }
                                                })

                                                Text(category.categoryName,Modifier.align(Alignment.Center))
                                                if (viewModel.appSession.isEditMode) {
//                                                    Log.e("ConfigStore",SingletonStoreConfig.toString())
//                                                    Log.e("caaaatts",SingletonHome.categories.value.toString())
                                                    // When category exists in the selected categories in SingletonStoreConfig
                                                    if (viewModel.appSession.selectedStore.storeConfig!!.categories.any { it == category.id }) {

                                                        // If category is not already added, display "تمت الاضافة بانتظار التأكيد"
                                                        if (category.id !in viewModel.appSession.categories) {
                                                            Text(
                                                                "تمت الاضافة بانتظار التأكيد",
                                                                Modifier
                                                                    .align(Alignment.BottomEnd)
                                                                    .clickable {
                                                                        viewModel.appSession.categories += category.id
                                                                    }
                                                            )
                                                        } else { // If category is already in the list, display "اضافة"
                                                            Text(
                                                                "اضافة",
                                                                Modifier
                                                                    .align(Alignment.BottomEnd)
                                                                    .clickable {
                                                                        viewModel.appSession.categories -= category.id
                                                                    }
                                                            )
                                                        }
                                                    } else { // If category is not in SingletonStoreConfig categories
                                                        // If the category is already in SingletonHome.categories.value, display "تمت الحذف بانتظار التأكيد"
                                                        if (category.id in viewModel.appSession.categories) {
                                                            Text(
                                                                "تمت الحذف بانتظار التأكيد",
                                                                Modifier
                                                                    .align(Alignment.BottomEnd)
                                                                    .clickable {
                                                                        viewModel.appSession.categories -= category.id
                                                                    }
                                                            )
                                                        } else { // If category is not in SingletonHome.categories.value, display "حذف"
                                                            Text(
                                                                "حذف",
                                                                Modifier
                                                                    .align(Alignment.BottomEnd)
                                                                    .clickable {
                                                                        viewModel.appSession.categories += category.id
                                                                    }
                                                            )
                                                        }
                                                    }
                                                }


//                                                if (SingletonHome.isEditMode.value){
//                                                    if (SingletonStoreConfig.categories.value.any { number -> number == category.id }){
//                                                        if (!SingletonHome.categories.value.any { it == category.id }) {
//                                                            Text(
//                                                                "تمت الاضافة بانتظار التأكيد",
//                                                                Modifier
//                                                                    .align(Alignment.BottomEnd)
//                                                                    .clickable {
//                                                                        SingletonHome.categories.value +=category.id
//                                                                    })
//                                                        }
//                                                        else{
//                                                            Text(
//                                                                "اضافة",
//                                                                Modifier
//                                                                    .align(Alignment.BottomEnd)
//                                                                    .clickable {
////                                                                        Log.e("rtrt", SingletonHome. categories.toString())
//                                                                        SingletonHome.categories.value -= category.id
////                                                                        Log.e("rtrt", SingletonHome. categories.toString())
//
//                                                                    })
//                                                        }
//                                                    }
//                                                    else{
//                                                        if ( SingletonHome.categories.value.any { it == category.id }){
//                                                            Text("تمت الحذف بانتظار التأكيد",
//                                                                Modifier
//                                                                    .align(Alignment.BottomEnd)
//                                                                    .clickable {
//                                                                        SingletonHome.categories.value -= category.id
//                                                                    })
//                                                        }else{
//                                                            Text("حذف",
//                                                                Modifier
//                                                                    .align(Alignment.BottomEnd)
//                                                                    .clickable {
//                                                                        SingletonHome.categories.value+=category.id
//                                                                    })
//                                                        }
//
//                                                    }
//                                                }

                                                }

                                            }
                                        }
//                            if (SelectedStore.store.value!!.typeId != 1)
//                            item {
//                                Card(
//                                    Modifier
//                                        .fillMaxWidth()
//                                        .height(100.dp)
//                                        .padding(8.dp)
//                                ) {
//                                    Box (
//                                        Modifier
//                                            .fillMaxSize()
//                                            .clickable {
//                                                isShowAddCatgory.value = true
//                                            }
//                                    ){
//                                        Text("+", modifier = Modifier.align(Alignment.Center))
//                                    }
//                                }
//                            }

//                                }
//                            }
//                            item {
//                                LazyRow(Modifier.fillMaxWidth().height(100.dp).padding(8.dp)) {
//                                    itemsIndexed(home.value!!.storeCategoriesSections){index, item ->
//
//                                        Card(
//                                            Modifier.fillMaxWidth().height(100.dp).padding(8.dp)
//                                        ) {
//                                            Box (
//                                                Modifier.fillMaxSize().clickable {
////                                                    goToSections(item)
//                                                }
//                                            ){
//                                                Text(item.sectionName,Modifier.align(Alignment.Center))
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//
//                            item{
//                                LazyRow(Modifier.fillMaxWidth().height(100.dp).padding(8.dp)) {
//                                    itemsIndexed(home.value!!.csps){index, item ->
//
//                                        Card(
//                                            Modifier.fillMaxWidth().height(100.dp).padding(8.dp)
//                                        ) {
//                                            Box (
//                                                Modifier.fillMaxSize().clickable {
////                                                    goToSections(item)
//                                                }
//                                            ){
//                                                Text(item.name,Modifier.align(Alignment.Center))
//                                            }
//                                        }
//                                    }
//                                }
//                            }
                        }
                        if ( viewModel.isShowAddCatgory.value) modalAddMyCategory()

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


    private fun goToSections(storeCategory: StoreCategory) {
    val intent = Intent(
        this,
        StoreSectionsActivity1::class.java
    )
    intent.putExtra("storeCategory", MyJson.MyJson.encodeToString(storeCategory))
    startActivity(intent)
}




    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddMyCategory() {
        if ( viewModel.categories.value.isEmpty()) {
            viewModel.readCategories()
        }
        ModalBottomSheet(
            onDismissRequest = {  viewModel.isShowAddCatgory.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                var ids by remember { mutableStateOf<List<Int>>(emptyList()) }

                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    item {
                        var categoryName by remember { mutableStateOf("") }
                        Card(Modifier.padding(8.dp)){
                            Row (Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                OutlinedTextField(
                                    modifier = Modifier.padding(8.dp),
                                    value = categoryName,
                                    onValueChange = {
                                        categoryName = it
                                    }
                                )
                                IconButton(onClick = {
                                    viewModel.addCategory(categoryName,{
                                        categoryName = ""
                                        viewModel.categories.value += it
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

                            IconDelete(ids) {


                                if (!viewModel.appSession.isSharedStore())
                                    viewModel.deleteCategories(ids){
                                        viewModel.isShowAddCatgory.value = false
                                        viewModel.categories.value.forEach {
                                            if (it.id in ids){
                                                viewModel. categories.value -= it
                                            }
                                        }
                                    }
                            }

                        }
                    }

                    itemsIndexed( viewModel.categories.value){index,category->
                        Card(Modifier.padding(8.dp)) {
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Checkbox(checked = ids.find { it == category.id } != null, onCheckedChange = {
                                    val itemC = ids.find { it == category.id}
                                    if (itemC == null) {
                                        ids = ids + category.id
                                    }else{
                                        ids = ids - category.id
                                    }
                                })
                                Text(category.name)
                                Button(
                                    enabled = !viewModel.appSession.home.value!!.storeCategories.any { it.categoryId == category.id } && category.acceptedStatus != 0,
                                    onClick = {
                                        viewModel.add(category.id.toString())
                                }) { Text(if (category.acceptedStatus == 0) "بانتظار الموافقة" else if (!viewModel.appSession.home.value!!.storeCategories.any { it.categoryId == category.id }) "اضافة" else "تمت الاضافة") }

                            }
                        }
                    }

                }
            }

        }
    }
}
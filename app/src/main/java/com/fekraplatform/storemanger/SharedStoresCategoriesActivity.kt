package com.fekraplatform.storemanger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fekraplatform.storemanger.models.Category
import com.fekraplatform.storemanger.models.Home
import com.fekraplatform.storemanger.models.Store
import com.fekraplatform.storemanger.models.StoreCategory1
import com.fekraplatform.storemanger.models.StoreConfig
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.U1R
import com.fekraplatform.storemanger.storage.HomeStorage
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import kotlinx.serialization.encodeToString
import okhttp3.MultipartBody
import java.time.Duration
import java.time.LocalDateTime


object SingletonStoreConfig{
    lateinit var storeId: String
    lateinit var typeId:String
    lateinit var storeIdReference:String

    fun isSharedStore():Boolean{
        return typeId == "1"
    }
    val categories   =  mutableStateOf<List<Int>>(emptyList())
    val sections   =  mutableStateOf<List<Int>>(emptyList())
    val nestedSection   =  mutableStateOf<List<Int>>(emptyList())
    val products   =  mutableStateOf<List<Int>>(emptyList())

    @Composable
    fun EditModeCompose() {
        if (typeId=="1")
        Card(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(Modifier.fillMaxWidth()) {
                Switch(
                    checked = SingletonHome.isEditMode.value,
                    onCheckedChange = { SingletonHome.isEditMode.value = it },
                    modifier = Modifier.padding(16.dp)
                )
                Text("وضع التعديل")

            }
            if ((SingletonHome.categories.value != categories.value ||
                        SingletonHome.sections.value != sections.value ||
                        SingletonHome.nestedSection.value != nestedSection.value ||
                        SingletonHome.products.value != products.value )

                && SingletonHome.isEditMode.value){
                if (SingletonHome.stateController.isLoadingAUD.value)
                    CircularProgressIndicator()
                else
                    Text("حفظ التعديلات", Modifier.clickable {
                        SingletonHome.updateStoreConfig()
                        Log.e("wwww", SingletonHome.categories.value.toString())
                    })
            }

        }
    }
}
object SingletonHome {

    val categories   =  mutableStateOf<List<Int>>(emptyList())
    val sections   =  mutableStateOf<List<Int>>(emptyList())
    val nestedSection   =  mutableStateOf<List<Int>>(emptyList())
    val products   =  mutableStateOf<List<Int>>(emptyList())
    val isEditMode = mutableStateOf(false)
//    val storeConfig = mutableStateOf(StoreConfig(
//        emptyList(), emptyList(),
//        emptyList(), emptyList()
//    ))

//    val store = mutableStateOf<Store?>(null)

    lateinit var stateController: StateController
    lateinit var requestServer : RequestServer
//
    fun setStateController1(states: StateController){
        stateController=states
    }
    fun setReqestController(request: RequestServer){
        requestServer=request
    }
    val homeStorage = HomeStorage();
    val home = mutableStateOf<Home?>(null)
    fun initHome(storeId: String){
        if (homeStorage.isSetHome(storeId)) {
            Log.e("frf23", home.value.toString())
            val diff =
                Duration.between(homeStorage.getDate(storeId), getCurrentDate()).toMinutes()
            if (diff <= 5) {
                stateController.successState()
                Log.e("frf", homeStorage.getHome(storeId).toString())
                home.value = homeStorage.getHome(storeId)

                return
//                return true
            }
        }
        Log.e("frf2344", home.value.toString())
        read(storeId)
//        return false
    }
    fun read(storeId: String) {
        stateController.startRead()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeId",storeId)
            .build()

        requestServer.request2(body, "getStoreCategories", { code, fail ->
            stateController.errorStateRead(fail)
        }
        ) { data ->
            val result:Home =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )
            home.value = result
            homeStorage.setHome(MyJson.IgnoreUnknownKeys.encodeToString(result),storeId)
            Log.e("dsd", home.value.toString())
            Log.e("dsd2",result.toString())
            stateController.successState()
        }
    }

    fun updateStoreConfig() {
        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeId", SingletonStoreConfig.storeId.toString())
            .addFormDataPart("products", products.value.toString())
            .addFormDataPart("nestedSections", nestedSection.value.toString())
            .addFormDataPart("sections", sections.value.toString())
            .addFormDataPart("categories",categories.value.toString())
            .build()

        requestServer.request2(body, "updateStoreConfig", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val result:StoreConfig =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            SingletonStoreConfig.categories.value = result.categories
             SingletonStoreConfig.sections.value = result.sections
             SingletonStoreConfig.nestedSection.value = result.nestedSections
           SingletonStoreConfig.products.value = result.products

            stateController.successStateAUD("تم بنجاح")
        }
    }
}
fun getCurrentDate(): LocalDateTime {
    return LocalDateTime.now()
}

class SharedStoresCategoriesActivity : ComponentActivity() {
    private val storeCategories = mutableStateOf<List<StoreCategory1>>(listOf())
    private val categories = mutableStateOf<List<Category>>(listOf())
    val stateController = StateController()
    val requestServer = RequestServer(this)
    val isShowAddCatgory = mutableStateOf(false)
    val home = mutableStateOf<Home?>(null)
//    lateinit var store:Store


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SingletonHome.setStateController1(stateController)
        SingletonHome.setReqestController(requestServer)

        val intent = intent
        val str = intent.getStringExtra("store")
        if (str != null) {
            try {
//                store =
                val store:Store = MyJson.IgnoreUnknownKeys.decodeFromString(str)

                SingletonStoreConfig.typeId = store.typeId.toString()
                SingletonStoreConfig.storeId = store.id.toString()
                if (store.storeConfig != null){
                    SingletonStoreConfig.storeIdReference = store.storeConfig!!.storeIdReference.toString()
                    SingletonStoreConfig.categories.value = store.storeConfig!!.categories
                    SingletonStoreConfig.sections.value = store.storeConfig!!.sections
                    SingletonStoreConfig.nestedSection.value = store.storeConfig!!.nestedSections
                    SingletonStoreConfig.products.value = store.storeConfig!!.products
                }


            }catch (e:Exception){
                finish()
            }

        } else {
            finish()
        }



//        SingletonHome.stateController.successState()

        if (SingletonStoreConfig.isSharedStore()){
            SingletonHome.initHome(SingletonStoreConfig.storeIdReference)
        }else{
            SingletonHome.initHome(SingletonStoreConfig.storeId)
        }

//        if (SingletonHome.isSetHome()){
//            stateController.successState()
//        }else{
//
//        }

//        val categories= mutableStateOf<List<Int>>(emptyList())

        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {

                SingletonHome.categories.value = SingletonStoreConfig.categories.value
                SingletonHome.sections.value = SingletonStoreConfig.sections.value
                SingletonHome.nestedSection.value = SingletonStoreConfig.nestedSection.value
                SingletonHome.products.value = SingletonStoreConfig.products.value

                    MainCompose1 (
                        0.dp,SingletonHome.stateController, this,
                        { SingletonHome.read(if (SingletonStoreConfig.isSharedStore()) SingletonStoreConfig.storeIdReference else SingletonStoreConfig.storeId) },
                    ) {
                        LazyColumn {
//                            if ()
                            item {
                                SingletonStoreConfig.EditModeCompose()
                            }

//                            item {
//                                Button(onClick = {
//                                    isShowAddCatgory.value = true
//                                }) {
//                                    Text("add")
//                                }
//                            }
//                            item {
//                                LazyRow(Modifier.fillMaxWidth().height(100.dp).padding(8.dp)) {

                            val cats = if (SingletonStoreConfig.isSharedStore()){
                                if (SingletonHome.isEditMode.value) SingletonHome.home.value!!.storeCategories else SingletonHome.home.value!!.storeCategories.filterNot { it.id in SingletonStoreConfig.categories.value }
                            }
                            else SingletonHome.home.value!!.storeCategories
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
                                                    .clickable {
                                                        if (! SingletonHome. categories.value.any { it == category.id }) goToSections(category)
                                                    }
                                            ){
                                                Text(category.categoryName,Modifier.align(Alignment.Center))
                                                if (SingletonHome.isEditMode.value){
                                                    if (SingletonStoreConfig.categories.value.any { number -> number == category.id }){
                                                        if (! SingletonHome. categories.value.any { it == category.id }) {
                                                            Text(
                                                                "تمت الاضافة بانتظار التأكيد",
                                                                Modifier
                                                                    .align(Alignment.BottomEnd)
                                                                    .clickable {
                                                                        SingletonHome. categories.value +=category.id
                                                                    })
                                                        }
                                                        else{
                                                            Text(
                                                                "اضافة",
                                                                Modifier
                                                                    .align(Alignment.BottomEnd)
                                                                    .clickable {
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())
                                                                        SingletonHome.categories.value -= category.id
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())

                                                                    })
                                                        }
                                                    }
                                                    else{
                                                        if ( SingletonHome. categories.value.any { it == category.id }){
                                                            Text("تمت الحذف بانتظار التأكيد",
                                                                Modifier
                                                                    .align(Alignment.BottomEnd)
                                                                    .clickable {
                                                                        SingletonHome.  categories.value -= category.id
                                                                    })
                                                        }else{
                                                            Text("حذف",
                                                                Modifier
                                                                    .align(Alignment.BottomEnd)
                                                                    .clickable {
                                                                        SingletonHome. categories.value+=category.id
                                                                    })
                                                        }

                                                    }
                                                }
                                                }
                                            }
                                        }

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



    private fun goToSections(storeCategory1: StoreCategory1) {
    val intent = Intent(
        this,
        SharedStoresSectionsActivity::class.java
    )
    intent.putExtra("storeCategory1", MyJson.MyJson.encodeToString(storeCategory1))
    startActivity(intent)
}
fun read() {
    SingletonHome.stateController.startRead()
    val body = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("storeId", SingletonStoreConfig.storeId)
        .build()

    requestServer.request2(body, "getStoreCategories", { code, fail ->
        SingletonHome.stateController.errorStateRead(fail)
    }
    ) { data ->
        val result:Home =
            MyJson.IgnoreUnknownKeys.decodeFromString(
                data
            )

       SingletonHome.home.value = result
//        SingletonHome.homeStorage.setHome(MyJson.IgnoreUnknownKeys.encodeToString(result))
        SingletonHome.stateController.successState()
    }
}
fun readCategories() {
    SingletonHome.stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeId", "1")
            .build()

        requestServer.request(body, "${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/getCategories", { code, fail ->
            SingletonHome.stateController.errorStateAUD(fail)
        }
        ) { data ->
            categories.value =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

          SingletonHome.  stateController.successStateAUD()
        }
    }
private fun add(storeId: String,categoryId:String) {

        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeId",storeId)
            .addFormDataPart("categoryId",categoryId)
            .build()

        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/addStoreCategory",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result: StoreCategory1 =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )

            storeCategories.value += result
            isShowAddCatgory.value = false
            stateController.successStateAUD("تمت الاضافه  بنجاح")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddMyCategory() {
        var category by remember { mutableStateOf<Category?>(null) }
        ModalBottomSheet(
            onDismissRequest = { isShowAddCatgory.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        var expanded by remember { mutableStateOf(false) }
                        Card(Modifier.padding(8.dp)) {
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        if (categories.value.isEmpty()) {
                                            readCategories()
                                        }
                                        expanded = !expanded
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Text(category?.name ?: "اختر فئة")
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                            if (expanded)
                                categories.value.filterNot { categoryItem ->
                                    storeCategories.value.any { storeCategory ->
                                        storeCategory.categoryId == categoryItem.id // Compare by the 'name' field
                                    }
                                }.forEach { item ->
                                    DropdownMenuItem(onClick = {
                                        category = item
                                        expanded = false // Close the dropdown after selection
                                    }, text = {
                                        Text(item.name)
                                    })
                                }

                        }
                        if (category != null )
                            Button(onClick = {
                                add("1",category!!.id.toString())
                            }) {
                                Text("حفظ")
                            }
                    }
                }
            }
        }
    }
}
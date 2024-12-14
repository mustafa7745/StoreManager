package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.models.Store
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.U1R
import com.fekraplatform.storemanger.shared.builderForm3
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import kotlinx.serialization.encodeToString
import okhttp3.MultipartBody



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
                                        Text(item.name,Modifier.align(Alignment.Center))
                                    }
                                }
                            }
                        }
//                        if (isShowAddCatgory.value) modalAddMyCategory()

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
fun readCategories() {
        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeId", "1")
            .build()

        requestServer.request(body, "${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/getCategories", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            stores.value =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            stateController.successStateAUD()
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
}
package com.fekraplatform.storemanger

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fekraplatform.storemanger.activities.ProductsActivity
import com.fekraplatform.storemanger.models.Category3
import com.fekraplatform.storemanger.models.CsPsSCR
import com.fekraplatform.storemanger.models.Scp
import com.fekraplatform.storemanger.models.StoreCategorySection
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.U1R
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import kotlinx.serialization.encodeToString
import okhttp3.MultipartBody

class SharedStoresNestedSectionsActivity : ComponentActivity() {
    private val CsPsSCRs = mutableStateOf<List<CsPsSCR>>(listOf())
    private val categories3 = mutableStateOf<List<Category3>>(listOf())
    val stateController = StateController()
    val requestServer = RequestServer(this)
    val isShowAddSection = mutableStateOf(false)

    lateinit var sectionStoreCategory: StoreCategorySection
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val str = intent.getStringExtra("sectionStoreCategory")
        if (str != null) {
            try {
                sectionStoreCategory = MyJson.IgnoreUnknownKeys.decodeFromString(str)
            }catch (e:Exception){
                finish()
            }

        } else {
            finish()
        }
        stateController.successState()
//        read()
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {

                    MainCompose1 (
                        0.dp, stateController, this,
                        { read() },
                    ) {
                        LazyColumn {
                            item {
                                SingletonStoreConfig.EditModeCompose()
                            }
                            item {
                                Button(onClick = {
                                    isShowAddSection.value = true
                                }) {
                                    Text("add")
                                }
                            }

                            val nestedCats = if (SingletonStoreConfig.isSharedStore()){
                                if (SingletonHome.isEditMode.value) SingletonHome.home.value!!.csps.filter { it.storeCategorySectionId == sectionStoreCategory.id } else SingletonHome.home.value!!.csps.filter { it.storeCategorySectionId == sectionStoreCategory.id }.filterNot { it.id in SingletonStoreConfig.nestedSection.value }                            }
                            else SingletonHome.home.value!!.csps.filter { it.storeCategorySectionId == sectionStoreCategory.id }

                            itemsIndexed(nestedCats){index, nestedSection ->

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
                                                if (! SingletonHome. nestedSection.value.any { it == nestedSection.id })    goToProduct(nestedSection)
                                            }
                                    ){
                                        Text(nestedSection.name,Modifier.align(Alignment.Center))
                                        if (SingletonHome.isEditMode.value){
                                            if (SingletonStoreConfig.nestedSection.value.any { number -> number == nestedSection.id }){
                                                if (! SingletonHome. nestedSection.value.any { it == nestedSection.id }) {
                                                    Text(
                                                        "تمت الاضافة بانتظار التأكيد",
                                                        Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .clickable {
                                                                SingletonHome. nestedSection.value +=nestedSection.id
                                                            })
                                                }
                                                else{
                                                    Text(
                                                        "اضافة",
                                                        Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .clickable {
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())
                                                                SingletonHome.nestedSection.value -= nestedSection.id
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())

                                                            })
                                                }
                                            }
                                            else{
                                                if ( SingletonHome. nestedSection.value.any { it == nestedSection.id }){
                                                    Text("تمت الحذف بانتظار التأكيد",
                                                        Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .clickable {
                                                                SingletonHome.nestedSection.value -= nestedSection.id
                                                            })
                                                }else{
                                                    Text("حذف",
                                                        Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .clickable {
                                                                SingletonHome. nestedSection.value+=nestedSection.id
                                                            })
                                                }

                                            }
                                        }
                                    }
                                }
                            }

                        }
                        if (isShowAddSection.value) modalAddMyCategory()

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

    private fun goToProduct(scr: Scp) {
        val intent = Intent(
            this,
            ProductsActivity::class.java
        )
        intent.putExtra("scr", MyJson.MyJson.encodeToString(scr))
        startActivity(intent)
    }
fun read() {
    stateController.startRead()

    val body = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("storeId", "1")
        .addFormDataPart("sectionsStoreCategoryId", sectionStoreCategory.id.toString())
        .build()

    requestServer.request(body, "${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/getCsPsSCR", { code, fail ->
        stateController.errorStateRead(fail)
    }
    ) { data ->
        CsPsSCRs.value =
            MyJson.IgnoreUnknownKeys.decodeFromString(
                data
            )

        stateController.successState()
    }
}
private fun add(category3Id: String) {

        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("category3Id",category3Id.toString())
            .addFormDataPart("sectionsStoreCategoryId",sectionStoreCategory.id.toString())
            .build()

        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/addCsPsSCR",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result: CsPsSCR =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )

            CsPsSCRs.value += result
            isShowAddSection.value = false
            stateController.successStateAUD("تمت الاضافه  بنجاح")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddMyCategory() {
        var value by remember { mutableStateOf("") }
        var category3 by remember { mutableStateOf<Category3?>(null) }
        ModalBottomSheet(
            onDismissRequest = { isShowAddSection.value = false }) {
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
                            Row (Modifier.fillMaxWidth().padding(8.dp).clickable {
                                if (categories3.value.isEmpty()){
                                    readCategories3()
                                }
                                expanded = !expanded
                            },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Text(category3?.name ?: "اختر قسم داخلي")
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                            if (expanded)
                                categories3.value.filterNot { sectionItem ->
                                    CsPsSCRs.value.any { storeCategory ->
                                        storeCategory.category3Id == sectionItem.id // Compare by the 'name' field
                                    }
                                }.forEach { item ->
                                    DropdownMenuItem(onClick = {
                                        category3 = item
                                        expanded = false // Close the dropdown after selection
                                    }, text = {
                                        Text(item.name)
                                    })
                                }

                        }

                        if (category3 != null)
                            Button(onClick = {
                                add(category3!!.id.toString())
                            },
                                Modifier.fillMaxWidth()) {
                                Text("حفظ")
                            }
                    }
                }
            }
        }
    }

    fun readCategories3() {
        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("sectionId", sectionStoreCategory.sectionId.toString())
            .addFormDataPart("storeId", "1")
            .build()

        requestServer.request(body, "${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/getCategories3", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            categories3.value =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            stateController.successStateAUD()
        }
    }
}
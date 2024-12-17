package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
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
import com.fekraplatform.storemanger.activities.SingletonHome.homeStorage
import com.fekraplatform.storemanger.models.Category
import com.fekraplatform.storemanger.models.Section
import com.fekraplatform.storemanger.models.StoreCategory
import com.fekraplatform.storemanger.models.StoreSection
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.U1R
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import kotlinx.serialization.encodeToString
import okhttp3.MultipartBody

class StoreSectionsActivity : ComponentActivity() {
    private val storeSections = mutableStateOf<List<StoreSection>>(listOf())
    private val sections = mutableStateOf<List<Section>>(listOf())
    val stateController = StateController()
    val requestServer = RequestServer(this)
    val isShowAddSection = mutableStateOf(false)

    lateinit var storeCategory: StoreCategory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val str = intent.getStringExtra("storeCategory")
        if (str != null) {
            try {
                storeCategory = MyJson.IgnoreUnknownKeys.decodeFromString(str)
            }catch (e:Exception){
                finish()
            }

        } else {
            finish()
        }

        stateController.successState()
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {

                    MainCompose1 (
                        0.dp, stateController, this,
                        {

                        },
                    ) {
                        LazyColumn {

                            item {
                                SingletonStoreConfig.EditModeCompose()
                            }



//                            val sec = if (SingletonStoreConfig.isSharedStore()){
//                                if (SingletonHome.isEditMode.value) SingletonHome.home.value!!.storeSections.filter { it.storeCategoryId == storeCategory.id } else SingletonHome.home.value!!.storeSections.filter { it.storeCategoryId == storeCategory.id }.filterNot { it.id in SingletonStoreConfig.sections.value }                            }
//                            else SingletonHome.home.value!!.storeSections.filter { it.storeCategoryId == storeCategory.id }

                            val sectionList = SingletonHome.home.value!!.storeSections
                                .filter { it.storeCategoryId == storeCategory.id }
                                .let { storeSectionList ->
                                    if (SingletonStoreConfig.isSharedStore()) {
                                        if (SingletonHome.isEditMode.value) {
                                            storeSectionList
                                        } else {
                                            storeSectionList.filterNot { it.id in SingletonStoreConfig.sections.value }
                                        }
                                    } else {
                                        storeSectionList
                                    }
                                }

                            itemsIndexed(sectionList){ index, section ->

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
                                                if (! SingletonHome.sections.value.any { it == section.id })    goToSections(section)
                                            }
                                    ){
                                        Text(section.sectionName,Modifier.align(Alignment.Center))
                                        if (SingletonHome.isEditMode.value){
                                            if (SingletonStoreConfig.sections.value.any { number -> number == section.id }){
                                                if (! SingletonHome.sections.value.any { it == section.id }) {
                                                    Text(
                                                        "تمت الاضافة بانتظار التأكيد",
                                                        Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .clickable {
                                                                SingletonHome.sections.value +=section.id
                                                            })
                                                }
                                                else{
                                                    Text(
                                                        "اضافة",
                                                        Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .clickable {
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())
                                                                SingletonHome.sections.value -= section.id
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())

                                                            })
                                                }
                                            }
                                            else{
                                                if ( SingletonHome.sections.value.any { it == section.id }){
                                                    Text("تمت الحذف بانتظار التأكيد",
                                                        Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .clickable {
                                                                SingletonHome.sections.value -= section.id
                                                            })
                                                }else{
                                                    Text("حذف",
                                                        Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .clickable {
                                                                SingletonHome.sections.value+=section.id
                                                            })
                                                }

                                            }
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
                                                isShowAddSection.value = true
                                            }
                                    ){
                                        Text("+", modifier = Modifier.align(Alignment.Center))
                                    }
                                }
                            }

                        }
                        if (isShowAddSection.value) modalAddMyCategory()

                    }
                }
//               LazyColumn {
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
private fun goToSections(storeSection: StoreSection) {
    val intent = Intent(
        this,
        StoreNestedSectionsActivity::class.java
    )
    intent.putExtra("storeSection", MyJson.MyJson.encodeToString(storeSection))
    startActivity(intent)
}

fun readSections() {
        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("categoryId", storeCategory.categoryId.toString())
            .addFormDataPart("storeId", SingletonStoreConfig.storeId)
            .build()

        requestServer.request2(body, "getSections", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            sections.value =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            stateController.successStateAUD()
        }
    }
private fun add(sectionId: String) {

        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("sectionId",sectionId)
            .addFormDataPart("storeCategoryId",storeCategory.id.toString())
            .build()

        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/addStoreSection",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result: StoreSection =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )

            storeSections.value += result
            homeStorage.setHome(MyJson.IgnoreUnknownKeys.encodeToString(SingletonHome.home.value!!),SingletonStoreConfig.storeId)
            SingletonHome.home.value!!.storeSections += result
            isShowAddSection.value = false
            stateController.successStateAUD("تمت الاضافه  بنجاح")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddMyCategory() {
        if (sections.value.isEmpty()) {
            readSections()
        }
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
                        var sectionName by remember { mutableStateOf("") }
                        Card(Modifier.padding(8.dp)){
                            Row (Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                OutlinedTextField(
                                    modifier = Modifier.padding(8.dp),
                                    value = sectionName,
                                    onValueChange = {
                                        sectionName = it
                                    }
                                )
                                IconButton(onClick = {
                                    addSection(sectionName,{
                                        sectionName = ""
                                        sections.value += it
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
                        }
                    }

                    itemsIndexed(sections.value){index,section->
                        Card(Modifier.padding(8.dp)) {
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Text(section.name)
                                Button(
                                    enabled = !SingletonHome.home.value!!.storeSections.any { it.sectionId == section.id } && section.acceptedStatus != 0,
                                    onClick = {
                                        add(section.id.toString())
                                    }) { Text(if (section.acceptedStatus == 0) "بانتظار الموافقة" else if (!SingletonHome.home.value!!.storeSections.any { it.sectionId == section.id }) "اضافة" else "تمت الاضافة") }

                            }
                        }
                    }

//                    item {
//                        var expanded by remember { mutableStateOf(false) }
//                        Card(Modifier.padding(8.dp)) {
//                            Row (Modifier.fillMaxWidth().padding(8.dp).clickable {
//                                if (sections.value.isEmpty()){
//                                    readSections()
//                                }
//                                expanded = !expanded
//                            },
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ){
//                                Text(section?.name ?: "اختر قسم")
//                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
//                            }
//                            if (expanded)
//                                sections.value.filterNot { sectionItem ->
//                                    storeSections.value.any { storeCategory ->
//                                        storeCategory.sectionId == sectionItem.id // Compare by the 'name' field
//                                    }
//                                }.forEach { item ->
//                                    DropdownMenuItem(onClick = {
//                                        section = item
//                                        expanded = false // Close the dropdown after selection
//                                    }, text = {
//                                        Text(item.name)
//                                    })
//                                }
//
//                        }
//                        if (section != null )
//                            Button(onClick = {
//                                add(section!!.id.toString())
//                            },
//                                Modifier.fillMaxWidth()) {
//                                Text("حفظ")
//                            }
//                    }
//
//
                }
            }
        }
    }

    fun addSection(name:String, onSuccess: (data: Section) -> Unit) {
        stateController.startAud()
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("name",name)
            .addFormDataPart("storeId", SingletonStoreConfig.storeId)
            .addFormDataPart("categoryId", storeCategory.categoryId.toString())
            .build()

        requestServer.request2(body, "addSection", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val result: Section = MyJson.IgnoreUnknownKeys.decodeFromString(data)
            homeStorage.setHome(MyJson.IgnoreUnknownKeys.encodeToString(result),SingletonStoreConfig.storeId)
            onSuccess(result)
            stateController.successStateAUD()
        }
    }
}
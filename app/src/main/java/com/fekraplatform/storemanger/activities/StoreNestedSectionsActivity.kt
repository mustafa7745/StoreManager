package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.fekraplatform.storemanger.activities.SingletonHome.homeStorage
import com.fekraplatform.storemanger.models.NestedSection
import com.fekraplatform.storemanger.models.Section
import com.fekraplatform.storemanger.models.StoreNestedSection
import com.fekraplatform.storemanger.models.StoreSection
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.IconDelete
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyHeader
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.U1R
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import kotlinx.serialization.encodeToString
import okhttp3.MultipartBody

class StoreNestedSectionsActivity : ComponentActivity() {
//    private val storeNestedSections = mutableStateOf<List<StoreNestedSection>>(listOf())
    private val nestedSections = mutableStateOf<List<NestedSection>>(listOf())
    val stateController = StateController()
    val requestServer = RequestServer(this)
    val isShowAddSection = mutableStateOf(false)
    var selectMode by mutableStateOf(false)

    lateinit var storeSection: StoreSection
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val str = intent.getStringExtra("storeSection")
        if (str != null) {
            try {
                storeSection = MyJson.IgnoreUnknownKeys.decodeFromString(str)
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
                        {  },
                    ) {
                        var ids by remember { mutableStateOf<List<Int>>(emptyList()) }



                        LazyColumn (Modifier.safeDrawingPadding()){
                            stickyHeader {
                                MyHeader({
                                    finish()
                                },{
                                    if (!CustomSingleton.isSharedStore()){
                                        IconDelete(ids) {
                                            deleteStoreNestedSections(ids){
                                                SingletonHome.home.value!!.storeNestedSections.forEach {
                                                    if (it.id in ids)
                                                        SingletonHome.home.value!!.storeNestedSections -= it
                                                }
                                            }
                                        }
                                        CustomIcon(Icons.Default.Add,true) {
                                            isShowAddSection.value = true
                                        }
                                    }

                                }) {
                                    Text("الاقسام الداخلية")
                                }
                            }
                            item {
                                SingletonStoreConfig.EditModeCompose()
                            }



                            itemsIndexed(if (CustomSingleton.isSharedStore()){
                                if (SingletonHome.isEditMode.value) SingletonHome.home.value!!.storeNestedSections.filter { it.storeSectionId == storeSection.id } else SingletonHome.home.value!!.storeNestedSections .filter { it.storeSectionId == storeSection.id }.filterNot { it.id in CustomSingleton.selectedStore!!.storeConfig!!.nestedSections }                            }
                            else SingletonHome.home.value!!.storeNestedSections.filter { it.storeSectionId == storeSection.id }){index, storeNestedSection ->

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
                                                    if (! SingletonHome.nestedSection.value.any { it == storeNestedSection.id })    goToProduct(storeNestedSection)

                                                },
                                                onLongClick = {
                                                    selectMode = !selectMode
                                                }
                                                )

                                    ){
                                        Text(storeNestedSection.nestedSectionName,Modifier.align(Alignment.Center))
                                        if (!CustomSingleton.isSharedStore() && selectMode)
                                        Checkbox(
                                            modifier = Modifier.align(Alignment.CenterStart),
                                            checked = ids.find { it == storeNestedSection.id } != null, onCheckedChange = {
                                                val itemC = ids.find { it == storeNestedSection.id}
                                                if (itemC == null) {
                                                    ids = ids + storeNestedSection.id
                                                }else{
                                                    ids = ids - storeNestedSection.id
                                                }
                                            })
                                        Column {

                                            if (SingletonHome.isEditMode.value){
                                            if (CustomSingleton.selectedStore!!.storeConfig!!.nestedSections.any { number -> number == storeNestedSection.id }){
                                                if (! SingletonHome.nestedSection.value.any { it == storeNestedSection.id }) {
                                                    Text(
                                                        "تمت الاضافة بانتظار التأكيد",
                                                        Modifier
                                                            .clickable {
                                                                SingletonHome.nestedSection.value +=storeNestedSection.id
                                                            })
                                                }
                                                else{
                                                    Text(
                                                        "اضافة",
                                                        Modifier

                                                            .clickable {
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())
                                                                SingletonHome.nestedSection.value -= storeNestedSection.id
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())

                                                            })
                                                }
                                            }
                                            else{
                                                if ( SingletonHome.nestedSection.value.any { it == storeNestedSection.id }){
                                                    Text("تمت الحذف بانتظار التأكيد",
                                                        Modifier

                                                            .clickable {
                                                                SingletonHome.nestedSection.value -= storeNestedSection.id
                                                            })
                                                }else{
                                                    Text("حذف",
                                                        Modifier

                                                            .clickable {
                                                                SingletonHome.nestedSection.value+=storeNestedSection.id
                                                            })
                                                }

                                            }
                                        }
                                        }


                                    }
                                }
                            }
//                            if (!CustomSingleton.isSharedStore())
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
//                                                isShowAddSection.value = true
//                                            }
//                                    ){
//                                        Text("+", modifier = Modifier.align(Alignment.Center))
//                                    }
//                                }
//                            }
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

    private fun goToProduct(storeNestedSection: StoreNestedSection) {
        val intent = Intent(
            this,
            ProductsActivity::class.java
        )
        intent.putExtra("storeNestedSection", MyJson.MyJson.encodeToString(storeNestedSection))
        startActivity(intent)
    }

private fun add(nestedSectionId: String) {

        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("nestedSectionId",nestedSectionId)
            .addFormDataPart("storeSectionId",storeSection.id.toString())
            .build()



        requestServer.request2(body,"addStoreNestedSection",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result: StoreNestedSection =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )

            SingletonHome.home.value!!.storeNestedSections += result
            homeStorage.setHome(MyJson.IgnoreUnknownKeys.encodeToString(SingletonHome.home.value!!),CustomSingleton.getCustomStoreId().toString())
            SingletonHome.home.value!!.storeNestedSections += result
            isShowAddSection.value = false
            stateController.successStateAUD("تمت الاضافه  بنجاح")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddMyCategory() {
        var ids by remember { mutableStateOf<List<Int>>(emptyList()) }
        if (nestedSections.value.isEmpty()) {
            readNestedSections()
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
                        var nestedSectionName by remember { mutableStateOf("") }
                        Card(Modifier.padding(8.dp)){
                            Row (Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                OutlinedTextField(
                                    modifier = Modifier.padding(8.dp),
                                    value = nestedSectionName,
                                    onValueChange = {
                                        nestedSectionName = it
                                    }
                                )
                                IconButton(onClick = {
                                    addNestedSection(nestedSectionName,{
                                        nestedSectionName = ""
                                        nestedSections.value += it
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
                                if (!CustomSingleton.isSharedStore())
                                deleteNestedSections(ids){
                                    isShowAddSection.value = false
                                    nestedSections.value.forEach {
                                        if (it.id in ids){
                                            nestedSections.value -= it
                                        }
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(nestedSections.value){index,nestedSection->
                        Card(Modifier.padding(8.dp)) {
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Checkbox(checked = ids.find { it == nestedSection.id } != null, onCheckedChange = {
                                    val itemC = ids.find { it == nestedSection.id}
                                    if (itemC == null) {
                                        ids = ids + nestedSection.id
                                    }else{
                                        ids = ids - nestedSection.id
                                    }
                                })

                                Text(nestedSection.name)
                                Button(
                                    enabled = !SingletonHome.home.value!!.storeNestedSections.any { it.nestedSectionId == nestedSection.id } && nestedSection.acceptedStatus != 0,
                                    onClick = {
                                        add(nestedSection.id.toString())
                                    }) { Text(if (nestedSection.acceptedStatus == 0) "بانتظار الموافقة" else if (!SingletonHome.home.value!!.storeNestedSections.any { it.nestedSectionId == nestedSection.id }) "اضافة" else "تمت الاضافة") }

                            }
                        }
                    }

//                    item {
//                        var expanded by remember { mutableStateOf(false) }
//                        Card(Modifier.padding(8.dp)) {
//                            Row (Modifier.fillMaxWidth().padding(8.dp).clickable {
//                                if (nestedSections.value.isEmpty()){
//                                    readCategories3()
//                                }
//                                expanded = !expanded
//                            },
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ){
//                                Text(category3?.name ?: "اختر قسم داخلي")
//                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
//                            }
//                            if (expanded)
//                                nestedSections.value.filterNot { sectionItem ->
//                                    storeNestedSections.value.any { storeCategory ->
//                                        storeCategory.nestedSectionId == sectionItem.id // Compare by the 'name' field
//                                    }
//                                }.forEach { item ->
//                                    DropdownMenuItem(onClick = {
//                                        category3 = item
//                                        expanded = false // Close the dropdown after selection
//                                    }, text = {
//                                        Text(item.name)
//                                    })
//                                }
//
//                        }
//
//                        if (category3 != null)
//                            Button(onClick = {
//                                add(category3!!.id.toString())
//                            },
//                                Modifier.fillMaxWidth()) {
//                                Text("حفظ")
//                            }
//                    }
                }
            }
        }
    }

    fun readNestedSections() {
        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("sectionId", storeSection.sectionId.toString())
            .addFormDataPart("storeId",  CustomSingleton.getCustomStoreId().toString())
            .build()
        Log.e("fr1",storeSection.sectionId.toString())
        Log.e("fr2",CustomSingleton.getCustomStoreId().toString())


        requestServer.request2(body, "getNestedSections", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            nestedSections.value =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            stateController.successStateAUD()
        }
    }
    fun addNestedSection(name:String, onSuccess: (data: NestedSection) -> Unit) {
        stateController.startAud()
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("name",name)
            .addFormDataPart("storeId", CustomSingleton.getCustomStoreId().toString())
            .addFormDataPart("sectionId", storeSection.sectionId.toString())
            .build()

        requestServer.request2(body, "addNestedSection", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val result: NestedSection = MyJson.IgnoreUnknownKeys.decodeFromString(data)
            onSuccess(result)
            stateController.successStateAUD()
        }
    }

    fun deleteNestedSections(ids:List<Int>,onDone:()->Unit) {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("ids",ids.toString())
            .build()

        requestServer.request2(body,"deleteNestedSections",{code,fail->
        stateController.errorStateAUD(fail)
        }
        ){it->
            onDone()
            stateController.successStateAUD()
        }
    }
    fun deleteStoreNestedSections(ids:List<Int>,onDone:()->Unit) {
        stateController.startAud()
        //
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("ids",ids.toString())
            .build()

        requestServer.request2(body,"deleteStoreNestedSections",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            onDone()
            stateController.successStateAUD()
        }
    }
}
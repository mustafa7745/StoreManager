package com.fekraplatform.storemanger.activities1

import android.content.Intent
import android.os.Bundle
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
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.activities.SingletonHome.homeStorage
import com.fekraplatform.storemanger.models.Category
import com.fekraplatform.storemanger.models.Section
import com.fekraplatform.storemanger.models.StoreCategory
import com.fekraplatform.storemanger.models.StoreSection
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.IconDelete
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyHeader
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.U1R
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class StoreSectionsViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    val appSession: AppSession,
    private val builder: FormBuilder,
    savedStateHandle: SavedStateHandle
): ViewModel(){
    var storeCategory: StoreCategory =  MyJson.IgnoreUnknownKeys.decodeFromString(savedStateHandle["storeCategory"]?:"")
    private val storeSections = mutableStateOf<List<StoreSection>>(listOf())
    val sections = mutableStateOf<List<Section>>(listOf())
    val stateController = StateController()
    val isShowAddSection = mutableStateOf(false)
    var selectMode by mutableStateOf(false)

    fun readSections() {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("categoryId", storeCategory.categoryId.toString())

                val data = requestServer.request(body, "getSections")
                sections.value = MyJson.IgnoreUnknownKeys.decodeFromString(data.toString())
                stateController.successStateAUD()
                isShowAddSection.value = true
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

    fun add(sectionId: String) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("sectionId", sectionId)
                    .addFormDataPart("storeCategoryId", storeCategory.id.toString())

                val data = requestServer.request(body, "addStoreSection")
                val result: StoreSection = MyJson.IgnoreUnknownKeys.decodeFromString(data.toString())

                storeSections.value += result
                appSession.home.value!!.storeSections += result
//                homeStorage.setHome(
//                    MyJson.IgnoreUnknownKeys.encodeToString(SingletonHome.home.value!!),
//                    CustomSingleton.getCustomStoreId().toString()
//                )
                isShowAddSection.value = false
                stateController.successStateAUD("تمت الإضافة بنجاح")
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

    fun addSection(name: String, onSuccess: (Section) -> Unit) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("name", name)
                    .addFormDataPart("categoryId", storeCategory.categoryId.toString())

                val data = requestServer.request(body, "addSection")
                val result: Section = MyJson.IgnoreUnknownKeys.decodeFromString(data.toString())
                onSuccess(result)
                homeStorage.setHome(
                    MyJson.IgnoreUnknownKeys.encodeToString(result),
                    appSession.getCustomStoreId().toString()
                )
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

    fun deleteSections(ids: List<Int>, onDone: () -> Unit) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("ids", ids.toString())

                requestServer.request(body, "deleteSections")
                onDone()
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

    fun deleteStoreSections(ids: List<Int>, onDone: () -> Unit) {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("ids", ids.toString())

                requestServer.request(body, "deleteStoreSections")
                onDone()
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

}
@AndroidEntryPoint
class StoreSectionsActivity1 : ComponentActivity() {
    val viewModel:StoreSectionsViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        viewModel.stateController.successState()
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {

                    MainCompose1 (
                        0.dp, viewModel.stateController, this,
                        {

                        },
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
                                                viewModel.deleteStoreSections(ids){
                                                    ids = emptyList()
//                                    isShowAddSection.value = false
//                                    nestedSections.value.forEach {
//                                        if (it.id in ids){
//                                            nestedSections.value -= it
//                                        }
//                                    }
                                                }
                                        }
                                        CustomIcon(Icons.Default.Add,true) {
                                            viewModel.readSections()
                                        }

                                    }


                                }) {
                                    Text("الاقسام")
                                }
                            }
//                            item {
//                                SingletonStoreConfig.EditModeCompose()
//                            }



//                            val sec = if (SingletonStoreConfig.isSharedStore()){
//                                if (SingletonHome.isEditMode.value) SingletonHome.home.value!!.storeSections.filter { it.storeCategoryId == storeCategory.id } else SingletonHome.home.value!!.storeSections.filter { it.storeCategoryId == storeCategory.id }.filterNot { it.id in SingletonStoreConfig.sections.value }                            }
//                            else SingletonHome.home.value!!.storeSections.filter { it.storeCategoryId == storeCategory.id }

                            val sectionList = viewModel.appSession.home.value!!.storeSections
                                .filter { it.storeCategoryId == viewModel.storeCategory.id }
                                .let { storeSectionList ->
                                    if (viewModel.appSession.isSharedStore()) {
                                        if (viewModel.appSession.isEditMode) {
                                            storeSectionList
                                        } else {
                                            storeSectionList.filterNot { it.id in viewModel.appSession.selectedStore!!.storeConfig!!.sections }
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
                                            .combinedClickable (
                                                onClick = {
                                                    if (! viewModel.appSession.sections.any { it == section.id })    goToSections(section)
                                                },
                                                onLongClick = {
                                                    viewModel.selectMode = !viewModel.selectMode
                                                }
                                            )
                                            .fillMaxSize()

                                    ){
                                        if (!viewModel.appSession.isSharedStore() && viewModel.selectMode)
                                        Checkbox(
                                        modifier = Modifier.align(Alignment.CenterStart),
                                        checked = ids.find { it == section.id } != null, onCheckedChange = {
                                        val itemC = ids.find { it == section.id}
                                        if (itemC == null) {
                                            ids = ids + section.id
                                        }else{
                                            ids = ids - section.id
                                        }
                                    })
                                        Text(section.sectionName,Modifier.align(Alignment.Center))
                                        if (viewModel.appSession.isEditMode){
                                            if (viewModel.appSession.selectedStore!!.storeConfig!!.sections.any { number -> number == section.id }){
                                                if (! viewModel.appSession.sections.any { it == section.id }) {
                                                    Text(
                                                        "تمت الاضافة بانتظار التأكيد",
                                                        Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .clickable {
                                                                viewModel.appSession.sections +=section.id
                                                            })
                                                }
                                                else{
                                                    Text(
                                                        "اضافة",
                                                        Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .clickable {
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())
                                                                viewModel.appSession.sections -= section.id
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())

                                                            })
                                                }
                                            }
                                            else{
                                                if ( viewModel.appSession.sections.any { it == section.id }){
                                                    Text("تمت الحذف بانتظار التأكيد",
                                                        Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .clickable {
                                                                viewModel.appSession.sections -= section.id
                                                            })
                                                }else{
                                                    Text("حذف",
                                                        Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .clickable {
                                                                viewModel.appSession.sections+=section.id
                                                            })
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
                        if (viewModel.isShowAddSection.value) modalAddMyCategory()

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
        StoreNestedSectionsActivity1::class.java
    )
    intent.putExtra("storeSection", MyJson.MyJson.encodeToString(storeSection))
    startActivity(intent)
}


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddMyCategory() {
        if (viewModel.appSession.sections.isEmpty()) {
            viewModel.readSections()
        }
        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowAddSection.value = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){
                var ids by remember { mutableStateOf<List<Int>>(emptyList()) }

                LazyColumn(
                    Modifier.fillMaxSize(),
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
                                    viewModel.addSection(sectionName,{
                                        sectionName = ""
                                        viewModel.appSession.sections += it.id
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
                                    viewModel.deleteSections(ids){
                                        viewModel.isShowAddSection.value = false
                                        viewModel.sections.value.forEach {
                                            if (it.id in ids){
                                                viewModel.appSession.sections -= it.id
                                            }
                                        }
                                    }
                            }
                        }
                    }

                    itemsIndexed(viewModel.sections.value){index,section->
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
                                    enabled = !viewModel.appSession.home.value!!.storeSections.any { it.sectionId == section.id } && section.acceptedStatus != 0,
                                    onClick = {
                                        viewModel. add(section.id.toString())
                                    }) { Text(if (section.acceptedStatus == 0) "بانتظار الموافقة" else if (!viewModel.appSession.home.value!!.storeSections.any { it.sectionId == section.id }) "اضافة" else "تمت الاضافة") }

                                Checkbox(checked = ids.find { it == section.id } != null, onCheckedChange = {
                                    val itemC = ids.find { it == section.id}
                                    if (itemC == null) {
                                        ids = ids + section.id
                                    }else{
                                        ids = ids - section.id
                                    }
                                })
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


}
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.activities.SingletonHome.homeStorage
import com.fekraplatform.storemanger.models.NestedSection
import com.fekraplatform.storemanger.models.StoreNestedSection
import com.fekraplatform.storemanger.models.StoreSection
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.IconDelete
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyHeader
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import javax.inject.Inject

@HiltViewModel
class StoreNestedSectionsViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    val appSession: AppSession,
    private val builder: FormBuilder,
    savedStateHandle: SavedStateHandle
): ViewModel()
{
    var storeSection: StoreSection =  MyJson.IgnoreUnknownKeys.decodeFromString(savedStateHandle["storeSection"]?:"")
    val nestedSections = mutableStateOf<List<NestedSection>>(listOf())
    val stateController = StateController()
    val isShowAddSection = mutableStateOf(false)
    var selectMode by mutableStateOf(false)
    fun readNestedSections() {
        viewModelScope.launch {
            stateController.startAud()

            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("sectionId", storeSection.sectionId.toString())


            try {
                val result = requestServer.request(body, "getNestedSections")
                nestedSections.value = MyJson.IgnoreUnknownKeys.decodeFromString(result.toString())
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun addNestedSection(name: String, onSuccess: (data: NestedSection) -> Unit) {
        viewModelScope.launch {
            stateController.startAud()

            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("name", name)
                .addFormDataPart("sectionId", storeSection.sectionId.toString())


            try {
                val result = requestServer.request(body, "addNestedSection")
                val parsed = MyJson.IgnoreUnknownKeys.decodeFromString<NestedSection>(result.toString())
                onSuccess(parsed)
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun deleteNestedSections(ids: List<Int>, onDone: () -> Unit) {
        viewModelScope.launch {
            stateController.startAud()

            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("ids", ids.toString())


            try {
                requestServer.request(body, "deleteNestedSections")
                onDone()
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun deleteStoreNestedSections(ids: List<Int>, onDone: () -> Unit) {
        viewModelScope.launch {
            stateController.startAud()

            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("ids", ids.toString())

            try {
                requestServer.request(body, "deleteStoreNestedSections")
                onDone()
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun addStoreNestedSection(nestedSectionId: String) {
        viewModelScope.launch {
            stateController.startAud()

            val body = builder.sharedBuilderFormWithStoreId()
                .addFormDataPart("nestedSectionId", nestedSectionId)
                .addFormDataPart("storeSectionId", storeSection.id.toString())

            try {
                val result = requestServer.request(body, "addStoreNestedSection")
                val parsed = MyJson.IgnoreUnknownKeys.decodeFromString<StoreNestedSection>(result.toString())

                appSession.home.value!!.storeNestedSections += parsed
                homeStorage.setHome(
                    MyJson.IgnoreUnknownKeys.encodeToString(appSession.home.value!!),
                    appSession.getCustomStoreId().toString()
                )
                isShowAddSection.value = false
                stateController.successStateAUD("تمت الاضافة بنجاح")
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }


}
@AndroidEntryPoint
class StoreNestedSectionsActivity1 : ComponentActivity() {
    val viewModel : StoreNestedSectionsViewModel by viewModels()
//    private val storeNestedSections = mutableStateOf<List<StoreNestedSection>>(listOf())


    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       viewModel. stateController.successState()
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {

                    MainCompose1 (
                        0.dp, viewModel.stateController, this,
                        {  },
                    ) {
                        var ids by remember { mutableStateOf<List<Int>>(emptyList()) }



                        LazyColumn (Modifier.safeDrawingPadding()){
                            stickyHeader {
                                MyHeader({
                                    finish()
                                },{
                                    if (!viewModel.appSession.isSharedStore()){
                                        IconDelete(ids) {
                                            viewModel.deleteStoreNestedSections(ids){
                                                viewModel.appSession.home.value!!.storeNestedSections.forEach {
                                                    if (it.id in ids)
                                                        viewModel.appSession.home.value!!.storeNestedSections -= it
                                                }
                                            }
                                        }
                                        CustomIcon(Icons.Default.Add,true) {
                                            viewModel.isShowAddSection.value = true
                                        }
                                    }

                                }) {
                                    Text("الاقسام الداخلية")
                                }
                            }
//                            item {
//                                SingletonStoreConfig.EditModeCompose()
//                            }



                            itemsIndexed(if (viewModel.appSession.isSharedStore()){
                                if (viewModel.appSession.isEditMode) viewModel.appSession.home.value!!.storeNestedSections.filter { it.storeSectionId ==viewModel. storeSection.id } else viewModel.appSession.home.value!!.storeNestedSections .filter { it.storeSectionId == viewModel.storeSection.id }.filterNot { it.id in viewModel.appSession.selectedStore!!.storeConfig!!.nestedSections }                            }
                            else viewModel.appSession.home.value!!.storeNestedSections.filter { it.storeSectionId == viewModel.storeSection.id }){index, storeNestedSection ->

                                Card(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .padding(8.dp)
                                ) {
                                    Box (
                                        Modifier
                                            .fillMaxSize()
                                            .combinedClickable(
                                                onClick = {
                                                    if (!viewModel.appSession.nestedSection.any { it == storeNestedSection.id }) {
                                                        viewModel.appSession.homeProduct = null
                                                        goToProduct(storeNestedSection)
                                                    }
                                                },
                                                onLongClick = {
                                                    viewModel.selectMode = !viewModel.selectMode
                                                }
                                            )

                                    ){
                                        Text(storeNestedSection.nestedSectionName,Modifier.align(Alignment.Center))
                                        if (!viewModel.appSession.isSharedStore() && viewModel.selectMode)
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

                                            if (viewModel.appSession.isEditMode){
                                            if (viewModel.appSession.selectedStore!!.storeConfig!!.nestedSections.any { number -> number == storeNestedSection.id }){
                                                if (! viewModel.appSession.nestedSection.any { it == storeNestedSection.id }) {
                                                    Text(
                                                        "تمت الاضافة بانتظار التأكيد",
                                                        Modifier
                                                            .clickable {
                                                                viewModel.appSession.nestedSection +=storeNestedSection.id
                                                            })
                                                }
                                                else{
                                                    Text(
                                                        "اضافة",
                                                        Modifier

                                                            .clickable {
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())
                                                                viewModel.appSession.nestedSection -= storeNestedSection.id
//                                                                        Log.e("rtrt", SingletonHome. categories.toString())

                                                            })
                                                }
                                            }
                                            else{
                                                if ( viewModel.appSession.nestedSection.any { it == storeNestedSection.id }){
                                                    Text("تمت الحذف بانتظار التأكيد",
                                                        Modifier

                                                            .clickable {
                                                                viewModel.appSession.nestedSection -= storeNestedSection.id
                                                            })
                                                }else{
                                                    Text("حذف",
                                                        Modifier

                                                            .clickable {
                                                                viewModel.appSession.nestedSection+=storeNestedSection.id
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
                        if (viewModel.isShowAddSection.value) modalAddMyCategory()

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
            StoreProductsActivity::class.java
        )
        intent.putExtra("storeNestedSection", MyJson.MyJson.encodeToString(storeNestedSection))
        startActivity(intent)
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddMyCategory() {
        var ids by remember { mutableStateOf<List<Int>>(emptyList()) }
        if (viewModel.nestedSections.value.isEmpty()) {
            viewModel.readNestedSections()
        }
        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowAddSection.value = false }) {
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
                                    viewModel.addNestedSection(nestedSectionName,{
                                        nestedSectionName = ""
                                        viewModel.nestedSections.value += it
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
                                    viewModel.deleteNestedSections(ids){
                                        viewModel. isShowAddSection.value = false
                                        viewModel.nestedSections.value.forEach {
                                        if (it.id in ids){
                                            viewModel.nestedSections.value -= it
                                        }
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(viewModel.nestedSections.value){index,nestedSection->
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
                                    enabled = !viewModel.appSession.home.value!!.storeNestedSections.any { it.nestedSectionId == nestedSection.id } && nestedSection.acceptedStatus != 0,
                                    onClick = {
                                        viewModel.addStoreNestedSection(nestedSection.id.toString())
                                    }) { Text(if (nestedSection.acceptedStatus == 0) "بانتظار الموافقة" else if (!viewModel.appSession.home.value!!.storeNestedSections.any { it.nestedSectionId == nestedSection.id }) "اضافة" else "تمت الاضافة") }

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

}
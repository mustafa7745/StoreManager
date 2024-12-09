package com.fekraplatform.storemanger.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fekraplatform.storemanger.models.MyCategory
import com.fekraplatform.storemanger.models.StoreCategory
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.U1R
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import okhttp3.MultipartBody


class MyCategoriesActivity : ComponentActivity() {

    val stateController = StateController()
    val requestServer = RequestServer(this)
    private val categories = mutableStateOf<List<MyCategory>>(listOf())
    private val storeCategories = mutableStateOf<List<StoreCategory>>(listOf())
    val isShowAddCategory = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readCategories()
        readStoreCategories()


        setContent {
            StoreMangerTheme {
                MainCompose1(
                    0.dp, stateController, this,
                    { readCategories() },
                ) {
                    LazyColumn {
                        item {
                            Button(onClick = {
                                isShowAddCategory.value = true
                            }) { Text("add") }
                        }
                        itemsIndexed(categories.value){index, item ->
                            Row {
                                Text(item.name)
                                val isAdded = storeCategories.value.find { it.categoryId == item.id }
                                if (isAdded ==null){
                                    Button(onClick = {

                                    }) {
                                        Text("Add to store")
                                    }
                                }
                                else{
                                    Text("Added")
                                }

                            }

                        }
                    }
                    if (isShowAddCategory.value)modalAddCategory()

                }
            }

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddCategory(

    ) {
        var value by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { isShowAddCategory.value = false }) {
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
                            TextField(value = value , onValueChange = {
                                value = it
                            })
                            Button(onClick = {
                              add(value.toString())
                            }) {
                                Text("حفظ")
                            }
                        }
                }
            }
        }
    }

    fun readCategories() {
        stateController.startRead()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeId", "1")
            .build()

        requestServer.request(body, "${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/getMyCategories", { code, fail ->
            stateController.errorStateRead(fail)
        }
        ) { data ->

            categories.value =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            stateController.successState()
        }
    }
    private fun add(name: String) {

        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("name",name.toString())
            .build()

        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/addMyCategory",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result: MyCategory =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )

            categories.value += result


            Log.e("jiamge",result.toString())

            isShowAddCategory.value = false
            stateController.successStateAUD("تمت الاضافه  بنجاح")
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

}
package com.fekraplatform.storemanger.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fekraplatform.storemanger.models.CsPsSCR
import com.fekraplatform.storemanger.models.MyCategory
import com.fekraplatform.storemanger.models.MyProduct
import com.fekraplatform.storemanger.models.StoreCategory
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.U1R
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import okhttp3.MultipartBody


class MyProductsActivity : ComponentActivity(){

    val stateController = StateController()
    val requestServer = RequestServer(this)
    private val myProducts = mutableStateOf<List<MyProduct>>(listOf())
    private val categories = mutableStateOf<List<MyCategory>>(listOf())
    val isShowAddProduct = mutableStateOf(false)

    lateinit var scr: CsPsSCR
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val str = intent.getStringExtra("storeCategory1")
        if (str != null) {
            try {
                scr = MyJson.IgnoreUnknownKeys.decodeFromString(str)
            }catch (e:Exception){
                finish()
            }
        } else {
            finish()
        }


        read()

        setContent {
            StoreMangerTheme {
                MainCompose1(
                    0.dp, stateController, this,
                    { read() },
                ) {
                    LazyColumn {
                        item {
                            Button(onClick = {
                                isShowAddProduct.value = true
                            }) { Text("add") }
                        }
                        itemsIndexed(myProducts.value){index, item ->
                            Text(item.name)
                        }
                    }
                    if (isShowAddProduct.value)modalAddMyProduct()

                }
            }

        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddMyProduct() {
        var value by remember { mutableStateOf("") }

        var category by remember { mutableStateOf<MyCategory?>(null) }
        ModalBottomSheet(
            onDismissRequest = { isShowAddProduct.value = false }) {
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

                    }

                    item {
                        var expanded by remember { mutableStateOf(false) }
                        Card(Modifier.padding(8.dp)) {
                            Row (Modifier.fillMaxWidth().padding(8.dp).clickable {
                                if (categories.value.isEmpty()){
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
                                categories.value.forEach { item ->
                                    DropdownMenuItem(onClick = {
                                        category = item
                                        expanded = false // Close the dropdown after selection
                                    }, text = {
                                        Text(item.name)
                                    })
                                }

                        }
                        if (category != null && value.isNotEmpty())
                        Button(onClick = {
                            add(value.toString(),category!!.id.toString())
                        }) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }

    fun read() {
        stateController.startRead()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("CsPsSCRId ", scr.id.toString())
            .addFormDataPart("storeId", "1")
            .build()

        requestServer.request(body, "${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/getMyProducts", { code, fail ->
            stateController.errorStateRead(fail)
        }
        ) { data ->

            myProducts.value =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            stateController.successState()
        }
    }
    private fun add(name: String,categoryId:String) {

        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("name",name)
            .addFormDataPart("categoryId",categoryId)
            .build()

        requestServer.request(body,"${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/addMyProduct",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result: MyProduct =  MyJson.IgnoreUnknownKeys.decodeFromString(
                it
            )

            myProducts.value += result




            isShowAddProduct.value = false
            stateController.successStateAUD("تمت الاضافه  بنجاح")
        }
    }
    fun readCategories() {
        stateController.startAud()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("storeId", "1")
            .build()

        requestServer.request(body, "${U1R.BASE_URL}${U1R.VERSION}/${U1R.TYPE}/getMyCategories", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->

            categories.value =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )

            stateController.successStateAUD()
        }
    }
}



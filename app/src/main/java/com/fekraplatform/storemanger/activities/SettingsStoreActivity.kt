package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.shared.CustomCard
import com.fekraplatform.storemanger.shared.CustomRow
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.VarRemoteConfig
import com.fekraplatform.storemanger.shared.builderForm
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class SettingsStoreActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)
    var uriFile by mutableStateOf<Uri?>(null)
//    lateinit var store: Store

    var file by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SingletonHome.setStateController1(stateController)
        SingletonHome.setReqestController(requestServer)

//        val intent = intent
//        val str = intent.getStringExtra("store")
//        if (str != null) {
//            try {
//                store = MyJson.IgnoreUnknownKeys.decodeFromString(str)
//            } catch (e: Exception) {
//                finish()
//            }
//
//        } else {
//            finish()
//        }

        setContent {
            StoreMangerTheme {
                MainCompose2(
                    0.dp, stateController, this,

                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        item {
                            CustomCard( modifierBox = Modifier.fillMaxSize().clickable {

                            }) {
                                CustomRow {
                                    Text("الموقع",Modifier.padding(8.dp))

                                    Button(onClick = {

                                        gotoStoreLocation()
                                    }) {
                                        Text(if (SelectedStore.store.value!!.latLng != null) "edit" else "add")
                                    }
                                }
                            }
                        }
                        if (CustomSingleton.selectedStore!!.app != null){


                            item {
                                LaunchedEffect(null) {
                                readFileFromCache()
                            }
                                CustomCard( modifierBox = Modifier.fillMaxSize().clickable {

                                }) {

                                    CustomRow {
                                        Text("اعدادات الخدمة",Modifier.padding(8.dp))

                                        Button(onClick = {
                                            if (file == null){
                                                getContentFile.launch("application/json")
                                            }else{
                                                saveFileToCache()
                                            }

                                        }) {
                                            Text(if (file != null) "edit" else "add")
//                                            Text("add")
                                        }
                                    }
                                    if (uriFile != null){
                                        Button(onClick = {
                                            saveFileToCache()
                                        }) {
                                            Text("Save this file")
                                        }
                                    }
                                }
                            }
                        }
                        ///
                        if (SelectedStore.store.value!!.typeId == 1)
                        item {
                            CustomCard( modifierBox = Modifier.fillMaxSize().clickable {

                            }) {
                                CustomRow{
                                    Switch(
                                        checked = SingletonHome.isEditMode.value,
                                        onCheckedChange = { SingletonHome.isEditMode.value = it },
                                        modifier = Modifier.padding(16.dp)
                                    )
                                    Text("وضع التعديل")
                                }
                            }
                        }
                        if (CustomSingleton.selectedStore != null && CustomSingleton.isSharedStore()){
                            if ((SingletonHome.categories.value !=  CustomSingleton.selectedStore!!.storeConfig!!.categories ||
                                        SingletonHome.sections.value != CustomSingleton.selectedStore!!.storeConfig!!.sections ||
                                        SingletonHome.nestedSection.value != CustomSingleton.selectedStore!!.storeConfig!!.nestedSections ||
                                        SingletonHome.products.value != CustomSingleton.selectedStore!!.storeConfig!!.products )
                                && SingletonHome.isEditMode.value){
                                item {
                                    CustomCard( modifierBox = Modifier.fillMaxSize().clickable {
                                        SingletonHome.updateStoreConfig()
                                    }){
                                        if (SingletonHome.stateController.isLoadingAUD.value)
                                            LinearProgressIndicator(Modifier.fillMaxWidth())
                                        else
                                            Text("حفظ التعديلات", Modifier.clickable {
                                                SingletonHome.updateStoreConfig()
                                                Log.e("wwww", SingletonHome.categories.value.toString())
                                            })
                                    }

                                }
                            }
                        }


                    }
                }
            }
        }
    }
    private fun gotoStoreLocation() {
        val intent = Intent(
            this,
            LocationStoreActivity::class.java
        )
        intent.putExtra("latLng", SelectedStore.store.value!! .latLng)
//        val intent =
//            Intent(this, SettingsStoreActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
//        finish()
    }
    fun saveFileToCache() {
        val cacheFile = File(cacheDir, "service.json")
        try {
            val fos = FileOutputStream(cacheFile)
            contentResolver.openInputStream(uriFile!!)?.use { input ->
                fos.write(input.readBytes())
            }
            fos.close()
            println("File saved to cache: ${cacheFile.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun readFileFromCache(): String? {
        val cacheFile = File(cacheDir, "service.json")
        if (cacheFile.exists()) {
            try {
                val fis = FileInputStream(cacheFile)
                val fileContents = fis.bufferedReader().use { it.readText() }
                fis.close()
                file = fileContents
                return fileContents
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    val getContentFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null){
            uriFile = uri
        }
    }
}
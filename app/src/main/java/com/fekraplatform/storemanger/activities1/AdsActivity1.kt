package com.fekraplatform.storemanger.activities1

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.activities.StringResult
import com.fekraplatform.storemanger.models.Ads
import com.fekraplatform.storemanger.models.CustomOption
import com.fekraplatform.storemanger.models.Order
import com.fekraplatform.storemanger.models.StoreOrders
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MainComposeRead
import com.fekraplatform.storemanger.shared.MyHeader
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.ServerConfig
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.shared.builderForm3
import com.fekraplatform.storemanger.storage.MyAppStorage1
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AdsViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    val appSession: AppSession,
    private val builder: FormBuilder,
): ViewModel()
{
    val stateController = StateController()
    var ads  by  mutableStateOf<List<Ads>>(emptyList())

    var uriImage by mutableStateOf<Uri?>(null)
    var isShowAddAds by mutableStateOf(false)

    fun read() {
        stateController.startRead()
        val body = builder.sharedBuilderFormWithStoreId()
        viewModelScope.launch {
            try {
                val data = requestServer.request(body, "getAds")
                val result:List<Ads> = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
               ads = result
                stateController.successState()
            } catch (e: Exception) {
                stateController.errorStateRead(e.message.toString())
            }
        }
    }
    fun upgradeAds(adsId: Int) {
        stateController.startAud()
        val body = builder.sharedBuilderFormWithStoreId()
            .addFormDataPart("adsId",adsId.toString())
        viewModelScope.launch {
            try {
                val data = requestServer.request(body, "updateAds")
                val result:StringResult = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                ads = ads.map {
                    if (it.id == adsId) it.copy(expireAt = result.result) else it
                }
                uriImage = null
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun addAds(days:Int,producyId: String? = null,imageRequestBody: RequestBody) {
        stateController.startAud()
        val body = builder.sharedBuilderFormWithStoreId()
            .addFormDataPart("days",days.toString())
            .addFormDataPart("image","file1.jpg", imageRequestBody)

        if (producyId != null){
            body.addFormDataPart("productId", producyId)
        }

        viewModelScope.launch {
            try {
                val data = requestServer.request(body, "addAds")
                val result:Ads = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                ads += result
                uriImage = null
                isShowAddAds = false
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun deleteAds(id: String ) {
        stateController.startAud()


        val body = builder.sharedBuilderFormWithStoreId()
            .addFormDataPart("id",id.toString())

        viewModelScope.launch {
            try {
                val data = requestServer.request(body, "deleteAds")
                val result:Ads = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
               ads.find { it.id.toString() == id }.let {
                    ads -= it!!
                }
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

    init {
        read()
    }
}

@AndroidEntryPoint
class AdsActivity1 : ComponentActivity() {

    val viewModel:AdsViewModel by viewModels()
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {
                MainComposeRead("الاعلانات",viewModel.stateController,{finish()},{viewModel.read()}) {
                    val carouselState = rememberCarouselState { viewModel.ads .size }
                    LazyColumn(
                        modifier = Modifier.safeDrawingPadding()
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        item {
                            Button(onClick = {
                                viewModel.isShowAddAds = true
                            }) { Text("Add") }
                        }
                        item {
                            HorizontalMultiBrowseCarousel(state = carouselState,300.dp, itemSpacing = 8.dp, modifier = Modifier.padding(8.dp).height(205.dp)) {page->
                                val ads =viewModel.ads[page]
                                val time = LocalDateTime.parse(ads.expireAt.replace(" ","T"))
                                val diff = Duration.between(getCurrentDate(),time ).toDays()
                                Box {
                                    CustomImageViewUri(viewModel.appSession .remoteConfig.BASE_IMAGE_URL+"stores/ads/"+ ads.image,Modifier.fillMaxSize().clickable {
//                                    if (ads.pid!= null){
//                                        var product:StoreProduct? = null
//                                        productViews.value.forEach {productView ->
//                                            productView.products.forEach { storeProduct ->
//                                                if (storeProduct.product.productId == ads.pid){
//                                                    product = storeProduct
//                                                }
//                                            }
//                                        }
//                                        if (product != null)
//                                            goToAddToCart(product!!)
//                                    }
                                    } .maskClip(MaterialTheme.shapes.extraLarge).background(Color.Red),
                                        contentScale = ContentScale.Fit
                                    )
                                    CustomIcon(Icons.Default.Delete, tint = Color.Red) {
                                        viewModel.deleteAds(ads.id.toString())
                                    }
                                    if (diff >= 0)
                                        Text("متبقي " + if(diff.toInt() == 0 ) "هذا اليوم" else diff, modifier = Modifier.align(Alignment.TopCenter))
                                    else{
                                        Text("تجديد", modifier = Modifier.align(Alignment.TopCenter).clickable {
                                            viewModel.upgradeAds(ads.id)
                                        })
                                    }
                                }
                            }
                        }


                    }

                    if (viewModel.isShowAddAds) modalAddMyCategory()
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddMyCategory() {

        ModalBottomSheet(
            onDismissRequest = { viewModel.isShowAddAds = false }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
            ){

                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        var selectedDays by remember { mutableIntStateOf(1) }
                        if (viewModel.uriImage != null)
                        CustomImageViewUri(imageUrl = viewModel.uriImage!!,Modifier.padding(8.dp).height(205.dp), contentScale =  ContentScale.Fit)
                        else{
                            Button(
                                modifier = Modifier.padding(8.dp).fillMaxWidth(), onClick = {
                                    getContentImage.launch("image/jpeg")
                                }){
                                Text("اختيار صورة")

                            }
                        }
                        Text("مدة الاعلان " + if (selectedDays == 1) "يوم واحد" else  selectedDays.toString() +"أيام",Modifier.padding(8.dp))
                        LazyRow(Modifier.height(80.dp).padding(8.dp)) {
                            items(7){ it->
                                val n = it + 1
                                CustomCard2(modifierBox = Modifier.size(50.dp).padding(8.dp).clickable {

                                    if (viewModel.appSession.isPremiumStore())
                                    selectedDays = n
                                }) {
                                    Text(n.toString(), color = Color.DarkGray)
                                }
                            }
                        }
                        Button(
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            onClick = {
                                val uriVal = viewModel.uriImage ?: return@Button

                                try {
                                    val inputStream = contentResolver.openInputStream(uriVal)
                                    val sizeInBytes = inputStream?.available() ?: 0
                                    val sizeInKB = sizeInBytes / 1024

                                    if (sizeInKB > 300) {
                                        viewModel.stateController.errorStateAUD("حجم الصورة يجب أن يكون أقل من 300 كيلوبايت")
                                        return@Button
                                    }

                                    inputStream!!.close()

                                    val requestBodyIcon = object : RequestBody() {
                                        val mediaType = "image/jpeg".toMediaTypeOrNull()
                                        override fun contentType(): MediaType? {
                                            return mediaType
                                        }

                                        override fun writeTo(sink: BufferedSink) {
                                            contentResolver.openInputStream(viewModel.uriImage!!)?.use { input ->
                                                val buffer = ByteArray(4096)
                                                var bytesRead: Int
                                                while (input.read(buffer).also { bytesRead = it } != -1) {
                                                    sink.write(buffer, 0, bytesRead)
                                                }
                                            }
                                        }
                                    }

                                viewModel.addAds(selectedDays,null,requestBodyIcon)
                                } catch (e: Exception) {
                                    viewModel.stateController.errorStateAUD("فشل في تحميل الصورة: ${e.message}")
                                }
                        }) {
                            Text("اضافة")
                        }
                    }
                }
            }
        }
    }
    val getContentImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null){
            viewModel.uriImage = uri
            viewModel.isShowAddAds = true
        }
    }
}
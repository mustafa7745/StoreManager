package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.models.Ads
import com.fekraplatform.storemanger.models.Order
import com.fekraplatform.storemanger.models.Store
import com.fekraplatform.storemanger.shared.AToken
import com.fekraplatform.storemanger.shared.AppInfoMethod
import com.fekraplatform.storemanger.shared.CustomCard
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.CustomRow
import com.fekraplatform.storemanger.shared.CustomRow2
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.IconDelete
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.VarRemoteConfig
import com.fekraplatform.storemanger.shared.builderForm
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.shared.builderForm3
import com.fekraplatform.storemanger.shared.builderForm4
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.nio.file.WatchEvent
import java.time.Duration


class AdsActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)
    private var orders by mutableStateOf<List<Order>>(listOf())



    var uriImage by mutableStateOf<Uri?>(null)
    var isShowAddAds by mutableStateOf(false)
//    var productId by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SingletonHome.setStateController1(stateController)
        SingletonHome.setReqestController(requestServer)

//        read()
        SingletonHome.initHome(CustomSingleton.getCustomStoreId().toString())

//        stateController.successState()
        setContent {
            StoreMangerTheme {
                MainCompose1(
                    0.dp, stateController, this,{
                        SingletonHome.read(CustomSingleton.getCustomStoreId().toString())
                    }

                ) {
                    CustomCard(modifierBox = Modifier) {
                        CustomRow2 {
                            CustomIcon(Icons.AutoMirrored.Default.ArrowBack, border = true) {
//                                backHandler()
                            }
                            Row {
                                Text("الاعلانات")
//                                if (page != pages.first()){
//                                    Text(" | ")
//                                    Text(page.pageName)
//                                }
                            }
                        }
                    }
                    val carouselState = rememberCarouselState { SingletonHome.home.value!!.ads.size }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        item {
                            HorizontalMultiBrowseCarousel(state = carouselState,300.dp, itemSpacing = 8.dp, modifier = Modifier.padding(8.dp).height(205.dp)) {page->
                                val ads =SingletonHome.home.value!!.ads[page]
                                val diff = Duration.between(ads.expireAt, getCurrentDate()).toSeconds()
                                Box {
                                    CustomImageViewUri(CustomSingleton.remoteConfig.BASE_IMAGE_URL+"stores/ads/"+ ads.image,Modifier.fillMaxSize().clickable {
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
                                        contentScale = ContentScale.Crop
                                        )
                                    CustomIcon(Icons.Default.Delete) {
                                        deleteAds(ads.id.toString())
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
                                            getContentImage.launch("image/jpeg")
                                        }
                                ){
                                    Text("+", modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }
                    }

                    if (isShowAddAds) modalAddMyCategory()
                }
            }
        }
    }

    private fun gotoOrderProducts(order: Order) {
        val intent = Intent(this, OrderProductsActivity::class.java)
        intent.putExtra("order", MyJson.MyJson.encodeToString(order))
        startActivity(intent)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun modalAddMyCategory() {

        ModalBottomSheet(
            onDismissRequest = { isShowAddAds = false }) {
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
                        CustomImageViewUri(imageUrl = uriImage!!,Modifier.padding(8.dp).height(205.dp), contentScale =  ContentScale.Crop)
                        Text("مدة الاعلان " + if (selectedDays == 1) "يوم واحد" else  selectedDays.toString() +"أيام",Modifier.padding(8.dp))
                        LazyRow(Modifier.height(80.dp).padding(8.dp)) {
                            items(7){it->
                                val n = it + 1
                                CustomCard2(modifierBox = Modifier.size(50.dp).padding(8.dp).clickable {

                                    if (CustomSingleton.isPremiumStore())
                                    selectedDays = n
                                }) {
                                    Text(n.toString(), color = Color.DarkGray)
                                }
                            }
                        }
                        Button(
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            onClick = {
                                addAds(selectedDays)
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
            uriImage = uri
            isShowAddAds = true
        }
    }
    private fun addAds(days:Int,producyId: String? = null) {
        stateController.startAud()

        val requestBodyIcon = object : RequestBody() {
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            override fun contentType(): MediaType? {
                return mediaType
            }

            override fun writeTo(sink: BufferedSink) {
                contentResolver.openInputStream(uriImage!!)?.use { input ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        sink.write(buffer, 0, bytesRead)
                    }
                }
            }
        }




        val body = builderForm4()
            .addFormDataPart("days",days.toString())
            .addFormDataPart("image","file1.jpg", requestBodyIcon)

        if (producyId != null){
            body.addFormDataPart("productId",producyId!!)
        }

        requestServer.request2(body.build(),"addAds",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
            val result: Ads =  MyJson.IgnoreUnknownKeys.decodeFromString(it)

            val newAds = SingletonHome.home.value!!.ads.toMutableList()
            newAds += result
            SingletonHome.home.value = SingletonHome.home.value!!.copy(
                ads = newAds
            )

            uriImage = null
            isShowAddAds = false
            stateController.successStateAUD("تمت   بنجاح")

        }
    }
    private fun deleteAds(id: String ) {
        stateController.startAud()

        val body = builderForm3()
            .addFormDataPart("storeId",CustomSingleton.getOriginalStoreId().toString())
            .addFormDataPart("id",id.toString())


        requestServer.request2(body.build(),"deleteAds",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
//            val result: Ads =  MyJson.IgnoreUnknownKeys.decodeFromString(it)

            val newAds = SingletonHome.home.value!!.ads.toMutableList()
            newAds -= SingletonHome.home.value!!.ads.find { it.id.toString() == id }!!
            SingletonHome.home.value = SingletonHome.home.value!!.copy(
                ads = newAds
            )

//            uriImage = null
//            isShowAddAds = false
            stateController.successStateAUD("تمت   بنجاح")

        }
    }
}
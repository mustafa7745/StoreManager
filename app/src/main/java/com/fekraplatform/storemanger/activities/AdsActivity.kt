package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.fekraplatform.storemanger.models.Ads
import com.fekraplatform.storemanger.models.Order
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyHeader
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.shared.builderForm3
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import kotlinx.serialization.encodeToString
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.time.Duration
import java.time.LocalDateTime


class AdsActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)
    private var orders by mutableStateOf<List<Order>>(listOf())



    var uriImage by mutableStateOf<Uri?>(null)
    var isShowAddAds by mutableStateOf(false)
    var isShowConfirmation by mutableStateOf(false)
//    var productId by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SingletonHome.setStateController1(stateController)
        SingletonHome.setReqestController(requestServer)

//        read()
        SingletonHome.initHome(CustomSingleton.getCustomStoreId().toString())

//        stateController.successState()
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {
//                if (isShowConfirmation) ConfirmationDialog {  }
                MainCompose1(
                    0.dp, stateController, this,{
                        SingletonHome.read(CustomSingleton.getCustomStoreId().toString())
                    }

                ) {
                    val carouselState = rememberCarouselState { SingletonHome.home.value!!.ads.size }
                    LazyColumn(
                        modifier = Modifier.safeDrawingPadding()
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        stickyHeader {
                            MyHeader({
                                finish()
                            },{
                                if (!CustomSingleton.isSharedStore()){
                                    CustomIcon(Icons.Default.Add,true) {
                                        getContentImage.launch("image/jpeg")
//                                isShowAddCatgory.value = true
                                    }
                                }

                            }) {
                                Text("الاعلانات")
                            }
                        }
                        item {
                            HorizontalMultiBrowseCarousel(state = carouselState,300.dp, itemSpacing = 8.dp, modifier = Modifier.padding(8.dp).height(205.dp)) {page->
                                val ads =SingletonHome.home.value!!.ads[page]
                                val time = LocalDateTime.parse(ads.expireAt.replace(" ","T"))
                                val diff = Duration.between(getCurrentDate(),time ).toDays()
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
                                    CustomIcon(Icons.Default.Delete, tint = Color.Red) {
                                        deleteAds(ads.id.toString())
                                    }
                                    if (diff >= 0)
                                    Text("متبقي " + if(diff.toInt() == 0 ) "هذا اليوم" else diff, modifier = Modifier.align(Alignment.TopCenter))
                                    else{
                                        Text("تجديد", modifier = Modifier.align(Alignment.TopCenter).clickable {
                                            upgradeAds(ads.id)
                                        })
                                    }
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
    private fun upgradeAds(adsId: Int) {
        stateController.startAud()

        val body = builderForm3()
            .addFormDataPart("adsId",adsId.toString())


        requestServer.request2(body.build(),"updateAds",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->

            val expireAt:StringResult  =  MyJson.IgnoreUnknownKeys.decodeFromString(it)

            var newAds = SingletonHome.home.value!!.ads.toMutableList()

            newAds = newAds.map {
                if (adsId == it.id){
                    it.copy(expireAt = expireAt.result)
                }else it
            }.toMutableList()
            SingletonHome.home.value = SingletonHome.home.value!!.copy(
                ads = newAds
            )

            uriImage = null
//            isShowAddAds = false
            stateController.successStateAUD("تمت   بنجاح")

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




        val body = builderForm3()
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

        val body = builderForm2()
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
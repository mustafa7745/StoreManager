package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fekraplatform.storemanger.Singlton.SelectedStore
import com.fekraplatform.storemanger.models.Order
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import kotlinx.serialization.encodeToString


class StorePostsActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)
    private var portraitPosts by mutableStateOf<List<Post>>(listOf(
        Post("https://i.ytimg.com/vi/rMAol1wA5Zs/oar2.jpg","https://www.youtube.com/shorts/rMAol1wA5Zs"),
        Post("https://i.ytimg.com/vi/VzJwvizuxu8/oar2.jpg","https://www.youtube.com/shorts/VzJwvizuxu8"),
        Post("https://i.ytimg.com/vi/aOI8ZR_9GKs/oar2.jpg","https://www.youtube.com/shorts/t0E_D2rv3nY"),
        Post("https://i.ytimg.com/vi/epDYVpbqKw0/oar2.jpg","https://www.youtube.com/shorts/epDYVpbqKw0"),
        Post("https://i.ytimg.com/vi/Tr2PeP01R4s/oar2.jpg","https://www.youtube.com/shorts/Tr2PeP01R4s")
    ))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        read()

        setContent {
            StoreMangerTheme {
                MainCompose1(
                    0.dp, stateController, this,{
                         read()
                    }

                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
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
                                        }
                                ){
                                    Text("+", modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }
//                        itemsIndexed(orders){index, order ->
//                            CustomCard( modifierBox = Modifier.fillMaxSize().clickable {
//
//                            }) {
//
//
//                            }
//                        }

                    }
                }
            }
        }
    }

    fun read() {
        stateController.startRead()

        val body = builderForm2()
            .addFormDataPart("storeId",SelectedStore.store.value!!.id.toString())
            .build()

        requestServer.request2(body, "getOrders", { code, fail ->
            stateController.errorStateRead(fail)
        }
        ) { data ->
//            orders =
//                MyJson.IgnoreUnknownKeys.decodeFromString(
//                    data
//                )

            stateController.successState()
        }
    }

    private fun gotoOrderProducts(order: Order) {
        val intent = Intent(this, OrderProductsActivity::class.java)
        intent.putExtra("order", MyJson.MyJson.encodeToString(order))
        startActivity(intent)
    }

    @Composable
    private fun OfferView() {
//        Spacer()
        LazyRow(Modifier.height(220.dp).fillMaxWidth()) {

            item {
                Card(Modifier.width(160.dp).padding(8.dp).fillParentMaxHeight()) {
                    Box (
                        Modifier
                            .fillMaxSize()
                            .clickable {
                            }
                    ){
                        Text("+", modifier = Modifier.align(Alignment.Center))
                    }

                }
            }

                item {
                    Card(Modifier.width(160.dp).padding(8.dp).fillParentMaxHeight()) {

                    }
                }

        }
        Text("اخر المنشورات")
        //

        Card(Modifier.height(130.dp).padding(8.dp).fillMaxWidth()) {
            Box (
                Modifier
                    .fillMaxSize()
                    .clickable {
                    }
            ){
                Text("+", modifier = Modifier.align(Alignment.Center))
            }
        }
        repeat(15){
            Card(Modifier.height(130.dp).padding(8.dp).fillMaxWidth()) {

            }
        }

    }
}

data class Post(val image:String ,val url:String)
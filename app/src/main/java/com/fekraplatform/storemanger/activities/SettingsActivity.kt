package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.models.PageModel
import com.fekraplatform.storemanger.models.UserInfo
import com.fekraplatform.storemanger.shared.AToken
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomImageView1
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.CustomRow
import com.fekraplatform.storemanger.shared.CustomRow2
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme

class SettingsActivity : ComponentActivity() {
    val requestServer = RequestServer(this)
    val stateController = StateController()
    var userInfo by mutableStateOf<UserInfo?>(null)

    val pages = listOf(
        PageModel("",0),
        PageModel("الملف الشخصي",1),
        PageModel("التصميم",2),
        PageModel("تسجيل الخروج",3)
        )



    var page by mutableStateOf(pages.first())



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme  {
                BackHand()
                Column(Modifier.safeDrawingPadding()) {
                    CustomCard2(modifierBox = Modifier) {
                        CustomRow2 {
                            CustomIcon(Icons.AutoMirrored.Default.ArrowBack, border = true) {
                                backHandler()
                            }
                            Row {
                                Text("الاعدادات")
                                if (page != pages.first()){
                                    Text(" | ")
                                    Text(page.pageName)
                                }
                            }

                        }
                    }

                    if (page.pageId == 0)
                        SettingsList()
                    MainCompose2(0.dp,stateController,this@SettingsActivity) {
                        if (page.pageId == 1)
                            UserProfile()
                    }

                }
            }
        }
    }

    @Composable
    private fun UserProfile() {
        if (userInfo != null){
            CustomCard2(modifierBox = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
                Row(Modifier.fillMaxSize()) {
                    //                                    if (accesstoken.logo!= null)
                    CustomImageView1(
                        modifier = Modifier
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(12.dp)
                            )
                            .size(50.dp)
                            .clickable {

                            },
                        imageUrl =  "y",
                    )
                    Column(Modifier.padding(8.dp)) { Text("مرحبا بك: " + userInfo!!.firstName + " " + userInfo!!.lastName) }


                }
            }
        }else{
            readUserProfile()
        }

    }

    @Composable
    private fun SettingsList() {
        CustomCard2(modifierBox = Modifier.clickable { page = pages[1]}) {
            CustomRow {
                Text("الملف الشخصي")
                CustomImageViewUri(modifier = Modifier.size(30.dp), imageUrl = R.drawable.uinfo)

            }
        }

        CustomCard2(modifierBox = Modifier.clickable { }) {
            CustomRow {
                Text("اللغات")
                CustomImageViewUri(
                    modifier = Modifier.size(30.dp),
                    imageUrl = R.drawable.languageicon,
                )


            }
        }

        CustomCard2(modifierBox = Modifier.clickable { }) {
            CustomRow {
                Text("التصميم")
                CustomImageViewUri(
                    modifier = Modifier.size(30.dp),
                    imageUrl = R.drawable.themeicon,
                )
            }
        }

        CustomCard2(modifierBox = Modifier.clickable {logout() }) {
            CustomRow {
                Text("تسجيل الخروج")
                CustomImageViewUri(
                    modifier = Modifier.size(30.dp),
                    imageUrl = R.drawable.logouticon,
                )
            }
        }
    }
    @Composable
    private fun BackHand() {
        BackHandler {
            backHandler()
        }
    }
    private fun backHandler() {
        if (page.pageId != 0) {
            page = pages.first()
        } else
            finish()
    }

    ///
    private fun readUserProfile() {
        stateController.startAud()
        val body = builderForm2().build()

        requestServer.request2(body, "getUserProfile", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            val result: UserInfo =
                MyJson.IgnoreUnknownKeys.decodeFromString(
                    data
                )
            userInfo= result
            stateController.successStateAUD()
        }
    }
    private fun logout() {
        stateController.startAud()
        val body = builderForm2().build()

        requestServer.request2(body, "logout", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
           AToken().setAccessToken("")
           gotoLogin()
        }
    }
    private fun gotoLogin() {
        val intent =
            Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}
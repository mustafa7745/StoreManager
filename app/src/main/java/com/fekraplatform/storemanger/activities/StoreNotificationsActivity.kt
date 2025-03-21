package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.models.CustomOption
import com.fekraplatform.storemanger.models.PageModel
import com.fekraplatform.storemanger.models.UserInfo
import com.fekraplatform.storemanger.shared.confirmDialog
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.CustomRow
import com.fekraplatform.storemanger.shared.CustomRow2
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme

class StoreNotificationsActivity : ComponentActivity() {
    val requestServer = RequestServer(this)
    val stateController = StateController()
    var userInfo by mutableStateOf<UserInfo?>(null)

    val pages = listOf(
        PageModel("",0),
        PageModel("ارسال اشعار",1),
        PageModel("عرض الاشعارات",2)
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
                                Text("الاشعارات")
                                if (page != pages.first()){
                                    Text(" | ")
                                    Text(page.pageName)
                                }
                            }

                        }
                    }
                    MainCompose2(0.dp,stateController,this@StoreNotificationsActivity) {
                        if (CustomSingleton.selectedStore!!.app != null){

                        }
                        else{
                            Text("لايوجد تطبيق لهذا المتجر")
                        }
                        if (page.pageId == 0)
                            SettingsList()
                        if (page.pageId == 1)
                            SendNotificationPage()
                    }

                }


            }
        }
    }

    @Composable
    private fun SendNotificationPage() {


        val radioOptions = listOf(
            CustomOption(1,"اشعار نصي"),
            CustomOption(2,"اشعار نصي مع الصورة"),
            CustomOption(2,"اشعار نصي مع الصورة داخل التطبيق")
        )
        var selectedOption by remember { mutableStateOf(radioOptions.first()) }
        LazyColumn {
            item {
                CustomCard2(modifierBox = Modifier) {
                    Column(Modifier.selectableGroup()) {
                        Text("نوع الاشعار", modifier = Modifier.padding(14.dp))
                        radioOptions.forEach { text ->
                            Row(
                                Modifier.fillMaxWidth().height(56.dp)
                                    .selectable(
                                        selected = (text == selectedOption),
                                        onClick = {
                                            selectedOption = text
//                                    if (selectedOption.id == 2 ){
////                                        selectedLocation = null
//                                    }
//
//                                    onOptionSelected(text)

                                        },
                                        role = Role.RadioButton
                                    ).padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            )
                            {
                                RadioButton(selected = (text == selectedOption), onClick = null)
                                Text(text = text.name,style = MaterialTheme. typography. bodyLarge,modifier = Modifier. padding(start = 16.dp))
                            }
                        }
                        var title by remember { mutableStateOf("") }
                        TextField(value = title, modifier = Modifier.padding(8.dp).fillMaxWidth(), onValueChange = {title =it}, label = { Text("العنوان") })
                        var description by remember { mutableStateOf("") }
                        TextField(value = description, modifier = Modifier.padding(8.dp).fillMaxWidth(), onValueChange = {description =it}, label = { Text("الوصف") })
                        Button(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            onClick = {
                                confirmDialog(this@StoreNotificationsActivity) {
                                    addNotification(it,title,description)
                                }

                            }) {
                            Text("ارسال")
                        }
                    }
                }
            }

        }


    }

    @Composable
    private fun SettingsList() {
        CustomCard2(modifierBox = Modifier.clickable { page = pages[1]}) {
            CustomRow {
                Text("ارسال اشعار")
                CustomImageViewUri(modifier = Modifier.size(30.dp), imageUrl = R.drawable.uinfo)

            }
        }


        CustomCard2(modifierBox = Modifier.clickable { page = pages[2]}) {
            CustomRow {
                Text("عرض")
                CustomImageViewUri(
                    modifier = Modifier.size(30.dp),
                    imageUrl = R.drawable.themeicon,
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
    private fun addNotification(password:String,title:String,description:String) {
        stateController.startAud()
        val body = builderForm2()
            .addFormDataPart("appId",CustomSingleton.selectedStore!!.app!!.id.toString())
            .addFormDataPart("title",title)
            .addFormDataPart("description",description)
            .addFormDataPart("passwordService",password)
            .build()

        requestServer.request2(body, "addNotification", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->

            stateController.successStateAUD("تمت   بنجاح")
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
package com.fekraplatform.storemanger.activities1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.models.PageModel
import com.fekraplatform.storemanger.models.Subscription
import com.fekraplatform.storemanger.models.UserInfo
import com.fekraplatform.storemanger.repositories.BillingRepository
import com.fekraplatform.storemanger.shared.AToken
import com.fekraplatform.storemanger.shared.AToken1
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon
import com.fekraplatform.storemanger.shared.CustomImageView1
import com.fekraplatform.storemanger.shared.CustomImageViewUri
import com.fekraplatform.storemanger.shared.CustomRow
import com.fekraplatform.storemanger.shared.CustomRow2
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.MainComposeAUD
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.ServerConfig
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm2
import com.fekraplatform.storemanger.shared.confirmDialog
import com.fekraplatform.storemanger.storage.MyAppStorage1
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    private val serverConfig: ServerConfig,
    private val appSession: AppSession,
    private val myAppStorage: MyAppStorage1,
    private val builder: FormBuilder,
    private val aToken: AToken1,
    private val billingRepository: BillingRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
): ViewModel()
{
    var gotoLogin by mutableStateOf(false)
    val stateController = StateController()
    var userInfo by mutableStateOf<UserInfo?>(null)
    fun logout() {
        viewModelScope.launch {
            stateController.startAud()
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                val data = requestServer.request(body, "logout")

                aToken.setAccessToken("")
                stateController.successStateAUD("تم تسجيل الخروج بنجاح")
                gotoLogin = true

                stateController.successState()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }

}
@AndroidEntryPoint
class SettingsActivity1 : ComponentActivity() {
   val viewModel:SettingsViewModel by viewModels()



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
            if (viewModel.gotoLogin){
                gotoLogin()
            }
            StoreMangerTheme  {
                MainComposeAUD("الاعدادات",viewModel.stateController,{finish()}) {
                    SettingsList()
                }
            }
        }
    }


    private fun gotoProfile() {
        val intent = Intent(this, ProfileActivity1::class.java)
        startActivity(intent)
    }

    @Composable
    private fun SettingsList() {
        CustomCard2(modifierBox = Modifier.clickable { gotoProfile()}) {
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

        CustomCard2(modifierBox = Modifier.clickable {
            confirmDialog(this,"تاكيد الخروج",false){
                viewModel.logout()
            } }) {
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

    private fun gotoLogin() {
        val intent = Intent(this, LoginActivity1::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
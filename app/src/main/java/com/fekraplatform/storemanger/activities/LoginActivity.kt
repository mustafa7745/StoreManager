package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.fekraplatform.storemanger.shared.AToken
import com.fekraplatform.storemanger.shared.AppInfoMethod
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.VarRemoteConfig
import com.fekraplatform.storemanger.shared.builderForm
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.launch


class LoginActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)
    val password = mutableStateOf("")
    val phone = mutableStateOf("")
    val appInfoMethod = AppInfoMethod()
    val aToken = AToken()
    lateinit var varRemoteConfig:VarRemoteConfig
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("subsecribed??",requestServer.serverConfig.getSubscribeApp())
        if (!requestServer.serverConfig.isSetSubscribeApp())
            subscribeToAppTobic()
        if (aToken.isSetAccessToken()){
            gotoDashboard()
        }

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
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // App Icon or Image
                                Image(
                                    painter = rememberImagePainter(R.mipmap.ic_launcher_round),
                                    contentDescription = "App Logo",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(bottom = 16.dp)
                                )


                                // Heading
                                Text(
                                    text = "تسجيل الدخول",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )


                                var isValidPhone by remember { mutableStateOf(true) }
                                if (!isValidPhone && phone.value.length>8) {
                                    Text(
                                        fontSize = 10.sp,
                                        text = "الرجاء إدخال رقم هاتف صحيح (يجب أن يتكون من 9 أرقام ويبدأ بـ 70, 71, 73, 77, أو 78)",
                                        color = Color.Red,
                                        modifier = Modifier.padding(start = 10.dp)
                                    )
                                }
                                // Phone Number Field
                                OutlinedTextField(
                                    value = phone.value,
                                    onValueChange = {
                                        phone.value = it
                                        isValidPhone = it.matches(Regex("^7[0|1|3|7|8][0-9]{7}$"))
                                    },
                                    label = { Text(text = "رقم الهاتف") },
                                    suffix = {
                                        Icon(
                                            modifier = Modifier.padding(5.dp),
                                            imageVector = Icons.Outlined.Phone,
                                            contentDescription = "",
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                                OutlinedTextField(
                                    value = password.value,
                                    onValueChange = {
                                        password.value = it
                                                    },
                                    label = { Text(text = "الرقم السري") },
                                    suffix = {
//                            Icon(
//                                modifier = Modifier.padding(5.dp),
//                                imageVector = Icons.Outlined.Phone,
//                                contentDescription = "",
//                            )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    visualTransformation = PasswordVisualTransformation()
                                )
                                Column (

                                    horizontalAlignment = Alignment.CenterHorizontally,

                                    verticalArrangement = Arrangement.Center
                                ){
                                    Button(
                                        onClick = {
                                            stateController.startAud()
                                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                                if (!task.isSuccessful) {
                                                    stateController.errorStateAUD("لا توجد شيكة")
//                                                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
//                                                    return@addOnCompleteListener
                                                }else{
                                                    // Get new token
                                                    val token = task.result
                                                    login(token)
                                                    Log.d("FCM Token", "Token: $token")
                                                }
                                            }
                                            lifecycleScope.launch {
                                                appInfoMethod.getAppToken(
                                                    {

                                                    },{

                                                    })
                                            }
                                        },
//                                        enabled = isValidPhone  && password.value.length in 4..8 && isInit.value,
                                        modifier = Modifier
                                            .padding(5.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text(text = "دخول")
                                    }
                                    // Error Message
                                }

                                // Sign Up Link
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "ليس لدي حساب",
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "اشتراك",
                                        color = Color.Blue,
                                        fontSize = 14.sp,
                                        modifier = Modifier.clickable {
//                                            showDialog.value = true
                                        }
                                    )
                                }
                                Text(
                                    text = "نسيت كلمة المرور؟",
                                    color = Color.Red,
                                    fontSize = 10.sp,
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .clickable {
//                                            showDialogResetPassword.value = true
//                                intentFunWhatsappForgetPassword()
                                        }
                                )






                                Spacer(modifier = Modifier.height(50.dp))

                                // Terms and Conditions
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row {
                                        Text(
                                            text = "من خلال تسجيل الدخول او الاشتراك فانك توافق على ",
                                            fontSize = 9.sp
                                        )
                                    }
                                    Row(
                                        Modifier.clickable {
//                                            val intent = Intent(Intent.ACTION_VIEW).apply {
//                                                data = Uri.parse("https://greenland-rest.com/policies-terms.html")
//                                            }
//                                            startActivity(intent)
                                        }
                                    ) {
                                        Text(

                                            text = "سياسة الاستخدام", color = Color.Blue, fontSize = 9.sp
                                        )
                                        Text(text = " و ", fontSize = 9.sp)
                                        Text(
                                            text = "شروط الخدمة ", color = Color.Blue, fontSize = 9.sp
                                        )

                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
//                            CopyrightText()
                        }
                    }
                }
            }
        }
    }

    private fun subscribeToAppTobic() {
        Log.e("start","app_1")
        Firebase.messaging.subscribeToTopic("app_1")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                requestServer.serverConfig.setSubscribeApp("1")
                    Log.e("subsecribed","app_1")
                }
            }
    }
    private fun gotoDashboard() {
        val intent =
            Intent(this, StoresActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
    private fun login(token:String) {
        Log.e("4535","app_1")
        stateController.startAud()
        val body = builderForm(token)
            .addFormDataPart("phone",phone.value.toString())
            .addFormDataPart("password",password.value.toString())
            .build()

        requestServer.request2(body,"login",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
           aToken.setAccessToken(it)
           gotoDashboard()
        }
    }
}
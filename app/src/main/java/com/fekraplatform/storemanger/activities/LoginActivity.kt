package com.fekraplatform.storemanger.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.shared.AToken
import com.fekraplatform.storemanger.shared.AppInfoMethod
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomSingleton
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
    private var countryList = listOf(
        Country("اليمن", "Yemen", "967"),
        Country("السعودية", "Saudi Arabia", "966"),
        Country("مصر", "Egypt", "20"),
        Country("الكويت", "Kuwait", "965"),
        Country("البحرين", "Bahrain", "973"),
        Country("عمان", "Oman", "968"),
        Country("الأردن", "Jordan", "962"),
        Country("لبنان", "Lebanon", "961"),
        Country("العراق", "Iraq", "964"),
        Country("سوريا", "Syria", "963"),
        Country("فلسطين", "Palestine", "970"),
        Country("دولة الإمارات", "United Arab Emirates", "971"),
        Country("قطر", "Qatar", "974"),
        Country("الولايات المتحدة", "United States", "1"),
        Country("كندا", "Canada", "1"),
        Country("المملكة المتحدة", "United Kingdom", "44"),
        Country("أستراليا", "Australia", "61"),
        Country("الهند", "India", "91"),
        Country("ألمانيا", "Germany", "49"),
        Country("فرنسا", "France", "33"),
        Country("البرازيل", "Brazil", "55"),
        Country("المكسيك", "Mexico", "52"),
        Country("اليابان", "Japan", "81"),
        Country("الصين", "China", "86"),
        Country("روسيا", "Russia", "7"),
        Country("إيطاليا", "Italy", "39"),
        Country("إسبانيا", "Spain", "34"),
        Country("كوريا الجنوبية", "South Korea", "82"),
        Country("تركيا", "Turkey", "90"),
        Country("الأرجنتين", "Argentina", "54"),
        Country("جنوب أفريقيا", "South Africa", "27"),
        Country("كولومبيا", "Colombia", "57"),
        Country("السويد", "Sweden", "46"),
        Country("سويسرا", "Switzerland", "41"),
        Country("البرتغال", "Portugal", "351"),
        Country("النمسا", "Austria", "43"),
        Country("اليونان", "Greece", "30"),
        Country("نيوزيلندا", "New Zealand", "64"),


    )
    var selectedCountryCode by mutableStateOf(countryList.first())
    var isShowSelecetCountryCode by mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {
                MainCompose2(
                    0.dp, stateController, this,
                ) {
                    var password by remember { mutableStateOf("") }
                    var phone by remember { mutableStateOf("") }
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



                                CompositionLocalProvider(LocalTextStyle provides TextStyle(textDirection = TextDirection.Ltr)){
                                    OutlinedTextField(
//                                    textDirection = TextDirection.Ltr,
                                                value = phone,
                                        onValueChange = {
                                            phone = it
//                                        isValidPhone = it.matches(Regex("^7[0|1|3|7|8][0-9]{7}$"))
                                        },

                                        label = { Text(text = "رقم الهاتف") },
                                        trailingIcon = {
                                            Row (
                                                verticalAlignment = Alignment.CenterVertically,
                                               modifier =  Modifier.padding(4.dp).clickable {
                                                isShowSelecetCountryCode = true
                                            }){

//                                                VerticalDivider(Modifier.padding(8.dp))
                                                Text("+"+selectedCountryCode.code )
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "choose country")
                                            }

                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                    )
                                }

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
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
                                                    login(token,phone,password)
                                                    Log.d("FCM Token", "Token: $token")
                                                }
                                            }
                                        },
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
                                            intentFunWhatsapp("اشتراك")
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
                                            intentFunWhatsapp("نسيت كلمة المرور")
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
                    if (isShowSelecetCountryCode){ DialogCountryCodes()}
                }
            }
        }
    }

    @Composable
    private fun DialogCountryCodes() {
        Dialog(onDismissRequest = { isShowSelecetCountryCode = false }) {

            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(Color.White)) {
                itemsIndexed(countryList) { index, item ->
                    CustomCard2(modifierBox = Modifier.clickable {
                        selectedCountryCode = item
                        isShowSelecetCountryCode = false
                    }) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp)) {
                            Text(item.nameAr + " " + item.code + "+")
                        }
                    }


                    HorizontalDivider(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp))
                }
            }
        }
    }
    private fun gotoDashboard() {
        val intent = Intent(this, StoresActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
    private fun login(token:String,phone:String,password:String) {
        stateController.startAud()
        val body = builderForm(token)
            .addFormDataPart("countryCode", selectedCountryCode.code)
            .addFormDataPart("phone",phone)
            .addFormDataPart("password",password)
            .build()

        requestServer.request2(body,"login",{code,fail->
            stateController.errorStateAUD(fail)
        }
        ){it->
           AToken().setAccessToken(it)
           gotoDashboard()
        }
    }
    private fun intentFunWhatsapp(message: String): Boolean {
        val formattedNumber = "967781874077"
        // Create the URI for the WhatsApp link
        val uri =
            "https://api.whatsapp.com/send?phone=$formattedNumber&text=${Uri.encode(message)}"

        // Create an Intent to open the WhatsApp application
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(uri)
            putExtra(Intent.EXTRA_TEXT, message)
        }
        try {
            startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "يجب تثبيت الواتس اولا", Toast.LENGTH_LONG).show()
            return false
        }
    }
    data class Country(val nameAr: String, val nameEn: String, val code: String)
}
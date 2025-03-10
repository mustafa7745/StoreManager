package com.fekraplatform.storemanger.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberImagePainter
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.shared.AToken
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon2
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm0
import com.fekraplatform.storemanger.shared.builderForm1
import com.fekraplatform.storemanger.storage.MyAppStorage
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.serialization.Serializable
import java.util.Locale

class LoginActivity : ComponentActivity() {
    val stateController = StateController()
    val requestServer = RequestServer(this)
    private var countryList  = emptyList<Country>()
    val myAppStorage = MyAppStorage()
    var selectedCountryCode by mutableStateOf<Country?>(null)
    var isShowSelecetCountryCode by mutableStateOf(false)
    var languages by mutableStateOf<List<Language>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLoginConfiguration()
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {
                MainCompose1(
                    0.dp, stateController, this,{

                        getLoginConfiguration()
                    }
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

                                DropDownLanguages()
                                HorizontalDivider(Modifier.fillMaxWidth().padding(16.dp))
//                                Row (Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center){
//
//                                }

                                // Heading
                                Text(
                                    text = getString(R.string.login),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )



                                CompositionLocalProvider(LocalLayoutDirection provides  LayoutDirection.Ltr){
                                    OutlinedTextField(
//                                    textDirection = TextDirection.Ltr,
                                        value = phone,
                                        onValueChange = {
                                            phone = it
//                                        isValidPhone = it.matches(Regex("^7[0|1|3|7|8][0-9]{7}$"))
                                        },
                                        maxLines = 1,

                                        label = { Text(text = stringResource(R.string.Phonenumber)) },
                                        leadingIcon = {
                                            Row (
                                                verticalAlignment = Alignment.CenterVertically,
                                               modifier = Modifier
                                                   .padding(2.dp)
                                                   .clickable {
                                                       isShowSelecetCountryCode = true
                                                   }){
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "choose country")
//                                                VerticalDivider(Modifier.padding(8.dp))
                                                Text("+"+selectedCountryCode!!.code )

                                            }

                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                       textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
                                    )
                                }

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                                    },
                                    maxLines = 1,
                                    label = { Text(text = stringResource(R.string.password)) },
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
                                        Text(text = stringResource(R.string.Go))
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
                                    var t = stringResource(R.string.donthavaccount)

                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = t,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    t = stringResource(R.string.register)
                                    Text(
                                        text = t,
                                        color = Color.Blue,
                                        fontSize = 14.sp,
                                        modifier = Modifier.clickable {
                                            intentFunWhatsapp(t)
//                                            showDialog.value = true
                                        }
                                    )
                                }
                                val t = stringResource(R.string.forgetpassword)
                                Text(

                                    text =t,
                                    color = Color.Red,
                                    fontSize = 10.sp,
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .clickable {
                                            intentFunWhatsapp(t)
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
                                            text = stringResource(R.string.acceptLogin),
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

                                            text = stringResource(R.string.policyUse), color = Color.Blue, fontSize = 9.sp
                                        )
                                        Text(text = " , ", fontSize = 9.sp)
                                        Text(
                                            text = stringResource(R.string.termSeivec), color = Color.Blue, fontSize = 9.sp
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
    fun DropDownLanguages() {
        val isDropDownExpanded = remember {
            mutableStateOf(false)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()

                ) {
                    Card(
                        colors  = CardDefaults.cardColors(
                            containerColor =Color.White
                        ),
                        modifier = Modifier
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(
                                    16.dp
                                )
                            )
                            .clip(
                                RoundedCornerShape(
                                    16.dp
                                )
                            )
                    ){
                        Box (
                            modifier = Modifier.clickable {
//                                isDropDownExpanded.value = true
                                isDropDownExpanded.value = true
                            }


                        ) {
                            Row ( horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                                ,
                            )
                            {
                                CustomIcon2(Icons.Default.KeyboardArrowDown) {   isDropDownExpanded.value = true}
                                val lang = languages.find { it.code == getAppLanguage(this@LoginActivity) }
                                Log.e("lannn",getAppLanguage(this@LoginActivity))
//                                Log.e("lannng",Locale.get)
                                Text(lang?.name ?: languages.find { it.code == "en" }!!.name,Modifier.padding(8.dp))
                            }
                        }
                    }

                }
                DropdownMenu(
                    expanded = isDropDownExpanded.value,
                    onDismissRequest = {
                        isDropDownExpanded.value = false
                    }) {



                    languages.forEachIndexed { index, language ->
                        DropdownMenuItem(text = {
                            Row {
                                Text(text = language.name)
                            }
                        },
                            onClick = {
                                myAppStorage.setLang(language)
                                setLocale(this@LoginActivity,language.code)
                                recreate()
//                                isDropDownExpanded.value = false
//                                read (listOf(language.id).toString()){
//                                    selectedCustomOption = language
//                                }
                            })
                    }
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
                            Text(item.name + " " + item.code + "+")
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
        val body = builderForm1(token)
            .addFormDataPart("countryCode", selectedCountryCode!!.code)
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
    private fun getLoginConfiguration() {
        stateController.startRead()
        val body = builderForm0()

        requestServer.request2(body.build(),"getLoginConfiguration",{code,fail->
            stateController.errorStateRead(fail)
        }
        ){it->
            val result:LoginConfiguration = MyJson.IgnoreUnknownKeys.decodeFromString(it)

            languages = result.languages
            countryList = result.countries

           selectedCountryCode =  countryList.first()
            stateController.successState()
//            AToken().setAccessToken(it)
//            gotoDashboard()
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
}
@Serializable
data class LoginConfiguration (val countries:List<Country>,val languages: List<Language>)
@Serializable
data class Language(val name:String,val code: String)
@Serializable
data class Country(val name: String, val code: String)
fun getAppLanguage(context: Context): String {
    val configuration = context.resources.configuration

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // For API 24 and above, use configuration.locales
        configuration.locales.get(0).language
    } else {
        // For API 23 and below, use configuration.locale (deprecated in API 24+)
        @Suppress("DEPRECATION")
        configuration.locale.language
    }
}
fun setLocale(activity: Activity, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val resources = activity.resources
    val configuration = resources.configuration
    configuration.setLocale(locale)
    resources.updateConfiguration(configuration, resources.displayMetrics)

}
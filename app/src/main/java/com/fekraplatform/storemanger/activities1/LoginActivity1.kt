package com.fekraplatform.storemanger.activities1

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
import androidx.activity.viewModels
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import coil.compose.rememberImagePainter
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.activities.Country
import com.fekraplatform.storemanger.activities.Language
import com.fekraplatform.storemanger.activities.LoginConfiguration
import com.fekraplatform.storemanger.activities.getAppLanguage
import com.fekraplatform.storemanger.shared.AToken
import com.fekraplatform.storemanger.shared.AToken1
import com.fekraplatform.storemanger.shared.CustomCard2
import com.fekraplatform.storemanger.shared.CustomIcon2
import com.fekraplatform.storemanger.shared.MainCompose
import com.fekraplatform.storemanger.shared.MainCompose1
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.ServerConfig
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderForm0
import com.fekraplatform.storemanger.shared.builderForm1
import com.fekraplatform.storemanger.storage.MyAppStorage
import com.fekraplatform.storemanger.storage.MyAppStorage1
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val requestServer: RequestServer2,
    private val serverConfig: ServerConfig,
    private val appSession: AppSession,
    private val myAppStorage: MyAppStorage1,
    private val builder: FormBuilder,
    private val aToken: AToken1
):ViewModel(){
    val stateController = StateController()
    var countryList  = emptyList<Country>()
    lateinit var selectedCountryCode : Country
    var isShowSelecetCountryCode by mutableStateOf(false)
    var languages by mutableStateOf<List<Language>>(emptyList())
    var selectedLanguageCode = appSession.selectedLanguageCode

    init {
        getLoginConfiguration()
    }

    suspend fun setLang(data:Language){
            myAppStorage.setLang(data)
        appSession.selectedLanguageCode = data

    }

    var password by mutableStateOf("")
        private set

    var phone by mutableStateOf("")
        private set

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun onPhoneChange(newPhone: String) {
        phone = newPhone
    }

    var successLogin by mutableStateOf(false)

    fun login() {stateController.startAud()
        viewModelScope.launch {
            try {
                val body = builder.loginBuilderForm()
                    .addFormDataPart("countryCode", selectedCountryCode.code)
                    .addFormDataPart("phone", phone)
                    .addFormDataPart("password", password)
                val data = requestServer.request(body, "login",false) as String
                aToken.setAccessToken(data)
                successLogin = true
            } catch (e: Exception) {
                Log.e("UUURRRL3",e.message.toString())
                stateController.errorStateAUD(e.message.toString())
            }
        }
    }
    fun signInWithGoogle(idToken:String) {
        stateController.startAud()
        viewModelScope.launch {
            try {
                val body = builder.sharedBuilderForm()
                    .addFormDataPart("loginType", "Google")
                    .addFormDataPart("googleToken", idToken)

                val data = requestServer.request(body, "login",false)
                aToken.setAccessToken(data as String)
                successLogin = true
            } catch (e: Exception) {
                Log.e("UUURRRL3",e.message.toString())
                stateController.errorStateAUD(e.message.toString())
            }
        }
//        viewModelScope.launch {
//            try {
//                val body = builder.loginBuilderForm()
//                    .addFormDataPart("loginType", "Google")
//                    .addFormDataPart("googleToken", idToken)
//                val token = requestServer.suspendRequest(body, "login")
//                aToken.setAccessToken(token)
//                onSuccess()
//            } catch (e: Exception) {
//                stateController.errorStateAUD(e.message ?: "خطأ غير معروف")
//            }
//        }
    }

    fun getLoginConfiguration() {
        if (countryList.isNotEmpty()) return
        stateController.startRead()

        viewModelScope.launch {
            try {
                val body = builder.loginBuilderForm()

                Log.e("UUURRRL","getLoginConfiguration")
                val data = requestServer.request(body, "getLoginConfiguration",false)
                Log.e("UUURRRL2",data.toString())
                val result: LoginConfiguration = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                countryList = result.countries
                languages = result.languages
                selectedCountryCode = countryList.first()
                stateController.successState()
            } catch (e: Exception) {
                Log.e("UUURRRL3",e.message.toString())
                stateController.errorStateRead(e.message.toString())
            }
        }
    }

   suspend fun getLanguage(): Language {
      return myAppStorage.processLanguage()

    }

//    private fun getLoginConfiguration() {
//        stateController.startRead()
//        val body = builderForm0()
//
//        requestServer.request2(body.build(),"getLoginConfiguration",{code,fail->
//            stateController.errorStateRead(fail)
//        }
//        ){it->
//            val result:LoginConfiguration = MyJson.IgnoreUnknownKeys.decodeFromString(it)
//
//            languages = result.languages
//            countryList = result.countries
//
//            selectedCountryCode =  countryList.first()
//            stateController.successState()
////            AToken().setAccessToken(it)
////            gotoDashboard()
//        }
//    }

}

@AndroidEntryPoint
class LoginActivity1 : ComponentActivity() {
    val viewModel:LoginViewModel by viewModels()


//    val myAppStorage = MyAppStorage()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        getLoginConfiguration()
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme {
                MainCompose (
                   viewModel.stateController, {viewModel.getLoginConfiguration()},{
                        if (viewModel.successLogin){
                            gotoDashboard()
                        }
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
                                            value = viewModel.phone,
                                            onValueChange = {
                                                viewModel.onPhoneChange(it)
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
                                                            viewModel.isShowSelecetCountryCode = true
                                                        }){
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "choose country")
//                                                VerticalDivider(Modifier.padding(8.dp))
                                                    Text("+"+viewModel.selectedCountryCode!!.code )

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
                                        value = viewModel.password,
                                        onValueChange = {
                                            viewModel.onPasswordChange(it)
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
                                                viewModel.login()
                                            },
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Text(text = stringResource(R.string.Go))
                                        }


                                        // Error Message
                                    }
                                    Image(
                                        painter = painterResource(id = R.drawable.android_light_sq), // تأكد من وضع الشعار في مجلد res/drawable
                                        contentDescription = "Google Logo",
                                        modifier = Modifier

                                            .padding(top = 30.dp).clickable {
                                                viewModel.stateController.startAud()
                                                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                                    if (!task.isSuccessful) {
                                                        viewModel.stateController.errorStateAUD("لا توجد شيكة")
//                                                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
//                                                    return@addOnCompleteListener
                                                    }else{
                                                        // Get new token
                                                        val token = task.result
                                                        signInWithGoogle(token,this@LoginActivity1)
                                                        Log.d("FCM Token", "Token: $token")
                                                    }
                                                }
                                            }
                                    )



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
                        if (viewModel.isShowSelecetCountryCode){ DialogCountryCodes()}

                    }
                )
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
                                Log.e("LAngs",viewModel.languages.toString())
                                Log.e("SLAngs",viewModel.selectedLanguageCode.toString())
//                                val lang = viewModel.languages.find { it.code == viewModel.selectedLanguageCode.code }
////                                Log.e("lannng",Locale.get)
                                Text(viewModel.selectedLanguageCode.name,Modifier.padding(8.dp))
                            }
                        }
                    }

                }
                DropdownMenu(
                    expanded = isDropDownExpanded.value,
                    onDismissRequest = {
                        isDropDownExpanded.value = false
                    }) {



                    viewModel.languages.forEachIndexed { index, language ->
                        DropdownMenuItem(text = {
                            Row {
                                Text(text = language.name)
                            }
                        },
                            onClick = {
                                lifecycleScope.launch {
                                    viewModel.setLang(language)
                                    recreate()
                                }
                                isDropDownExpanded.value = false
//                                setLocale(this@LoginActivity1,language.code)

//
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
        val viewModel: LoginViewModel = hiltViewModel()
        Dialog(onDismissRequest = { viewModel.isShowSelecetCountryCode = false }) {

            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .selectableGroup()
                    .padding(16.dp)
                    .background(Color.White)) {
                itemsIndexed(viewModel.countryList) { index, item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp)
                            .selectable(
                                selected = (item == viewModel.selectedCountryCode),
                                onClick = {
                                    viewModel.selectedCountryCode = item
                                    viewModel.isShowSelecetCountryCode= false
                                },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    )
                    {
                        Row(
                            Modifier.height(56.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        )
                        {
                            RadioButton(selected = (item == viewModel.selectedCountryCode), onClick = null)
                            Text(text = item.name["ar"].toString(),style = MaterialTheme. typography. bodyLarge,modifier = Modifier. padding(start = 16.dp))
                        }
                        Text(text = item.code + "+",style = MaterialTheme. typography. bodyLarge,modifier = Modifier. padding(start = 16.dp))
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    //    @Composable
//    private fun DialogCountryCodes() {
//        Dialog(onDismissRequest = { viewModel.isShowSelecetCountryCode = false }) {
//
//            LazyColumn(
//                Modifier
//                    .fillMaxSize()
//                    .padding(16.dp)
//                    .background(Color.White)) {
//                itemsIndexed(viewModel.countryList) { index, item ->
//                    CustomCard2(modifierBox = Modifier.clickable {
//                        viewModel.selectedCountryCode = item
//                        viewModel.isShowSelecetCountryCode = false
//                    }) {
//                        Row(
//                            Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp)) {
//                            Text(item.name + " " + item.code + "+")
//                        }
//                    }
//
//
//                    HorizontalDivider(
//                        Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp))
//                }
//            }
//        }
//    }
    private fun gotoDashboard() {
        val intent = Intent(this, StoresActivity1::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
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

    fun signInWithGoogle(token:String,context: Context) {

//        stateController.startAud()
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("635175556369-ltr2c9r3caj7805kgi4vo8l34uukok58.apps.googleusercontent.com") // استبدل بـ Web Client ID الخاص بك
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential.data

                    val idToken = credential.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
                    val email = credential.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID")
                    val displayName = credential.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_DISPLAY_NAME")
                    val givenName = credential.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_GIVEN_NAME")
                    val familyName = credential.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_FAMILY_NAME")
                    val profilePictureUri = credential.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_PROFILE_PICTURE_URI")

                    Log.d("SignIn", "ID Token: $idToken")
                    Log.d("SignIn", "Email: $email")
                    Log.d("SignIn", "Display Name: $displayName")
                    Log.d("SignIn", "Given Name: $givenName")
                    Log.d("SignIn", "Family Name: $familyName")
                    Log.d("SignIn", "Profile Picture URI: $profilePictureUri")
                    Log.d("SignIn", "Credential: ${credential}")
                    // يمكنك الآن إرسال الـ idToken لخادمك للتحقق من هوية المستخدم
                    // أو التعامل مع بيانات المستخدم كما تشاء



                viewModel.signInWithGoogle(idToken.toString())
//                val body = builderForm1(token)
//                    .addFormDataPart("loginType", "Google")
//                    .addFormDataPart("googleToken", idToken.toString())
////                    .addFormDataPart("email", email.toString())
////                    .addFormDataPart("fname",givenName.toString())
////                    .addFormDataPart("lname",familyName.toString())
//                    .build()
//
//                requestServer.request2(body,"login",{code,fail->
//                    stateController.errorStateAUD(fail)
//                }
//                ){it->
//                    AToken().setAccessToken(it)
//                    gotoDashboard()
//                }

                // التعامل مع بيانات المستخدم هنا

            } catch (e: GetCredentialException) {
                e.message?.let { viewModel.stateController.errorStateAUD(it) }
                Log.e("SignIn", "Sign-in failed", e)
            }
        }
    }
}
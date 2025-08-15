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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.models.UserInfo
import com.fekraplatform.storemanger.shared.CustomException
import com.fekraplatform.storemanger.shared.CustomImageView
import com.fekraplatform.storemanger.shared.MainComposeRead
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer2
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private  val builder: FormBuilder,
    private val requestServer: RequestServer2,
    private val appSession: AppSession
):ViewModel(){
    val stateController = StateController()
    var userInfo by mutableStateOf<UserInfo?>(null)
    var uriLogo by  mutableStateOf<Uri?>(null)
    var shouldExit by mutableStateOf(false)

    fun getAppSession(): AppSession {
        return appSession
    }


    suspend fun readUserProfile() {
        stateController.startRead()
        try {
            val body = builder.loginBuilderForm()

            Log.e("UUURRRL","getLoginConfiguration")
            val data = requestServer.request(body, "getUserProfile")
            Log.e("UUURRRL2",data.toString())
            val result:UserInfo = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
            userInfo = result
            stateController.successState()
        } catch (e: CustomException) {
//            Log.e("UUURRRL3",e.message.toString())
//            Log.e("UUURRRLCode",e.code.toString())
            stateController.errorStateRead(e.message.toString())
//            if (e.code == 2000){
//                stateController.errorStateAUD(e.message)
//                shouldExit = true
//            }
        }
    }
    suspend fun updateProfile(firstName:String, secondName:String, thirdName:String, lastName:String, requestBodyIcon: RequestBody) {
        stateController.startAud()
        val body = builder.builderFormWithAccessToken()
            .addFormDataPart("firstName",firstName)
            .addFormDataPart("secondName",secondName)
            .addFormDataPart("thirdName",thirdName)
            .addFormDataPart("lastName",lastName)



        if (uriLogo != null){
            body.addFormDataPart("logo", "file1.jpg", requestBodyIcon)
        }

        try {

            val data = requestServer.request(body, "updateProfile")
            Log.e("UUURRRL2",data.toString())
            val result: UserInfo = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
            userInfo = result
            stateController.successStateAUD()
        } catch (e: Exception) {
            Log.e("UUURRRL3",e.message.toString())
            stateController.errorStateAUD(e.message.toString())
        }

//            .build()
//        requestServer.request2(body.build(), "updateProfile", { code, fail ->
//            stateController.errorStateAUD(fail)
//        }
//        ) { data ->
//            userInfo =  MyJson.IgnoreUnknownKeys.decodeFromString(data)
//            uriLogo = null
//            stateController.successStateAUD("تمت   بنجاح")
//        }
    }
    fun addEmail(idToken:String){
        stateController.startAud()
        viewModelScope.launch {
            try {
                val body = builder.sharedBuilderFormWithStoreId()
                    .addFormDataPart("googleToken", idToken.toString())
                val data = requestServer.request(body, "addEmail")
                userInfo = MyJson.IgnoreUnknownKeys.decodeFromString(data as String)
                stateController.successStateAUD()
            } catch (e: Exception) {
                stateController.errorStateAUD(e.message.toString())
            }
        }
//        val body = builder.builderFormWithAccessToken()
//
//            .build()
//
//        requestServer.request2(body,"addEmail",{code,fail->
//            stateController.errorStateAUD(fail)
//        }
//        ){data->
//            userInfo =  MyJson.IgnoreUnknownKeys.decodeFromString(data)
//            uriLogo = null
//            stateController.successStateAUD("تمت   بنجاح")
//        }
    }
}

@AndroidEntryPoint
class ProfileActivity1 : ComponentActivity() {

    val viewModel :ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            if (viewModel.shouldExit){
                gotoLogin()
            }
            ProfileScreen()
        }

}
    private fun gotoLogin() {
        val intent = Intent(this, LoginActivity1::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }



    @Composable
    private fun ProfileScreen() {
        val viewModel:ProfileViewModel = hiltViewModel()
        val activity = LocalContext.current as Activity
        val coroutineScope = rememberCoroutineScope()



        LaunchedEffect(1) {
            viewModel.readUserProfile()
        }

        StoreMangerTheme  {
            MainComposeRead("الملف الشخصي",viewModel.stateController,{activity.finish()},{
                coroutineScope.launch {
                    viewModel.readUserProfile()
                }
               }) {
                UserProfile()
            }
        }
    }






}
@Composable
private fun UserProfile() {
    val viewModel:ProfileViewModel = hiltViewModel()
    val context = LocalContext.current as Activity
    val coroutineScope = rememberCoroutineScope()

    val getContentlogo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null){
            viewModel.uriLogo = uri
        }
    }
    val googleSignInLauncher = rememberLauncherForActivityResult (
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    viewModel.addEmail(idToken)
                }
                // إرسال idToken إلى السيرفر هنا
            } catch (e: ApiException) {
                viewModel.stateController.errorStateAUD(e.message.toString())
                Log.e("GoogleSignIn", "فشل تسجيل الدخول", e)
            }

        }
    }

    if (viewModel.userInfo != null){
        var firstname by remember {mutableStateOf(viewModel.userInfo!!.firstName)}
        var secondname by remember {mutableStateOf(viewModel.userInfo!!.secondName)}
        var thirdname by remember {mutableStateOf(viewModel.userInfo!!.thirdName)}
        var lastname by remember {mutableStateOf(viewModel.userInfo!!.lastName)}

        LazyColumn (Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // صورة المستخدم
                    CustomImageView(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .clickable { getContentlogo.launch("image/*") },
                        imageUrl = viewModel.uriLogo ?: (viewModel.getAppSession().remoteConfig.BASE_IMAGE_URL +
                                viewModel.getAppSession().remoteConfig.SUB_FOLDER_USERS_LOGOS +
                                viewModel.userInfo!!.logo.toString()),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp)) // مسافة بين الصورة والنص

                    // معلومات المستخدم
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$firstname $lastname", // رقم الهاتف
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                        HorizontalDivider(Modifier.padding(8.dp))


//                                Text(
//                                    text = "+967 777777777", // رقم الهاتف
//                                    style = MaterialTheme.typography.bodyLarge,
//                                    color = Color.Black
//                                )
                        if (!viewModel.userInfo!!.phone.isNullOrEmpty()) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr){
                                Row {
                                    Text(
                                        text = "+"+ viewModel.userInfo!!.code, // رقم الهاتف
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Black
                                    )
                                    Text(
                                        text =  " "+viewModel.userInfo!!.phone, // رقم الهاتف
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Black
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "إضافة رقم هاتف",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Blue,
                                modifier = Modifier.clickable {

                                    val message = """ مرحبا بك
يرجى إضافة رقم الهاتف المرتبط ب واتساب إلى حسابي 
البريد الإلكتروني: ${viewModel.userInfo!!.email}
شكرًا لتعاونك.
""".trimIndent()

                                    addPhoneNumber(message,context)
                                }
                            )
                        }

                        if (!viewModel.userInfo!!.email.isNullOrEmpty()) {
                            Text(
                                text = viewModel.userInfo!!.email!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        } else {
                            Text(
                                text = "إضافة بريد إلكتروني",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Blue,
                                modifier = Modifier.clickable {
                                    viewModel.stateController.startAud()

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isCredentialManagerSupported()) {
//                                                    signInWithCredentialManager()
                                        signInWithGoogle(viewModel,context)
                                    } else {
                                        signInWithGoogleClassic(context,googleSignInLauncher)
                                    }

                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
            }
            item {
                // الصف الأول: الاسم الأول + الاسم الثاني
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                        value = firstname,
                        onValueChange = { firstname = it },
                        label = { Text("الاسم الأول") }
                    )

                    TextField(
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                        value = if(secondname!= null) secondname.toString() else "",
                        onValueChange = { secondname = it },
                        label = { Text("الاسم الثاني") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp)) // مسافة بين الصفين

                // الصف الثاني: الاسم الثالث + الاسم الأخير
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                        value = if(thirdname!= null) thirdname.toString() else "",
                        onValueChange = { thirdname = it },
                        label = { Text("الاسم الثالث") }
                    )

                    TextField(
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                        value = lastname,
                        onValueChange = { lastname = it },
                        label = { Text("الاسم الأخير") }
                    )
                }
            }


            if (viewModel.uriLogo != null || viewModel.userInfo!!.firstName != firstname|| viewModel.userInfo!!.secondName != secondname ||viewModel.userInfo!!.thirdName != thirdname || viewModel.userInfo!!.lastName != lastname)

                item {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        onClick = {
                            val requestBodyIcon = object : RequestBody() {
                                val mediaType = "image/jpeg".toMediaTypeOrNull()
                                override fun contentType(): MediaType? {
                                    return mediaType
                                }

                                override fun writeTo(sink: BufferedSink) {
                                   context.contentResolver.openInputStream(viewModel.uriLogo!!)?.use { input ->
                                        val buffer = ByteArray(4096)
                                        var bytesRead: Int
                                        while (input.read(buffer).also { bytesRead = it } != -1) {
                                            sink.write(buffer, 0, bytesRead)
                                        }
                                    }
                                }
                            }

                            coroutineScope.launch {
                                viewModel.  updateProfile(firstname,secondname.toString(),thirdname.toString(),lastname,requestBodyIcon)
                            }
                        }) {

                        Text("حفظ التعديلات")
                    }
                }

//                Row(Modifier.fillMaxSize()) {
//                    //                                    if (accesstoken.logo!= null)
//                    CustomImageView1(
//                        modifier = Modifier
//                            .border(
//                                1.dp,
//                                MaterialTheme.colorScheme.primary,
//                                RoundedCornerShape(12.dp)
//                            )
//                            .size(50.dp)
//                            .clickable {
//
//                            },
//                        imageUrl = CustomSingleton.remoteConfig.BASE_IMAGE_URL + CustomSingleton.remoteConfig.SUB_FOLDER_STORE_COVERS + "y",
//                    )
//                    Column(Modifier.padding(8.dp)) { Text("مرحبا بك: " + userInfo!!.firstName + " " + userInfo!!.lastName) }
//
//
//                }
        }
    }
}
private fun signInWithGoogleClassic(activity: Activity,googleSignInLauncher: ActivityResultLauncher<Intent>) {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("635175556369-ltr2c9r3caj7805kgi4vo8l34uukok58.apps.googleusercontent.com")
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(activity, gso)
    val signInIntent = googleSignInClient.signInIntent
    googleSignInLauncher.launch(signInIntent)
}
private fun signInWithGoogle(viewModel: ProfileViewModel,context: Context) {

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

            if (idToken != null) {
                viewModel.addEmail(idToken)
            }

        } catch (e: GetCredentialException) {

            viewModel.stateController.errorStateAUD(e.message.toString())

//                e.message?.let { stateController.errorStateAUD(it) }
            Log.e("SignIn", "Sign-in failed", e)
        }
    }
}
private fun isCredentialManagerSupported(): Boolean {
    // مثلاً: تحقق من Google Play Services أو من توفر CredentialManager
    return try {
        Class.forName("androidx.credentials.CredentialManager")
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}
private fun addPhoneNumber(message:String,context: Context): Boolean {
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
        context.startActivity(intent)
        return true
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "يجب تثبيت الواتس اولا", Toast.LENGTH_LONG).show()
        return false
    }
}
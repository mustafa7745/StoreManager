package com.fekraplatform.storemanger.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.fekraplatform.storemanger.models.UserInfo
import com.fekraplatform.storemanger.shared.CustomImageView
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.IconBackWithTitle
import com.fekraplatform.storemanger.shared.MainCompose2
import com.fekraplatform.storemanger.shared.MyJson
import com.fekraplatform.storemanger.shared.RequestServer
import com.fekraplatform.storemanger.shared.StateController
import com.fekraplatform.storemanger.shared.builderFormWithAccessToken
import com.fekraplatform.storemanger.ui.theme.StoreMangerTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink

class ProfileActivity : ComponentActivity() {
    val requestServer = RequestServer(this)
    val stateController = StateController()
    private var userInfo by mutableStateOf<UserInfo?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readUserProfile()
        enableEdgeToEdge()
        setContent {
            StoreMangerTheme  {
                Column(Modifier) {
                    MainCompose2(0.dp,stateController,this@ProfileActivity) {
                        IconBackWithTitle(onBack = {finish()},"الملف الشخصي")
                        HorizontalDivider()
                        UserProfile()
                    }
                }
            }
        }
    }



    var uriLogo by  mutableStateOf<Uri?>(null)
    val getContentlogo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null){
            uriLogo = uri
        }
    }
    @Composable
    private fun UserProfile() {
        if (userInfo != null){
            var firstname by remember {mutableStateOf(userInfo!!.firstName)}
            var secondname by remember {mutableStateOf(userInfo!!.secondName)}
            var thirdname by remember {mutableStateOf(userInfo!!.thirdName)}
            var lastname by remember {mutableStateOf(userInfo!!.lastName)}

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
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .clickable { getContentlogo.launch("image/*") },
                                imageUrl = uriLogo ?: (CustomSingleton.remoteConfig.BASE_IMAGE_URL +
                                        CustomSingleton.remoteConfig.SUB_FOLDER_USERS_LOGOS +
                                        userInfo!!.logo.toString()),
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
                                if (!userInfo!!.phone.isNullOrEmpty()) {
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr){
                                        Row {
                                            Text(
                                                text = "+"+ userInfo!!.code, // رقم الهاتف
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color.Black
                                            )
                                            Text(
                                                text =  " "+userInfo!!.phone, // رقم الهاتف
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
البريد الإلكتروني: ${userInfo!!.email}
شكرًا لتعاونك.
""".trimIndent()

                                             addPhoneNumber(message)
                                        }
                                    )
                                }

                                if (!userInfo!!.email.isNullOrEmpty()) {
                                    Text(
                                        text = userInfo!!.email!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                } else {
                                    Text(
                                        text = "إضافة بريد إلكتروني",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Blue,
                                                modifier = Modifier.clickable {
                                                    stateController.startAud()

                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isCredentialManagerSupported()) {
//                                                    signInWithCredentialManager()
                                                        signInWithGoogle()
                                                    } else {
                                                        signInWithGoogleClassic()
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


                    if (uriLogo != null || userInfo!!.firstName != firstname|| userInfo!!.secondName != secondname ||userInfo!!.thirdName != thirdname || userInfo!!.lastName != lastname)

                    item {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            onClick = {
                            updateProfile(firstname,secondname.toString(),thirdname.toString(),lastname)
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

    private fun addPhoneNumber(message:String): Boolean {
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
    private fun readUserProfile() {
        stateController.startAud()
        val body = builderFormWithAccessToken().build()

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
    private fun updateProfile(firstName:String,secondName:String,thirdName:String,lastName:String) {
        stateController.startAud()
        val body = builderFormWithAccessToken()
            .addFormDataPart("firstName",firstName)
            .addFormDataPart("secondName",secondName)
            .addFormDataPart("thirdName",thirdName)
            .addFormDataPart("lastName",lastName)



        if (uriLogo != null){
            val requestBodyIcon = object : RequestBody() {
                val mediaType = "image/jpeg".toMediaTypeOrNull()
                override fun contentType(): MediaType? {
                    return mediaType
                }

                override fun writeTo(sink: BufferedSink) {
                    contentResolver.openInputStream(uriLogo!!)?.use { input ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            sink.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
            body.addFormDataPart("logo", "file1.jpg", requestBodyIcon)
        }

//            .build()
        requestServer.request2(body.build(), "updateProfile", { code, fail ->
            stateController.errorStateAUD(fail)
        }
        ) { data ->
            userInfo =  MyJson.IgnoreUnknownKeys.decodeFromString(data)
            uriLogo = null
            stateController.successStateAUD("تمت   بنجاح")
        }
    }
    private fun signInWithGoogleClassic() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("635175556369-ltr2c9r3caj7805kgi4vo8l34uukok58.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }
    private fun signInWithGoogle() {

//        stateController.startAud()
        val credentialManager = CredentialManager.create(this)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("635175556369-ltr2c9r3caj7805kgi4vo8l34uukok58.apps.googleusercontent.com") // استبدل بـ Web Client ID الخاص بك
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(this@ProfileActivity, request)
                val credential = result.credential.data
                val idToken = credential.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")

                val body = builderFormWithAccessToken()
                    .addFormDataPart("googleToken", idToken.toString())
//                    .addFormDataPart("email", email.toString())
//                    .addFormDataPart("fname",givenName.toString())
//                    .addFormDataPart("lname",familyName.toString())
                    .build()

                requestServer.request2(body,"addEmail",{code,fail->
                    stateController.errorStateAUD(fail)
                }
                ){data->
                    userInfo =  MyJson.IgnoreUnknownKeys.decodeFromString(data)
                    uriLogo = null
                    stateController.successStateAUD("تمت   بنجاح")
                }

                // التعامل مع بيانات المستخدم هنا

            } catch (e: GetCredentialException) {

                stateController.errorStateAUD(e.message.toString())

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
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                val email = account.email
                val name = account.displayName

                Log.d("GoogleSignIn", "idToken: $idToken")
                Log.d("GoogleSignIn", "email: $email")

                val body = builderFormWithAccessToken()
                    .addFormDataPart("googleToken", idToken.toString())
//                    .addFormDataPart("email", email.toString())
//                    .addFormDataPart("fname",givenName.toString())
//                    .addFormDataPart("lname",familyName.toString())
                    .build()

                requestServer.request2(body,"addEmail",{code,fail->
                    stateController.errorStateAUD(fail)
                }
                ){data->
                    userInfo =  MyJson.IgnoreUnknownKeys.decodeFromString(data)
                    uriLogo = null
                    stateController.successStateAUD("تمت   بنجاح")
                }


                // إرسال idToken إلى السيرفر هنا
            } catch (e: ApiException) {
                stateController.errorStateAUD(e.message.toString())
                Log.e("GoogleSignIn", "فشل تسجيل الدخول", e)
            }

        }
    }
}
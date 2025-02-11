package com.fekraplatform.storemanger.shared

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.fekraplatform.storemanger.activities.StoresActivity
import com.fekraplatform.storemanger.activities.LoginActivity
import com.fekraplatform.storemanger.application.MyApplication
import com.fekraplatform.storemanger.models.ErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class RequestServer(private val activity: ComponentActivity) {
    val serverConfig = ServerConfig()

    fun request(body: RequestBody, url:String, onFail:(code:Int, fail:String)->Unit, onSuccess:(data:String)->Unit,) {
        activity.lifecycleScope.launch {
            withContext(Dispatchers.IO){
                val okHttpClient = createOkHttpClientWithCustomCert()
                try {
                    if (!isInternetAvailable()) {
                        onFail(0, "لايوجد اتصال بالانترنت")
                    }
                    else{

                        val request = Request.Builder()
                            .url(url)
                            .post(body)
                            .build()
                        val response = okHttpClient.newCall(request).execute()
                        val data = response.body!!.string()
                        Log.e("dataaUrl",url)
                        println(data)
                        Log.e("dataa",data)


                        when(response.code){
                            200->{
                                if (MyJson.isJson(data)){
                                    onSuccess(data)
                                }
                                else{
                                    onFail(response.code,"E 10NJ") //not json
                                    Log.e("daattt",response.body.toString())
                                }
                            }
//                            400->{
//
//                            }
                            else->{
                                if (MyJson.isJson(data)){
                                    val message = MyJson.IgnoreUnknownKeys.decodeFromString<ErrorMessage>(data)
                                    onFail(response.code,message.message)
                                }else{
                                    onFail(response.code,response.code.toString())
                                }

                            }
                        }
                    }



                } catch (e:Exception){
//                onFail(0,e.message.toString())
                    val errorMessage = when (e) {
                        is java.net.SocketTimeoutException -> "Request timed out"
                        is java.net.UnknownHostException -> "Unable to resolve host"
                        is java.net.ConnectException -> "Failed to connect to server"
                        else -> e.message ?: "Unknown error occurred"
                    }
                    onFail(0, "Request failed: $errorMessage")
//                    Log.e("request2", "Exception: ", e)
                }
                finally {
                    okHttpClient.connectionPool.evictAll()
                }
            }
        }
    }
    fun request2(body: RequestBody,urlPostfix:String,onFail:(code:Int, fail:String)->Unit, onSuccess:(data:String)->Unit,) {
        if (!isInternetAvailable()) {
            onFail(0, "لايوجد اتصال بالانترنت")
        } else {
            mainRequest(urlPostfix, body, onSuccess, onFail)
        }
    }

    private fun mainRequest(
        urlPostfix: String,
        body: RequestBody,
        onSuccess: (data: String) -> Unit,
        onFail: (code: Int, fail: String) -> Unit
    ) {
        activity.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val okHttpClient = createOkHttpClientWithCustomCert()
                try {
                    val finalUrl =
                        "${serverConfig.getRemoteConfig().BASE_URL}${serverConfig.getRemoteConfig().VERSION}/${serverConfig.getRemoteConfig().TYPE}/${urlPostfix}"

                    val request = Request.Builder()
                        .url(finalUrl)
                        .post(body)
                        .build()
                    val response = okHttpClient.newCall(request).execute()
                    val data = response.body!!.string()
                    Log.e("dataaUrl", finalUrl)
                    println(data)
                    Log.e("dataa", data)


                    when (response.code) {
                        200 -> {
                            if (MyJson.isJson(data)) {
                                onSuccess(data)
                            } else {
                                onFail(response.code, "E 10NJ S") //not json
                                Log.e("daattt", response.body.toString())
                            }
                        }
                        else -> {
                            if (MyJson.isJson(data)) {
                                val respone =
                                    MyJson.IgnoreUnknownKeys.decodeFromString<ErrorMessage>(data)


                                if (respone.code == 1000){//refresh access token

                                    refreshToken{code, fail ->
                                        onFail(code,fail)
                                    }
                                }
                               else if (respone.code == 2000){//invalid access token
                                    AToken().setAccessToken("")
                                    gotoLogin()
                                }
                                else{
                                    onFail(response.code, respone.message)
                                }
                            } else {
                                onFail(response.code, "E 10NJ E")
                            }
                        }
                    }
                } catch (e: Exception) {
                    val errorMessage = when (e) {
                        is SocketTimeoutException -> "Request timed out"
                        is UnknownHostException -> "Unable to resolve host"
                        is ConnectException -> "Failed to connect to server"
                        else -> e.message ?: "Unknown error occurred"
                    }
                    initVarConfig({
                        onFail(0, "Not Updated Request failed: $errorMessage")
                    }){
                        onFail(0, "Updated Request failed: $errorMessage")
                    }
                } finally {
                    okHttpClient.connectionPool.evictAll()
                }
            }
        }
    }
    fun initVarConfig(onFail:()->Unit, onSuccess: () -> Unit) {
        val remoteConfig = getRemoteConfig()
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val BASE_URL = remoteConfig.getString("BASE_URL")
                    val BASE_IMAGE_URL = remoteConfig.getString("BASE_IMAGE_URL")
                    val SUB_FOLDER_PRODUCT = remoteConfig.getString("SUB_FOLDER_PRODUCT")
                    val TYPE = "storeManager"
                    val SUB_FOLDER_STORE_LOGOS = remoteConfig.getString("SUB_FOLDER_STORE_LOGOS")
                    val SUB_FOLDER_STORE_COVERS = remoteConfig.getString("SUB_FOLDER_STORE_COVERS")
                    val SUB_FOLDER_USERS_LOGOS = remoteConfig.getString("SUB_FOLDER_USERS_LOGOS")
                    val varRemoteConfig = VarRemoteConfig(
                        BASE_URL = BASE_URL,
                        BASE_IMAGE_URL = BASE_IMAGE_URL,
                        SUB_FOLDER_PRODUCT = SUB_FOLDER_PRODUCT,
                        TYPE = TYPE,
                        SUB_FOLDER_STORE_LOGOS = SUB_FOLDER_STORE_LOGOS,
                        SUB_FOLDER_STORE_COVERS = SUB_FOLDER_STORE_COVERS,
                    )
                    serverConfig.setRemoteConfig(MyJson.IgnoreUnknownKeys.encodeToString(varRemoteConfig))
//                    remoteConfigInRequest = SingletonRemoteConfig.remoteConfig
                    onSuccess()
//                stateController.successStateAUD()
                } else {
//                stateController.errorStateAUD("frc")
                    onFail()
                    Log.e("RemoteConfig", "Failed to fetch remote config", task.exception)
                }
            }
    }


    fun createOkHttpClientWithCustomCert(): OkHttpClient {
//        // Load the certificate from raw resources
//        val certInputStream: InputStream = activity.resources.openRawResource(R.raw.isrgrootx1)
//
//        // Create a CertificateFactory
//        val certificateFactory = CertificateFactory.getInstance("X.509")
//
//        // Generate the certificate
//        val certificate = certificateFactory.generateCertificate(certInputStream) as X509Certificate
//
//        // Create a KeyStore and add the certificate
//        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
//            load(null, null)
//            setCertificateEntry("ca", certificate)
//        }
//
//        // Initialize TrustManagerFactory with the KeyStore
//        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
//        trustManagerFactory.init(keyStore)
//
//        // Create SSLContext with the custom TrustManager
//        val sslContext = SSLContext.getInstance("TLS")
//        sslContext.init(null, trustManagerFactory.trustManagers, null)

        // Build OkHttpClient with the custom SSLContext




        return OkHttpClient.Builder().connectTimeout(2,TimeUnit.MINUTES).readTimeout(2,TimeUnit.MINUTES).writeTimeout(2,TimeUnit.MINUTES)
//            .sslSocketFactory(sslContext.socketFactory, trustAllCertificates)
//            .sslSocketFactory(sslContext.socketFactory, trustManagerFactory.trustManagers.first() as X509TrustManager)
            .build()
    }

    fun isInternetAvailable(): Boolean {
        val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    private fun gotoLogin() {
        val intent =
            Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
       activity.startActivity(intent)
        activity.finish()
    }
    private fun refreshToken( onFail: (code: Int, fail: String) -> Unit) {
//        Toast.makeText(MyApplication.AppContext,"تحديث الجلسة",Toast.LENGTH_SHORT).show()
        val aToken = AToken()
        val body = builderForm2()
            .addFormDataPart("accessToken",aToken.getAccessToken().token)
            .build()
        request2(body,"refreshToken",{code, fail ->
            Log.e("frf","454")
            onFail(code,fail)}){it->
            aToken.setAccessToken(it)
            val intent =
                Intent(activity, StoresActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            activity.finish()
        }

    }

}
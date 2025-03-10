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
import com.fekraplatform.storemanger.activities.RemoteConfigModel
import com.fekraplatform.storemanger.models.ErrorMessage
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class RequestServer(private val activity: ComponentActivity) {
    val serverConfig = ServerConfigStorage()
    fun initVarConfig(onFail:()->Unit, onSuccess: () -> Unit) {
        val remoteConfig = getRemoteConfig()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // 1 hour
            fetchTimeoutInSeconds = 60 // 60 seconds
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val allConfigs = remoteConfig.all
                    // Convert the map to a JSON object
                    val jsonObject = JSONObject()
                    for ((key, value) in allConfigs) {
                        jsonObject.put(key, value.asString())
                    }

                    val myRemoteConfig = MyJson.IgnoreUnknownKeys.decodeFromString<RemoteConfigModel>(
                        jsonObject.toString()
                    )
                    serverConfig.setRemoteConfig(MyJson.IgnoreUnknownKeys.encodeToString(myRemoteConfig))
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
                    val finalUrl = "${CustomSingleton.remoteConfig.BASE_URL}${CustomSingleton.remoteConfig.VERSION}/${CustomSingleton.remoteConfig.TYPE_STORE_MANAGER}/${urlPostfix}"
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
                                if (respone.errors.isNotEmpty())
                                activity.runOnUiThread {
                                    Toast.makeText(activity, respone.errors.joinToString(separator = ", "), Toast.LENGTH_SHORT).show()
                                }

                                when (respone.code) {
                                    1000 -> {//refresh access token

                                        refreshToken{code, fail ->
                                            onFail(code,fail)
                                        }
                                    }
                                    2000 -> {//invalid access token
                                        AToken().setAccessToken("")
                                        gotoLogin()
                                    }

                                    else -> {
                                        onFail(response.code, respone.message)
                                    }
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

    private fun createOkHttpClientWithCustomCert(): OkHttpClient {
        return OkHttpClient.Builder().connectTimeout(2,TimeUnit.MINUTES).readTimeout(2,TimeUnit.MINUTES).writeTimeout(2,TimeUnit.MINUTES)
            .build()
    }
    private fun isInternetAvailable(): Boolean {
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
    private fun refreshToken(onFail: (code: Int, fail: String) -> Unit) {
        val aToken = AToken()
        val body = builderForm2()
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
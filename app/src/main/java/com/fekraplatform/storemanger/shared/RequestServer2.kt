package com.fekraplatform.storemanger.shared
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.Singlton.FormBuilder
import com.fekraplatform.storemanger.models.ErrorMessage
import com.fekraplatform.storemanger.models.RemoteConfigModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class RequestServer2 @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSession: AppSession,
    private val formBuilder: FormBuilder,
    private val aToken: AToken1,
    private val serverConfig: ServerConfig
) {
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build()
    }

    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    private suspend fun innerRequest(
        body: RequestBody,
        urlPostfix: String,
    ) = withContext(Dispatchers.IO) {
        val okHttpClient = createOkHttpClient()
        try {
        if (!isInternetAvailable()) {
            throw Exception("لا يوجد اتصال بالانترنت")
        }


            val baseUrl = appSession.remoteConfig.BASE_URL
            val version = appSession.remoteConfig.VERSION
            val type = appSession.remoteConfig.TYPE_STORE_MANAGER
            val finalUrl = "$baseUrl$version/$type/$urlPostfix"

            Log.e("uuurl", finalUrl)

            val request = Request.Builder()
                .url(finalUrl)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val data = response.body?.string() ?: throw Exception("Empty response body")

            Log.e("DATA", data)

            when (response.code) {
                200 -> {
                    if (MyJson.isJson(data)) {
                        data
                    } else {
                        throw CustomException(0,"Response is not valid JSON")
                    }
                }

                else -> {
                    if (MyJson.isJson(data)) {
                        val respone = MyJson.IgnoreUnknownKeys.decodeFromString<ErrorMessage>(data)
                        when (respone.code) {
                            1000 -> { // refresh access token
                                // إذا تريد تدعم إعادة المحاولة هنا، يمكنك استدعاء refreshToken suspend أيضاً
                                throw CustomException(1000,"Refresh token required")
                            }

                            2000 -> { // invalid access token
                                aToken.setAccessToken("")
                                throw CustomException(2000,"Invalid access token")
                            }

                            3000, 404 -> {
                                 initVarConfig()
                            }

                            else -> throw CustomException(0,"Error ${response.code}: ${respone.message}")
                        }
                    } else {
                         initVarConfig()
                    }
                }
            }
        } catch (e: Exception) {
            processException(e)
        } finally {
            okHttpClient.connectionPool.evictAll()


        }
    }

    suspend fun request(
        body: MultipartBody.Builder,
        urlPostfix: String,
        withToken:Boolean = true
    ) = withContext(Dispatchers.IO) {
        val okHttpClient = createOkHttpClient()
        try {
            if (!isInternetAvailable()) {
                throw CustomException(0,"لا يوجد اتصال بالانترنت")
            }
            ////
            if (withToken){
                checkToken()
            }


            val mainBody = body
            if (withToken)
                mainBody.addFormDataPart("accessToken",aToken.accessToken.token)



            val baseUrl = appSession.remoteConfig.BASE_URL
            val version = appSession.remoteConfig.VERSION
            val type = appSession.remoteConfig.TYPE_STORE_MANAGER
            val finalUrl = "$baseUrl$version/$type/$urlPostfix"

            Log.e("uuurl", finalUrl)

            val request = Request.Builder()
                .url(finalUrl)
                .post(body.build())
                .build()

            val response = okHttpClient.newCall(request).execute()
            val data = response.body?.string() ?: throw CustomException(0,"Empty response body")

            Log.e("DATA", data)
            Log.e("CODE", response.code.toString())

            when (response.code) {
                200 -> {
                    if (MyJson.isJson(data)) {
                        data
                    } else {
                        throw CustomException(0,"Response is not valid JSON")
                    }
                }

                else -> {
                    if (MyJson.isJson(data)) {
                        val respone = MyJson.IgnoreUnknownKeys.decodeFromString<ErrorMessage>(data)
                        when (respone.code) {
                            1000 -> { // refresh access token
                                val subBody = body
                                refreshToken()
                                subBody.addFormDataPart("accessToken",aToken.accessToken.token)
                                innerRequest(subBody.build(),urlPostfix)
//                                // إذا تريد تدعم إعادة المحاولة هنا، يمكنك استدعاء refreshToken suspend أيضاً
//                                throw CustomException(1000,"Refresh token required")
                            }

                            2000 -> { // invalid access token
                                aToken.setAccessToken("")
//                                gotoLogin()
                                throw CustomException(2000,"Invalid access token")
                            }

                            3000, 404 -> {
                                initVarConfig("Request failed with unknown errors")
                            }

                            else -> throw CustomException(0,"Error ${response.code}: ${respone.message}")
                        }
                    } else {
                        Log.e("START INI","rtrt")
                       initVarConfig("Request failed with unknown error")
                        Log.e("START INI","rtrt")
                    }
                }
            }
        } catch (e: Exception) {
            processException(e)

        } finally {
            okHttpClient.connectionPool.evictAll()
        }
    }

    private suspend fun processException(e: Exception): Any {
        Log.e("EXX", e.message.toString())
        Log.e("EXX", e.toString())
        val errorMessage = when (e) {
            is SocketTimeoutException -> "Request timed out"
            is UnknownHostException -> "Unable to resolve host"
            is ConnectException -> "تأكد من توفر الانترنت"
            else -> e.message ?: "Unknown error occurred"
        }
        return if (e is CustomException) {
            throw e
        } else {
            initVarConfig(errorMessage)
        }
    }
//    private suspend fun checkToken() = withContext(Dispatchers.IO) {
//        try {
//            val token = aToken.accessToken
//
//            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
////            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
//
//            val expireDate: Date = dateFormat.parse(token.expireAt)
//                ?: throw CustomException(2001,"Invalid expireAt format")
//            Log.e("Stored Token Time",token.expireAt)
//            Log.e("Formatted Stored Token Time",expireDate.toString())
//
//
//
//            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
//            val now = Date()
//            Log.e("Local Time String",now.toString())
//            val utcFormatted = dateFormat.format(now)
//            Log.e("UTC Formatted Local Time String", utcFormatted)
//
//            val nowDate: Date = dateFormat.parse(utcFormatted)
//                ?: throw CustomException(2001,"Invalid nowDate format")
//
//            Log.e("UTC2 Formatted Local Time String", utcFormatted)
//
//            if (nowDate.before(expireDate)) {
//                Log.e("Token", "Still valid: ${token.token}")
////                token.token
//            } else {
//               refreshToken()
//            }
//        } catch (e: Exception) {
//            Log.e("TokenCheck", "Failed: ${e.message}")
//            throw e
//        }
//    }

    private suspend fun checkToken() = withContext(Dispatchers.IO) {
        try {
            val token = aToken.accessToken

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

            // Parse expireAt as UTC
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val expireDate: Date = dateFormat.parse(token.expireAt)
                ?: throw CustomException(2001, "Invalid expireAt format")

            Log.e("Stored Token Time", token.expireAt)
            Log.e("Parsed Expire Time (UTC)", expireDate.toString())

            // Get device timezone
            val deviceZone = TimeZone.getDefault()
            val now = Date()

            val nowDate: Date = if (deviceZone.id == "UTC") {
                // Already UTC, use directly
                now
            } else {
                // Convert to UTC
                val utcFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                utcFormat.timeZone = TimeZone.getTimeZone("UTC")
                val utcString = utcFormat.format(now)
                utcFormat.parse(utcString)
                    ?: throw CustomException(2001, "Invalid nowDate format")
            }

            Log.e("Current Time UTC", nowDate.toString())

            if (nowDate.before(expireDate)) {
                Log.e("Token", "Still valid: ${token.token}")
            } else {
                refreshToken()
            }
        } catch (e: Exception) {
            Log.e("TokenCheck", "Failed: ${e.message}")
            throw e
        }
    }


    private suspend fun refreshToken() = withContext(Dispatchers.IO) {
        try {
            val body = formBuilder.builderFormWithAccessToken().build()
            val token = innerRequest(body, "refreshToken")
            aToken.setAccessToken(token as String)
        } catch (e: CustomException) {
            Log.e("RefreshToken", "Error refreshing token:${e.code} ${e.message}")
            // يمكنك هنا اختيار إما رمي الخطأ أو التعامل معه بهدوء
            throw e
        }
    }
    suspend fun initVarConfig(error:String? = null) = withContext(Dispatchers.IO) {
        if (!isInternetAvailable()) {
            throw CustomException(0,"No Internet")
        }

        val remoteConfig = Firebase.remoteConfig
//        val configSettings = remoteConfigSettings {
//            minimumFetchIntervalInSeconds = 5
//            fetchTimeoutInSeconds = 60
//        }
//        remoteConfig.setConfigSettingsAsync(configSettings).await()

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0 // لتجربة التحديث دائمًا
        }
        remoteConfig.setConfigSettingsAsync(configSettings).await()

         try {
            val activated = remoteConfig.fetchAndActivate().await()
             Log.e("RemoteConfig", "activated = $activated")
             for ((key, value) in remoteConfig.all) {
                 Log.e("RC", "$key = ${value.asString()}")
             }

                val allConfigs = remoteConfig.all
                val jsonObject = JSONObject()
                for ((key, value) in allConfigs) {
                    jsonObject.put(key, value.asString())
                }

                val myRemoteConfig = MyJson.IgnoreUnknownKeys.decodeFromString<RemoteConfigModel>(
                    jsonObject.toString()
                )
                serverConfig.setRemoteConfig(myRemoteConfig) // دالة suspend
                appSession.remoteConfig = myRemoteConfig
                if (error != null)
                    throw  CustomException(0, "RC Updated. $error")
//            } else {
//              throw  CustomException(0,"Failed to activate fetched config" + if (error != null) " $error" else "" )
//
//            }
        } catch (e: Exception) {
             throw CustomException(0,e.message.toString() + if (error != null) " $error" else "")
        }
    }
}


class CustomException(
    val code: Int ,
    override val message: String
) : Exception(message)

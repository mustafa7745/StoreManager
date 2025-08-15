package com.fekraplatform.storemanger.shared
import android.content.Context
import android.util.Log
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.activities.getCurrentDate
import com.fekraplatform.storemanger.models.RemoteConfigModel
import com.fekraplatform.storemanger.storage.StorageDataStore
import com.fekraplatform.storemanger.storage.TinkEncryptor
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

@Singleton
class  ServerConfig @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSession: AppSession
)
{
    private val aead by lazy { TinkEncryptor.getAead(context) }
    private val storageDataStore = StorageDataStore(context);
    private val remoteConfigKey = "rc"
    private val appTokenKey = "appToken"
    private val subscribeAppKey = "sa"
    private val dateKey = "dateKey"
    ////
//    lateinit var remoteConfig: RemoteConfigModel


    suspend fun setRemoteConfig(data: RemoteConfigModel?){
        if (data == null){
            storageDataStore.setData(dateKey, getCurrentDate().toString())
            storageDataStore.setData(remoteConfigKey, "")
            return
        }

        // 🔐 تشفير BASE_URL فقط
        val encryptedBaseUrl = TinkEncryptor.encrypt(aead, data.BASE_URL)
        val encryptedBaseUrlBase64 = android.util.Base64.encodeToString(encryptedBaseUrl, android.util.Base64.DEFAULT)

        val encryptedBaseImageUrl = TinkEncryptor.encrypt(aead, data.BASE_IMAGE_URL)
        val encryptedBaseImageUrlBase64 = android.util.Base64.encodeToString(encryptedBaseImageUrl, android.util.Base64.DEFAULT)

        // 📦 إنشاء نسخة جديدة من RemoteConfigModel مع BASE_URL مشفّر
        val withEncryptedUrl = data.copy(BASE_URL = encryptedBaseUrlBase64 , BASE_IMAGE_URL = encryptedBaseImageUrlBase64)

        val stringData = MyJson.IgnoreUnknownKeys.encodeToString(withEncryptedUrl)
        storageDataStore.setData(dateKey, getCurrentDate().toString())
        storageDataStore.setData(remoteConfigKey, stringData)
    }

    suspend fun getRemoteConfig(): RemoteConfigModel {
        return MyJson.IgnoreUnknownKeys.decodeFromString(storageDataStore.getData(remoteConfigKey))
    }

    suspend fun isSetRemoteConfig():Boolean = withContext(Dispatchers.IO){
         try {
            val data =  getRemoteConfig()
            Log.e("encRemoteConfig",data.toString())
            val encryptedBytes1 = android.util.Base64.decode(data.BASE_URL, android.util.Base64.DEFAULT)
            val baseUrl =  TinkEncryptor.decrypt(aead, encryptedBytes1)

            val encryptedBytes2 = android.util.Base64.decode(data.BASE_IMAGE_URL, android.util.Base64.DEFAULT)
            val baseImageUrl =  TinkEncryptor.decrypt(aead, encryptedBytes2)
            appSession.remoteConfig = data.copy(BASE_URL = baseUrl, BASE_IMAGE_URL = baseImageUrl)
            Log.e("remoteConfig",appSession.remoteConfig.toString())
             true
        }catch (e:Exception){
            Log.e("ff","sdds22")
            setRemoteConfig(null)
            false
        }
    }

    suspend fun setAppToken(data:String){
        storageDataStore.setData(appTokenKey, data)
    }

    suspend fun getAppToken(): String {
        return storageDataStore.getData(appTokenKey)
    }

    suspend fun isSetAppToken():Boolean{
        return try {
            return getAppToken().isNotEmpty() && getAppToken() != ""
        }catch (e:Exception){
            setAppToken("")
            false
        }
    }

    //
    private suspend fun getSubscribeApp():String{
        return storageDataStore.getData(subscribeAppKey)
    }
    suspend fun setSubscribeApp(data:String){
        storageDataStore.setData(subscribeAppKey, data)
    }
    suspend fun isSetSubscribeApp(): Boolean {
        return getSubscribeApp().isNotEmpty()
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getFcmTokenSuspend(): String = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cont.resume(task.result, null)
                    appSession.appToken = task.result
                    Log.e("Success App Token ",task.result)
                } else {

                    cont.resumeWithException(task.exception ?: Exception("Unknown error"))
                    Log.e("Error App Token ", task.exception?.message.toString())
                }
            }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun subscribeToTopicSuspend(topic: String): Boolean = suspendCancellableCoroutine { cont ->
        Firebase.messaging.subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                cont.resume(task.isSuccessful, null)
                Log.e("App Subscribe ","Done")
            }
    }

}

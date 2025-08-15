package com.fekraplatform.storemanger.shared
import android.content.Context
import android.util.Log
import com.fekraplatform.storemanger.models.AccessToken
import com.fekraplatform.storemanger.storage.SecureDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AToken1 @Inject constructor(
    @ApplicationContext private val context: Context
)
{
    private val secureStorage = SecureDataStore(context)
    private val accessTokenKey = "token"
    private val appIdKey = "app_id"
    lateinit var accessToken: AccessToken

    suspend fun setAccessToken(data: String) {
        withContext(Dispatchers.IO) {
            secureStorage.setSecureString(accessTokenKey, data)
            val at = getAccessToken1()
            if(at != null){
                accessToken = at
            }
        }
    }

    private suspend fun getAccessToken1(): AccessToken? {
        return withContext(Dispatchers.IO) {
            try {
                val json = secureStorage.getSecureString(accessTokenKey)
                if (json.isNotEmpty()) {
                    MyJson.IgnoreUnknownKeys.decodeFromString(json)
                } else null
            } catch (e: Exception) {
                Log.e("AToken2", "Failed to decode token", e)
                null
            }
        }
    }
    suspend fun getAccessToken(): AccessToken {
        return accessToken
    }

    suspend fun isSetAccessToken(): Boolean {
        val at = getAccessToken1()
         if(at != null){
             accessToken = at
            return true
        }
        return false
    }

    suspend fun setAppId(data: String) {
        withContext(Dispatchers.IO) {
            secureStorage.setSecureString(appIdKey, data)
        }
    }

    suspend fun getAppId(): String {
        return withContext(Dispatchers.IO) {
            secureStorage.getSecureString(appIdKey)
        }
    }

    suspend fun isSetAppId(): Boolean {
        return getAppId().isNotEmpty()
    }
}

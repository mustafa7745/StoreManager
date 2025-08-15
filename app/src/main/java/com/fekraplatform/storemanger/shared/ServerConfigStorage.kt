package com.fekraplatform.storemanger.shared

import GetStorage
import com.fekraplatform.storemanger.activities.getCurrentDate
import com.fekraplatform.storemanger.models.RemoteConfigModel
import java.time.LocalDateTime

class ServerConfigStorage {
    private val inventory = "config"
    private val getStorage = GetStorage(inventory);
    private val remoteConfig = "rc"
    private val subscribeApp = "sa"
    private val appToken = "appToken"
    private val dateKey = "dateKey"

    fun setRemoteConfig(data:String){
        getStorage.setData(remoteConfig, data)
        getStorage.setData(dateKey, getCurrentDate().toString())
    }
    fun getDate(): LocalDateTime? {
        return (LocalDateTime.parse(getStorage.getData(dateKey)))
    }
    fun getRemoteConfig(): RemoteConfigModel {
        return MyJson.IgnoreUnknownKeys.decodeFromString(getStorage.getData(remoteConfig))
    }
    fun isSetRemoteConfig():Boolean{
        return try {
            getRemoteConfig()
            true
//            val diff =
//                Duration.between(getDate(), getCurrentDate()).toMinutes()
//            diff <= 1
        }catch (e:Exception){
            setRemoteConfig("")
            false
        }
    }


    fun setAppToken(data:String){
        getStorage.setData(appToken, data)
    }

    fun getAppToken(): String {
        return getStorage.getData(appToken)
    }

    fun isSetAppToken():Boolean{
        return try {
            return getAppToken().isNotEmpty() && getAppToken() != ""
        }catch (e:Exception){
            setAppToken("")
            false
        }
    }

    //
    fun getSubscribeApp():String{
        return getStorage.getData(subscribeApp)
    }
    fun setSubscribeApp(data:String){
        getStorage.setData(subscribeApp, data)
    }
    fun isSetSubscribeApp(): Boolean {
        return getSubscribeApp().isNotEmpty()
    }
}
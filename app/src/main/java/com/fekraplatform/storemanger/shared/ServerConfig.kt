package com.fekraplatform.storemanger.shared

import GetStorage
import com.fekraplatform.storemanger.models.AccessToken

class ServerConfig {
    private val inventory = "config"
    private val getStorage = GetStorage(inventory);
    private val remoteConfig = "rc"
    private val subscribeApp = "sa"

    fun setRemoteConfig(data:String){
        getStorage.setData(remoteConfig, data)
    }
    fun getRemoteConfig(): VarRemoteConfig {
        return MyJson.IgnoreUnknownKeys.decodeFromString(getStorage.getData(remoteConfig))
    }
    fun isSetRemoteConfig():Boolean{
        return try {
            getRemoteConfig()
            true
        }catch (e:Exception){
            setRemoteConfig("")
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
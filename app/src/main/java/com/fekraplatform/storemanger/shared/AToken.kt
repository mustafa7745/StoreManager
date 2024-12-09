package com.fekraplatform.storemanger.shared

import GetStorage
import com.fekraplatform.storemanger.models.AccessToken

class AToken {
    private val inventory = "at"
    private val getStorage = GetStorage(inventory);
    private val Accesstoken = "at123"
    private val appId = "aid"

    fun setAccessToken(data:String){
        getStorage.setData(Accesstoken, data)
    }
    fun getAccessToken(): AccessToken {
        return MyJson.IgnoreUnknownKeys.decodeFromString(getStorage.getData(Accesstoken))
    }
    fun isSetAccessToken():Boolean{
        return try {
            getAccessToken()
            true
        }catch (e:Exception){
            setAccessToken("")
            false
        }
    }

    fun getAppId():String{
        return getStorage.getData(appId)
    }
    fun setAppId(data:String){
        getStorage.setData(appId, data)
    }
    fun isSetAppId(): Boolean {
        return getAppId().isNotEmpty()
    }
}
package com.fekraplatform.storemanger.storage

import GetStorage
import com.fekraplatform.storemanger.activities.getCurrentDate
import com.fekraplatform.storemanger.models.Home
import com.fekraplatform.storemanger.shared.MyJson
import java.time.LocalDateTime

class HomeStorage {
    private val getStorage = GetStorage("home")
    private val homeComponentKey = "home"
    private val dateKey = "dateKey"

    fun isSetHome(storeId:String):Boolean{
        return try {
//            Log.e("gtgt",getHome().toString())
            getHome(storeId)
            true
        }catch (e:Exception){
            setHome("",storeId)
            false
        }
    }
    fun setHome(data:String,storeId:String){
        getStorage.setData(dateKey+storeId, getCurrentDate().toString())
        getStorage.setData(homeComponentKey+storeId,data)
    }

    fun getDate(storeId:String): LocalDateTime? {
       return (LocalDateTime.parse(getStorage.getData(dateKey+storeId)))
    }
    fun getHome(storeId:String):Home{
       return MyJson.IgnoreUnknownKeys.decodeFromString(getStorage.getData(homeComponentKey+storeId))
    }
}
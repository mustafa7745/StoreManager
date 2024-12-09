package com.fekraplatform.storemanger.storage

import GetStorage
import android.util.Log
import com.fekraplatform.storemanger.getCurrentDate
import com.fekraplatform.storemanger.models.Home
import com.fekraplatform.storemanger.shared.MyJson
import java.time.LocalDateTime

class HomeStorage {
    private val getStorage = GetStorage("home")
    private val homeComponentKey = "home"
    private val dateKey = "dateKey"

    fun isSetHome():Boolean{
        return try {
//            Log.e("gtgt",getHome().toString())
            getHome()
            true
        }catch (e:Exception){
            setHome("")
            false
        }
    }
    fun setHome(data:String){
        getStorage.setData(dateKey, getCurrentDate().toString())
        getStorage.setData(homeComponentKey,data)
    }

    fun getDate(): LocalDateTime? {
       return (LocalDateTime.parse(getStorage.getData(dateKey)))
    }
    fun getHome():Home{
       return MyJson.IgnoreUnknownKeys.decodeFromString(getStorage.getData(homeComponentKey))
    }
}
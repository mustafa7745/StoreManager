package com.fekraplatform.storemanger.storage

import GetStorage
import com.fekraplatform.storemanger.shared.MyJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

class GoogleBillingStorage {
    private val getStorage = GetStorage("ggbing")
    private val homeComponentKey = "main"

    fun setMyBillings(data:List<MyBilling>){
        getStorage.setData(homeComponentKey,MyJson.MyJson.encodeToString(data))
    }

    fun getMyBillings():List<MyBilling>{
        return try {
             MyJson.IgnoreUnknownKeys.decodeFromString(getStorage.getData(homeComponentKey))
        }catch (e:Exception){
         emptyList()
        }
    }
}

@Serializable
data class MyBilling(val isPending:Boolean,val name:String,val productId: String,val purchaseToken:String)
//@Serializable
//data class MyBillingPurchase(val token:String, val status:Int)
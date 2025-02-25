package com.fekraplatform.storemanger.storage

import GetStorage
import com.fekraplatform.storemanger.activities.Language
import com.fekraplatform.storemanger.shared.MyJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.util.Locale

class MyAppStorage {
    private val getStorage = GetStorage("appstorage")
    private val languageKey = "lang"

    fun setLang(data:Language){
        getStorage.setData(languageKey,MyJson.MyJson.encodeToString(data))
    }

    fun getLang():Language{
        return try {
             MyJson.IgnoreUnknownKeys.decodeFromString(getStorage.getData(languageKey))
        }catch (e:Exception){
         val s = Language("",Locale.getDefault().language)
            setLang(s)
            s
        }
    }
}
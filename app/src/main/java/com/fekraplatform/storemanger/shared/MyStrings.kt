package com.fekraplatform.storemanger.shared

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

object MyStrings {
//    val LOGIN = JsonObject()
//        listOf(MyTranslate("تسجيل الدخول","ar"),MyTranslate("تسجيل الدخول","ar"))
}

data class MyTranslate(val name:String,val language:String)
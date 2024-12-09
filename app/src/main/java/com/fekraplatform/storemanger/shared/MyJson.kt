package com.fekraplatform.storemanger.shared
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MyJson {
    companion object{
        val json = Json
        val IgnoreUnknownKeys = Json {  ignoreUnknownKeys = true

        }
        val IgnoreUnknownKeysAndNull = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val MyJson = Json
        fun isJson(str: String): Boolean {
            try {
                JSONObject(str)
            } catch (ex: JSONException) {
                try {
                    JSONArray(str)
                } catch (ex1: JSONException) {
                    return false
                }
            }
            return true
            }
        }

}
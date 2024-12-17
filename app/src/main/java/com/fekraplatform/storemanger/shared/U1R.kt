package com.fekraplatform.storemanger.shared

import kotlinx.serialization.Serializable

class U1R {
    companion object{
        val BASE_URL = "https://user2121.greenland-rest.com/api/"
        val BASE_IMAGE_URL = "https://yemen-apps.s3.ap-southeast-2.amazonaws.com/"
        val SUB_FOLDER_PRODUCT = "products/"
        val VERSION = "v1"
        val TYPE = "storeManager"
    }
}

@Serializable
class VarRemoteConfig(
    var BASE_URL:String,
    var BASE_IMAGE_URL:String,
    var SUB_FOLDER_PRODUCT:String,
    var SUB_FOLDER_STORE_COVERS:String,
    var SUB_FOLDER_STORE_LOGOS:String,
    var TYPE :String,
    val VERSION:String = "v1",
)
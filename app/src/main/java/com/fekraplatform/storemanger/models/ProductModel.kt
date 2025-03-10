package com.fekraplatform.storemanger.models

import kotlinx.serialization.Serializable

@Serializable
data class ErrorMessage(
    val message: String,
    val code:Int,
    val errors:List<String>
)

@Serializable
data class AccessToken(
    val token: String,
    val expireAt:String
)

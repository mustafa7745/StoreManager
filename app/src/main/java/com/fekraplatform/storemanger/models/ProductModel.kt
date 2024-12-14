package com.fekraplatform.storemanger.models

import kotlinx.serialization.Serializable

@Serializable
data class ErrorMessage(
    val message: String,
    val code:Int
)

@Serializable
data class AccessToken(
    val token: String,
    val expireAt:String
)

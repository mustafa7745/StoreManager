package com.fekraplatform.storemanger.models

import kotlinx.serialization.Serializable


@Serializable
data class StoreCategory(
    val storeCategoryId: Int,
    val categoryId: Int,
    val categoryName: String
)

@Serializable
data class StoreProduct(
    val storeProductId:Int,
//    val storeCategoryId:Int,
    val productId: Int,
    val productName: String,
    val productDescription: String?,
    val options: List<ProductOption>,
    val images: List<ProductImage>
)

@Serializable
data class ProductOption(
    val optionId:Int,
    val storeProductId: Int,
    val name: String,
    val price: String
)
@Serializable
data class ProductImage(
    val id : Int,
    val image: String
)
@Serializable
data class Option(
    val id : Int,
    val name: String
)
@Serializable
data class StoreProducts(val storeProducts: List<StoreProduct>)

@Serializable
data class MyProduct(
    val id:Int,
    val name: String,
    val description: String?,
    val images: List<ProductImage>
)

@Serializable
data class MyCategory(
    val id: Int,
    val name: String
)

@Serializable
data class Product(
    val id:Int,
    val name: String,
)

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

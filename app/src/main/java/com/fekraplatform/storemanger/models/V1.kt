package com.fekraplatform.storemanger.models

import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id: Int,
    val typeId: Int,
    val name: String,
    var storeConfig:StoreConfig?
)

@Serializable
data class Category(
    val id: Int,
    val name: String
)

@Serializable
data class Section(
    val id: Int,
    val name: String
)
@Serializable
data class NestedSection(
    val id: Int,
    val name: String
)

@Serializable
data class Option(
    val id : Int,
    val name: String
)
@Serializable
data class Product(
    val id:Int,
    val name: String,
    val description: String?,
    val images: List<ProductImage>
)

@Serializable
data class ProductToSelect(
    val id:Int,
    val name: String
)
////////
@Serializable
data class StoreCategory(
    val id:Int,
    val categoryId: Int,
    val categoryName: String
)




@Serializable
data class StoreNestedSection(
    val id: Int,
    val storeSectionId: Int,
    val nestedSectionId: Int,
    val nestedSectionName: String
)





@Serializable
data class StoreSection(
    val id: Int,
    val sectionName: String,
    val sectionId: Int,
    val storeCategoryId: Int,
)

@Serializable
data class Home(
    val storeCategories: List<StoreCategory>,
    val storeSections:List<StoreSection>,
    val storeNestedSections:List<StoreNestedSection>
)

@Serializable
data class StoreConfig(
    val categories: List<Int>,
    val sections: List<Int>,
    val nestedSections: List<Int>,
    val products: List<Int>,
    val storeIdReference :Int
)


@Serializable
data class StoreProduct(
    val productId:Int,
    val storeNestedSectionId: Int,
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

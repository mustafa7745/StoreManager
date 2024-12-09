package com.fekraplatform.storemanger.models

import kotlinx.serialization.Serializable


@Serializable
data class Category(
    val id: Int,
    val name: String
)
@Serializable
data class StoreCategory1(
    val id:Int,
    val categoryId: Int,
    val categoryName: String
)
@Serializable
data class Section(
    val id: Int,
    val name: String
)

@Serializable
data class SectionStoreCategory(
    val id: Int,
    val sectionId: Int,
    val sectionName: String
)


@Serializable
data class CsPsSCR(
    val id: Int,
    val category3Id: Int,
    val category3Name: String
)

@Serializable
data class Category3(
    val id: Int,
    val name: String
)

@Serializable
data class Store(
    val id: Int,
    val typeId: Int,
    val name: String,
    var storeConfig:StoreConfig?
)

@Serializable
data class StoreCategorySection(
    val id: Int,
    val sectionName: String,
    val sectionId: Int,
    val storeCategoryId: Int,
)
@Serializable
data class Scp(
    val id: Int,
    val name: String,
    val storeCategorySectionId:Int,
    val category3Id: Int
)


@Serializable
data class Home(
    val storeCategories: List<StoreCategory1>,
    val storeCategoriesSections:List<StoreCategorySection>,
    val csps:List<Scp>
)

@Serializable
data class StoreConfig(
    val categories: List<Int>,
    val sections: List<Int>,
    val nestedSections: List<Int>,
    val products: List<Int>,
)
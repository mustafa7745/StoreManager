package com.fekraplatform.storemanger.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fekraplatform.storemanger.models.ProductView
import com.fekraplatform.storemanger.models.Store
import com.fekraplatform.storemanger.models.StoreProduct
import java.time.LocalDateTime

object CustomSingleton {
    lateinit var remoteConfig: VarRemoteConfig
    fun getStoreLogo(): String {
        return remoteConfig.BASE_IMAGE_URL + remoteConfig.SUB_FOLDER_STORE_LOGOS + selectedStore!!.logo
    }
    fun getStoreCover(): String {
        return remoteConfig.BASE_IMAGE_URL + remoteConfig.SUB_FOLDER_STORE_COVERS + selectedStore!!.cover
    }
    var stores by mutableStateOf<List<Store>>(emptyList())
    var selectedStore by mutableStateOf<Store?>(null)
    fun isSharedStore():Boolean{
        return selectedStore!!.typeId == 1
    }
    fun getCustomStoreId(): Int {
        return if (selectedStore!!.storeConfig != null) selectedStore!!.storeConfig!!.storeIdReference else selectedStore!!.id
    }
    var storedProducts:List<StoredProducts> = emptyList()

}

data class StoredProducts(
    val storeId:Int,
    val storeNestedSectionId: Int,
    val productViews:List<ProductView>,
    val storeAt: LocalDateTime
)
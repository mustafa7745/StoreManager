package com.fekraplatform.storemanger.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fekraplatform.storemanger.models.CustomPrice
import com.fekraplatform.storemanger.models.ProductView
import com.fekraplatform.storemanger.models.RemoteConfigModel
import com.fekraplatform.storemanger.models.Store
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

object CustomSingleton {
    lateinit var remoteConfig: RemoteConfigModel
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
    fun getOriginalStoreId(): Int {
        return  selectedStore!!.id
    }
    var storedProducts:List<StoredProducts> = emptyList()
    var storedCustomPrices:List<StoredCustomPrice> = emptyList()

    fun isPremiumStore(): Boolean {
        return selectedStore!!.subscription.isPremium == 1
    }
    var location: LatLng? = null
}

object Situations {
    const val VIEWED = 2
    const val NEW = 1
    const val COMPLETED = 10000
    const val CANCELED = 9999
    const val ASSIGN_DELIVERY_MAN = 3
    const val PREPARING = 5
    const val SYSTEM_ORDER_READY = 4
    const val AT_WAY = 7
}

data class StoredProducts(
    val storeId:Int,
    val storeNestedSectionId: Int,
    val productViews:List<ProductView>,
    val storeAt: LocalDateTime
)

data class StoredCustomPrice(
    val storeId:Int,
    val customPrices:List<CustomPrice>,
    val storeAt: LocalDateTime
)
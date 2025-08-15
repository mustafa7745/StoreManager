package com.fekraplatform.storemanger.Singlton
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fekraplatform.storemanger.activities.Language
import com.fekraplatform.storemanger.models.Coupon
import com.fekraplatform.storemanger.models.Home
import com.fekraplatform.storemanger.models.HomeProduct
import com.fekraplatform.storemanger.models.HomeStoreProduct
import com.fekraplatform.storemanger.models.OrderComponent
import com.fekraplatform.storemanger.models.OrdersHome
import com.fekraplatform.storemanger.models.RemoteConfigModel
import com.fekraplatform.storemanger.models.Store
import com.fekraplatform.storemanger.models.StoreCurrency
import com.fekraplatform.storemanger.models.StoreOrders
import com.fekraplatform.storemanger.shared.AToken1
import com.fekraplatform.storemanger.shared.AppInfoMethod
import com.fekraplatform.storemanger.shared.CustomSingleton
import com.fekraplatform.storemanger.shared.ServerConfig
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MySessionEntryPoint {
    fun getAppSession(): AppSession
}

@Singleton
class AppSession @Inject constructor() {
    lateinit var selectedStore: Store
    var myStore by mutableStateOf<Store?>(null)
    lateinit var remoteConfig: RemoteConfigModel
    var appToken = "101"
    var location: LatLng? = null
    lateinit var selectedLanguageCode:Language
    var isEditMode by mutableStateOf(false)
    ////
    var stores   by  mutableStateOf<List<Store>>(emptyList())
    ////
    var categories   by  mutableStateOf<List<Int>>(emptyList())
    var sections   by  mutableStateOf<List<Int>>(emptyList())
    var nestedSection   by  mutableStateOf<List<Int>>(emptyList())
    var products   by  mutableStateOf<List<Int>>(emptyList())

    var storeOrders by mutableStateOf<StoreOrders?>(null)
    var storeOrders2 by mutableStateOf<OrdersHome?>(null)
    var coupons by mutableStateOf<List<Coupon>>(listOf())

    var orderComponent by mutableStateOf<OrderComponent?>(null)

    fun setStoreAndUpdateStores(store: Store) {
        selectedStore = store
        val updatedStores = stores.map {
            if (it.id == selectedStore.id)
                selectedStore
            else
                it
        }
        stores = updatedStores
    }
    fun isSharedStore(): Boolean {
        return selectedStore.typeId == 1
    }
    fun isPremiumStore(): Boolean {
        return selectedStore.subscription.isPremium == 1
    }
    fun getCustomStoreId(): Int {
        return if (selectedStore.storeConfig != null) selectedStore.storeConfig!!.storeIdReference else selectedStore.id
    }
    //
    val home = mutableStateOf<Home?>(null)
    var homeStoreProduct by mutableStateOf<HomeStoreProduct?>(null)
    var homeProduct by mutableStateOf<HomeProduct?>(null)
    var storeCurrencies by mutableStateOf<List<StoreCurrency>?>(null)
}

@Singleton
class FormBuilder @Inject constructor(
    private val serverConfig: ServerConfig,
    private val remoteConfigRepository: AppSession,
    private val appInfoMethod: AppInfoMethod,
    private val aToken: AToken1,
    private val appSession: AppSession
)
{

    fun sharedBuilderForm(): MultipartBody.Builder {
        val appToken = appSession.appToken
        val remoteConfigVersion = remoteConfigRepository.remoteConfig.REMOTE_CONFIG_VERSION

        Log.e("appToken",appToken)
        Log.e("deviceId",appInfoMethod.getDeviceId().toString())
        return MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("sha",  appInfoMethod.getAppSha())
            .addFormDataPart("packageName", appInfoMethod.getAppPackageName())
            .addFormDataPart("deviceId", appInfoMethod.getDeviceId().toString())
            .addFormDataPart("appToken", appToken)
            .addFormDataPart("remoteConfigVersion", remoteConfigVersion.toString())
    }
    fun sharedBuilderFormWithStoreId(): MultipartBody.Builder {
        return sharedBuilderForm()
            .addFormDataPart("storeId", appSession.selectedStore.id.toString())
    }

    fun loginBuilderForm(): MultipartBody.Builder {
        return sharedBuilderForm()
            .addFormDataPart("model", Build.MODEL)
            .addFormDataPart("version", Build.VERSION.RELEASE)
    }

    fun builderFormWithAccessToken(): MultipartBody.Builder {
        return sharedBuilderForm()
            .addFormDataPart("accessToken", aToken.accessToken.token)
    }

    fun builderFormWithAccessTokenAndStoreId_2(token:String): MultipartBody.Builder {
        return builderFormWithAccessToken2(token)
            .addFormDataPart("storeId", appSession.selectedStore.id.toString())
    }
    fun builderFormWithAccessTokenAndStoreId(): MultipartBody.Builder {
        return builderFormWithAccessToken()
            .addFormDataPart("storeId", appSession.selectedStore.id.toString())
    }
    fun builderFormWithAccessToken2(token: String): MultipartBody.Builder {
        return sharedBuilderForm()
            .addFormDataPart("accessToken", token)
    }

}
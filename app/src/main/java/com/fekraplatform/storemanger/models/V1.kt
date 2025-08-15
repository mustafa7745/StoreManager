package com.fekraplatform.storemanger.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fekraplatform.storemanger.activities1.OrderEntity
import com.fekraplatform.storemanger.activities1.OrderSituationEntity
import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id: Int,
    val typeId: Int,
    val name: String,
    val logo : String,
    val cover : String,
    val timezone : String,
    var latLng:String?,
    val deliveryPrice:Double,
    val storeCurrencies:List<StoreCurrency>,
    val app: App?,
    val subscription: Subscription,
    var storeConfig:StoreConfig?,
    val storeMainCategory: StoreMainCategory
)

@Serializable
data class StoreCurrency(
    val id: Int,
    val currencyId:Int,
    val currencyName:String,
    val lessCartPrice: Double,
    val freeDeliveryPrice:Double,
    val deliveryPrice: Double,
    val isSelected:Int,
    val countUsed: Int,
)

@Serializable
data class App(
    val id: Int,
    val hasServiceAccount:Boolean
)
@Serializable
data class Subscription(
    val id: Int,
    val points: Int,
    val expireAt:String,
    val isPremium:Int
)

@Serializable
data class Order(
    val id: Int,
    val userName: String,
    val situation: String,
    val situationId: Int,
    val createdAt: String,
    val userPhone: String,
    val amounts:List<OrderAmount>
)

//@Serializable
//data class OrderSituation(
//    val id: Int,
//    val name: String,
//    val createdAt: String,
//    val updatedAt: String,
//)
@Serializable
data class OrdersHome(
    val situations: List<OrderSituationEntity>,
    val orders : List<OrderEntity>,
    val pendingIds : List<Int>,
    val completedIds : List<Int>
)

@Serializable
data class StoreDeliveryMan(
    val id: Int,
    val deliveryManId:Int,
    val firstName: String,
    val lastName: String,
    val phone: String,
)

@Serializable
data class Coupon(
    val id: Int,
    val code: String,
    val isActive: Int,
    val type: Int,
    val amount: Double,
    val currencyId: Int,
    val used: Int,
    val countUsed: Int?,

)

@Serializable
data class OrderProduct(
    val id: Int,
    val productName: String,
    val currencyName: String,
    val currencyId: Int,
    val optionName: String,
    val quantity:Int,
    val price:Double,
)

@Serializable
data class OrderDelivery(
    val id: Int,
    val latLng: String,
    val street: String,
    val distance:Int,
    val duration:Int,
    val storeDeliveryMan: StoreDeliveryMan?,
    val deliveryPrice: Double,
    val currencyName:String,
    val createdAt:String,
    val updatedAt:String,
)
@Serializable
data class OrderPayment(
    val id: Int,
    val paymentId: Int,
    val paymentName: String,
    val paymentImage: String,
    val createdAt:String,
    val updatedAt:String,
)

@Serializable
data class OrderDetail(
    val id: Int,
    val storeId: Int,
    val userId: Int,
    val withApp: Int,
    val paid: Int,
    val inStore: Int,
    val systemOrderNumber: String?,
    val situationId: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class OrderStatus(
    val id: Int,
    val situationId: Int,
    val orderId: Int,
    val createdAt: String,
)

@Serializable
data class OrderComponent(
    val orderDelivery: OrderDelivery?,
    val orderProducts: List<OrderProduct>,
    val orderPayment: OrderPayment?,
    val orderDetail: OrderDetail,
    val orderCoupon:Coupon?,
    val orderStatusList:List<OrderStatus>
){
    // Example function to update a product in the list
    fun updateOrderProduct(updatedProduct: OrderProduct): OrderComponent {
        // Replace the product with the same id or add it if it doesn't exist
        val updatedProducts = orderProducts.map { product ->
            if (product.id == updatedProduct.id) updatedProduct else product
        }
        return this.copy(orderProducts = updatedProducts)
    }
    fun updateOrderDetail(orderDetail: OrderDetail): OrderComponent {
        // Replace the product with the same id or add it if it doesn't exist

        return this.copy(orderDetail = orderDetail)
    }
    fun updateOrderStatusList(orderStatusList:List<OrderStatus>): OrderComponent {
        // Replace the product with the same id or add it if it doesn't exist

        return this.copy(orderStatusList = orderStatusList)
    }
    fun updateOrderDelivery(updatedDelivery: OrderDelivery): OrderComponent {
//        // Replace the product with the same id or add it if it doesn't exist
//        val updatedProducts = orderProducts.map { product ->
//            if (product.id == updatedProduct.id) updatedProduct else product
//        }
        return this.copy(orderDelivery = updatedDelivery)
    }


    fun filterProduct(ids: List<Int>): OrderComponent {
        // Filter out the products with ids present in the `ids` list
        val updatedProducts = orderProducts.filterNot { it.id in ids }
        // Return a new OrderComponent with the updated list of products
        return this.copy(orderProducts = updatedProducts)
    }
}



@Serializable
data class OrderAmount(
    val id: Int,
    val currencyId: Int,
    val currencyName: String,
    val amount: String,
)

@Serializable
data class Category(
    val id: Int,
    val name: String,
    val acceptedStatus : Int
)

@Serializable
data class Section(
    val id: Int,
    val name: String,
    val acceptedStatus : Int
)
@Serializable
data class NestedSection(
    val id: Int,
    val name: String,
    val acceptedStatus : Int
)

@Serializable
data class Option(
    val id : Int,
    val name: String
)
@Serializable
data class Product2(
    val id:Int,
    val name: String,
    val acceptedStatus: Int
//    val description: String?,
//    val images: List<ProductImage>
)

@Serializable
data class Product(
    val productId: Int,
    val productViewId:Int,
    val productName: String,
    val productDescription: String?,
    val images: List<ProductImage>
)

@Serializable
data class PrimaryProduct(
    val id: Int,
    val name: String,
    val storeId: Int,
    val description: String?
)

@Serializable
data class PrimaryProductOption(
    val id: Int,
    val storeId: Int,
    val name: String
)

@Serializable
data class PrimaryStoreProduct(
    val id: Int,
    val storeNestedSectionId:Int,
    val name:String,
    val description:String,
    val storeId:Int,
    val productId:Int,
    val currencyId:Int,
    val price:Double,
    val prePrice:Double,
    val likes:Int,
    val stars:Int,
    val reviews:Int,
    val storeProductViewId:Int,
    val orderNo:Int,
    val orderAt:String,
    val info:List<String>,
    val createdAt:String,
    val updatedAt:String
)

@Serializable
data class HomeStoreProduct(
    val products: List<PrimaryProduct>,
    val options: List<PrimaryProductOption>,
    val storeProducts: List<PrimaryStoreProduct>,
    val productsImages: List<ProductImage>
)
@Serializable
data class HomeProduct(
    val products: List<PrimaryProduct>,
    val productsImages: List<ProductImage>
)


//@Serializable
//data class ProductToSelect(
//    val id:Int,
//    val name: String
//)
////////
@Entity(tableName = "store_categories")
@Serializable
data class StoreCategory(
    @PrimaryKey val id:Int,
    val categoryId: Int,
    val categoryName: String,
)
//@Dao
//interface StoreCategoryDao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(storeCategory: StoreCategory)
//
//    @Query("SELECT * FROM store_categories")
//    suspend fun getAll(): List<StoreCategory>
//
//    @Query("SELECT * FROM billing_items WHERE purchaseToken = :token LIMIT 1")
//    suspend fun getByToken(token: String): BillingEntity?
//
//    @Delete
//    suspend fun delete(billing: BillingEntity)
//}


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
    val storeProductViews:List<StoreProductView>,
    val ads :List<Ads>,
    var storeCategories: List<StoreCategory>,
    var storeSections:List<StoreSection>,
    var storeNestedSections:List<StoreNestedSection>
)
@Serializable
data class StoreProductView(
    val productViewId: Int,
    val name: String,
    val storeProductViewId: Int,
    val storeId: Int,
)

@Serializable
data class Ads(
    val id: Int,
    val image: String,
    val productId:Int?,
    val expireAt: String,
)

@Serializable
data class MorableStoreProducts(
    val id: Int,
    val image: String,
    val productId:Int?,
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
    val product: Product,
    val storeNestedSectionId:Int,
    val options: List<ProductOption>,
)
@Serializable
data class CustomPrice(
    val id: Int,
    val storeProductId: Int,
    val price:String,
)

//@Serializable
//data class StoreProduct(
//    val productId:Int,
////    val productViewName:String,
////    val productViewId:Int,
//    val storeNestedSectionId: Int,
//    val productName: String,
//    val productDescription: String?,
//    val options: List<ProductOption>,
//    val images: List<ProductImage>
//)
@Serializable
data class NativeProductView(
    val id:Int,
    val name:String
)

@Serializable
data class ProductOption(
    val optionId:Int,
    val currency: Currency,
    val storeProductId: Int,
    val name: String,
    val price: String,
    val isCustomPrice:Boolean
)
@Serializable
data class Currency(
    var id: Int,
    var name:String,
    var sign:String
)
@Serializable
data class ProductImage(
    val id : Int,
    val image: String,
    val productId:Int,
)

@Serializable
data class PageModel(val pageName:String,val pageId:Int)
@Serializable
data class UserInfo(
    val firstName: String,
    val secondName: String?,
    val thirdName: String?,
    val lastName: String,
    val code: String?,
    val phone: String?,
    val email: String?,
    val logo: String?,
)


@Serializable
data class ProductView(
    var id: Int,
    var name:String,
    val products:List<StoreProduct>
)

@Serializable
data class CustomOption (val id:Int, val name:String)

@Serializable
data class StoreOrders (val situations:List<CustomOption>, val orders:List<Order>)


@Serializable
data class RemoteConfigModel(
    val TYPE_STORE_MANAGER: String,
    val SUB_FOLDER_STORE_COVERS: String,
    val SUB_FOLDER_PRODUCT: String,
    val BASE_IMAGE_URL: String,
    val BASE_URL: String,
    val SUB_FOLDER_STORE_LOGOS: String,
    val SUB_FOLDER_USERS_LOGOS: String,
    val VERSION:String = "v1",
    val REMOTE_CONFIG_VERSION : Int
)

@Serializable
data class Location (val id:Int, val street:String, val deliveryPrice: DeliveryPrice,val distance:Int,val duration:Int)
@Serializable
data class DeliveryPrice (val currencyId:Int, val deliveryPrice:Double,val currencyName:String)

@Serializable
data class MainCategory(val id:Int,val name :String,val image:String,val sharedableStores : ArrayList<Int>)

@Serializable
data class StoreMainCategory(val storeMainCategoryName :String,val storeMainCategoryImage:String)

@Serializable
data class MainData(val mainCategories:List<MainCategory>,val currencies :List<Currency>)
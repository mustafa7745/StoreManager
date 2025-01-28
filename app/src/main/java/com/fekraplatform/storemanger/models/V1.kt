package com.fekraplatform.storemanger.models

import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id: Int,
    val typeId: Int,
    val name: String,
    val logo : String,
    val cover : String,
    var latLng:String?,
    val app: App?,
    var storeConfig:StoreConfig?
)

@Serializable
data class App(
    val id: Int,
)

@Serializable
data class Order(
    val id: Int,
    val userName: String,
    val userPhone: String,
    val amounts:List<OrderAmount>
)

@Serializable
data class DeliveryMan(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val phone: String,
)

@Serializable
data class OrderProduct(
    val id: Int,
    val productName: String,
    val currencyName: String,
    val optionName: String,
    val quantity:Int,
    val price:Double,
)

@Serializable
data class OrderDelivery(
    val id: Int,
    val latLng: String,
    val street: String,
    val deliveryMan: DeliveryMan?,
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
data class OrderComponent(
    val orderDelivery: OrderDelivery?,
    val orderProducts: List<OrderProduct> ,
    val orderPayment: OrderPayment?,
    val orderDetail: OrderDetail
){
    // Example function to update a product in the list
    fun updateOrderProduct(updatedProduct: OrderProduct): OrderComponent {
        // Replace the product with the same id or add it if it doesn't exist
        val updatedProducts = orderProducts.map { product ->
            if (product.id == updatedProduct.id) updatedProduct else product
        }
        return this.copy(orderProducts = updatedProducts)
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

//@Serializable
//data class ProductToSelect(
//    val id:Int,
//    val name: String
//)
////////
@Serializable
data class StoreCategory(
    val id:Int,
    val categoryId: Int,
    val categoryName: String,

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
    var storeCategories: List<StoreCategory>,
    var storeSections:List<StoreSection>,
    var storeNestedSections:List<StoreNestedSection>
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
    val price: String
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
    val image: String
)

@Serializable
data class PageModel(val pageName:String,val pageId:Int)
@Serializable
data class UserInfo(
    val firstName: String,
    val secondName: String?,
    val thirdName: String?,
    val lastName: String,
    val logo: String?,
)

@Serializable
data class ProductView(
    var id: Int,
    var name:String,
    val products:List<StoreProduct>
)

data class CustomOption (val id:Int, val name:String)
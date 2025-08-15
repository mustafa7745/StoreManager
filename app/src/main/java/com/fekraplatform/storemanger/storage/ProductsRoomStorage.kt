package com.fekraplatform.storemanger.storage

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.serialization.Serializable



@Entity(tableName = "billing_items")
data class BillingEntity(
    val productId: String,
    @PrimaryKey val purchaseToken: String
)


@Dao
interface BillingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(billing: BillingEntity)

    @Query("SELECT * FROM billing_items")
    suspend fun getAll(): List<BillingEntity>

    @Query("SELECT * FROM billing_items WHERE purchaseToken = :token LIMIT 1")
    suspend fun getByToken(token: String): BillingEntity?

    @Delete
    suspend fun delete(billing: BillingEntity)
}



//@Entity
//data class Images(
//    @PrimaryKey
//    @ColumnInfo(name = "id") val id: Int,
//    @ColumnInfo(name = "image") val image: String,
//    @ColumnInfo(name = "productId") val productId: Int,
//)
//@Entity
//data class ProductViews(
//    @PrimaryKey
//    @ColumnInfo(name = "id") val id: Int,
//    @ColumnInfo(name = "name") val name: String
//)


//
//@Entity
//data class Date(
//    @PrimaryKey
//    @ColumnInfo(name = "id") val id: Int,
//    @ColumnInfo(name = "date") val date: String
//)

//@Entity
//data class StoreProducts(
//    @PrimaryKey
//    @ColumnInfo(name = "id") val id: Int,
//    @ColumnInfo(name = "productId") val productId: Int,
//    @ColumnInfo(name = "ProductName") val ProductName: String,
//    @ColumnInfo(name = "productDescription") val productDescription: String,
//    @ColumnInfo(name = "storeNestedSectionId") val storeNestedSectionId: Int,
//    @ColumnInfo(name = "price") val price: Double,
//    @ColumnInfo(name = "storeId") val storeId: Int,
//    @ColumnInfo(name = "optionId") val optionId: Int,
//    @ColumnInfo(name = "optionName") val optionName: String,
//    @ColumnInfo(name = "currencyId") val currencyId: Int,
//    @ColumnInfo(name = "currencyName") val currencyName: String,
//    @ColumnInfo(name = "currencySign") val currencySign: String,
//    @ColumnInfo(name = "productViewId") val productViewId: Int,
//    @ColumnInfo(name = "productViewName") val productViewName: String,
//)


//@Dao
//interface ProductViewDao {
////    @Query("SELECT * FROM productviews")
////    fun loadDate(): List<ProductViews>
////    @Insert
////    fun insert(vararg data: ProductViews)
//}

//@Dao
//interface DateDao {
////    @Query("SELECT date FROM date WHERE id = 1 LIMIT 1")
////    fun loadDate(): String?
////
////    @Insert
////    fun insert(vararg users: Images)
////
////    @Update
////    fun update(date: Date)
//
////    @Insert(onConflict = OnConflictStrategy.REPLACE)
////    fun insertOrUpdate(vararg date: Date)
//}



//@Dao
//interface ImagesDao {
////    @Query("SELECT * FROM images WHERE productId IN (:Ids)")
////    fun loadAllByIds(Ids: IntArray): List<Images>
//
////    @Insert
////    fun insert(vararg image: Images)
//
////    @Query("DELETE FROM images WHERE productId IN (:Ids)")
////    fun delete(Ids: IntArray)
//}
//
//@Dao
//interface OptionsDao {
//    @Query("SELECT * FROM options WHERE productId = :productId AND storeNestedSectionId = :storeNestedSectionId")
//    fun loadAllByIds(productId: Int,storeNestedSectionId: Int): List<Options>
//
//    @Insert
//    fun insertAll(vararg options: List<Options>)
//
//    @Query("DELETE FROM options WHERE productId IN (:Ids)")
//    fun delete(Ids: IntArray)
//}

//@Dao
//interface StoreProductsDao {
////    @Query("SELECT * FROM storeproducts WHERE storeId = :storeId AND storeNestedSectionId = :storeNestedSectionId")
////    fun loadAllByIds(storeId: Int,storeNestedSectionId: Int): List<StoreProducts>
//
////    @Insert
////    fun insertAll(vararg options: StoreProducts)
//
////    @Query("DELETE FROM storeproducts WHERE storeId = :storeId AND storeNestedSectionId = :storeNestedSectionId")
////    fun delete(storeId: Int,storeNestedSectionId: Int)
//}


package com.fekraplatform.storemanger.storage

import GetStorage
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.fekraplatform.storemanger.activities.getCurrentDate
import com.fekraplatform.storemanger.models.ProductImage
import com.fekraplatform.storemanger.models.ProductOption
import com.fekraplatform.storemanger.models.StoreProduct
import java.time.LocalDateTime

const val DATABASE_PRODUCT_NAME = "products.db"
const val DATABASE_PRODUCT_VERSION = 16
class StoreProductStructure{
    companion object {

        const val TABLE_PRODUCTS = "products"

        // Product column names

        const val COLUMN_PRODUCT_ID = "productId"
        const val COLUMN_STORE_NESTED_SECTION_ID = "storeNestedSectionId"
        const val COLUMN_STORE_ID = "storeId"
        const val COLUMN_PRODUCT_NAME = "productName"
        const val COLUMN_PRODUCT_DESCRIPTION = "productDescription"
        const val COLUMN_PRODUCT_View_Id = "productViewId"
        const val COLUMN_PRODUCT_View_Name = "productViewName"
    }
}
class ProductOptionStructure{
    companion object {

        const val TABLE_OPTIONS = "options"
        const val COLUMN_OPTION_ID = "optionId"
        const val COLUMN_STORE_PRODUCT_ID = "storeProductId"
        const val COLUMN_PRODUCT_ID = "productId"
        const val COLUMN_NSETED_SECTION_ID = "nestedSectionId"
        const val COLUMN_NAME = "name"
        const val COLUMN_PRICE = "price"
    }
}
class ProductImagesStructure{
    companion object {

        const val TABLE_IMAGES = "images"

        // Product column names
        const val COLUMN_ID = "id"
        const val COLUMN_PRODUCT_ID = "productId"
        const val COLUMN_IMAGE = "IMAGE"
    }
}

class ProductsStorageDBManager(context: Context) : SQLiteOpenHelper(context, DATABASE_PRODUCT_NAME, null, DATABASE_PRODUCT_VERSION){
    private val getStorage = GetStorage("products")
    private val dateKey = "dateKey"
    fun getDate(storeId:String,CsPsSCRId:String): LocalDateTime? {
//        Log.e("rer",dateKey+CsPsSCRId+storeId)
//        Log.e("rer",(LocalDateTime.parse(getStorage.getData(dateKey+CsPsSCRId))).toString())
        return (LocalDateTime.parse(getStorage.getData(dateKey+CsPsSCRId+storeId)))
    }
    fun setDate(storeId:String,CsPsSCRId:String){
        getStorage.setData(dateKey+CsPsSCRId+storeId, getCurrentDate().toString())
    }

    fun isSet(storeId:String,CsPsSCRId:String): Boolean {
       return getStorage.getData(dateKey+CsPsSCRId+storeId).isNotEmpty()
    }


    override fun onCreate(db: SQLiteDatabase?) {
        // Creating Store Products table
        val CREATE_STORE_PRODUCTS_TABLE =
            ("CREATE TABLE " + StoreProductStructure.TABLE_PRODUCTS + " ("
                    + StoreProductStructure.COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY, "
                    + StoreProductStructure.COLUMN_STORE_NESTED_SECTION_ID + " INTEGER, "
                    + StoreProductStructure.COLUMN_STORE_ID + " INTEGER, "
                    + StoreProductStructure.COLUMN_PRODUCT_NAME + " TEXT, "
                    + StoreProductStructure.COLUMN_PRODUCT_View_Id + " INTEGER, "
                    + StoreProductStructure.COLUMN_PRODUCT_View_Name + " TEXT, "
                    + StoreProductStructure.COLUMN_PRODUCT_DESCRIPTION + " TEXT)")

        // Creating Options table
        val CREATE_OPTIONS_TABLE = ("CREATE TABLE " + ProductOptionStructure.TABLE_OPTIONS + " ("
                + ProductOptionStructure.COLUMN_STORE_PRODUCT_ID + " INTEGER PRIMARY KEY, "
                + ProductOptionStructure.COLUMN_OPTION_ID + " INTEGER, "
                + ProductOptionStructure.COLUMN_PRODUCT_ID + " INTEGER, "
                + ProductOptionStructure.COLUMN_NSETED_SECTION_ID + " INTEGER, "
                + ProductOptionStructure.COLUMN_NAME + " TEXT, "
                + ProductOptionStructure.COLUMN_PRICE + " TEXT)")

        // Creating Images table
        val CREATE_IMAGES_TABLE = ("CREATE TABLE " + ProductImagesStructure.TABLE_IMAGES + " ("
                + ProductImagesStructure.COLUMN_ID + " INTEGER, "
                + ProductImagesStructure.COLUMN_PRODUCT_ID + " INTEGER, "
                + ProductImagesStructure.COLUMN_IMAGE + " TEXT)")

        db?.execSQL(CREATE_STORE_PRODUCTS_TABLE)
        db?.execSQL(CREATE_OPTIONS_TABLE)
        db?.execSQL(CREATE_IMAGES_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop tables if they exist and recreate them
        db?.execSQL("DROP TABLE IF EXISTS " + StoreProductStructure.TABLE_PRODUCTS)
        db?.execSQL("DROP TABLE IF EXISTS " + ProductOptionStructure.TABLE_OPTIONS)
        db?.execSQL("DROP TABLE IF EXISTS " + ProductImagesStructure.TABLE_IMAGES)
        onCreate(db)
    }

    // Function to fetch store products with related options and images
    @SuppressLint("Range")
    fun getStoreProducts(storeId:String,storeNestedSectionId:String): List<StoreProduct> {
        val db = this.readableDatabase
        val storeProductsList = mutableListOf<StoreProduct>()

        // Query to get all store products
        val storeProductQuery = "SELECT * FROM ${StoreProductStructure.TABLE_PRODUCTS}  WHERE ${StoreProductStructure.COLUMN_STORE_NESTED_SECTION_ID} = ? AND ${StoreProductStructure.COLUMN_STORE_ID} = ?"
        val cursor: Cursor = db.rawQuery(storeProductQuery, arrayOf(storeNestedSectionId,storeId))
        Log.e("1111",cursor.count.toString())
        if (cursor.moveToFirst()) {
            do {
                Log.e("45345",cursor.count.toString())
                val productId = cursor.getInt(cursor.getColumnIndex(StoreProductStructure.COLUMN_PRODUCT_ID))
                val storeNestedSectionId1 = cursor.getInt(cursor.getColumnIndex(StoreProductStructure.COLUMN_STORE_NESTED_SECTION_ID))
                val productName = cursor.getString(cursor.getColumnIndex(StoreProductStructure.COLUMN_PRODUCT_NAME))
                val productDescription = cursor.getString(cursor.getColumnIndex(StoreProductStructure.COLUMN_PRODUCT_DESCRIPTION))
                val COLUMN_PRODUCT_View_Id = cursor.getInt(cursor.getColumnIndex(StoreProductStructure.COLUMN_PRODUCT_View_Id))
                val COLUMN_PRODUCT_View_Name = cursor.getString(cursor.getColumnIndex(StoreProductStructure.COLUMN_PRODUCT_View_Name))
                // Fetch options for this storeProductId
                val options = getOptionsForStoreProduct(storeNestedSectionId1,productId.toString())

                // Fetch images for this storeProductId
                val images = getImagesForStoreProduct(productId.toString())

                // Add store product with its options and images
//                val storeProduct = StoreProduct(productId,COLUMN_PRODUCT_View_Name,COLUMN_PRODUCT_View_Id,storeNestedSectionId1, productName, productDescription, options, images)
//                storeProductsList.add(storeProduct)

            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return storeProductsList
    }
    @SuppressLint("Range")
    fun getProductIds(storeNestedSectionId:String): List<String> {
        val db = this.readableDatabase
        val ids = mutableListOf<String>()

        // Query to get all store products
        val storeProductQuery = "SELECT * FROM ${StoreProductStructure.TABLE_PRODUCTS}  WHERE ${StoreProductStructure.COLUMN_STORE_NESTED_SECTION_ID} = ?"
        val cursor: Cursor = db.rawQuery(storeProductQuery, arrayOf(storeNestedSectionId))
        Log.e("1111",cursor.count.toString())
        if (cursor.moveToFirst()) {
            do {
                Log.e("45345",cursor.count.toString())
//                val storeProductId = cursor.getLong(cursor.getColumnIndex(StoreProductStructure.COLUMN_STORE_PRODUCT_ID))
                val productId = cursor.getInt(cursor.getColumnIndex(StoreProductStructure.COLUMN_PRODUCT_ID))
//                val CsPsSCRId = cursor.getInt(cursor.getColumnIndex(StoreProductStructure.COLUMN_CsPsSCR_ID))
//                val productName = cursor.getString(cursor.getColumnIndex(StoreProductStructure.COLUMN_PRODUCT_NAME))
//                val productDescription = cursor.getString(cursor.getColumnIndex(StoreProductStructure.COLUMN_PRODUCT_DESCRIPTION))
//
//                // Fetch options for this storeProductId
//                val options = getOptionsForStoreProduct(productId.toString())
//
//                // Fetch images for this storeProductId
//                val images = getImagesForStoreProduct(productId.toString())
//
//                // Add store product with its options and images
//                val storeProduct = StoreProduct(productId,storeProductId.toInt(),CsPsSCRId, productName, productDescription, options, images)
                ids.add(productId.toString())

            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return ids
    }
    @SuppressLint("Range")
    fun getStoreProductIds(storeNestedSectionId:String): List<String> {
        val db = this.readableDatabase
        val ids = mutableListOf<String>()

        // Query to get all store products
        val storeProductQuery = "SELECT * FROM ${StoreProductStructure.TABLE_PRODUCTS}  WHERE ${StoreProductStructure.COLUMN_STORE_NESTED_SECTION_ID} = ?"
        val cursor: Cursor = db.rawQuery(storeProductQuery, arrayOf(storeNestedSectionId))
        Log.e("1111",cursor.count.toString())
        if (cursor.moveToFirst()) {
            do {
                Log.e("45345",cursor.count.toString())
                val productId = cursor.getLong(cursor.getColumnIndex(StoreProductStructure.COLUMN_PRODUCT_ID))
//                val productId = cursor.getInt(cursor.getColumnIndex(StoreProductStructure.COLUMN_PRODUCT_ID))
//                val CsPsSCRId = cursor.getInt(cursor.getColumnIndex(StoreProductStructure.COLUMN_CsPsSCR_ID))
//                val productName = cursor.getString(cursor.getColumnIndex(StoreProductStructure.COLUMN_PRODUCT_NAME))
//                val productDescription = cursor.getString(cursor.getColumnIndex(StoreProductStructure.COLUMN_PRODUCT_DESCRIPTION))
//
//                // Fetch options for this storeProductId
//                val options = getOptionsForStoreProduct(productId.toString())
//
//                // Fetch images for this storeProductId
//                val images = getImagesForStoreProduct(productId.toString())
//
//                // Add store product with its options and images
//                val storeProduct = StoreProduct(productId,storeProductId.toInt(),CsPsSCRId, productName, productDescription, options, images)
                ids.add(productId.toString())

            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return ids
    }

    // Fetch options for a store product
    @SuppressLint("Range")
    private fun getOptionsForStoreProduct(nestedSectionId: Int,productId: String): List<ProductOption> {
        val db = this.readableDatabase
        val optionsList = mutableListOf<ProductOption>()

        val optionsQuery = "SELECT * FROM ${ProductOptionStructure.TABLE_OPTIONS} WHERE ${ProductOptionStructure.COLUMN_PRODUCT_ID} = ? AND ${ProductOptionStructure.COLUMN_NSETED_SECTION_ID} = ?"
        val cursor: Cursor = db.rawQuery(optionsQuery, arrayOf(productId.toString(),nestedSectionId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val optionId = cursor.getString(cursor.getColumnIndex(ProductOptionStructure.COLUMN_OPTION_ID))
                val storeProductId = cursor.getInt(cursor.getColumnIndex(ProductOptionStructure.COLUMN_STORE_PRODUCT_ID))
                val name = cursor.getString(cursor.getColumnIndex(ProductOptionStructure.COLUMN_NAME))
                val price = cursor.getString(cursor.getColumnIndex(ProductOptionStructure.COLUMN_PRICE))

//                val option = ProductOption(optionId.toInt(),storeProductId, name, price)
//                optionsList.add(option)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return optionsList
    }

    @SuppressLint("Range")
    private fun getImagesForStoreProduct(productId: String): List<ProductImage> {
        val db = this.readableDatabase
        val imagesList = mutableListOf<ProductImage>()

        val imagesQuery = "SELECT * FROM ${ProductImagesStructure.TABLE_IMAGES} WHERE ${ProductImagesStructure.COLUMN_PRODUCT_ID} = ?"
        val cursor: Cursor = db.rawQuery(imagesQuery, arrayOf(productId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndex(ProductImagesStructure.COLUMN_ID))
                val imagePath = cursor.getString(cursor.getColumnIndex(ProductImagesStructure.COLUMN_IMAGE))
//
//                val image = ProductImage(id.toInt(), imagePath)
//                imagesList.add(image)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return imagesList
    }

    // CRUD Operations for Products Table (with manual ID management)
    fun addProductStore(db: SQLiteDatabase,productId: String,storeId:String,storeNestedSectionId:Int,name: String, description: String,productViewId:Int,productViewName:String) {

        val values = ContentValues()
        values.put(StoreProductStructure.COLUMN_PRODUCT_NAME, name)
        values.put(StoreProductStructure.COLUMN_PRODUCT_DESCRIPTION, description)
        values.put(StoreProductStructure.COLUMN_PRODUCT_ID, productId)
        values.put(StoreProductStructure.COLUMN_STORE_NESTED_SECTION_ID, storeNestedSectionId)
        values.put(StoreProductStructure.COLUMN_STORE_ID, storeId)
        values.put(StoreProductStructure.COLUMN_PRODUCT_View_Id, productViewId)
        values.put(StoreProductStructure.COLUMN_PRODUCT_View_Name, productViewName)
        // Inserting Row
         db.insert(StoreProductStructure.TABLE_PRODUCTS, null, values)

    }

    // Function to clear all data from the database
    fun clearAllData(srcId: String) {
        val productIds = getProductIds(srcId)
        val storeProductIds = getStoreProductIds(srcId)
        val db = this.writableDatabase

        // Begin a transaction to ensure all data is deleted in a safe way
        db.beginTransaction()
        try {
            // Delete all records from the store_products table
            db.delete(
                StoreProductStructure.TABLE_PRODUCTS,
                "${StoreProductStructure.COLUMN_PRODUCT_ID} IN (${storeProductIds.joinToString(",") { "?" }})",storeProductIds.map { it.toString() }.toTypedArray()
            )

            // Delete all records from the options table
            db.delete(ProductOptionStructure.TABLE_OPTIONS, "${ProductOptionStructure.COLUMN_PRODUCT_ID} IN (${productIds.joinToString(",") { "?" }})", productIds.map { it.toString() }.toTypedArray())

            // Delete all records from the images table
            db.delete(ProductImagesStructure.TABLE_IMAGES, "${ProductImagesStructure.COLUMN_PRODUCT_ID} IN (${productIds.joinToString(",") { "?" }})", productIds.map { it.toString() }.toTypedArray())

            // Set the transaction as successful
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            // Handle any exceptions if necessary (optional)
            e.printStackTrace()
        } finally {
            // End the transaction (commit or rollback depending on success)
            db.endTransaction()
            db.close()
        }
    }

    fun addStoreProducts(storeProducts:List<StoreProduct>,storeId: String,storeNestedSectionId: Int) {
        val db = this.writableDatabase

        db.beginTransaction()
        try {
            storeProducts.forEach { storeProduct->
//                addProductStore(db,storeProduct.productId.toString(),
//                    storeId,storeNestedSectionId,storeProduct.productName,storeProduct.productDescription.toString(),storeProduct.productViewId,storeProduct.productViewName)

//                storeProduct.options.forEach { option ->
//                    addProductOption(db,option.storeProductId.toString(),option.optionId.toString(),storeNestedSectionId,storeProduct.productId.toString(),option.name.toString(),option.price)
//                }

//                storeProduct.images.forEach { image ->
//                    addImage(db,image.id.toString(),storeProduct.productId.toString(),image.image.toString())
//                }

            }

            setDate(storeId,storeNestedSectionId.toString())
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            // Handle any exceptions if necessary (optional)
            e.printStackTrace()
        } finally {
            // End the transaction (commit or rollback depending on success)
            db.endTransaction()
            db.close()
        }
    }

    fun addStoreProduct(storeProduct:StoreProduct,storeId: String,storeNestedSectionId: Int) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {

//                addProductStore(db,storeProduct.productId.toString(),
//                    storeId,storeNestedSectionId,storeProduct.productName,storeProduct.productDescription.toString(),storeProduct.productViewId,storeProduct.productViewName)
//                storeProduct.options.forEach { option ->
//                    addProductOption(db,option.storeProductId.toString(),option.optionId.toString(),storeNestedSectionId,storeProduct.productId.toString(),option.name.toString(),option.price)
//                }

//                storeProduct.images.forEach { image ->
//                    addImage(db,image.id.toString(),storeProduct.productId.toString(),image.image.toString())
//                }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            // Handle any exceptions if necessary (optional)
            e.printStackTrace()
        } finally {
            // End the transaction (commit or rollback depending on success)
            db.endTransaction()
            db.close()
        }
    }

    fun addProductOption2(storeProductId:String,optionId: String,nestedSectionId: Int,productId: Int,optionName:String,optionPrice: String) {
        val db = this.writableDatabase

        db.beginTransaction()
        try {
            addProductOption(db,storeProductId,optionId,nestedSectionId,productId.toString(),optionName,optionPrice)
            db.setTransactionSuccessful()
            Log.e("fob","done")

        } catch (e: Exception) {
            // Handle any exceptions if necessary (optional)
            e.printStackTrace()
        } finally {
            // End the transaction (commit or rollback depending on success)
            db.endTransaction()
            db.close()
        }
    }



    // CRUD Operations for Product Options Table (with manual ID management)
    fun addProductOption(db: SQLiteDatabase, storeProductId:String, optionId:String,nestedSectionId:Int, productId:String, name: String, price:String) {
        val values = ContentValues()
        values.put(ProductOptionStructure.COLUMN_NAME, name)
        values.put(ProductOptionStructure.COLUMN_PRODUCT_ID, productId)
        values.put(ProductOptionStructure.COLUMN_STORE_PRODUCT_ID, storeProductId)
        values.put(ProductOptionStructure.COLUMN_NSETED_SECTION_ID, nestedSectionId)
        values.put(ProductOptionStructure.COLUMN_OPTION_ID, optionId)
        values.put(ProductOptionStructure.COLUMN_PRICE, price)
        // Inserting Row
         db.insert(ProductOptionStructure.TABLE_OPTIONS, null, values)

    }


    // CRUD Operations for Product Images Table (with manual ID management)
    fun addImage(db: SQLiteDatabase,id: String,productId: String,imagePath: String) {
        val values = ContentValues()
        values.put(ProductImagesStructure.COLUMN_IMAGE, imagePath)
        values.put(ProductImagesStructure.COLUMN_PRODUCT_ID, productId)
        values.put(ProductImagesStructure.COLUMN_ID, id)

        // Inserting Row
     db.insert(ProductImagesStructure.TABLE_IMAGES, null, values)

    }
}
package com.fekraplatform.storemanger.databases

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.fekraplatform.storemanger.activities1.OrderEntity
import com.fekraplatform.storemanger.activities1.OrderSituationDao
import com.fekraplatform.storemanger.activities1.OrderSituationEntity
import com.fekraplatform.storemanger.activities1.OrdersDao
import com.fekraplatform.storemanger.models.OrderAmount
import com.fekraplatform.storemanger.storage.BillingDao
import com.fekraplatform.storemanger.storage.BillingEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.Executors


@Database(entities = [BillingEntity::class,OrderSituationEntity::class,OrderEntity::class], version = 4)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billingDao(): BillingDao

    abstract fun orderSituationDao(): OrderSituationDao
    abstract fun ordersDao(): OrdersDao

}
////
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .setQueryCallback({ sqlQuery, bindArgs ->
                Log.d("ROOM_QUERY", "SQL: $sqlQuery")
                Log.d("ROOM_QUERY", "Args: $bindArgs")
            }, Executors.newSingleThreadExecutor())
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBillingDao(db: AppDatabase): BillingDao = db.billingDao()

    @Provides
    fun provideOrderSituationDao(db: AppDatabase): OrderSituationDao = db.orderSituationDao()

    @Provides
    fun provideOrdersDao(db: AppDatabase): OrdersDao = db.ordersDao()

}


class Converters {

    @TypeConverter
    fun fromOrderAmountList(value: List<OrderAmount>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toOrderAmountList(value: String): List<OrderAmount> {
        return Json.decodeFromString(value)
    }
}


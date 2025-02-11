package com.fekraplatform.storemanger.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fekraplatform.storemanger.models.Order
import com.fekraplatform.storemanger.models.Store
import com.fekraplatform.storemanger.models.StoreOrders

object CustomSingleton2 {
    var storeOrders by mutableStateOf<StoreOrders?>(null)
    var selectedStoreOrder by mutableStateOf<Order?>(null)
}
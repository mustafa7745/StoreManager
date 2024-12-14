package com.fekraplatform.storemanger.Singlton

import androidx.compose.runtime.mutableStateOf
import com.fekraplatform.storemanger.models.Store

object SelectedStore {
    val store = mutableStateOf<Store?>(null)
}
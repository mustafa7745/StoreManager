package com.fekraplatform.storemanger.shared
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StateController {
    val isLoadingRead = mutableStateOf(false)
    val isSuccessRead = mutableStateOf(false)
    val isErrorRead = mutableStateOf(false)
    val errorRead = mutableStateOf("")
    //
    val isLoadingAUD = mutableStateOf(false)
    val isErrorAUD = mutableStateOf(false)
    val errorAUD = mutableStateOf("")
    val successAUD = mutableStateOf("")

    val isShowMessage = mutableStateOf(false)
    val message = mutableStateOf("")

    fun startRead() {
        isErrorRead.value = false
       errorRead.value = ""
       isLoadingRead.value = true
    }

     fun errorStateRead(e:String) {
       isLoadingRead.value = false
       isErrorRead.value = true
        errorRead.value = e
    }
     fun successState() {
       isLoadingRead.value = false
         isSuccessRead.value = true
       isErrorRead.value = false
    }

     fun startAud() {
        errorAUD.value = ""
       isLoadingAUD.value = true
        isErrorAUD.value = false
    }

    fun errorStateAUD(e:String) {
       isLoadingAUD.value = false
       isErrorAUD.value = true
        errorAUD.value = e
    }
     fun successStateAUD(message: String = "") {
        isLoadingAUD.value = false
        isErrorAUD.value = false
         if (message.length > 0){
             successAUD.value=message
         }
    }
    fun isHaveSuccessAudMessage(): Boolean {
       return successAUD.value.length > 0
    }
    fun getMessage(): String {
        val message = successAUD.value;
        successAUD.value = ""
        return message
    }
    fun showMessage(m: String){

        GlobalScope.launch {
            isShowMessage.value = true
            message.value = m
            delay(500)
            isShowMessage.value = false
            message.value = ""

        }
    }
}
package com.fekraplatform.storemanger.shared

import GetStorage
import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.ViewModel
import com.fekraplatform.storemanger.application.MyApplication.Companion.AppContext
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject


class AppInfoMethod @Inject constructor() {

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String? {
      return  Settings.Secure.getString(AppContext.contentResolver, Settings.Secure.ANDROID_ID)
    }
    fun getAppPackageName():String{
        val packageManager = AppContext.packageManager
        val packageName = AppContext.packageName
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
        return packageInfo.packageName
    }
    fun getAppVersion():String{
        val packageManager = AppContext.packageManager
        val packageName = AppContext.packageName
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
        return PackageInfoCompat.getLongVersionCode(packageInfo).toString()
    }
    //TOKEN
    fun getAppToken():String{
        try {
            val res =  FirebaseMessaging.getInstance().token.result
            if (res !=null) {
                return res
            }
            return "e"
        }
        catch (e:Exception){
            return "ex"
        }
    }
    suspend fun getAppToken(onFail:()->Unit, onSuccess:(token:String)->Unit){
        try {
            val res =  FirebaseMessaging.getInstance().token.await()
            if (res !=null) {
                onSuccess(res)
            }
            else{
                onFail()
            }
        }
        catch (e:Exception){
            onFail()
        }
    }
    ///SHA
    fun getAppSha():String{
        var sha = ""
        try {
            val info: PackageInfo = AppContext.packageManager.getPackageInfo(
                getAppPackageName(),
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures!!) {
                val md = MessageDigest.getInstance("SHA-256")
                md.update(signature.toByteArray())
                val digest = md.digest()
                val toRet = StringBuilder()
                for (i in digest.indices) {
                    if (i != 0) toRet.append(":")
                    val b = digest[i].toInt() and 0xff
                    val hex = Integer.toHexString(b)
                    if (hex.length == 1) toRet.append("0")
                    toRet.append(hex)
                }
                val s = toRet.toString()
                sha = s
//                Log.e("sig", s)
            }
            return sha
        } catch (e1: PackageManager.NameNotFoundException) {
            return e1.toString()
        } catch (e: NoSuchAlgorithmException) {
            return e.toString()
        } catch (e: Exception) {
            return e.toString()
        }
    }
}
//class TokenVM: ViewModel(){
//    fun check(onFail:()->Unit,onSuccess:()->Unit){
//        GlobalScope.launch {
//            val s= isGetToken()
//            if (s){
//                onSuccess()
//            }
//            else{
//              onFail()
//            }
//        }
//    }
//   private suspend fun isGetToken():Boolean{
//        val token = getToken()
//        if (token){
//            return true
//        }
//        return false
//    }
//    private suspend fun getToken():Boolean{
//        try {
//            val res =  FirebaseMessaging.getInstance().token.await()
//            if (res !=null) {
//                return true
//            }
//            return false
//        }
//        catch (e:Exception){
//            return false
//        }
//    }
//}

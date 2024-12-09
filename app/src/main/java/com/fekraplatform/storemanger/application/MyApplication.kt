package com.fekraplatform.storemanger.application

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppContext = this
    }
    companion object {
        lateinit var AppContext: Application
            private set

    }

}
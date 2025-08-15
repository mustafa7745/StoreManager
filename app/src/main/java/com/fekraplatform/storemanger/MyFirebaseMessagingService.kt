package com.fekraplatform.storemanger
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fekraplatform.storemanger.Singlton.MySessionEntryPoint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.EntryPointAccessors

class MyFirebaseMessagingService:FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (message.notification != null) {
            val title = message.notification!!.title?: "عنوان"
            val body = message.notification!!.body?: "محتوى"
            showNotification(title, body)

            val context = applicationContext

            val entryPoint = EntryPointAccessors.fromApplication(
                context,
                MySessionEntryPoint::class.java
            )
            val appSession = entryPoint.getAppSession()

            if (message.data.isNotEmpty()){
                val points = message.data["points"]
                if (points != null){
                    val newPoints = appSession.selectedStore.subscription.points + points.toInt()
                    appSession.myStore  = appSession.selectedStore.copy(subscription = appSession.selectedStore.subscription.copy(points = newPoints))
                }
            }

            // ✅ تحديث المتغير داخل Singleton
        }
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "default_channel"
        val notificationId = System.currentTimeMillis().toInt()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // إنشاء قناة الإشعار (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH // ← ضروري لعرضه فوراً
            ).apply {
                description = "Channel for important notifications"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo) // ← يجب أن تكون أيقونة موجودة
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // ← ضروري
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }

}
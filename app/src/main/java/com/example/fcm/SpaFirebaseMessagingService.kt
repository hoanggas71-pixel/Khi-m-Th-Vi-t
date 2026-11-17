package com.example.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SpaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token received: $token")
        // Lưu token vào SharedPreferences để SpaStore đồng bộ lên Firestore profile
        val prefs = applicationContext.getSharedPreferences(PREFS_FCM, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DEVICE_TOKEN, token).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message from: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Khiếm Thị Việt"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Bạn có thông báo mới từ hệ thống."
        val type = remoteMessage.data["type"] ?: "DEFAULT"

        sendHighPriorityAccessibilityNotification(title, body, type)
    }

    private fun sendHighPriorityAccessibilityNotification(title: String, messageBody: String, type: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("EXTRA_NOTIFICATION_TYPE", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = CHANNEL_ID_HIGH_PRIORITY
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Rung nhịp điệu đặc biệt để KTV khiếm thị dễ cảm nhận: 0ms, 400ms rung, 200ms nghỉ, 400ms rung
        val vibrationPattern = longArrayOf(0, 450, 200, 450)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(vibrationPattern)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Thông Báo Vé & Điều Phối KTV (Ưu Tiên Cao)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh thông báo âm thanh và rung báo mạnh mẽ dành cho KTV và Lễ tân khiếm thị"
                enableVibration(true)
                vibrationPattern = vibrationPattern
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "SpaFcmService"
        const val PREFS_FCM = "spa_fcm_prefs"
        const val KEY_DEVICE_TOKEN = "fcm_device_token"
        const val CHANNEL_ID_HIGH_PRIORITY = "khiem_thi_viet_high_priority_v1"
    }
}

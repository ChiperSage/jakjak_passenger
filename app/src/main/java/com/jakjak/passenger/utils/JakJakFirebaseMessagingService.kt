package com.jakjak.passenger.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jakjak.passenger.R
import com.jakjak.passenger.ui.home.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class JakJakFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Update token FCM ke Firestore jika user sudah login
        FirebaseHelper.currentUserRef()?.let { ref ->
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { ref.update("fcmToken", token).await() }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title   = message.notification?.title ?: message.data["title"] ?: "JakJak"
        val body    = message.notification?.body  ?: message.data["body"]  ?: ""
        val channel = message.data["channel"]     ?: Constants.CHANNEL_RIDE

        showNotification(title, body, channel)
    }

    private fun showNotification(title: String, body: String, channel: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

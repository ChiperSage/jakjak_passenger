package com.jakjak.passenger

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.jakjak.passenger.utils.Constants

class JakJakPassengerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val rideChannel = NotificationChannel(
                Constants.CHANNEL_RIDE,
                "Notifikasi Perjalanan",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pemberitahuan status perjalanan JakJak"
            }

            val promoChannel = NotificationChannel(
                Constants.CHANNEL_PROMO,
                "Promo & Info",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Promo dan informasi dari JakJak"
            }

            manager.createNotificationChannels(listOf(rideChannel, promoChannel))
        }
    }
}

package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.local.RadioDatabase

class GalaxyRadioApplication : Application() {

    lateinit var database: RadioDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = RadioDatabase.getInstance(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Galaxy Radio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Live radio background streaming notifications"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "galaxy_radio_playback_channel"
        lateinit var instance: GalaxyRadioApplication
            private set
    }
}

package com.example.kerberos.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.kerberos.R
import com.example.kerberos.settings.SettingsManager

class KerberosNotificationManager(private val context: Context) {

    companion object {
        const val SECURITY_CHANNEL_ID = "kerberos_security"
        private const val SECURITY_CHANNEL_NAME = "Security Alerts"
        private const val SECURITY_CHANNEL_DESCRIPTION =
            "Security and account activity alerts"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            SECURITY_CHANNEL_ID,
            SECURITY_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = SECURITY_CHANNEL_DESCRIPTION
        }

        notificationManager.createNotificationChannel(channel)
    }

    fun showNewDeviceLogin() {
        if (!canSendNotifications()) {
            return
        }

        val notification = NotificationCompat.Builder(
            context,
            SECURITY_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Security alert")
            .setContentText("A new device signed in to your Kerberos account.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            1001,
            notification
        )
    }

    fun showSyncFailure() {
        if (!canSendNotifications()) {
            return
        }

        val notification = NotificationCompat.Builder(
            context,
            SECURITY_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Sync failed")
            .setContentText("Kerberos could not synchronize your latest changes.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            1002,
            notification
        )
    }

    private fun canSendNotifications(): Boolean {
        val settingsManager = SettingsManager(context)

        val notificationsEnabled =
            settingsManager.areNotificationsEnabled()

        val permissionGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        return notificationsEnabled && permissionGranted
    }
}
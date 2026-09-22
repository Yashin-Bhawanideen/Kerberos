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
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            SECURITY_CHANNEL_ID,
            context.getString(R.string.security_alerts),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.security_channel_description)
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
            .setContentTitle(context.getString(R.string.security_alert))
            .setContentText(context.getString(R.string.new_device_login))
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
            .setContentTitle(context.getString(R.string.sync_failed))
            .setContentText(context.getString(R.string.sync_failure_message))
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
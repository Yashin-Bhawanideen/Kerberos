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

// Centralizes creation and sending of the app's security-related
// notifications (new device login, sync failures)
class KerberosNotificationManager(private val context: Context) {

    companion object {
        const val SECURITY_CHANNEL_ID = "kerberos_security"
        private const val SECURITY_CHANNEL_NAME = "Security Alerts"
        private const val SECURITY_CHANNEL_DESCRIPTION =
            "Security and account activity alerts"
    }

    // Ensures the notification channel exists as soon as this class is used
    init {
        createNotificationChannel()
    }

    // Registers the "Security Alerts" channel (required on Android 8+
    // before any notification can be shown on it). Safe to call repeatedly —
    // creating an existing channel is a no-op.
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

    // Notifies the user that a new device signed in to their account
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

    // Notifies the user that a background sync attempt failed
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
//References
//Android, 2026. Create a notification. [Online]
//Available at: https://developer.android.com/develop/ui/compose/notifications/create-notification
//Android, 2026. Create and manage notification channels. [Online]
//Available at: https://developer.android.com/develop/ui/compose/notifications/channels
//Android, 2026. Notification runtime permission. [Online]
//Available at: https://developer.android.com/develop/ui/compose/notifications/notification-permission
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

    // Shared list shown when the bell icon is tapped on the vault screen
    private val notificationStore = NotificationStore.get(context)

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
        // Always recorded in the bell list, even if the tray notification is blocked
        notificationStore.add(NotificationType.NEW_DEVICE_LOGIN)

        // Setting switch must be on, and the permission must be granted
        if (!areNotificationsEnabledInSettings() || !hasNotificationPermission()) {
            return
        }

        val notification = NotificationCompat.Builder(
            context,
            SECURITY_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.security_alert_title))
            .setContentText(context.getString(R.string.new_device_login_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Permission is checked directly above; SecurityException is handled in
        // case the user revokes it at the last moment (Android Developers, 2026)
        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(
                    1001,
                    notification
                )
            }
        } catch (e: SecurityException) {
            // Permission was revoked; nothing to show
        }
    }

    // Notifies the user that a background sync attempt failed
    fun showSyncFailure() {
        // Always recorded in the bell list, even if the tray notification is blocked
        notificationStore.add(NotificationType.SYNC_FAILED)

        // Setting switch must be on, and the permission must be granted
        if (!areNotificationsEnabledInSettings() || !hasNotificationPermission()) {
            return
        }

        val notification = NotificationCompat.Builder(
            context,
            SECURITY_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.sync_failed_title))
            .setContentText(context.getString(R.string.sync_failed_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(
                    1002,
                    notification
                )
            }
        } catch (e: SecurityException) {
            // Permission was revoked; nothing to show
        }
    }

    // True when the user has the "Security notifications" switch turned on
    private fun areNotificationsEnabledInSettings(): Boolean {
        return SettingsManager(context).areNotificationsEnabled()
    }

    // True when the POST_NOTIFICATIONS permission has been granted
    private fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
//References
//Android, 2026. Create a notification. [Online]
//Available at: https://developer.android.com/develop/ui/compose/notifications/create-notification
//Android, 2026. Create and manage notification channels. [Online]
//Available at: https://developer.android.com/develop/ui/compose/notifications/channels
//Android, 2026. Notification runtime permission. [Online]
//Available at: https://developer.android.com/develop/ui/compose/notifications/notification-permission
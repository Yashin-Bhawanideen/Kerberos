package com.example.kerberos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kerberos.R
import com.example.kerberos.notifications.AppNotification
import com.example.kerberos.notifications.NotificationType
import com.example.kerberos.ui.theme.KerberosBlue
import com.example.kerberos.ui.theme.KerberosGrayText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Popup listing every saved notification (newest first) with its date and time
@Composable
fun NotificationsDialog(
    notifications: List<AppNotification>,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.notifications_title)) },
        text = {
            if (notifications.isEmpty()) {
                Text(
                    stringResource(R.string.notifications_empty),
                    color = KerberosGrayText
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(notifications, key = { it.id }) { notification ->
                        NotificationRow(notification)
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.notifications_close), color = KerberosBlue)
            }
        },
        dismissButton = if (notifications.isNotEmpty()) {
            {
                TextButton(onClick = onClearAll) {
                    Text(stringResource(R.string.notifications_clear_all))
                }
            }
        } else {
            null
        }
    )
}

@Composable
private fun NotificationRow(notification: AppNotification) {
    val message = notificationMessage(notification)

    // Formatted with the device/app locale so it matches the chosen language
    val time = remember(notification.timestamp) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(notification.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Blue dot marks notifications that haven't been seen yet
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .background(
                    if (notification.isRead) androidx.compose.ui.graphics.Color.Transparent
                    else KerberosBlue,
                    CircleShape
                )
        )

        Spacer(Modifier.width(10.dp))

        Column {
            Text(
                message,
                fontSize = 14.sp,
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
            )
            Text(time, fontSize = 11.sp, color = KerberosGrayText)
        }
    }
}

// Builds the text at display time so it follows the current app language
@Composable
private fun notificationMessage(notification: AppNotification): String {
    return when (notification.type) {
        NotificationType.LOGIN ->
            stringResource(R.string.notif_login)

        NotificationType.BIOMETRIC_ON ->
            stringResource(R.string.notif_biometric_on)

        NotificationType.BIOMETRIC_OFF ->
            stringResource(R.string.notif_biometric_off)

        NotificationType.SECURITY_ALERTS_ON ->
            stringResource(R.string.notif_security_on)

        NotificationType.SECURITY_ALERTS_OFF ->
            stringResource(R.string.notif_security_off)

        NotificationType.LANGUAGE_CHANGED -> {
            val languageName = if (notification.detail == "af") {
                stringResource(R.string.afrikaans)
            } else {
                stringResource(R.string.english)
            }
            stringResource(R.string.notif_language_changed, languageName)
        }

        NotificationType.CREDENTIAL_ADDED ->
            stringResource(R.string.notif_credential_added, notification.detail)

        NotificationType.CREDENTIAL_EDITED ->
            stringResource(R.string.notif_credential_edited, notification.detail)

        NotificationType.CREDENTIAL_DELETED ->
            stringResource(R.string.notif_credential_deleted, notification.detail)

        NotificationType.NEW_DEVICE_LOGIN ->
            stringResource(R.string.notif_new_device)

        NotificationType.SYNC_FAILED ->
            stringResource(R.string.notif_sync_failed)
    }
}
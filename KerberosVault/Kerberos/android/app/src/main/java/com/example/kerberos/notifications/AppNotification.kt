package com.example.kerberos.notifications

enum class NotificationType {
    LOGIN,
    BIOMETRIC_ON,
    BIOMETRIC_OFF,
    SECURITY_ALERTS_ON,
    SECURITY_ALERTS_OFF,
    LANGUAGE_CHANGED,
    CREDENTIAL_ADDED,
    CREDENTIAL_EDITED,
    CREDENTIAL_DELETED,
    NEW_DEVICE_LOGIN,
    SYNC_FAILED
}

data class AppNotification(
    val id: Long,
    val type: NotificationType,
    // Extra info: language code for LANGUAGE_CHANGED, service name for credential events
    val detail: String,
    val timestamp: Long,
    val isRead: Boolean = false
)
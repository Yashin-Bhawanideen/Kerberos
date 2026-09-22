package com.example.kerberos.settings

import android.content.Context

class SettingsManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "kerberos_settings",
        Context.MODE_PRIVATE
    )

    // Whether the user wants biometric unlock required to open the vault
    fun isBiometricEnabled(): Boolean {
        return preferences.getBoolean("biometric_enabled", true)
    }

    // Whether the user wants to receive app notifications
    // (checked alongside the OS permission in KerberosNotificationManager)
    fun setBiometricEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean("biometric_enabled", enabled)
            .apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return preferences.getBoolean("notifications_enabled", true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean("notifications_enabled", enabled)
            .apply()
    }

    // User's preferred app language; defaults to English
    fun getLanguage(): String {
        return preferences.getString(
            "language",
            "en"
        ) ?: "en"
    }

    fun setLanguage(language: String) {
        preferences.edit()
            .putString("language", language)
            .apply()
    }

    // Wipes all stored settings back to defaults (e.g. on sign-out/reset)
    fun clearSettings() {
        preferences.edit().clear().apply()
    }
}
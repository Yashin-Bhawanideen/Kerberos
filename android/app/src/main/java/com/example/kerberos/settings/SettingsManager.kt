package com.example.kerberos.settings

import android.content.Context

class SettingsManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "kerberos_settings",
        Context.MODE_PRIVATE
    )

    fun isBiometricEnabled(): Boolean {
        return preferences.getBoolean("biometric_enabled", true)
    }

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

    fun getLanguage(): String {
        return preferences.getString(
            "language",
            "English"
        ) ?: "English"
    }

    fun setLanguage(language: String) {
        preferences.edit()
            .putString("language", language)
            .apply()
    }

    fun clearSettings() {
        preferences.edit().clear().apply()
    }
}
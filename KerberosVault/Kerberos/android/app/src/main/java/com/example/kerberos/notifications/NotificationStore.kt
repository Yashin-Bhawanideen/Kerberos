package com.example.kerberos.notifications

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

// Saves notifications to SharedPreferences and exposes them as a StateFlow so
// any screen that collects it updates automatically. One shared instance is
// used across the whole app via NotificationStore.get(context).
class NotificationStore private constructor(context: Context) {

    private val preferences = context.getSharedPreferences(
        "kerberos_notifications",
        Context.MODE_PRIVATE
    )

    private val _notifications = MutableStateFlow(load())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    @Synchronized
    fun add(type: NotificationType, detail: String = "") {
        val current = _notifications.value
        val nextId = (current.maxOfOrNull { it.id } ?: 0L) + 1

        val item = AppNotification(
            id = nextId,
            type = type,
            detail = detail,
            timestamp = System.currentTimeMillis()
        )

        // Newest first, capped so the list can't grow forever
        val updated = (listOf(item) + current).take(MAX_NOTIFICATIONS)
        save(updated)
        _notifications.value = updated
    }

    @Synchronized
    fun markAllRead() {
        val updated = _notifications.value.map { it.copy(isRead = true) }
        save(updated)
        _notifications.value = updated
    }

    @Synchronized
    fun clearAll() {
        save(emptyList())
        _notifications.value = emptyList()
    }

    private fun save(list: List<AppNotification>) {
        val array = JSONArray()
        list.forEach { n ->
            array.put(
                JSONObject()
                    .put("id", n.id)
                    .put("type", n.type.name)
                    .put("detail", n.detail)
                    .put("timestamp", n.timestamp)
                    .put("isRead", n.isRead)
            )
        }
        preferences.edit().putString(KEY_LIST, array.toString()).apply()
    }

    private fun load(): List<AppNotification> {
        val json = preferences.getString(KEY_LIST, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { i ->
                runCatching {
                    val o = array.getJSONObject(i)
                    AppNotification(
                        id = o.getLong("id"),
                        type = NotificationType.valueOf(o.getString("type")),
                        detail = o.optString("detail", ""),
                        timestamp = o.getLong("timestamp"),
                        isRead = o.optBoolean("isRead", false)
                    )
                }.getOrNull()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val KEY_LIST = "notification_list"
        private const val MAX_NOTIFICATIONS = 50

        @Volatile
        private var instance: NotificationStore? = null

        fun get(context: Context): NotificationStore {
            return instance ?: synchronized(this) {
                instance ?: NotificationStore(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
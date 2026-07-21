package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private val loggedInUserIdKey = intPreferencesKey("logged_in_user_id")
    private val isDarkModeKey = booleanPreferencesKey("is_dark_mode")
    private val loginTimestampKey = longPreferencesKey("login_timestamp")
    private val lastSeenNotificationTimeKey = longPreferencesKey("last_seen_notification_time")

    val lastSeenNotificationTime: Flow<Long> = dataStore.data.map { preferences ->
        preferences[lastSeenNotificationTimeKey] ?: 0L
    }

    suspend fun saveLastSeenNotificationTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[lastSeenNotificationTimeKey] = timestamp
        }
    }

    val loggedInUserId: Flow<Int?> = dataStore.data.map { preferences ->
        val userId = preferences[loggedInUserIdKey]
        val timestamp = preferences[loginTimestampKey]
        if (userId != null && timestamp != null) {
            val elapsed = System.currentTimeMillis() - timestamp
            val threeDaysMs = 3 * 24 * 60 * 60 * 1000L // 3 days in milliseconds
            if (elapsed > threeDaysMs) {
                null // Session expired!
            } else {
                userId
            }
        } else {
            userId
        }
    }

    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[isDarkModeKey] ?: true
    }

    suspend fun setDarkMode(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[isDarkModeKey] = isDark
        }
    }

    suspend fun saveLoggedInUserId(userId: Int) {
        dataStore.edit { preferences ->
            preferences[loggedInUserIdKey] = userId
            preferences[loginTimestampKey] = System.currentTimeMillis()
        }
    }

    suspend fun saveLoginTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[loginTimestampKey] = timestamp
        }
    }

    suspend fun clearLoggedInUserId() {
        dataStore.edit { preferences ->
            preferences.remove(loggedInUserIdKey)
            preferences.remove(loginTimestampKey)
        }
    }
}

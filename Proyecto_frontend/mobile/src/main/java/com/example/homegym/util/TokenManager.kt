package com.example.homegym.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object TokenManager {
    private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    private val IS_GUEST = stringPreferencesKey("is_guest")

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { settings ->
            settings[AUTH_TOKEN] = token
            settings[IS_GUEST] = "false"
        }
    }

    fun getToken(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN]
        }
    }

    suspend fun setGuestMode(context: Context, isGuest: Boolean) {
        context.dataStore.edit { settings ->
            if (isGuest) {
                settings[IS_GUEST] = "true"
                settings.remove(AUTH_TOKEN)
            } else {
                settings[IS_GUEST] = "false"
            }
        }
    }

    fun isGuestMode(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[IS_GUEST] == "true"
        }
    }

    suspend fun deleteToken(context: Context) {
        context.dataStore.edit { settings ->
            settings.remove(AUTH_TOKEN)
            settings.remove(IS_GUEST)
        }
    }
}

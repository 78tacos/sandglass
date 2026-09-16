package com.tacos78.sandglass.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.hapticDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sandglass_settings",
)

class HapticPreferences(context: Context) {
    private val dataStore = context.applicationContext.hapticDataStore

    val enabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_HAPTICS] ?: false
    }

    suspend fun setEnabled(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_HAPTICS] = value
        }
    }

    private companion object {
        val KEY_HAPTICS = booleanPreferencesKey("gentle_haptic_ticks")
    }
}

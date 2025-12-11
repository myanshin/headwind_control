package com.myanshin.headwindcontrol.data
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

data class AppSettings(
    val savedDeviceAddress: String,
    val isNotificationEnabled: Boolean
)
class AppSettingsRepository (
    private val dataStore: DataStore<Preferences>
){
    private companion object {
        val SAVED_DEVICE_ADDRESS = stringPreferencesKey("saved_device_address")
        val IS_NOTIFICATION_ENABLED = booleanPreferencesKey("is_notification_enabled")
        const val TAG = "HW_SCAN_DATASTORE"
    }

    val appSettingsFlow: Flow<AppSettings> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            AppSettings(
            preferences[SAVED_DEVICE_ADDRESS] ?: "",
            preferences[IS_NOTIFICATION_ENABLED] ?: false
            )
        }

    suspend fun saveDeviceAddress(deviceAddress: String) {
        dataStore.edit { preferences ->
            preferences[SAVED_DEVICE_ADDRESS] = deviceAddress
        }
    }

    suspend fun setIsNotificationEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_NOTIFICATION_ENABLED] = isEnabled
        }
    }

}
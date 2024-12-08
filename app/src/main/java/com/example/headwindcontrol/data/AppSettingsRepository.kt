package com.example.headwindcontrol.data
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class AppSettingsRepository (
    private val dataStore: DataStore<Preferences>
){
    private companion object {
        val SAVED_DEVICE_ADDRESS = stringPreferencesKey("saved_device_address")
        const val TAG = "HW_SCAN_DATASTORE"
    }

    val savedDeviceAddress: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[SAVED_DEVICE_ADDRESS] ?: ""
        }

    suspend fun saveDeviceAddress(deviceAddress: String) {
        dataStore.edit { preferences ->
            preferences[SAVED_DEVICE_ADDRESS] = deviceAddress
        }
    }
}
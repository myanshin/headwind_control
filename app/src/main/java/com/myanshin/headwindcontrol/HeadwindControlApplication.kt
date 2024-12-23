package com.myanshin.headwindcontrol

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.myanshin.headwindcontrol.data.AppSettingsRepository
import com.myanshin.headwindcontrol.data.BleManagerRepository

private const val APP_SETTINGS_NAME = "app_settings"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = APP_SETTINGS_NAME
)

class HeadwindControlApplication: Application() {

    lateinit var appSettingsRepository: AppSettingsRepository
    lateinit var bleManagerRepository: BleManagerRepository

    override fun onCreate() {
        super.onCreate()
        appSettingsRepository = AppSettingsRepository(dataStore)
        bleManagerRepository = BleManagerRepository(this)
    }
}
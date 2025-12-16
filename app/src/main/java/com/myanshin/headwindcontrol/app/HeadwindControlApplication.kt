package com.myanshin.headwindcontrol.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.myanshin.headwindcontrol.data.AppSettingsRepository
import com.myanshin.headwindcontrol.data.BleManagerRepository

private const val APP_SETTINGS_NAME = "app_settings"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = APP_SETTINGS_NAME
)

enum class FanMode(val code: Byte) {
    OFF(1),
    HR(2),
    SPEED(3),
    MANUAL(4);
//    SLEEP(5);

    companion object {
        fun find(code: Byte): FanMode {
            return FanMode.entries.find { it.code == code } ?: OFF
        }
    }
}

enum class MessType(val code: Byte) {
    UPD(-3),
    WR(-2);

    companion object {
        fun find(code: Byte): MessType {
            return MessType.entries.find { it.code == code } ?: UPD
        }
    }
}

enum class CommandType(val code: Byte) {
    SPEED(2),
    MODE(4);

    companion object {
        fun find(code: Byte): CommandType {
            return CommandType.entries.find { it.code == code } ?: SPEED
        }
    }
}

enum class ConnectionStatus {
    ACTIVE,
    INACTIVE,
    PENDING,
    SCANNING,
}


class HeadwindControlApplication: Application() {

    lateinit var appSettingsRepository: AppSettingsRepository
    lateinit var bleManagerRepository: BleManagerRepository

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            "running_channel",
            "Running Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableLights(false)
            enableVibration(false)
            setSound(null, null)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        appSettingsRepository = AppSettingsRepository(dataStore)
        bleManagerRepository = BleManagerRepository(this)
    }
}
package com.myanshin.headwindcontrol.app.presentation

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.myanshin.headwindcontrol.R
import com.myanshin.headwindcontrol.app.ServiceLocator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        // Return your binder here
        return null
    }

    var appViewModel: AppViewModel? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            Actions.START.toString() -> {
                start()
                appViewModel = ServiceLocator.appViewModel
                collectUIState()
            }
            Actions.STOP.toString() -> stopSelf()
            Actions.UPDATE_NOTIFICATION.toString() -> {
                val currentFanSpeed = intent.getByteExtra("SPEED", 0)
                val currentFanMode = intent.getStringExtra("MODE")
                updateNotification("Speed: $currentFanSpeed", "Mode: $currentFanMode")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createPendingIntent(context: Context, controlType: Int): PendingIntent {
        val intent = Intent(MainActivity.ACTION_FAN_CONTROL).
            putExtra("CONTROL_TYPE", controlType)
        return PendingIntent.getBroadcast(
            context,
            controlType.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createNotification(title: String, text: String): Notification {
        return NotificationCompat.Builder(this, "running_channel")
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .setSmallIcon(R.drawable.ecg_heart_24dp)
            .setOngoing(true)
            .setDefaults(0)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .addAction(
                R.drawable.speed_decrease_24dp,
                "Speed Decrease",
                createPendingIntent(this, MainActivity.Companion.FAN_CONTROL_TYPE_SPEED_DECREASE)
            )
            .addAction(
                R.drawable.speed_increase_24dp,
                "Speed Increase",
                createPendingIntent(this, MainActivity.Companion.FAN_CONTROL_TYPE_SPEED_INCREASE)
            )
            .addAction(
                R.drawable.ecg_heart_24dp,
                "Mode HR",
                createPendingIntent(this, MainActivity.Companion.FAN_CONTROL_TYPE_MODE_HR)
            )
            .build()
    }

    private fun start() {
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, createNotification("Заголовок", "Текст"))
        } else {
            startForeground(1, createNotification("Заголовок", "Текст"),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        }
    }

    private fun updateNotification(title: String, text: String) {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(1, createNotification(title, text))
    }

    private fun collectUIState() {
        GlobalScope.launch {
                appViewModel?.uiState?.collect { uiState ->
                     updateNotification("Speed: ${uiState.currentFanSpeed}","${uiState.currentFanMode}")
                }
            }
    }

    enum class Actions {
        START, STOP, UPDATE_NOTIFICATION
    }


}
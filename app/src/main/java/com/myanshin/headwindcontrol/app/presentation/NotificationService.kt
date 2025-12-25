package com.myanshin.headwindcontrol.app.presentation

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.myanshin.headwindcontrol.R
import com.myanshin.headwindcontrol.app.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NotificationService : Service() {

// coroutine scope for UI State flow collection
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var appViewModel: AppViewModel? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            Actions.START.toString() -> {
                start()
                appViewModel = ServiceLocator.appViewModel
                collectUIState()
            }
            Actions.STOP.toString() -> {
                stop()
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
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "running_channel")
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .setSmallIcon(R.drawable.mode_fan_24px)
            .setOngoing(true)
            .setDefaults(0)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(contentIntent)
            .addAction(
                R.drawable.speed_decrease_24dp,
                "Speed Decrease",
                createPendingIntent(this, MainActivity.FAN_CONTROL_TYPE_SPEED_DECREASE)
            )
            .addAction(
                R.drawable.speed_increase_24dp,
                "Speed Increase",
                createPendingIntent(this, MainActivity.FAN_CONTROL_TYPE_SPEED_INCREASE)
            )
            .addAction(
                R.drawable.ecg_heart_24dp,
                "Mode HR",
                createPendingIntent(this, MainActivity.FAN_CONTROL_TYPE_MODE_HR)
            )
            .build()
    }

    private fun start() {
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, createNotification(getString(R.string.notification_header),
                getString(
                    R.string.notification_text
                )))
        } else {
            startForeground(1, createNotification(getString(R.string.notification_header), getString(
                R.string.notification_text
            )),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        }
    }

    private fun stop() {
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification(title: String, text: String) {
        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(1, createNotification(title, text))
    }

    private fun collectUIState() {
        serviceScope.launch {
                appViewModel?.uiState?.collect { uiState ->
                    updateNotification(
                        getString(R.string.notification_header) + uiState.currentFanSpeed,
                        getString(R.string.notification_text) + uiState.currentFanMode)
                }
            }
    }

    enum class Actions {
        START, STOP
    }


}
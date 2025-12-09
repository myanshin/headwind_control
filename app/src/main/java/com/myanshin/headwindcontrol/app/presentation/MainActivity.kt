package com.myanshin.headwindcontrol.app.presentation

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.core.graphics.toRect
import androidx.lifecycle.ViewModelProvider
import com.myanshin.headwindcontrol.R
import com.myanshin.headwindcontrol.app.ServiceLocator
import com.myanshin.headwindcontrol.app.presentation.NotificationService
import com.myanshin.headwindcontrol.app.presentation.theme.HeadwindControlTheme
import com.myanshin.headwindcontrol.data.ble.BleManager

class MainActivity : ComponentActivity() {

    private lateinit var appViewModel: AppViewModel
    private val broadcastReceiver = UIBroadcastReceiver()
    var isPipModeEnabled = false
    private lateinit var pipParams: PictureInPictureParams

    companion object {
        const val ACTION_FAN_CONTROL =
            "MainActivity.ACTION_FAN_CONTROL"
        const val FAN_CONTROL_TYPE_SPEED_DECREASE = 0
        const val FAN_CONTROL_TYPE_SPEED_INCREASE = 1
        const val FAN_CONTROL_TYPE_MODE_HR = 2
    }

    private fun setContent() {
        setContent {
            HeadwindControlTheme {
                Surface(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .onGloballyPositioned { layoutCoordinates ->
                            val sourceRect =
                                layoutCoordinates.boundsInWindow().toAndroidRectF().toRect()
                            val aspectRect =
                                Rect(0, 0, sourceRect.width(), sourceRect.width() * 9 / 16)
                            pipParams = updatePictureInPictureParams(aspectRect)
                        },
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isPipModeEnabled)
                        CheckBluetoothPermissions(this) { MainScreen(appViewModel, false) }
                    else
                        MainScreen(appViewModel, true)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent()

        appViewModel = ViewModelProvider(this, AppViewModel.Factory)[AppViewModel::class.java]
        ServiceLocator.appViewModel = appViewModel

        val startIntent = Intent(this, NotificationService::class.java)
            .apply { action = NotificationService.Actions.START.toString() }
        startService(startIntent)

        val filter = IntentFilter().apply {
            addAction("BROADCAST_TEST_ACTION")
            addAction(BleManager.Companion.ACTION_FAN_STATE_RECEIVED)
            addAction(ACTION_FAN_CONTROL)
        }
        registerReceiver(broadcastReceiver, filter, RECEIVER_EXPORTED)
    }

    override fun onDestroy() {
        val intent = Intent(this, NotificationService::class.java).apply {
            action = NotificationService.Actions.STOP.toString()
        }
        startService(intent)
        super.onDestroy()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            enterPictureInPictureMode(pipParams)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        isPipModeEnabled = isInPictureInPictureMode
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        setContent()
    }

    private fun updatePictureInPictureParams(sourceRect: Rect = Rect()): PictureInPictureParams {
        val aspectRatio = Rational(16, 9)
        val pipParams = PictureInPictureParams.Builder()
            .setSourceRectHint(sourceRect)
            .setAspectRatio(aspectRatio)
            .setActions(
                listOf(
                    createRemoteAction(
                        R.drawable.speed_decrease_24dp,
                        R.string.conn_status_inactive,
                        FAN_CONTROL_TYPE_SPEED_DECREASE,
                    ),
                    createRemoteAction(
                        R.drawable.speed_increase_24dp,
                        R.string.conn_status_scanning,
                        FAN_CONTROL_TYPE_SPEED_INCREASE,
                    ),
                    createRemoteAction(
                        R.drawable.ecg_heart_24dp,
                        R.string.conn_status_scanning,
                        FAN_CONTROL_TYPE_MODE_HR,
                    )
                )
            )


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pipParams
                .setAutoEnterEnabled(true)
        }
        return pipParams.build().also {
            setPictureInPictureParams(it)
        }
    }

    private fun createRemoteAction(
        @DrawableRes iconResId: Int,
        @StringRes titleResId: Int,
        controlType: Int,
    ): RemoteAction {

        return RemoteAction(
            Icon.createWithResource(this, iconResId),
            getString(titleResId),
            getString(titleResId),
            PendingIntent.getBroadcast(
                this,
                controlType,
                Intent(ACTION_FAN_CONTROL)
                    .putExtra("CONTROL_TYPE", controlType),
                PendingIntent.FLAG_IMMUTABLE,
            ),
        )
    }

}
package com.example.headwindcontrol

import android.app.PendingIntent
import android.content.res.Configuration
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import android.graphics.drawable.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.headwindcontrol.ui.CheckBluetoothPermissions
import com.example.headwindcontrol.ui.FanMode
import com.example.headwindcontrol.ui.MainScreen
import com.example.headwindcontrol.ui.theme.HeadwindControlTheme


class MainActivity : ComponentActivity() {

    private var isPipModeEnabled = false
    private lateinit var pipParams: PictureInPictureParams

    companion object {
        private val TAG = "HW_SCAN"
        const val ACTION_FAN_CONTROL =
            "MainActivity.ACTION_FAN_CONTROL"
        const val FAN_CONTROL_TYPE_SPEED_DECREASE = 0
        const val FAN_CONTROL_TYPE_SPEED_INCREASE = 1
        const val FAN_CONTROL_TYPE_MODE_HR = 2
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            updatePictureInPictureParams()
        }

        Log.i(TAG, "APP Started")

//        enableEdgeToEdge()
        setContent {
            HeadwindControlTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CheckBluetoothPermissions(this, { MainScreen(isPipModeEnabled = isPipModeEnabled) })
                }
            }
        }
    }


    override fun onUserLeaveHint() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            pipParams = updatePictureInPictureParams()
            enterPictureInPictureMode(pipParams)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipModeEnabled = isInPictureInPictureMode
    }

    private fun updatePictureInPictureParams(): PictureInPictureParams {

        Log.i(TAG, "Maximum actions: ${this.maxNumPictureInPictureActions}")

        val aspectRatio = Rational(16, 9)
        val pipParams = PictureInPictureParams.Builder()
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
            pipParams.setAutoEnterEnabled(true)
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

        Log.i(TAG, "Request code: $controlType")
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
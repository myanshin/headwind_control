package com.example.headwindcontrol

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.headwindcontrol.ui.CheckBluetoothPermissions
import com.example.headwindcontrol.ui.MainScreen
import com.example.headwindcontrol.ui.theme.HeadwindControlTheme


class MainActivity : ComponentActivity() {


    companion object {
        private val TAG = "HW_SCAN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "APP Started")

//        enableEdgeToEdge()
        setContent {
            HeadwindControlTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CheckBluetoothPermissions(this, { MainScreen() })
                }
            }
        }
    }
}
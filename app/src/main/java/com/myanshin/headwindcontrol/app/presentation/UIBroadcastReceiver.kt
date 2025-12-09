package com.myanshin.headwindcontrol.app.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class UIBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras
        if (extras == null || extras.size() == 0) {
            Log.d("ReceivedIntent", "No extras found")
            return
        }
        for (key in extras.keySet()) {
            val value = extras.get(key)
            Log.d("ReceivedIntent", "Key: $key, Value: $value")
        }
    }
}
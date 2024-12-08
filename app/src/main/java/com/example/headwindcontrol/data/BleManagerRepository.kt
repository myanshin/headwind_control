package com.example.headwindcontrol.data

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.headwindcontrol.ble.BleManager
import com.example.headwindcontrol.ble.BleManager.LocalBinder
import com.example.headwindcontrol.ui.ConnectionStatus
import com.example.headwindcontrol.ui.FanMode
import kotlinx.coroutines.flow.update

class BleManagerRepository (private val context: Context){

    val TAG = "HW_SCAN"
    var bluetoothService : BleManager? = null

    //BLE service

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            bluetoothService = (service as LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth")
                }
            }
            Log.i(TAG, "BLE service started $bluetoothService")
        }

        override fun onServiceDisconnected(className: ComponentName) {
            bluetoothService = null
        }
    }

    private fun createBleService() {
        Log.i(TAG, "createService started")
        val gattServiceIntent = Intent(context, BleManager::class.java)
        context.bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE)
    }

    init {
        createBleService()
    }

    fun scanBleDevices() {
        Log.i(TAG, "Init scan. BLE Service $bluetoothService")
        bluetoothService?.startScan()
    }

    fun connectToFan(deviceAddress: String) {
        bluetoothService?.connect(deviceAddress)
    }

    fun disconnectFromFan() {
        bluetoothService?.disconnect()
    }

    fun setFanMode(mode: Byte) {
        bluetoothService?.writeToCharacteristic(byteArrayOf(4, mode, 0, 0))
    }

    fun setFanSpeed (speed: Byte) {
       bluetoothService?.writeToCharacteristic(byteArrayOf(2, speed, 0, 0))
    }



}
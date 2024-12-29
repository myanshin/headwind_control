package com.myanshin.headwindcontrol.ble

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.DEVICE_TYPE_LE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import java.util.UUID

class BleManager: Service() {

    private val tag = "HW_SCAN"
    private var connectionState = STATE_DISCONNECTED

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    private val serviceUuid = UUID.fromString("a026ee0c-0a7d-4ab3-97fa-f1500f9feb8b")
    private val characteristicUuid = UUID.fromString("a026e038-0a7d-4ab3-97fa-f1500f9feb8b")

    private var scanning = false
    private val scanPeriod: Long = 10000

    val devicesFound = mutableListOf<BluetoothDevice>()

    // Set up a bound service
    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService() : BleManager {
            return this@BleManager
        }
    }

    // Broadcast updates to ViewModel
    private fun broadcastUpdate(action: String, extraData: Any? = null) {
        val intent = Intent(action)

        when (extraData) {
            is ByteArray -> intent.putExtra("EXTRA_DATA", extraData)
            is Array<*> -> intent.putExtra("EXTRA_DATA", extraData)
        }
        sendBroadcast(intent)
    }

    // Init bluetooth adapter
    fun initialize(): Boolean {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter?.isEnabled == false) {
            broadcastUpdate(BLUETOOTH_DISABLED)
        }
        if (bluetoothAdapter == null) {
            Log.e(tag, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    // Bluetooth scanner results callback
    @SuppressLint("MissingPermission")
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (!devicesFound.contains(result.device) && result.device.type == DEVICE_TYPE_LE) {
                devicesFound.add(result.device)
                broadcastUpdate(ACTION_GATT_DEVICE_FOUND, arrayOf(result.device.name ?: "No name", result.device.address))
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            Handler(
                Looper.getMainLooper()).postDelayed({
                broadcastUpdate(ACTION_GATT_SCAN_FINISHED)
                scanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
                devicesFound.clear()
            }, scanPeriod)
            scanning = true
            bluetoothLeScanner?.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        devicesFound.clear()
        scanning = false
        bluetoothLeScanner?.stopScan(leScanCallback)
        bluetoothLeScanner = null
    }

    // Callback on BLE device interactions results
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED
                broadcastUpdate(ACTION_GATT_CONNECTED,
                    bluetoothGatt?.device!!.address.encodeToByteArray()
                )
                bluetoothGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
                gatt.close()
                bluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                readCharacteristic(UUID_GENERIC_ACCESS_SERVICE, UUID_DEVICE_NAME)
            } else {
                Log.e(tag,"Service discovery failed with status: $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic.uuid) {
                    UUID_DEVICE_NAME -> {
                        broadcastUpdate(ACTION_DEVICE_NAME_READ, characteristic.value)
                        setCharacteristicNotification(UUID_FAN_SPEED_SERVICE, UUID_FAN_SPEED, true)
                    }
                }
            } else {
                Log.e(tag, "Characteristic read failed with status: $status")
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_CHAR_WRITE_COMPLETE)
            } else {
                Log.e(tag, "Failed to write characteristic: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (UUID_FAN_SPEED == characteristic.uuid ) {
                val value = characteristic.value
                broadcastUpdate(ACTION_FAN_STATE_RECEIVED, value)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String): Boolean {
        bluetoothAdapter?.let { adapter ->
            try {
                if (scanning) stopScan()
                val device = adapter.getRemoteDevice(address)
                bluetoothGatt = device.connectGatt(this, false, gattCallback)
                return true
            } catch (exception: IllegalArgumentException) {
                Log.e(tag, "Device not found with provided address.")
                return false
            }
        } ?: run {
            Log.e(tag, "BluetoothAdapter not initialized")
            return false
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect() ?: Log.e(tag, "BluetoothGatt is null. Cannot disconnect.")
    }

    @SuppressLint("MissingPermission")
    private fun readCharacteristic(service: UUID, characteristic: UUID) {
        val service = bluetoothGatt?.getService(service)
        val characteristic = service?.getCharacteristic(characteristic)
        if (characteristic != null) {
            bluetoothGatt?.readCharacteristic(characteristic)
        } else {
            println("Characteristic not found")
        }
    }

    @SuppressLint("MissingPermission")
    fun writeToCharacteristic(valueToWrite: ByteArray) {
        val service = bluetoothGatt?.getService(serviceUuid)
        val characteristic = service?.getCharacteristic(characteristicUuid)
        if (characteristic != null) {
            broadcastUpdate(ACTION_GATT_CHAR_WRITE_BEGIN)
            characteristic.value = valueToWrite
            bluetoothGatt?.writeCharacteristic(characteristic)
            Log.e(tag, "Char write ${valueToWrite.toList()}")
        } else {
            Log.e(tag, "Characteristic not found!")
        }
    }

    @SuppressLint("MissingPermission")
    fun setCharacteristicNotification(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        enabled: Boolean
    ) {
        bluetoothGatt?.let { gatt ->
            val service = gatt.getService(serviceUuid)
            val characteristic = service?.getCharacteristic(characteristicUuid)
            if (characteristic != null) {
                gatt.writeCharacteristic(characteristic)
                gatt.setCharacteristicNotification(characteristic, enabled)
                if (UUID_FAN_SPEED == characteristicUuid) {
                    val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
                    descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            } else {
                Log.e(tag, "Characteristic not found!")
            }
        } ?: run {
            Log.e(tag, "BluetoothGatt not initialized")
        }
    }

    companion object {
        const val BLUETOOTH_DISABLED =
            "BleManager.BLUETOOTH_DISABLED"
        const val ACTION_GATT_SCAN_FINISHED =
            "BleManager.ACTION_GATT_SCAN_FINISHED"
        const val ACTION_GATT_CONNECTED =
            "BleManager.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "BleManager.ACTION_GATT_DISCONNECTED"
        const val ACTION_FAN_STATE_RECEIVED =
            "BleManager.ACTION_FAN_STATE_RECEIVED"
        const val ACTION_DEVICE_NAME_READ =
            "BleManager.ACTION_DEVICE_NAME_READ"
        const val ACTION_GATT_CHAR_WRITE_BEGIN =
            "BleManager.ACTION_GATT_CHAR_WRITE_BEGIN"
        const val ACTION_GATT_CHAR_WRITE_COMPLETE =
            "BleManager.ACTION_GATT_CHAR_WRITE_COMPLETE"
        const val ACTION_GATT_DEVICE_FOUND =
            "BleManager.ACTION_GATT_DEVICE_FOUND"

        private val UUID_GENERIC_ACCESS_SERVICE: UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
        private val UUID_DEVICE_NAME: UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
        private val UUID_FAN_SPEED_SERVICE: UUID = UUID.fromString("a026ee0c-0a7d-4ab3-97fa-f1500f9feb8b")
        private val UUID_FAN_SPEED: UUID = UUID.fromString("a026e038-0a7d-4ab3-97fa-f1500f9feb8b")
        private val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2
    }
}
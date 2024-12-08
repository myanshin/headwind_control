package com.example.headwindcontrol.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.headwindcontrol.HeadwindControlApplication
import com.example.headwindcontrol.ble.BleManager
import com.example.headwindcontrol.data.AppSettingsRepository
import com.example.headwindcontrol.data.BleManagerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak", "UnspecifiedRegisterReceiverFlag")
class AppViewModel(
    application: Application,
    private val appSettingsRepository: AppSettingsRepository,
    private val bleManagerRepository: BleManagerRepository
) : ViewModel() {

    private val TAG = "HW_SCAN"
    private val context: Context = application.applicationContext
    private val _uiState = MutableStateFlow(AppUiState())

    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // Action on receiving broadcast messages from BleManager
    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BleManager.ACTION_GATT_CONNECTED -> {
                    Log.i(TAG, "GATT CONNECTED")
                    _uiState.update { currentState ->
                        currentState.copy(
                            isDeviceConnected = true,
                            connectionStatus = ConnectionStatus.ACTIVE,
                            connectedDeviceName = intent.getByteArrayExtra("EXTRA_DATA")!!.decodeToString()
                        )
                    }
                }
                BleManager.ACTION_GATT_DISCONNECTED -> {
                    Log.i(TAG, "GATT DISCONNECTED")
                    _uiState.update { currentState ->
                        currentState.copy(
                            isDeviceConnected = false,
                            connectionStatus = ConnectionStatus.INACTIVE,
                            connectedDeviceName = ""
                        )
                    }
                }
                BleManager.ACTION_GATT_SERVICES_DISCOVERED -> {
                    Log.i(TAG, "GATT SERVICES DISCOVERED")

                }
                BleManager.ACTION_FAN_STATE_RECEIVED -> {
                    val currentFanSpeed = intent.getByteArrayExtra("EXTRA_DATA")!![2]
                    val currentFanMode = FanMode.find(intent.getByteArrayExtra("EXTRA_DATA")!![3])

                    Log.i(TAG, "Speed ${currentFanSpeed}, mode $currentFanMode")

                    _uiState.update { currentState ->
                        currentState.copy(
                            currentFanSpeed = if (currentFanSpeed == 1.toByte() && currentFanMode !in arrayOf(FanMode.HR,
                                    FanMode.SPEED))
                                _uiState.value.currentFanSpeed else currentFanSpeed,
                            currentFanMode = currentFanMode ?: FanMode.OFF
                        )
                    }
                }
                BleManager.ACTION_DEVICE_NAME_READ -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            connectedDeviceName = intent.getByteArrayExtra("EXTRA_DATA")!!.decodeToString()
                        )
                    }
                }
                BleManager.ACTION_GATT_CHAR_WRITE_BEGIN -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            waitForCharWrite = true
                        )
                    }
                    viewModelScope.launch {
                        delay(5000)
                        if (_uiState.value.waitForCharWrite) {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    waitForCharWrite = false
                                )
                            }
                        }
                    }
                }
                BleManager.ACTION_GATT_CHAR_WRITE_COMPLETE -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            waitForCharWrite = false,
                        )
                    }
                    val requestedFanSpeed = _uiState.value.requestedFanSpeed
                    if (requestedFanSpeed.toInt() != 0) {
                        Log.i(TAG, "Send speed change request $requestedFanSpeed")
                        _uiState.update { currentState ->
                            currentState.copy(
                                requestedFanSpeed = 0,
                            )
                        }
                        setFanSpeed(requestedFanSpeed)
                    }
                }
                BleManager.ACTION_GATT_DEVICE_FOUND -> {
                    if (_uiState.value.connectionStatus == ConnectionStatus.SCANNING) {
                        intent.getStringArrayExtra("EXTRA_DATA")?.let {
                            val devicesFound = _uiState.value.devicesFound.toMutableList()
                            devicesFound.add(it)
                            _uiState.update { currentState ->
                                currentState.copy(
                                    devicesFound = devicesFound
                                )
                            }

                        }
                    }
                }
                BleManager.ACTION_GATT_SCAN_FINISHED -> {
                    if (_uiState.value.connectionStatus == ConnectionStatus.SCANNING) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                connectionStatus = ConnectionStatus.INACTIVE,
                            )
                        }
                    }
                }
            }
        }
    }

    init {
        // Init broadcast messages receiver from BleManager
        context.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter(), RECEIVER_EXPORTED)
        collectAppSettingsFlow()
    }

    // Collect settings flow from AppSettingsRepository
    private fun collectAppSettingsFlow() {
        viewModelScope.launch {
            appSettingsRepository.savedDeviceAddress.collect { newAddress ->
                _uiState.update { currentState ->
                    currentState.copy(
                        savedDeviceAddress = newAddress
                    )
                }
            }
        }
    }

    // Fan control functions
    fun scanBleDevices() {
        _uiState.update { currentState ->
            currentState.copy(
                connectionStatus = ConnectionStatus.SCANNING,
                devicesFound = listOf<Array<String>>()
            )
        }
        return bleManagerRepository.scanBleDevices()
    }

    fun connectToFan(deviceAddress: String) {

        _uiState.update { currentState ->
            currentState.copy(
                connectionStatus = ConnectionStatus.PENDING,
                devicesFound = listOf<Array<String>>()
            )
        }

        if (deviceAddress != _uiState.value.savedDeviceAddress) {
            viewModelScope.launch {
                appSettingsRepository.saveDeviceAddress(deviceAddress)
            }
        }
            return bleManagerRepository.connectToFan(deviceAddress)
    }

    fun disconnectFromFan() {
        return bleManagerRepository.disconnectFromFan()
    }

    fun setFanMode(mode: Byte) {
        return bleManagerRepository.setFanMode(mode)
    }

    fun setFanSpeed (speed: Byte) {
        if (_uiState.value.currentFanMode == FanMode.MANUAL) {
            return bleManagerRepository.setFanSpeed(speed)
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    requestedFanSpeed = speed.toByte()
                )
            }
            return bleManagerRepository.setFanMode(FanMode.MANUAL.code)
        }
    }

    // BleManager messages filter
    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BleManager.ACTION_GATT_SCAN_FINISHED)
            addAction(BleManager.ACTION_GATT_CONNECTED)
            addAction(BleManager.ACTION_GATT_DISCONNECTED)
            addAction(BleManager.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(BleManager.ACTION_FAN_STATE_RECEIVED)
            addAction(BleManager.ACTION_DEVICE_NAME_READ)
            addAction(BleManager.ACTION_GATT_CHAR_WRITE_BEGIN)
            addAction(BleManager.ACTION_GATT_CHAR_WRITE_COMPLETE)
            addAction(BleManager.ACTION_GATT_DEVICE_FOUND)
        }
    }

    // ViewModel custom init
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as HeadwindControlApplication)
                AppViewModel(application, application.appSettingsRepository, application.bleManagerRepository)
            }
        }
    }
}


package com.myanshin.headwindcontrol.ui

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.myanshin.headwindcontrol.CommandType
import com.myanshin.headwindcontrol.ConnectionStatus
import com.myanshin.headwindcontrol.FanMode
import com.myanshin.headwindcontrol.HeadwindControlApplication
import com.myanshin.headwindcontrol.MainActivity
import com.myanshin.headwindcontrol.MessType
import com.myanshin.headwindcontrol.ble.BleManager
import com.myanshin.headwindcontrol.data.AppSettingsRepository
import com.myanshin.headwindcontrol.data.BleManagerRepository
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

    private val context: Context = application.applicationContext
    private val _uiState = MutableStateFlow(AppUiState())

    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // Action on receiving broadcast messages from BleManager
    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BleManager.BLUETOOTH_DISABLED -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isBtAdapterEnabled = false,
                        )
                    }
                }
                BleManager.ACTION_GATT_CONNECTED -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isDeviceConnected = true,
                            connectionStatus = ConnectionStatus.ACTIVE,
//                            connectedDeviceName = intent.getByteArrayExtra("EXTRA_DATA")!!.decodeToString()
                        )
                    }
                }
                BleManager.ACTION_GATT_DISCONNECTED -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isDeviceConnected = false,
                            connectionStatus = ConnectionStatus.INACTIVE,
                            connectedDeviceName = "",
                            currentFanSpeed = 0
                        )
                    }
                }
                BleManager.ACTION_FAN_STATE_RECEIVED -> {

                    if (intent.getByteArrayExtra("EXTRA_DATA")?.size !=4 )
                        return

                    val messType = MessType.find(intent.getByteArrayExtra("EXTRA_DATA")!![0])
                    if (messType == MessType.UPD) {
                        Log.i("HW_SCAN", "${intent.getByteArrayExtra("EXTRA_DATA")?.toList()}. Waiting to write: ${_uiState.value.waitForCharWrite}")
                        val newCurrentFanSpeed = intent.getByteArrayExtra("EXTRA_DATA")!![2]
                        val newCurrentFanMode = FanMode.find(intent.getByteArrayExtra("EXTRA_DATA")!![3])
                        if ((newCurrentFanSpeed != _uiState.value.currentFanSpeed
                            || newCurrentFanMode != _uiState.value.currentFanMode)
                            && !_uiState.value.waitForCharWrite) {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    currentFanSpeed = newCurrentFanSpeed,
                                    currentFanMode = newCurrentFanMode
                                )
                            }
                        }
                    } else if (messType == MessType.WR) {
                        Log.w("HW_SCAN", "${intent.getByteArrayExtra("EXTRA_DATA")?.toList()}")
                        val commandType = CommandType.find(intent.getByteArrayExtra("EXTRA_DATA")!![1])
                        if (commandType == CommandType.MODE) {
                            val newCurrentFanMode =
                                FanMode.find(intent.getByteArrayExtra("EXTRA_DATA")!![3])
                            _uiState.update { currentState ->
                                currentState.copy(
                                    currentFanMode = newCurrentFanMode,
                                    waitForCharWrite = false
                                )
                            }

                            if (newCurrentFanMode == FanMode.MANUAL && _uiState.value.requestedFanSpeed != 0.toByte()) {

                                val requestedFanSpeed = _uiState.value.requestedFanSpeed
                                Log.i("HW_SCAN", "Should change speed to $requestedFanSpeed")
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        requestedFanSpeed = 0,
                                        waitForCharWrite = true
                                    )
                                }
                                setFanSpeed(requestedFanSpeed)
                            }
                        }
                        else if (commandType == CommandType.SPEED) {
                            val newCurrentFanSpeed =
                                intent.getByteArrayExtra("EXTRA_DATA")!![3]
                            _uiState.update { currentState ->
                                currentState.copy(
                                    currentFanSpeed = newCurrentFanSpeed,
                                    waitForCharWrite = false
                                )
                            }
                        }

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
//                    viewModelScope.launch {
//                        delay(5000)
//                        if (_uiState.value.waitForCharWrite) {
//                            _uiState.update { currentState ->
//                                currentState.copy(
//                                    waitForCharWrite = false
//                                )
//                            }
//                        }
//                    }
                }
//                BleManager.ACTION_GATT_CHAR_WRITE_COMPLETE -> {
//                    _uiState.update { currentState ->
//                        currentState.copy(
//                            waitForCharWrite = false,
//                        )
//                    }
//                    val requestedFanSpeed = _uiState.value.requestedFanSpeed
//                    if (requestedFanSpeed != 0.toByte()) {
//                        _uiState.update { currentState ->
//                            currentState.copy(
//                                requestedFanSpeed = 0,
//                            )
//                        }
//                        setFanSpeed(requestedFanSpeed)
//                    }
//                }
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
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isBtAdapterEnabled = false,
                                    connectionStatus = ConnectionStatus.INACTIVE
                                )
                            }
                        }
                        BluetoothAdapter.STATE_ON -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isBtAdapterEnabled = true,
                                )
                            }
                        }
                    }
                }
                LocationManager.PROVIDERS_CHANGED_ACTION -> {
                    checkLocationEnabled()
                }
                MainActivity.ACTION_FAN_CONTROL -> {
                    val controlType =  intent.getIntExtra("CONTROL_TYPE", 0)
                    when (controlType) {
                        MainActivity.FAN_CONTROL_TYPE_MODE_HR -> {
                            setFanMode(FanMode.HR)
                        }
                        MainActivity.FAN_CONTROL_TYPE_SPEED_DECREASE -> {
                            when  {
                                uiState.value.currentFanSpeed > 15 -> setFanSpeed((uiState.value.currentFanSpeed - 10).toByte())
                                uiState.value.currentFanSpeed > 9  -> setFanSpeed((uiState.value.currentFanSpeed - 5).toByte())
                                uiState.value.currentFanSpeed > 6  -> setFanSpeed(4.toByte())
                                else -> setFanSpeed(0.toByte())
                            }
                        }
                        MainActivity.FAN_CONTROL_TYPE_SPEED_INCREASE -> {
                            if (uiState.value.currentFanSpeed < 10)
                                setFanSpeed((uiState.value.currentFanSpeed + 5).toByte())
                            else if (uiState.value.currentFanSpeed <= 90)
                                setFanSpeed((uiState.value.currentFanSpeed + 10).toByte())
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
        checkLocationEnabled()
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
                devicesFound = listOf()
            )
        }
        return bleManagerRepository.scanBleDevices()
    }

    fun connectToFan(deviceAddress: String) {
        _uiState.update { currentState ->
            currentState.copy(
                connectionStatus = ConnectionStatus.PENDING,
                devicesFound = listOf()
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

    fun setFanMode(mode: FanMode) {
        return bleManagerRepository.setFanMode(mode.code)
    }

    fun setFanSpeed (speed: Byte) {
        if (_uiState.value.currentFanMode == FanMode.MANUAL) {
            return bleManagerRepository.setFanSpeed(speed)
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    requestedFanSpeed = speed
                )
            }
            return setFanMode(FanMode.MANUAL)
        }
    }

    private fun checkLocationEnabled() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        _uiState.update { currentState ->
            currentState.copy(
                isLocationEnabled = locationManager.isLocationEnabled
            )
        }
    }

    // BleManager messages filter
    private fun makeGattUpdateIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(BleManager.BLUETOOTH_DISABLED)
            addAction(BleManager.ACTION_GATT_SCAN_FINISHED)
            addAction(BleManager.ACTION_GATT_CONNECTED)
            addAction(BleManager.ACTION_GATT_DISCONNECTED)
            addAction(BleManager.ACTION_FAN_STATE_RECEIVED)
            addAction(BleManager.ACTION_DEVICE_NAME_READ)
            addAction(BleManager.ACTION_GATT_CHAR_WRITE_BEGIN)
//            addAction(BleManager.ACTION_GATT_CHAR_WRITE_COMPLETE)
            addAction(BleManager.ACTION_GATT_DEVICE_FOUND)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(MainActivity.ACTION_FAN_CONTROL)
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


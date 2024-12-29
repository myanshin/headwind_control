package com.myanshin.headwindcontrol.ui
import com.myanshin.headwindcontrol.ConnectionStatus
import com.myanshin.headwindcontrol.FanMode

data class AppUiState(
    val isBtAdapterEnabled: Boolean = true,
    val isLocationEnabled: Boolean = false,
    val savedDeviceAddress: String = "",
    val connectedDeviceName: String = "",
    val currentFanSpeed: Byte = 0,
    val requestedFanSpeed: Byte = 0,
    val currentFanMode: FanMode = FanMode.OFF,
    val isDeviceConnected: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.INACTIVE,
    val waitForCharWrite: Boolean = false,
    val devicesFound: List<Array<String>> = listOf()
)

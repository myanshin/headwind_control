package com.myanshin.headwindcontrol.app.presentation
import com.myanshin.headwindcontrol.app.ConnectionStatus
import com.myanshin.headwindcontrol.app.FanMode

data class AppUiState(
    val isBtAdapterEnabled: Boolean = true,
    val isLocationEnabled: Boolean = false,
    val savedDeviceAddress: String = "",
    val connectedDeviceName: String = "",
    val currentFanSpeed: Byte = 0,
    val requestedFanSpeed: Byte = -1,
    val currentFanMode: FanMode = FanMode.OFF,
    val isDeviceConnected: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.INACTIVE,
    val waitForCharWrite: Boolean = false,
    val devicesFound: List<Array<String>> = listOf()
)

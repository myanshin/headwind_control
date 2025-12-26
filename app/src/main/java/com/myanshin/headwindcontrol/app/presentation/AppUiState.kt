package com.myanshin.headwindcontrol.app.presentation
import com.myanshin.headwindcontrol.app.ConnectionStatus
import com.myanshin.headwindcontrol.app.FanMode

data class AppUiState(
    val isNotificationEnabled: Boolean? = null,
    val isBtAdapterEnabled: Boolean = true,
    val isLocationEnabled: Boolean = false,
    val savedDeviceAddress: String = "",
    val connectedDeviceName: String = "",
    val currentFanSpeed: Int = 0,
    val requestedFanSpeed: Int = -1,
    val currentFanMode: FanMode = FanMode.OFF,
    val connectionStatus: ConnectionStatus = ConnectionStatus.INACTIVE,
    val waitForCharWrite: Boolean = false,
    val devicesFound: List<Array<String>> = listOf()
)

package com.example.headwindcontrol.ui

enum class FanMode(val code: Byte) {
    OFF(1),
    HR(2),
    SPEED(3),
    MANUAL(4),
    SLEEP(5);

    companion object {
        fun find(code: Byte): FanMode? {
            return FanMode.entries.find { it.code == code }
        }
    }
}

enum class ConnectionStatus {
    ACTIVE,
    INACTIVE,
    PENDING,
    SCANNING
}

data class AppUiState(
    val savedDeviceAddress: String = "",
    val connectedDeviceName: String = "",
    val currentFanSpeed: Byte = 0,
    val requestedFanSpeed: Byte = 0,
    val currentFanMode: FanMode = FanMode.OFF,
    val isDeviceConnected: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.INACTIVE,
    val waitForCharWrite: Boolean = false,
    val devicesFound: List<Array<String>> = listOf<Array<String>>()
)

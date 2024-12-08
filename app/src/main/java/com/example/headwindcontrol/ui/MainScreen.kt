package com.example.headwindcontrol.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.headwindcontrol.HeadwindControlApplication


@Composable
fun MainScreen(appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)) {
    val appUiState by appViewModel.uiState.collectAsState()

    Column (
        modifier = Modifier.padding(start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {

        Row (
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp, bottom = 10.dp),
        ) {

            ConstraintLayout (
                modifier = Modifier.fillMaxWidth()
            ){

                val (deviceConnected, scanButton) = createRefs()
                DeviceConnected(appUiState.savedDeviceAddress, appUiState.connectedDeviceName,
                    modifier = Modifier.constrainAs(deviceConnected) {
                        centerTo(parent)
                    })
                Button(
                    enabled = appUiState.connectionStatus in arrayOf(ConnectionStatus.INACTIVE),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(48.dp)
                        .constrainAs(scanButton) {
                            end.linkTo(parent.end)
                        },
                    onClick = { appViewModel.scanBleDevices() },

                ) {
                    Icon(Icons.Filled.Search, "Scan for devices", modifier = Modifier.size(20.dp))
                }

            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {

            Row (

            ){
                Column (
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ){
//                SpeedButton(appUiState.waitForCharWrite, appUiState.currentFanSpeed,
//                    1, "1", appUiState.connectionStatus) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }
                    SpeedButton(
                        appUiState.waitForCharWrite, appUiState.currentFanSpeed, appUiState.currentFanMode,
                        5, "5", appUiState.connectionStatus
                    ) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }
                    SpeedButton(
                        appUiState.waitForCharWrite, appUiState.currentFanSpeed, appUiState.currentFanMode,
                        10, "10", appUiState.connectionStatus
                    ) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }
                    SpeedButton(
                        appUiState.waitForCharWrite, appUiState.currentFanSpeed, appUiState.currentFanMode,
                        15, "15", appUiState.connectionStatus
                    ) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }
//                    SpeedButton(
//                        appUiState.waitForCharWrite, appUiState.currentFanSpeed,
//                        20, "20", appUiState.connectionStatus
//                    ) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }

                }
            }
            IndeterminateCircularIndicator(
                appUiState.connectionStatus,
                appUiState.currentFanSpeed
            ) {
                if (appUiState.connectionStatus == ConnectionStatus.INACTIVE) {
                    appViewModel.connectToFan(appUiState.savedDeviceAddress)
                } else {
                    appViewModel.disconnectFromFan()
                }
            }
            Column {
                ModeButton(
                    appUiState.waitForCharWrite, appUiState.currentFanMode.code,
                    FanMode.SPEED.code, "SPD", appUiState.connectionStatus
                ) { fanMode -> appViewModel.setFanMode(fanMode) }
                ModeButton(
                    appUiState.waitForCharWrite, appUiState.currentFanMode.code,
                    FanMode.HR.code, "HR", appUiState.connectionStatus
                ) { fanMode -> appViewModel.setFanMode(fanMode) }
                ModeButton(
                    appUiState.waitForCharWrite, appUiState.currentFanMode.code,
                    FanMode.OFF.code, "OFF", appUiState.connectionStatus
                ) { fanMode -> appViewModel.setFanMode(fanMode) }
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(top = 20.dp).fillMaxWidth()
        ) {

            SpeedButton(
                appUiState.waitForCharWrite, appUiState.currentFanSpeed, appUiState.currentFanMode,
                20, "20", appUiState.connectionStatus
            ) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }
            SpeedButton(
                appUiState.waitForCharWrite, appUiState.currentFanSpeed, appUiState.currentFanMode,
                25, "25", appUiState.connectionStatus
            ) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }
            SpeedButton(
                appUiState.waitForCharWrite, appUiState.currentFanSpeed, appUiState.currentFanMode,
                35, "35", appUiState.connectionStatus
            ) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }
            SpeedButton(
                appUiState.waitForCharWrite, appUiState.currentFanSpeed, appUiState.currentFanMode,
                50, "50", appUiState.connectionStatus
            ) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }
            SpeedButton(
                appUiState.waitForCharWrite, appUiState.currentFanSpeed, appUiState.currentFanMode,
                75, "75", appUiState.connectionStatus
            ) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }
            SpeedButton(
                appUiState.waitForCharWrite, appUiState.currentFanSpeed, appUiState.currentFanMode,
                100, "100", appUiState.connectionStatus
            ) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }

        }
        DevicesList(appUiState.devicesFound) { deviceAddress -> appViewModel.connectToFan(deviceAddress) }
    }
}


@Composable
fun ModeButton(
    waitForCharWrite: Boolean,
    currentFanMode: Byte,
    onClickFanMode: Byte,
    buttonText: String,
    connectionStatus: ConnectionStatus,
    callback: (Byte) -> Unit
) {
    Button(
        onClick = {
            callback(onClickFanMode)
        },
        enabled = !waitForCharWrite && connectionStatus == ConnectionStatus.ACTIVE,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (currentFanMode == onClickFanMode) MaterialTheme.colorScheme.tertiary else Color.Unspecified,
            contentColor = Color.White,
        ),
        modifier = Modifier.width(80.dp)
    )
        {
            Text(text = buttonText)
        }
}


@Composable
fun SpeedButton(
    waitForCharWrite: Boolean,
    currentFanSpeed: Byte,
    currentFanMode: FanMode,
    onClickFanSpeed: Byte,
    buttonText: String,
    connectionStatus: ConnectionStatus,
    callback: (Byte) -> Unit
) {
    Button(
        onClick = {
            callback(onClickFanSpeed)
        },
        enabled = !waitForCharWrite && connectionStatus == ConnectionStatus.ACTIVE,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (currentFanSpeed == onClickFanSpeed && currentFanMode == FanMode.MANUAL)
                MaterialTheme.colorScheme.tertiary else Color.Unspecified,
            contentColor = Color.White,
        ),
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(48.dp)
    )
    {
        Text(text = buttonText)
    }

}

@Composable
fun IndeterminateCircularIndicator(connectionStatus: ConnectionStatus, currentSpeed: Byte, callback: () -> Unit) {

    Box(
        modifier = Modifier
            .size(150.dp)
            .padding(start = 0.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center

    ) {
            Log.i("HW_SCAN", "Current Fan Speed ${currentSpeed.toFloat()/100}")
        if (connectionStatus == ConnectionStatus.PENDING) {
            CircularProgressIndicator(
                modifier = Modifier.width(150.dp).height(150.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 5.dp,
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.width(150.dp).height(150.dp).clickable { callback() },
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 5.dp,
                progress = { currentSpeed.toFloat()/100 }
            )
        }
        Text(
            text = if (connectionStatus == ConnectionStatus.PENDING)
                "Connecting..." else if (connectionStatus == ConnectionStatus.ACTIVE) currentSpeed.toString() else "Connect",
            color = if (connectionStatus == ConnectionStatus.ACTIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontSize = if (connectionStatus == ConnectionStatus.ACTIVE) 40.sp else 15.sp,
            fontWeight = if (connectionStatus == ConnectionStatus.ACTIVE) FontWeight.Normal else FontWeight.Bold
        )
    }
}


@Composable
fun DeviceConnected(
    savedDeviceAddress: String,
    connectedDeviceName: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (connectedDeviceName != "") connectedDeviceName else savedDeviceAddress,
        fontSize = 20.sp,
        lineHeight = 20.sp,
        modifier = modifier,
        color = if (connectedDeviceName == "") Color.Gray else MaterialTheme.colorScheme.primary,
        fontWeight = if (connectedDeviceName == "") FontWeight.Normal else FontWeight.Bold,

    )
}


@SuppressLint("MissingPermission")
@Composable
fun DevicesList(
    devicesList: List<Array<String>>,
    modifier: Modifier = Modifier,
    callback: (String) -> Unit
) {

    Column (
        horizontalAlignment = Alignment.Start
    ){
        for (device in devicesList) {
            val clickable = "HEADWIND" in device[0]
            Column (
                modifier = Modifier.padding(vertical = 10.dp).clickable { if (clickable) callback(device[1])}
            ){
                Text(
                    text = device[0],
                    fontSize = 17.sp,
                    lineHeight = 19.sp,
                    color = if (clickable) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = modifier.padding(end = 10.dp)
                )
                Text(
                    text = device[1],
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    color = Color.Gray,
                )
            }
            HorizontalDivider()
        }
    }
}
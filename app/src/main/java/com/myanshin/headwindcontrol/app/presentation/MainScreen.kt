package com.myanshin.headwindcontrol.app.presentation

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Slider
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.myanshin.headwindcontrol.app.ConnectionStatus
import com.myanshin.headwindcontrol.app.FanMode
import com.myanshin.headwindcontrol.R
import kotlin.math.roundToInt


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MainScreen(
    appViewModel: AppViewModel,
    isPipModeEnabled: Boolean,
    enterPiP: () -> Unit
) {
    val appUiState by appViewModel.uiState.collectAsState()

    if (isPipModeEnabled) {
        PipWindow(appUiState)
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
    ) {
        Header(appUiState, enterPiP)
        Column(
            modifier = Modifier.verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            NotificationSearchPanel(appViewModel, appUiState)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .fillMaxWidth()
            ) {
                val speeds = listOf(5, 10, 15, 20, 25)
                for (speed in speeds) {
                    SpeedButton(
                        appUiState,
                        speed,
                    ) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row {
                    Column {
                        val modes = listOf(FanMode.SPEED, FanMode.HR, FanMode.OFF)
                        for (mode in modes) {
                            ModeButton(
                                appUiState,
                                mode,
                            ) { fanMode -> appViewModel.setFanMode(fanMode) }
                        }
                    }
                }
                IndeterminateCircularIndicator(appUiState) {
                    if (
                        appUiState.connectionStatus == ConnectionStatus.INACTIVE
                        && appUiState.savedDeviceAddress != ""
                    ) {
                        appViewModel.connectToFan(appUiState.savedDeviceAddress)
                    } else {
                        appViewModel.disconnectFromFan()
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val speeds = listOf(30, 40, 50)
                    for (speed in speeds) {
                        SpeedButton(
                            appUiState,
                            speed,
                        ) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 20.dp)
                    .fillMaxWidth()
            ) {
                val speeds = listOf(100, 90, 80, 70, 60)
                for (speed in speeds) {
                    SpeedButton(
                        appUiState,
                        speed,
                    ) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }
                }
            }
            SpeedSlider(appUiState) { fanSpeed -> appViewModel.setFanSpeed(fanSpeed) }
            DevicesList(appUiState) { deviceAddress -> appViewModel.connectToFan(deviceAddress) }
        }
    }

}

@Composable
fun Header(appUiState: AppUiState, enterPiP: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (logo, deviceConnected, scanButton) = createRefs()
            Column(
                modifier = Modifier.constrainAs(logo) {
                    start.linkTo(parent.start)
                    centerVerticallyTo(parent)
                }
            ) {
                Row {
                    Text(
                        text = "Headwind",
                        fontSize = 12.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row {
                    Text(
                        text = "Control",
                        fontSize = 12.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (appUiState.isBtAdapterEnabled) {
                DeviceConnected(
                    appUiState,
                    modifier = Modifier.constrainAs(deviceConnected) {
                        centerTo(parent)
                    })
            } else {
                Text(
                    text = stringResource(R.string.bluetooth_disabled),
                    fontSize = 18.sp,
                    color = Color.Red,
                    modifier = Modifier.constrainAs(deviceConnected) {
                        centerTo(parent)
                    }
                )
            }
            WindowButton(
                Modifier.constrainAs(scanButton) {
                    end.linkTo(parent.end)
                },
            ) { enterPiP() }
        }
    }
}
@Composable
fun NotificationSearchPanel(appViewModel: AppViewModel, appUiState: AppUiState) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 20.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            NotificationSwitch(appUiState.isNotificationEnabled == true) { enabled ->
                appViewModel.toggleNotificationEnabled(enabled)
            }
            Text(
                modifier = Modifier.padding(start = 5.dp),
                text = stringResource(R.string.notifications),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchButton(appUiState) { appViewModel.scanBleDevices() }
            Text(
                modifier = Modifier.padding(start = 5.dp, end = 10.dp),
                text = stringResource(R.string.ble_search),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun DeviceConnected(
    s: AppUiState,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (s.connectedDeviceName != "") s.connectedDeviceName else s.savedDeviceAddress,
        fontSize = if (s.connectedDeviceName != "") 20.sp else 18.sp,
        lineHeight = 20.sp,
        modifier = modifier,
        color = if (s.connectedDeviceName == "") MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.primary,
        fontWeight = if (s.connectedDeviceName == "") FontWeight.Normal else FontWeight.Bold,
        )
}

@Composable
fun ModeButton(
    s: AppUiState,
    onClickFanMode: FanMode,
    modifier: Modifier = Modifier,
    callback: (FanMode) -> Unit
) {
    TextButton(
        onClick = {
            callback(onClickFanMode)
        },
        enabled = isButtonEnabled(s),
        modifier = modifier.width(60.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (s.currentFanMode == onClickFanMode) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary
        ),
    )
    {
        Text(
            text = onClickFanMode.uiText,
            fontWeight = if (s.currentFanMode == onClickFanMode && s.connectionStatus == ConnectionStatus.ACTIVE)
                FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = if (s.currentFanMode == onClickFanMode && s.connectionStatus == ConnectionStatus.ACTIVE)
                17.sp else 15.sp
        )
    }
}


@Composable
fun SpeedButton(
    s: AppUiState,
    onClickFanSpeed: Int,
    modifier: Modifier = Modifier,
    callback: (Int) -> Unit
) {
    TextButton(
        onClick = {
            callback(onClickFanSpeed)
        },
        enabled = isButtonEnabled(s),
        modifier = modifier.width(60.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (s.currentFanSpeed == onClickFanSpeed && s.currentFanMode == FanMode.MANUAL) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondary
        ),
        contentPadding = PaddingValues(0.dp)
    )
    {
        Text(
            fontWeight = if (s.currentFanSpeed == onClickFanSpeed && s.currentFanMode == FanMode.MANUAL)
                FontWeight.ExtraBold else FontWeight.SemiBold,
            text = onClickFanSpeed.toString(),
            fontSize = if (s.currentFanSpeed == onClickFanSpeed && s.currentFanMode == FanMode.MANUAL)
                19.sp else 17.sp
        )
    }

}

@Composable
fun SpeedSlider(
    s: AppUiState,
    callback: (Int) -> Unit
) {
    var sliderPosition by remember { mutableFloatStateOf( s.currentFanSpeed.toFloat()) }
    var fanSpeed by remember { mutableIntStateOf(s.currentFanSpeed) }
    if (fanSpeed != s.currentFanSpeed) {
        sliderPosition = s.currentFanSpeed.toFloat()
        @Suppress("AssignedValueIsNeverRead")
        fanSpeed = s.currentFanSpeed
    }
    Column(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp))
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp))
            .padding(10.dp)

    ) {
        Text(
            modifier = Modifier.padding(start = 5.dp),
            text = stringResource(R.string.slider_speed) + sliderPosition.roundToInt().toString(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = if (isButtonEnabled(s)) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        Slider(
            enabled = isButtonEnabled(s),
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
            },
            onValueChangeFinished = {
                val intValue = sliderPosition.roundToInt()
                callback(intValue)
            },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                disabledInactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                inactiveTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        )
    }
}

@Composable
fun SearchButton(
    s: AppUiState,
    modifier: Modifier = Modifier,
    callback: () -> Unit
) {
    Button(
        enabled = s.connectionStatus == ConnectionStatus.INACTIVE &&
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || s.isLocationEnabled) &&
                s.isBtAdapterEnabled,
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .size(48.dp),
        onClick = { callback() },
        ) {
        Icon(
            Icons.Filled.Search,
            "Scan for devices",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun WindowButton(
    modifier: Modifier = Modifier,
    callback: () -> Unit
) {
    IconButton(
        modifier = modifier.size(48.dp),
        onClick = { callback() }
    ) {
        Icon(
            painterResource(R.drawable.close_fullscreen_24px),
            "To window mode",
            tint = Color.DarkGray
        )
    }
}

@Composable
fun NotificationSwitch(isNotificationEnabled: Boolean, callback: (Boolean) -> Unit) {
    var checked by remember { mutableStateOf(false) }
    checked = isNotificationEnabled
    Switch(
        checked = checked,
        onCheckedChange = {
            checked = it
            callback(checked)
        }
    )
}

@Composable
fun IndeterminateCircularIndicator(
    s: AppUiState,
    callback: () -> Unit) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .padding(start = 0.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center

    ) {

        val circleText = when (s.connectionStatus) {
            ConnectionStatus.PENDING -> stringResource(R.string.conn_status_pending)
            ConnectionStatus.SCANNING -> stringResource(R.string.conn_status_scanning)
            ConnectionStatus.ACTIVE -> s.currentFanSpeed.toString()
            ConnectionStatus.INACTIVE -> stringResource(R.string.conn_status_inactive)
        }

        if (s.connectionStatus in arrayOf(ConnectionStatus.PENDING, ConnectionStatus.SCANNING)) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 5.dp,
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
                    .clickable(
                        enabled = s.isBtAdapterEnabled && s.savedDeviceAddress != ""
                    ) { callback() },
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 5.dp,
                progress = { s.currentFanSpeed.toFloat()/100 }
            )
        }

        val textColor = if (!s.isBtAdapterEnabled || s.savedDeviceAddress == "") {
            Color.Gray
        } else if (s.connectionStatus == ConnectionStatus.ACTIVE) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondary
        }

        Text(
            modifier = Modifier.padding(horizontal = 10.dp),
            text = circleText,
            textAlign = TextAlign.Center,
            color = textColor,
            fontSize = if (s.connectionStatus == ConnectionStatus.ACTIVE) 40.sp else 15.sp,
            fontWeight = if (s.connectionStatus == ConnectionStatus.ACTIVE) FontWeight.Normal else FontWeight.Bold
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DevicesList(
    s: AppUiState,
    modifier: Modifier = Modifier,
    callback: (String) -> Unit
) {

    if (
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S
        && !s.isLocationEnabled
        && s.connectionStatus == ConnectionStatus.INACTIVE
        ) {
        Text(
            text = stringResource(R.string.enable_location_service),
            color = Color.Red,
            textAlign = TextAlign.Center
        )
    }

    Column (
        horizontalAlignment = Alignment.Start
    ){

        if (s.devicesFound.isNotEmpty()) {
            Text(
                text = stringResource(R.string.ble_devices_found),
                fontSize = 19.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                modifier = modifier.padding(bottom = 5.dp, top = 10.dp)
            )
        }

        for (device in s.devicesFound) {
            val clickable = "HEADWIND" in device[0]
            Column (
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .clickable { if (clickable) callback(device[1]) }
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

@Composable
fun PipWindow(appUiState: AppUiState) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Text(
            text = appUiState.currentFanMode.uiText,
            color = if (appUiState.connectionStatus == ConnectionStatus.ACTIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = appUiState.currentFanSpeed.toString(),
            color = if (appUiState.connectionStatus == ConnectionStatus.ACTIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            fontSize = 40.sp,
            modifier = Modifier.padding(end = 30.dp)
        )
    }
}

fun isButtonEnabled(s: AppUiState) = !s.waitForCharWrite && s.connectionStatus == ConnectionStatus.ACTIVE && s.connectedDeviceName != ""
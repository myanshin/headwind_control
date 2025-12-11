package com.myanshin.headwindcontrol.app.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.myanshin.headwindcontrol.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


val requiredPermissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.POST_NOTIFICATIONS
        )
    }
    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

@Composable
fun CheckBluetoothPermissions(activity: Activity, composable: @Composable () -> Unit ) {
        var locationPermissionsGranted by remember { mutableStateOf(areLocationPermissionsAlreadyGranted(activity))
    }

    var shouldShowPermissionRationale by remember {
        mutableStateOf(
            requiredPermissions.all { shouldShowRequestPermissionRationale(activity, it)}
        )
    }

    var shouldDirectUserToApplicationSettings by remember {
        mutableStateOf(false)
    }

    var currentPermissionsStatus by remember {
        mutableStateOf(decideCurrentPermissionStatus(locationPermissionsGranted, shouldShowPermissionRationale))
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            locationPermissionsGranted = permissions.values.reduce { acc, isPermissionGranted ->
                acc && isPermissionGranted
            }

            if (!locationPermissionsGranted) {
                shouldShowPermissionRationale =
                    requiredPermissions.all { shouldShowRequestPermissionRationale(activity, it)}
            }
            shouldDirectUserToApplicationSettings = !shouldShowPermissionRationale && !locationPermissionsGranted
            currentPermissionsStatus = decideCurrentPermissionStatus(locationPermissionsGranted, shouldShowPermissionRationale)
        })

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START &&
                !locationPermissionsGranted &&
                !shouldShowPermissionRationale) {
                locationPermissionLauncher.launch(requiredPermissions)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    )

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }


    Scaffold(snackbarHost = {
        SnackbarHost(hostState = snackBarHostState)
    }) {
            contentPadding ->
        if (!locationPermissionsGranted) {
            Column(modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.padding(20.dp))
                Text(
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    fontSize = 20.sp,
                    text = stringResource(R.string.permissions_required),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            composable()
        }

        if (shouldShowPermissionRationale) {
            LaunchedEffect(Unit) {
                scope.launch {
                    val userAction = snackBarHostState.showSnackbar(
                        message = activity.getString(R.string.authorize_permissions),
                        actionLabel = activity.getString(R.string.approve),
                        duration = SnackbarDuration.Indefinite,
                        withDismissAction = true
                    )
                    when (userAction) {
                        SnackbarResult.ActionPerformed -> {
                            shouldShowPermissionRationale = false
                            locationPermissionLauncher.launch(requiredPermissions)
                        }
                        SnackbarResult.Dismissed -> {
                            shouldShowPermissionRationale = false
                        }
                    }
                }
            }
        }
        if (shouldDirectUserToApplicationSettings) {
            LaunchedEffect(Unit) {
                delay(3000) // Delay of 2 seconds
                openApplicationSettings(activity)
            }

        }
    }
}

@SuppressLint("InlinedApi")
private fun areLocationPermissionsAlreadyGranted(activity: Activity): Boolean {
    return requiredPermissions.all {
        ContextCompat.checkSelfPermission(activity, it) ==
                PackageManager.PERMISSION_GRANTED
    }
}

private fun openApplicationSettings(activity: Activity) {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.packageName, null)).also {
        startActivity(activity, it, null)
    }
}

private fun decideCurrentPermissionStatus(locationPermissionsGranted: Boolean,
                                          shouldShowPermissionRationale: Boolean): String {
    return if (locationPermissionsGranted) "Granted"
    else if (shouldShowPermissionRationale) "Rejected"
    else "Denied"
}
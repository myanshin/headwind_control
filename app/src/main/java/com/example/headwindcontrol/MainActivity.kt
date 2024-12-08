package com.example.headwindcontrol

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.headwindcontrol.ui.MainScreen
import com.example.headwindcontrol.ui.theme.HeadwindControlTheme
import android.Manifest


class MainActivity : ComponentActivity() {


    companion object {
        private const val REQUEST_CODE_BLUETOOTH_PERMISSIONS = 1001 // Define a unique request code
        private val TAG = "HW_SCAN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkBluetoothPermissions()
        Log.i(TAG, "APP Started")

//        enableEdgeToEdge()
        setContent {
            HeadwindControlTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    private fun checkBluetoothPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        // Check for needed permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        // Location permissions may still be needed for Bluetooth scanning
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        // If permissions are needed, request them
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                REQUEST_CODE_BLUETOOTH_PERMISSIONS
            )
        }
    }
}
//
//@SuppressLint("MissingPermission")
//@Composable
//fun MainScreen(name: String, modifier: Modifier = Modifier) {

//    val context: Context = LocalContext.current
//    val bleManager: BleManager = BleManager(context)

//    val bluetoothDevices = remember { mutableStateListOf<BluetoothDevice>() }

//    Column {
//        for (device in bluetoothDevices) {
//            Text(text = device.name ?: "Unknown Device")
//        }


//    Check how this works
//    val coroutineScope = rememberCoroutineScope()



//    Column(
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = modifier
//    ) {
//        Text(
//            text = "Hello $name!",
//            fontSize = 50.sp,
//            lineHeight = 60.sp,
//            textAlign = TextAlign.Center,
////            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
//        )
//        Text(
//            text = "Hello $name!",
//            fontSize = 50.sp,
//            lineHeight = 60.sp,
//            modifier = Modifier
//                .padding(16.dp)
////                .align(alignment = Alignment.End)
//        )
//    }
//    }

//    @Preview(showBackground = true)
//    @Composable
//    fun MainScreenPreview() {
//        HeadwindControlTheme {
//            Surface(
//                modifier = Modifier.fillMaxSize(),
//                color = MaterialTheme.colorScheme.background
//            ) {
//                MainScreen(name = "Test1", modifier = Modifier.background(color = Color.Gray))
//            }
//        }
//    }
//}
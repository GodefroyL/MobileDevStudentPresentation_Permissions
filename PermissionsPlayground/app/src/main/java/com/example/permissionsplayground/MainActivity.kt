package com.example.permissionsplayground

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PermissionDemoScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDemoScreen() {
    val cameraPermission = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val photoPermission = rememberPermissionState(permission = Manifest.permission.READ_MEDIA_IMAGES)
    val locationPermission = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    var message by remember { mutableStateOf("Select an action below.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Permissions Playground", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            if (cameraPermission.status.isGranted) {
                message = "Camera permission already granted!"
            } else cameraPermission.launchPermissionRequest()
        }) {
            Text("Request Camera Permission")
        }
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (photoPermission.status.isGranted) {
                message = "Photo permission already granted!"
            } else photoPermission.launchPermissionRequest()
        }) {
            Text("Request Photo Permission")
        }
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (locationPermission.status.isGranted) {
                message = "Location permission already granted!"
            } else locationPermission.launchPermissionRequest()
        }) {
            Text("Request Location Permission")
        }

        Spacer(Modifier.height(32.dp))
        Text(message, fontSize = 16.sp)
    }
}

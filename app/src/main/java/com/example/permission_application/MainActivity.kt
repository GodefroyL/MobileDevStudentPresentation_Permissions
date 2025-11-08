package com.example.permission_application

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // UI
    private lateinit var btnSms: Button
    private lateinit var btnLocation: Button
    private lateinit var btnCamera: Button
    private lateinit var btnStorage: Button
    private lateinit var btnNotifPolicy: Button

    private lateinit var tvSmsStatus: TextView
    private lateinit var tvLocationStatus: TextView
    private lateinit var tvCameraStatus: TextView
    private lateinit var tvStorageStatus: TextView
    private lateinit var tvNotifPolicyStatus: TextView

    private val TAG = "PermDemo"

    // launcher that can request multiple permissions and returns a Map<permission, Boolean>
    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Log results for debugging
        results.forEach { (perm, granted) ->
            Log.d(TAG, "Permission result => $perm : $granted")
        }
        // update UI
        updateAllStatuses()
        // show summary toast
        val grantedCount = results.values.count { it }
        val deniedCount = results.size - grantedCount
        Toast.makeText(this, "$grantedCount granted, $deniedCount denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // bind views
        btnSms = findViewById(R.id.btnSms)
        btnLocation = findViewById(R.id.btnLocation)
        btnCamera = findViewById(R.id.btnCamera)
        btnStorage = findViewById(R.id.btnStorage)
        btnNotifPolicy = findViewById(R.id.btnNotifPolicy)

        tvSmsStatus = findViewById(R.id.tvSmsStatus)
        tvLocationStatus = findViewById(R.id.tvLocationStatus)
        tvCameraStatus = findViewById(R.id.tvCameraStatus)
        tvStorageStatus = findViewById(R.id.tvStorageStatus)
        tvNotifPolicyStatus = findViewById(R.id.tvNotifPolicyStatus)

        // set click listeners
        btnSms.setOnClickListener { requestPermission(Manifest.permission.READ_SMS) }
        btnLocation.setOnClickListener { requestPermission(Manifest.permission.ACCESS_FINE_LOCATION) }
        btnCamera.setOnClickListener { requestPermission(Manifest.permission.CAMERA) }
        btnStorage.setOnClickListener { requestStoragePermission() }

        // Notification policy access - special handling (opens system settings)
        btnNotifPolicy.setOnClickListener { handleNotificationPolicyAccess() }
    }

    override fun onResume() {
        super.onResume()
        updateAllStatuses()
    }

    // Update all status TextViews
    private fun updateAllStatuses() {
        tvSmsStatus.text = permissionStatusText(Manifest.permission.READ_SMS)
        tvLocationStatus.text = permissionStatusText(Manifest.permission.ACCESS_FINE_LOCATION)
        tvCameraStatus.text = permissionStatusText(Manifest.permission.CAMERA)
        // For storage show combined status depending on Android version
        tvStorageStatus.text = storageStatusText()
        tvNotifPolicyStatus.text = if (isNotificationPolicyAccessGranted()) "Granted" else "Not granted"
    }

    // Return "Granted" or "Not granted" for a normal permission
    private fun permissionStatusText(permission: String): String {
        val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        return if (granted) "Granted" else "Not granted"
    }

    // Storage status helper that considers Android 13+ behavior
    private fun storageStatusText(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val img = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
            val vid = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
            val aud = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (img && vid && aud) "Granted" else "Not granted"
        } else {
            permissionStatusText(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // Generic permission requester (single)
    private fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Already granted", Toast.LENGTH_SHORT).show()
            updateAllStatuses()
            return
        }
        permissionsLauncher.launch(arrayOf(permission))
    }

    // Storage requester that handles Android 13+ names
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // On Android 13+, request READ_MEDIA_* permissions
            val perms = arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
            permissionsLauncher.launch(perms)
        } else {
            // below Android 13 use the legacy READ_EXTERNAL_STORAGE
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // Notification policy: check and open the settings screen if not granted
    private fun isNotificationPolicyAccessGranted(): Boolean {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }

    private fun handleNotificationPolicyAccess() {
        if (isNotificationPolicyAccessGranted()) {
            Toast.makeText(this, "Notification policy access already granted", Toast.LENGTH_SHORT).show()
            return
        }
        // Open the special settings screen where user can grant "Do Not Disturb" access
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivity(intent)
    }
}

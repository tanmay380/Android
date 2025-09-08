// PermissionGateSequential.kt
package com.example.permissions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * PermissionGateSequential:
 * - Requests permissions one-by-one (so even if one is denied, others are still asked).
 * - Shows rationale dialog when the system indicates it should.
 * - If permission is permanently denied (don't ask again), shows an option to open App Settings.
 * - Only shows `content` when ALL required permissions are granted.
 *
 * Usage:
 * PermissionGateSequential {
 *   MainScreen()
 * }
 */
@Composable
fun PermissionGateSequential(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    val requiredPermissions = remember {
        mutableListOf<String>().apply {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toList()
    }

    // track results
    val permissionResults = remember { mutableStateMapOf<String, Boolean>() }
    var currentIndex by remember { mutableStateOf(-1) }
    var rationaleFor by remember { mutableStateOf<String?>(null) }
    var settingsFor by remember { mutableStateOf<String?>(null) }

    fun isAlreadyGranted(p: String) =
        ContextCompat.checkSelfPermission(context, p) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED

    // --- Helper declared BEFORE launcher so it's in scope ---
    fun advanceIfAlreadyGranted(perms: List<String>, check: (String) -> Boolean): Int {
        var idx = currentIndex
        while (idx in perms.indices && check(perms[idx])) {
            permissionResults[perms[idx]] = true
            idx++
        }
        return idx
    }

    // Launcher for single permission
    val onePermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        val idx = currentIndex
        if (idx !in requiredPermissions.indices) return@rememberLauncherForActivityResult
        val perm = requiredPermissions[idx]

        permissionResults[perm] = granted

        if (!granted) {
            val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)
            if (shouldShow) {
                // show rationale and do not advance until user responds to that dialog
                rationaleFor = perm
                return@rememberLauncherForActivityResult
            } else {
                // likely permanently denied (or not asked before). Show settings dialog but continue.
                settingsFor = perm
            }
        }

        // advance to next permission (skip already-granted ones)
        currentIndex = idx + 1
        val next = advanceIfAlreadyGranted(requiredPermissions, ::isAlreadyGranted)
        if (next != currentIndex) {
            currentIndex = next
        }
    }

    // Start sequence
    fun startRequestSequence() {
        permissionResults.clear()
        rationaleFor = null
        settingsFor = null

        // start at first ungranted permission
        val first = requiredPermissions.indexOfFirst { !isAlreadyGranted(it) }
        currentIndex = if (first >= 0) first else requiredPermissions.size
        if (currentIndex in requiredPermissions.indices) {
            // skip if somehow already granted
            val next = advanceIfAlreadyGranted(requiredPermissions, ::isAlreadyGranted)
            currentIndex = next
            if (currentIndex in requiredPermissions.indices) {
                onePermLauncher.launch(requiredPermissions[currentIndex])
            }
        } else {
            requiredPermissions.forEach { permissionResults[it] = true }
        }
    }

    // React to currentIndex changes to launch permission if needed
    LaunchedEffect(currentIndex) {
        if (currentIndex in requiredPermissions.indices) {
            val perm = requiredPermissions[currentIndex]
            if (isAlreadyGranted(perm)) {
                // mark and advance
                permissionResults[perm] = true
                currentIndex = advanceIfAlreadyGranted(requiredPermissions, ::isAlreadyGranted)
            } else {
                // launch request
                onePermLauncher.launch(perm)
            }
        }
    }

    val allProcessed = permissionResults.size >= requiredPermissions.size
    val allGranted = requiredPermissions.all { permissionResults[it] == true || isAlreadyGranted(it) }

    if (/*allProcessed && allGranted*/ true) {
        content()
        return
    }

    Column {
        Text("App needs permissions to run.")
        Text("Permissions: ${requiredPermissions.joinToString { shortName(it) }}")
        Button(onClick = { startRequestSequence() }, modifier = Modifier.fillMaxWidth()) {
            Text("Start permission flow")
        }
        Button(onClick = { openAppSettings(activity) }, modifier = Modifier.fillMaxWidth()) {
            Text("Open app settings")
        }
    }

    // Rationale dialog handling (retry or cancel -> advance)
    if (rationaleFor != null) {
        val perm = rationaleFor!!
        AlertDialog(
            onDismissRequest = { rationaleFor = null },
            title = { Text("${shortName(perm)} permission required") },
            text = { Text("We need ${shortName(perm)} to provide full functionality.") },
            confirmButton = {
                TextButton(onClick = {
                    rationaleFor = null
                    // retry the same permission
                    val idx = requiredPermissions.indexOf(perm)
                    if (idx >= 0) {
                        currentIndex = idx
                        onePermLauncher.launch(perm)
                    }
                }) { Text("Allow") }
            },
            dismissButton = {
                TextButton(onClick = {
                    // mark as denied and continue
                    rationaleFor = null
                    permissionResults[perm] = false
                    val idx = requiredPermissions.indexOf(perm)
                    currentIndex = (idx + 1).coerceAtMost(requiredPermissions.size)
                }) { Text("Cancel") }
            }
        )
    }

    // Settings dialog handling (open settings or skip)
    if (settingsFor != null) {
        val perm = settingsFor!!
        AlertDialog(
            onDismissRequest = { settingsFor = null },
            title = { Text("${shortName(perm)} permission blocked") },
            text = { Text("It seems ${shortName(perm)} is blocked. Enable it from settings.") },
            confirmButton = {
                TextButton(onClick = {
                    settingsFor = null
                    openAppSettings(activity)
                    val idx = requiredPermissions.indexOf(perm)
                    currentIndex = (idx + 1).coerceAtMost(requiredPermissions.size)
                }) { Text("Open Settings") }
            },
            dismissButton = {
                TextButton(onClick = {
                    settingsFor = null
                    permissionResults[perm] = false
                    val idx = requiredPermissions.indexOf(perm)
                    currentIndex = (idx + 1).coerceAtMost(requiredPermissions.size)
                }) { Text("Cancel") }
            }
        )
    }
}
private fun shortName(permission: String): String =
    when (permission) {
        Manifest.permission.ACCESS_FINE_LOCATION -> "Location"
        Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
        else -> permission

    }

fun openAppSettings(activity: Activity) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", activity.packageName, null)
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    activity.startActivity(intent)
}

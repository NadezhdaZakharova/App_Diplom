package com.example.diplom.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.app.Activity
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.diplom.R

/**
 * Для API 29+: перед системным диалогом показываем объяснение; при отказе — обучение и «Настройки».
 * Для API &lt; 29 шаги не требуют runtime-разрешения — сразу [onGranted].
 */
@Composable
fun ActivityRecognitionPermissionGate(
    onGranted: () -> Unit,
    onPermissionLost: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivityOrNull() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val permission = Manifest.permission.ACTIVITY_RECOGNITION

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || activity == null) {
        LaunchedEffect(Unit) { onGranted() }
        content()
        return
    }

    fun checkGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    var hasPermission by remember { mutableStateOf(checkGranted()) }
    var showRationaleIntro by rememberSaveable { mutableStateOf(true) }
    var postDeny by remember { mutableStateOf<PostDeny?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            postDeny = null
        } else {
            postDeny = PostDeny(
                ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val now = checkGranted()
                if (now != hasPermission) {
                    hasPermission = now
                    if (now) postDeny = null
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) onGranted() else onPermissionLost()
    }

    if (hasPermission) {
        content()
        return
    }

    val openAppSettings: () -> Unit = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    if (postDeny != null) {
        val info = postDeny!!
        val title = stringResource(R.string.activity_recognition_denied_title)
        val body = if (info.canAskAgain) {
            stringResource(R.string.activity_recognition_denied_can_ask_again)
        } else {
            stringResource(R.string.activity_recognition_denied_settings_only)
        }
        AlertDialog(
            onDismissRequest = { postDeny = null },
            title = { Text(title) },
            text = { Text(body) },
            confirmButton = {
                TextButton(onClick = {
                    postDeny = null
                    openAppSettings()
                }) {
                    Text(stringResource(R.string.activity_recognition_open_settings))
                }
            },
            dismissButton = {
                if (info.canAskAgain) {
                    TextButton(onClick = {
                        postDeny = null
                        permissionLauncher.launch(permission)
                    }) {
                        Text(stringResource(R.string.activity_recognition_try_again))
                    }
                } else {
                    TextButton(onClick = { postDeny = null }) {
                        Text(stringResource(R.string.activity_recognition_got_it))
                    }
                }
            }
        )
    } else if (showRationaleIntro) {
        AlertDialog(
            onDismissRequest = {
                showRationaleIntro = false
            },
            title = { Text(stringResource(R.string.activity_recognition_rationale_title)) },
            text = { Text(stringResource(R.string.activity_recognition_rationale_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleIntro = false
                    permissionLauncher.launch(permission)
                }) {
                    Text(stringResource(R.string.activity_recognition_allow))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleIntro = false }) {
                    Text(stringResource(R.string.activity_recognition_not_now))
                }
            }
        )
    }

    content()
}

private data class PostDeny(val canAskAgain: Boolean)

private fun android.content.Context.findActivityOrNull(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

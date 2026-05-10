package com.example.diplom.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

/**
 * Запрос [Manifest.permission.POST_NOTIFICATIONS] (API 33+), чтобы показывать
 * уведомления о цели по шагах и бонусе. Без разрешения баннер на экране наград всё равно работает.
 */
@Composable
fun PostNotificationsPermissionEffect() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val permission = Manifest.permission.POST_NOTIFICATIONS

    fun granted(): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    var alreadyRequested by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (alreadyRequested) return@LaunchedEffect
        delay(1200)
        alreadyRequested = true
        if (!granted()) {
            launcher.launch(permission)
        }
    }
}

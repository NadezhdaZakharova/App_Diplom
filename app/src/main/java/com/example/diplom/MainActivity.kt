package com.example.diplom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.diplom.data.sensor.StepCounterManager
import com.example.diplom.ui.DiplomApp
import com.example.diplom.ui.MainViewModel
import com.example.diplom.ui.theme.DiplomTheme
import com.example.diplom.work.DailyWorkScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var dailyWorkScheduler: DailyWorkScheduler
    private lateinit var stepCounterManager: StepCounterManager
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            stepCounterManager.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stepCounterManager = StepCounterManager(applicationContext) { delta ->
            mainViewModel.addSteps(delta)
        }
        dailyWorkScheduler.scheduleDailyRecalculation()
        enableEdgeToEdge()
        setContent {
            DiplomTheme {
                DiplomApp(mainViewModel)
            }
        }
        handleSendIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSendIntent(intent)
    }

    private fun handleSendIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        val mime = intent.type.orEmpty()
        if (mime.isNotBlank() && !mime.startsWith("text/")) return
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            ?: intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        if (!text.isNullOrBlank()) {
            mainViewModel.importWorkoutFromShareIntent(text)
        }
    }

    override fun onStart() {
        super.onStart()
        if (requiresActivityPermission() && !hasActivityPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            stepCounterManager.start()
        }
    }

    override fun onStop() {
        stepCounterManager.stop()
        super.onStop()
    }

    private fun requiresActivityPermission(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private fun hasActivityPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
}

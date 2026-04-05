package com.example.diplom

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.diplom.app.AppContainer
import com.example.diplom.data.sensor.StepCounterManager
import com.example.diplom.ui.DiplomApp
import com.example.diplom.ui.MainViewModel
import com.example.diplom.ui.theme.DiplomTheme

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.factory(
            activityRepository = container.activityRepository,
            gamificationRepository = container.gamificationRepository,
            trainingRepository = container.trainingRepository
        )
    }
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
        container = AppContainer(applicationContext)
        stepCounterManager = StepCounterManager(applicationContext) { delta ->
            mainViewModel.addSteps(delta)
        }
        container.scheduleDailyRecalculation(applicationContext)
        enableEdgeToEdge()
        setContent {
            DiplomTheme {
                DiplomApp(mainViewModel)
            }
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

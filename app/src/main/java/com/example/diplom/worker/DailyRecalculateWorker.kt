package com.example.diplom.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.diplom.di.BootstrapGameUseCaseEntryPoint
import dagger.hilt.android.EntryPointAccessors

class DailyRecalculateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return runCatching {
            val bootstrap = EntryPointAccessors.fromApplication(
                applicationContext,
                BootstrapGameUseCaseEntryPoint::class.java
            ).bootstrapGameUseCase()
            bootstrap()
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }

    companion object {
        const val UNIQUE_WORK_NAME = "daily_game_recalculate"
    }
}

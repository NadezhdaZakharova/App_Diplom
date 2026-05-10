package com.example.diplom.domain.repository

import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.model.StudentRewardsStats
import com.example.diplom.domain.model.WorkoutExercise
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    fun observeUserMode(): Flow<AppUserMode>
    suspend fun setUserMode(mode: AppUserMode)
    fun observeExerciseBank(): Flow<List<Exercise>>
    suspend fun seedExerciseBankIfEmpty()
    suspend fun addExercise(title: String, description: String, defaultReps: Int)
    suspend fun updateExercise(id: Long, title: String, description: String, defaultReps: Int)
    fun observeSelfWorkoutToday(): Flow<List<WorkoutExercise>>
    fun observeTrainerWorkoutToday(): Flow<List<WorkoutExercise>>
    suspend fun addExerciseToSelfWorkout(exercise: Exercise)
    suspend fun addExerciseToTrainerWorkout(exercise: Exercise)
    suspend fun removeWorkoutItem(id: Long)
    suspend fun moveWorkoutItem(id: Long, moveDown: Boolean)
    suspend fun importTrainerWorkoutFromJson(json: String): Result<Unit>
    suspend fun exportTrainerWorkoutAsJson(): String
    suspend fun recordCompletedStudentWorkout(items: List<WorkoutExercise>, fromTrainerPlan: Boolean)
    suspend fun ensureFirstInstallDateRecorded()
    fun observeStudentRewardsStats(): Flow<StudentRewardsStats>
    fun observeStepsConversionPromptVisible(): Flow<Boolean>
    suspend fun declineStepsToWorkoutConversion()
    suspend fun recordWorkoutFromStepsConversion()
}

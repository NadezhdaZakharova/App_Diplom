package com.example.diplom.domain.repository

import com.example.diplom.domain.model.Achievement
import com.example.diplom.domain.model.DailyStats
import com.example.diplom.domain.model.PlayerProfile
import com.example.diplom.domain.model.StoryChapter
import com.example.diplom.domain.model.WeeklyChallenge
import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.model.StudentRewardsStats
import com.example.diplom.domain.model.WorkoutExercise
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeToday(): Flow<DailyStats>
    fun observeRecentDays(limit: Int = 7): Flow<List<DailyStats>>
    fun observeDailyGoal(): Flow<Int>
    suspend fun addSteps(steps: Int)
    suspend fun setDailyGoal(steps: Int)
}

interface GamificationRepository {
    fun observeProfile(): Flow<PlayerProfile>
    fun observeAchievements(): Flow<List<Achievement>>
    fun observeChapters(): Flow<List<StoryChapter>>
    fun observeWeeklyChallenge(): Flow<WeeklyChallenge>
    suspend fun seedIfEmpty()
    suspend fun recalculate()
}

interface LeaderboardRepository {
    suspend fun topPlayers(): List<String>
}

interface SyncRepository {
    suspend fun syncNow(): Boolean
}

interface TrainingRepository {
    fun observeUserMode(): Flow<AppUserMode>
    suspend fun setUserMode(mode: AppUserMode)
    fun observeExerciseBank(): Flow<List<Exercise>>
    suspend fun seedExerciseBankIfEmpty()
    suspend fun addExercise(title: String, description: String, defaultReps: Int)
    fun observeSelfWorkoutToday(): Flow<List<WorkoutExercise>>
    fun observeTrainerWorkoutToday(): Flow<List<WorkoutExercise>>
    suspend fun addExerciseToSelfWorkout(exercise: Exercise)
    suspend fun addExerciseToTrainerWorkout(exercise: Exercise)
    suspend fun removeWorkoutItem(id: Long)
    suspend fun importTrainerWorkoutFromJson(json: String): Result<Unit>
    suspend fun exportTrainerWorkoutAsJson(): String
    suspend fun recordCompletedStudentWorkout(items: List<WorkoutExercise>, fromTrainerPlan: Boolean)
    suspend fun ensureFirstInstallDateRecorded()
    fun observeStudentRewardsStats(): Flow<StudentRewardsStats>
}

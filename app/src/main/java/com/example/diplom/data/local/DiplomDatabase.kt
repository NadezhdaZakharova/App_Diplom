package com.example.diplom.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DailyActivityEntity::class,
        UserSettingsEntity::class,
        AchievementEntity::class,
        StoryChapterEntity::class,
        WeeklyChallengeEntity::class,
        ExerciseEntity::class,
        PlannedWorkoutEntity::class,
        CompletedWorkoutSessionEntity::class,
        ExerciseCompletionEventEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class DiplomDatabase : RoomDatabase() {
    abstract fun dao(): DiplomDao

    companion object {
        @Volatile
        private var INSTANCE: DiplomDatabase? = null

        fun getInstance(context: Context): DiplomDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DiplomDatabase::class.java,
                    "diplom.db"
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
    }
}

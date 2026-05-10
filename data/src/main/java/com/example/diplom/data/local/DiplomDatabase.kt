package com.example.diplom.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Локальное хранилище. Схема экспортируется в `app/schemas` (KSP, см. app/build.gradle.kts).
 *
 * Обновление версии: добавить [Migration] в [DiplomDatabaseMigrations], увеличить [version].
 */
@Database(
    entities = [
        DailyActivityEntity::class,
        UserSettingsEntity::class,
        AchievementEntity::class,
        WeeklyChallengeEntity::class,
        ExerciseEntity::class,
        PlannedWorkoutEntity::class,
        CompletedWorkoutSessionEntity::class,
        ExerciseCompletionEventEntity::class
    ],
    version = 9,
    exportSchema = true
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
                )
                    .addMigrations(*DiplomDatabaseMigrations.ALL)
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

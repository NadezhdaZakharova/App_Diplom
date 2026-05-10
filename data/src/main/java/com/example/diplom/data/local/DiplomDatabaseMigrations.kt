package com.example.diplom.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Цепочка миграций Room. При изменении сущностей: увеличить [DiplomDatabase.version],
 * добавить новую [Migration], сгенерировать схему (KSP) и при необходимости SQL в migrate {}.
 */
object DiplomDatabaseMigrations {

    /**
     * Версия 8: без изменения таблиц — фиксируем переход на миграции вместо destructive fallback.
     */
    val MIGRATION_7_8: Migration = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // noop
        }
    }

    val MIGRATION_8_9: Migration = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE user_settings ADD COLUMN stepsToWorkoutOfferDeclinedDateIso TEXT NOT NULL DEFAULT ''"
            )
            db.execSQL("ALTER TABLE exercise_bank ADD COLUMN titleKey TEXT")
            db.execSQL("ALTER TABLE exercise_completion_event ADD COLUMN titleKey TEXT")
            db.execSQL("ALTER TABLE planned_workout ADD COLUMN titleKey TEXT")
            db.execSQL(
                "UPDATE exercise_bank SET titleKey = 'preset_squats' WHERE title = 'Приседания'"
            )
            db.execSQL(
                "UPDATE exercise_bank SET titleKey = 'preset_pushups' WHERE title = 'Отжимания'"
            )
            db.execSQL(
                "UPDATE exercise_bank SET titleKey = 'preset_plank' WHERE title = 'Планка'"
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_7_8, MIGRATION_8_9)
}

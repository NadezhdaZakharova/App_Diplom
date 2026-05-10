package com.example.diplom.data.repository

import com.example.diplom.domain.TrainingConstraints
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONException
import org.json.JSONObject

/**
 * Разбор поля времени (секунды) при импорте JSON тренировки тренера.
 * Вынесено из [TrainingRepositoryImpl] для тестируемости и единой ответственности.
 */
@Singleton
class TrainerWorkoutPlannedSecondsParser @Inject constructor() {

    fun parse(item: JSONObject, exerciseOrdinal: Int): Int {
        val key = when {
            item.has("plannedSeconds") -> "plannedSeconds"
            item.has("plannedReps") -> "plannedReps"
            else -> throw IllegalArgumentException(
                "Импорт невозможен: в упражнении $exerciseOrdinal задайте время в секундах (поле plannedSeconds или устаревшее plannedReps)."
            )
        }
        val raw = try {
            item.get(key)
        } catch (e: JSONException) {
            throw IllegalArgumentException(
                "Импорт невозможен: в упражнении $exerciseOrdinal не удалось прочитать поле $key.",
                e
            )
        }
        if (raw === JSONObject.NULL) {
            throw IllegalArgumentException(
                "Импорт невозможен: в упражнении $exerciseOrdinal поле $key пустое."
            )
        }
        val seconds = when (raw) {
            is Number -> {
                val d = raw.toDouble()
                val v = d.toInt()
                if (d.isNaN() || d != v.toDouble()) {
                    throw IllegalArgumentException(
                        "Импорт невозможен: в упражнении $exerciseOrdinal поле $key должно быть целым числом секунд."
                    )
                }
                v
            }
            is String -> {
                raw.trim().toIntOrNull() ?: throw IllegalArgumentException(
                    "Импорт невозможен: в упражнении $exerciseOrdinal поле $key должно быть положительным целым числом секунд."
                )
            }
            else -> throw IllegalArgumentException(
                "Импорт невозможен: в упражнении $exerciseOrdinal недопустимый тип значения в поле $key (ожидается число секунд)."
            )
        }
        if (seconds <= 0) {
            throw IllegalArgumentException(
                "Импорт невозможен: в упражнении $exerciseOrdinal время должно быть больше 0 секунд."
            )
        }
        if (seconds > TrainingConstraints.MAX_EXERCISE_DURATION_SECONDS) {
            throw IllegalArgumentException(
                "Импорт невозможен: в упражнении $exerciseOrdinal слишком большое значение времени (максимум ${TrainingConstraints.MAX_EXERCISE_DURATION_SECONDS} с)."
            )
        }
        return seconds
    }
}

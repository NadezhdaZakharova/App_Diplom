package com.example.diplom.data.repository

import com.example.diplom.data.local.PlannedWorkoutEntity
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Разбор и сбор JSON плана «от тренера». Репозиторий только сохраняет [PlannedWorkoutEntity] через DAO.
 */
@Singleton
class TrainerWorkoutJsonStore @Inject constructor(
    private val plannedSecondsParser: TrainerWorkoutPlannedSecondsParser
) {
    fun parseTrainerWorkoutEntities(json: String, todayIso: String): List<PlannedWorkoutEntity> {
        val root = try {
            JSONObject(json)
        } catch (e: JSONException) {
            throw IllegalArgumentException(
                "Импорт невозможен: файл не является корректным JSON или имеет неверную структуру.",
                e
            )
        }
        val trainerArray = root.optJSONArray("trainerWorkout")
        val plannedArray = root.optJSONArray("plannedWorkout") ?: JSONArray()
        val fromDedicatedTrainerFile = trainerArray != null && trainerArray.length() > 0
        val sourceArray = if (fromDedicatedTrainerFile) trainerArray else plannedArray
        val trainerItems = mutableListOf<PlannedWorkoutEntity>()
        repeat(sourceArray.length()) { index ->
            val item = try {
                sourceArray.getJSONObject(index)
            } catch (e: JSONException) {
                throw IllegalArgumentException(
                    "Импорт невозможен: элемент ${index + 1} в списке упражнений не является JSON-объектом.",
                    e
                )
            }
            if (!fromDedicatedTrainerFile) {
                val itemDate = item.optString("dateIso", todayIso)
                if (itemDate != todayIso) return@repeat
                val itemType = item.optString("workoutType", WORKOUT_TRAINER)
                if (itemType != WORKOUT_TRAINER) return@repeat
            }
            val title = item.optString("title", "").trim()
            if (title.isEmpty()) {
                throw IllegalArgumentException(
                    "Импорт невозможен: у упражнения ${index + 1} отсутствует или пустое поле title."
                )
            }
            val plannedSeconds = plannedSecondsParser.parse(item, index + 1)
            val titleKey = item.optString("titleKey", "").trim().ifEmpty { null }
            trainerItems += PlannedWorkoutEntity(
                dateIso = todayIso,
                exerciseId = item.optLong("exerciseId", 0L),
                title = title,
                description = extractImportedDescription(item),
                plannedReps = plannedSeconds,
                sortOrder = item.optInt("sortOrder", index),
                workoutType = WORKOUT_TRAINER,
                titleKey = titleKey
            )
        }
        if (trainerItems.isEmpty()) {
            throw IllegalArgumentException(
                "Импорт невозможен: не найдено ни одного упражнения для загрузки (проверьте структуру файла, дату и поля title / plannedSeconds)."
            )
        }
        return trainerItems
            .sortedWith(compareBy<PlannedWorkoutEntity> { it.sortOrder }.thenBy { it.exerciseId })
            .mapIndexed { idx, entity -> entity.copy(sortOrder = idx) }
    }

    fun buildExportJson(rows: List<PlannedWorkoutEntity>): String {
        val root = JSONObject()
        root.put("schemaVersion", 1)
        root.put(
            "trainerWorkout",
            JSONArray().apply {
                rows.forEach {
                    put(
                        JSONObject()
                            .put("dateIso", it.dateIso)
                            .put("exerciseId", it.exerciseId)
                            .put("title", it.title)
                            .put("description", it.description)
                            .put("plannedSeconds", it.plannedReps)
                            .put("sortOrder", it.sortOrder)
                            .put("workoutType", it.workoutType)
                            .apply {
                                if (it.titleKey != null) put("titleKey", it.titleKey)
                            }
                    )
                }
            }
        )
        return root.toString(2)
    }

    private companion object {
        const val WORKOUT_TRAINER = "TRAINER"
    }
}

private fun extractImportedDescription(item: JSONObject): String {
    if (!item.has("description")) return ""
    return when (val v = item.opt("description")) {
        null, JSONObject.NULL -> ""
        else -> v.toString().trim()
    }
}

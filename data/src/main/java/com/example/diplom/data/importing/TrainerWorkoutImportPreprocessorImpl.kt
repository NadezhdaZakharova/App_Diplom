package com.example.diplom.data.importing

import com.example.diplom.domain.TrainerWorkoutImportPreprocessor
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class TrainerWorkoutImportPreprocessorImpl @Inject constructor() : TrainerWorkoutImportPreprocessor {
    override fun preparePayload(raw: String): Result<String> {
        val normalized = normalize(raw.trim())
            ?: return Result.failure(
                IllegalArgumentException("Импорт невозможен: пустой текст.")
            )
        if (!hasWorkoutPayloadStructure(normalized)) {
            return Result.failure(
                IllegalArgumentException(
                    "Импорт невозможен: в файле нет корректного списка упражнений (нужен непустой массив trainerWorkout или plannedWorkout в JSON)."
                )
            )
        }
        return Result.success(normalized)
    }

    private fun normalize(raw: String): String? {
        if (raw.isEmpty()) return null
        if (raw.startsWith("{") && raw.endsWith("}")) return raw
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start == -1 || end <= start) return null
        return raw.substring(start, end + 1)
    }

    private fun hasWorkoutPayloadStructure(json: String): Boolean = runCatching {
        val root = JSONObject(json)
        val trainer = root.optJSONArray("trainerWorkout")
        val planned = root.optJSONArray("plannedWorkout")
        (trainer != null && trainer.length() > 0) || (planned != null && planned.length() > 0)
    }.getOrDefault(false)
}

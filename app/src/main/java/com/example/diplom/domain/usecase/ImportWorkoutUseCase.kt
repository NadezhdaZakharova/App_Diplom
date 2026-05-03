package com.example.diplom.domain.usecase

import com.example.diplom.domain.repository.TrainingRepository
import javax.inject.Inject
import org.json.JSONObject

/**
 * Импорт тренировки тренера из JSON (в т.ч. из «Поделиться»).
 * Поверх [TrainingRepository.importTrainerWorkoutFromJson] — быстрая проверка структуры.
 */
class ImportWorkoutUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke(payload: String): Result<Unit> {
        val normalized = SharedWorkoutPayloadParser.normalize(payload.trim())
            ?: return Result.failure(IllegalArgumentException("Пустой текст"))
        if (!SharedWorkoutPayloadParser.hasWorkoutPayloadStructure(normalized)) {
            return Result.failure(
                IllegalArgumentException("Ожидается JSON с полем trainerWorkout или plannedWorkout")
            )
        }
        return trainingRepository.importTrainerWorkoutFromJson(normalized)
    }
}

private object SharedWorkoutPayloadParser {
    /** Выделяет объект `{...}` если вокруг есть текст (мессенджер, подпись к сообщению). */
    fun normalize(raw: String): String? {
        if (raw.isEmpty()) return null
        val trimmed = raw.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) return trimmed
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start == -1 || end <= start) return null
        return trimmed.substring(start, end + 1)
    }

    fun hasWorkoutPayloadStructure(json: String): Boolean = runCatching {
        val root = JSONObject(json)
        val trainer = root.optJSONArray("trainerWorkout")
        val planned = root.optJSONArray("plannedWorkout")
        (trainer != null && trainer.length() > 0) || (planned != null && planned.length() > 0)
    }.getOrDefault(false)
}

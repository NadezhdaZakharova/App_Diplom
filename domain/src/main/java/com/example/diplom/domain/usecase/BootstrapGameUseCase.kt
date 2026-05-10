package com.example.diplom.domain.usecase

import com.example.diplom.domain.repository.GamificationRepository
import javax.inject.Inject

class BootstrapGameUseCase @Inject constructor(
    private val gamificationRepository: GamificationRepository
) {
    suspend operator fun invoke() {
        gamificationRepository.seedIfEmpty()
        gamificationRepository.recalculate()
    }
}

package com.example.diplom.di

import com.example.diplom.domain.usecase.BootstrapGameUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BootstrapGameUseCaseEntryPoint {
    fun bootstrapGameUseCase(): BootstrapGameUseCase
}

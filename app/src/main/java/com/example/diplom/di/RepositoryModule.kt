package com.example.diplom.di

import com.example.diplom.data.repository.ActivityRepositoryImpl
import com.example.diplom.data.repository.GamificationRepositoryImpl
import com.example.diplom.data.repository.TrainingRepositoryImpl
import com.example.diplom.domain.repository.ActivityRepository
import com.example.diplom.domain.repository.GamificationRepository
import com.example.diplom.domain.repository.TrainingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds
    @Singleton
    fun bindGamificationRepository(impl: GamificationRepositoryImpl): GamificationRepository

    @Binds
    @Singleton
    fun bindTrainingRepository(impl: TrainingRepositoryImpl): TrainingRepository
}

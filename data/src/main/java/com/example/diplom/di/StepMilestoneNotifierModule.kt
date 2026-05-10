package com.example.diplom.di

import com.example.diplom.data.notification.AndroidStepMilestoneNotifier
import com.example.diplom.domain.StepMilestoneNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface StepMilestoneNotifierModule {

    @Binds
    @Singleton
    fun bindStepMilestoneNotifier(impl: AndroidStepMilestoneNotifier): StepMilestoneNotifier
}

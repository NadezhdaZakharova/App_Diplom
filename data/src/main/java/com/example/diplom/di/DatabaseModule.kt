package com.example.diplom.di

import android.content.Context
import com.example.diplom.data.local.DiplomDao
import com.example.diplom.data.local.DiplomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DiplomDatabase =
        DiplomDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideDao(database: DiplomDatabase): DiplomDao = database.dao()
}

package com.jixing.launcher.di

import android.content.Context
import com.jixing.launcher.managers.AirConditionManager
import com.jixing.launcher.managers.VehicleStateManager
import com.jixing.launcher.managers.VoiceAssistantManager
import com.jixing.launcher.data.repository.AppRepository
import com.jixing.launcher.data.repository.MediaRepository
import com.jixing.launcher.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context
    ): AppRepository {
        return AppRepository(context)
    }

    @Provides
    @Singleton
    fun provideMediaRepository(
        @ApplicationContext context: Context
    ): MediaRepository {
        return MediaRepository(context)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideVehicleStateManager(
        @ApplicationContext context: Context
    ): VehicleStateManager {
        return VehicleStateManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideAirConditionManager(
        @ApplicationContext context: Context
    ): AirConditionManager {
        return AirConditionManager(context)
    }

    @Provides
    @Singleton
    fun provideVoiceAssistantManager(
        @ApplicationContext context: Context
    ): VoiceAssistantManager {
        return VoiceAssistantManager(context)
    }
}

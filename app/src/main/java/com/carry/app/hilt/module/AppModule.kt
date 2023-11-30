package com.carry.app.hilt.module

import android.app.Application
import com.carry.app.hilt.MyApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Create by SunnyDay /11/30 20:44:25
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun providerMyApplication(application: Application) = application as MyApplication
}
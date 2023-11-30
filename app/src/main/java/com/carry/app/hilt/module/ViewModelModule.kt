package com.carry.app.hilt.module


import com.carry.app.hilt.entity.Fish
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

/**
 * Create by SunnyDay /11/30 21:36:07
 */

@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {
    @Provides
    fun providerFish() = Fish()
}
package com.carry.app.hilt.module

import com.carry.app.hilt.entity.Animal
import com.carry.app.hilt.entity.DogImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

/**
 * Create by Carry /11/29 15:07:52
 */
@Module
@InstallIn(ActivityComponent::class)
abstract class AbsModule {
    @Binds
    abstract fun providerDog(dogImpl: DogImpl): Animal
}
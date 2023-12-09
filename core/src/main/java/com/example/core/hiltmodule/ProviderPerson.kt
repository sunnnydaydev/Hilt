package com.example.core.hiltmodule

import com.example.core.entity.Woman
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Create by SunnyDay /12/09 19:03:17
 */

@Module
@InstallIn(SingletonComponent::class)
class ProviderPerson {
    @Provides
    fun providerWomen() = Woman()
}
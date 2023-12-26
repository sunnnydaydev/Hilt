package com.example.test.dagger

import com.example.test.entity.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Create by SunnyDay /12/26 21:28:20
 */

@Module
@InstallIn(SingletonComponent::class)
class RepoProvider {
    @Provides
    fun providerRepository() = Repository()
}
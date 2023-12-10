package com.example.core.hiltmodule

import com.example.core.entity.Person
import com.example.core.entity.PersonImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Create by SunnyDay /12/10 12:43:17
 */

@Module
@InstallIn(SingletonComponent::class)
interface BindsModule {
    @Binds
    fun getPerson(personImpl: PersonImpl):Person
}
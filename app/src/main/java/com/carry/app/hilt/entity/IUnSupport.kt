package com.carry.app.hilt.entity

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Create by SunnyDay /12/07 20:40:48
 */

@EntryPoint
@InstallIn(SingletonComponent::class)
interface IUnSupport {
    fun getDog():Dog
}
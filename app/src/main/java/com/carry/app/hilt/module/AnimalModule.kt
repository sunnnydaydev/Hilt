package com.carry.app.hilt.module

import android.util.Log
import com.carry.app.hilt.entity.Cat
import com.carry.app.hilt.entity.DogImpl
import com.carry.app.hilt.entity.SpecialCat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

/**
 * Create by Carry /11/29 14:55:43
 */

@Module
@InstallIn(ActivityComponent::class)
class AnimalModule {
    @Provides
    fun providerCat(): Cat {
        val result = Cat()
        Log.d("我的测试1", "hashCode1 $result")
        return result
    }

    @Provides
    fun providerDogImpl() = DogImpl()

    @SpecialCat
    @Provides
    fun providerSpecialCat(): Cat {
        val result = Cat()
        Log.d("我的测2", "hashCode2 $result")
        return result
    }
}

package com.carry.app.hilt

import android.app.Application
import com.carry.app.hilt.entity.Dog
import com.example.core.entity.Man
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Create by Carry /11/29 14:30:32
 */
@HiltAndroidApp
class MyApplication : Application() {
    @Inject
    lateinit var dog:Dog
    override fun onCreate() {
        super.onCreate()
        println("my test:${dog}")
    }
}

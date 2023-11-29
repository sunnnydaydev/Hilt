package com.carry.app.hilt

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Create by Carry /11/29 14:30:32
 */
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

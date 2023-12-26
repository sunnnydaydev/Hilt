package com.example.test

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Create by SunnyDay /12/26 21:27:12
 */

@HiltAndroidApp
class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
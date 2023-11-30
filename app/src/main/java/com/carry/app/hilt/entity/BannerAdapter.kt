package com.carry.app.hilt.entity

import android.app.Activity
import android.app.Application
import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Create by SunnyDay /11/30 20:19:46
 */
data class BannerAdapter @Inject constructor(@ActivityContext val context: Context)
data class ImageAdapter @Inject constructor(@ApplicationContext val context: Context)


data class ApplicationAdapter @Inject constructor(val context: Application)
data class ActivityAdapter @Inject constructor(val activity: Activity)
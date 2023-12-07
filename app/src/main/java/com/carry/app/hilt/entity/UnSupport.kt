package com.carry.app.hilt.entity

import android.content.Context
import android.util.Log
import dagger.hilt.android.EntryPointAccessors

/**
 * Create by SunnyDay /12/07 20:40:16
 */
class UnSupport(val context: Context) {

    lateinit var dog: Dog
    init {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(context,IUnSupport::class.java)
        dog = hiltEntryPoint.getDog()
        Log.d("TAG-TEST","my test:${dog}")
        //my test:com.carry.app.hilt.entity.Dog@6354b8d
    }
}
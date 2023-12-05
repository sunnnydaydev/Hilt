package com.carry.app.hilt.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import com.carry.app.hilt.entity.Cat
import com.carry.app.hilt.entity.Dog
import com.carry.app.hilt.other.ImageHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Create by SunnyDay /12/05 21:22:23
 */

@AndroidEntryPoint
class MyTextView constructor(
    private val cs: Context,
    private val attributes: AttributeSet?
) :
    AppCompatTextView(
        cs, attributes
    ) {

    @Inject
    lateinit var dog: Dog

    init {

    }

}
package com.carry.app.hilt.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.carry.app.hilt.MyApplication
import com.carry.app.hilt.entity.ActivityAdapter
import com.carry.app.hilt.entity.Animal
import com.carry.app.hilt.entity.ApplicationAdapter
import com.carry.app.hilt.entity.BannerAdapter
import com.carry.app.hilt.entity.Cat
import com.carry.app.hilt.entity.Dog
import com.carry.app.hilt.entity.ImageAdapter
import com.zennioptical.app.hilt.R
import com.carry.app.hilt.entity.SpecialCat
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var applicationAdapter: ApplicationAdapter

    @Inject
    lateinit var activityAdapter: ActivityAdapter

    @Inject
    lateinit var application:MyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "adapter1:$applicationAdapter")
        Log.d("MainActivity", "adapter2:$activityAdapter")
        Log.d("MainActivity", "application:$application")
    }
}

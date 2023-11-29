package com.carry.app.hilt.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.carry.app.hilt.entity.Animal
import com.carry.app.hilt.entity.Cat
import com.carry.app.hilt.entity.Dog
import com.zennioptical.app.hilt.R
import com.carry.app.hilt.entity.SpecialCat
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var cat: Cat

    @SpecialCat
    @Inject
    lateinit var cat2: Cat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
     
        Log.d("MainActivity", "animal1:$cat")
        Log.d("MainActivity", "animal2:$cat2")
    }
}

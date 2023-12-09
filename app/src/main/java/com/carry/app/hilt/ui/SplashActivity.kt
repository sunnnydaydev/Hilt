package com.carry.app.hilt.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.carry.app.hilt.entity.Animal
import com.carry.app.hilt.entity.Cat
import com.carry.app.hilt.entity.Dog
import com.example.core.ui.CoreActivity
import com.zennioptical.app.hilt.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var dog: Dog

    @Inject
    lateinit var cat: Cat

    @Inject lateinit var dog2: Animal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Log.d("SplashActivity", "dog:$dog")
        Log.d("SplashActivity", "cat:$cat")
        Log.d("SplashActivity", "dog2:$dog2")
        findViewById<View>(R.id.btnOpen).setOnClickListener {
            startActivity(Intent(this, CoreActivity::class.java))
        }
    }
}

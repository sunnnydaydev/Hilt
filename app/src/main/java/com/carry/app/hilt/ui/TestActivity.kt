package com.carry.app.hilt.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zennioptical.app.hilt.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        supportFragmentManager.beginTransaction().replace(R.id.fl_container,TestFragment()).commit()
    }
}
package com.carry.app.hilt.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.carry.app.hilt.entity.Dog
import com.carry.app.hilt.entity.IUnSupport
import com.carry.app.hilt.entity.UnSupport
import com.zennioptical.app.hilt.R
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

@AndroidEntryPoint
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        supportFragmentManager.beginTransaction().replace(R.id.fl_container,TestFragment()).commit()
        UnSupport(applicationContext)
    }
}
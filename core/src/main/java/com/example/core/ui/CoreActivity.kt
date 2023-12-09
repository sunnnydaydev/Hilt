package com.example.core.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.core.R
import com.example.core.entity.Man
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CoreActivity : AppCompatActivity() {
    @Inject
    lateinit var person: Man
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_core)
        println("i am core activity:${person}")
    }
}
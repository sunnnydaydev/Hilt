package com.example.core.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.core.R
import com.example.core.entity.Man
import com.example.core.entity.Person
import com.example.core.entity.Woman
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CoreActivity : AppCompatActivity() {
    @Inject
    lateinit var person: Man

    @Inject
    lateinit var woman: Woman

    @Inject
    lateinit var personImpl: Person
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_core)
    }
}
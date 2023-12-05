package com.carry.app.hilt.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.zennioptical.app.hilt.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        val myTextView: MyTextView = findViewById(R.id.myTextView)
        myTextView.text = myTextView.dog.javaClass.simpleName
        //IllegalStateException: class com.carry.app.hilt.ui.MyTextView, Hilt view must be attached to an @AndroidEntryPoint Fragment or Activity.
    }
}
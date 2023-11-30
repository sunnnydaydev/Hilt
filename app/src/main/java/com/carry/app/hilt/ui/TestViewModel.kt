package com.carry.app.hilt.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.carry.app.hilt.entity.Fish
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
data class TestViewModel @Inject constructor(val fish: Fish) : ViewModel() {
    init {
        Log.d("TestViewModel","fish:$fish")
    }
}
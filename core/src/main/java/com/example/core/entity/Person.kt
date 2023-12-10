package com.example.core.entity

import javax.inject.Inject

/**
 * Create by SunnyDay /12/09 17:37:46
 */
interface Person

class Man @Inject constructor() :Person

class Woman{
    init {
        println("women:$this")
    }
}

class PersonImpl @Inject constructor():Person{
    init {
        println("PersonImpl:$this")
    }
}

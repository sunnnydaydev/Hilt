package com.carry.app.hilt.entity

import javax.inject.Inject

/**
 * Create by Carry /11/29 14:37:52
 */
abstract class Animal

class Dog @Inject constructor() : Animal()

class Cat : Animal()

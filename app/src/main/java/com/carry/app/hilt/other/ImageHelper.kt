package com.carry.app.hilt.other

import com.carry.app.hilt.entity.Cat
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewComponent

/**
 * Create by SunnyDay /12/05 22:01:34
 */

@EntryPoint //1、创建入口点
@InstallIn(ViewComponent::class)//2、指定要在其中安装入口点的组件
interface  ImageHelper {
     fun getCat(): Cat
}
# 多模块中使用hilt

[readme](./README.md)中的代码都是基于app project进行开展的，接下来就搞core模块，app 依赖这个模块。


# 碰到的坑

[官方文档](https://developer.android.google.cn/training/dependency-injection/hilt-multi-module?hl=zh-cn)的开头两开了一遍没太留意
然后自己开始模拟多模块环境了~

###### step1、依赖迁移坑

心想：由于app依赖core那就直接把hilt依赖放core层吧，以api的方式这样上层也可以使用

app/build.gradle.kts

移除所有的hilt配置
添加core模块依赖

```groovy
dependencies {
    ...
    implementation(project(mapOf("path" to ":core")))
}
```

core/build.gradle.kts

添加hilt相关配置

```groovy
plugins {
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    api("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}
```

ojbk 直接run 工程跑下：

Unable to start activity ComponentInfo{com.zennioptical.app.hilt/com.carry.app.hilt.ui.SplashActivity}: kotlin.UninitializedPropertyAccessException: lateinit property dog has not been initialized

直接crash了？看下代码，注入失败，，，，，，

```kotlin
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var dog: Dog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Log.d("SplashActivity", "dog:$dog")
    }
}
```



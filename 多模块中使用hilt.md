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

好了看来自己的想法跑不通该如何解决呢？这里问了下ChatGPT其中有这样一句话"对于每个有 Dagger-Hilt 依赖的模块，都需要进行配置。"看到这里又把app module所需要的hilt依赖加了回来。

# 入门

Core module 定义

```kotlin
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
```

```kotlin
@Module
@InstallIn(SingletonComponent::class)
class ProviderPerson {
    @Provides
    fun providerWomen() = Woman()
}
```

```kotlin
@Module
@InstallIn(SingletonComponent::class)
interface BindsModule {
    @Binds
    fun getPerson(personImpl: PersonImpl):Person
}
```

###### 1、@Inject 注解

测试在 app Module 中使用

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    @Inject
    lateinit var person:Man
    override fun onCreate() {
        super.onCreate()
        println("my test:${person}")
    }
}
```


在Core Module 中使用

```kotlin
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
```

观察正常打印结果


###### 2、@Provides 注解

测试在 app Module 中使用

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    @Inject
    lateinit var woman:Woman

    override fun onCreate() {
        super.onCreate()
    }
}
```

测试在Core Module 中使用

```kotlin
@AndroidEntryPoint
class CoreActivity : AppCompatActivity() {
    @Inject
    lateinit var woman: Woman
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_core)
    }
}
```

观察正常打印结果

###### 3、@Binds

测试在 app Module 中使用

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    @Inject
    lateinit var personImpl: Person

    override fun onCreate() {
        super.onCreate()
    }
}
```

测试在Core Module 中使用

```kotlin
@AndroidEntryPoint
class CoreActivity : AppCompatActivity() {
    @Inject
    lateinit var personImpl: Person
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_core)
    }
}
```

观察正常打印结果










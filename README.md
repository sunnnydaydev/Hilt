# Hilt

一款基于dagger封装的依赖注入库，它可以为项目中的每个 Android 类提供容器并自动管理其生命周期。

# 一、项目引入

build.gradle

```kotlin
plugins {
    id("com.google.dagger.hilt.android") version "2.44" apply false
}
```
app/build.gradle

```kotlin
plugins {
  kotlin("kapt")
  id("com.google.dagger.hilt.android")
}

dependencies {
  implementation("com.google.dagger:hilt-android:2.44")
  kapt("com.google.dagger:hilt-android-compiler:2.44")
}

// Allow references to generated code
kapt {
  correctErrorTypes = true
}
```

# 二、Hilt应用类

所有使用 Hilt 的应用都必须包含一个带有 @HiltAndroidApp 注解的 Application 类。

@HiltAndroidApp 会触发 Hilt 的代码生成操作，生成的代码包括应用的一个基类，该基类充当应用级依赖项容器。

```kotlin
/**
 * Create by Carry /11/29 14:30:32
 */
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
```

# 三、使用

###### 1、入门

这里先以一个简单的🌰来踏入Hilt的门槛

```kotlin
abstract class Animal

class Dog @Inject constructor() : Animal()

class Cat : Animal()
```

```kotlin
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var dog: Dog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Log.d("SplashActivity", "dog:$dog")
        //2023-11-29 21:09:29.785 28761-28761 SplashActivity dog:com.carry.app.hilt.entity.Dog@527681e
    }
}
```
流程很简单->
（1）首先为无参构造的Dog类添加@Inject注解告诉Hilt为我生成这个Dog类的实例

（2）其次就是在activity中定义成员变量了写法与Dagger一致

（3）最重要的一步为SplashActivity添加@AndroidEntryPoint注解，这样Hilt才会为这个Activity注入字段值。

###### 2、Hilt 模块

有些类我们不能通过构造函数注入，发生这种情况可能有多种原因。例如，您不能通过构造函数注入接口。此外，您也不能通过构造函数注入不归您所有的类型，如来自外部库的类。在这些情况下，您可以使用 Hilt 模块向 Hilt 提供绑定信息。

Hilt 模块是一个带有 @Module 注解的类。与 Dagger 模块一样，它会告知 Hilt 如何提供某些类型的实例。与 Dagger 模块不同的是，您必须使用 @InstallIn 为 Hilt 模块添加注解，以告知 Hilt 每个模块将用在或安装在哪个 Android 类中。

（1）使用@Provides提供三方库的实例

一个很典型的代表就是Retrofit、OkHttpClient 或 Room 这些三方库，我们无法通过构造函数创建其实例，此时我们就无法通过构造函数方式注入他们的实例了。

看看使用Hilt模块的@Provides是如何实现的，这里为了方便就简单模拟下

```kotlin
abstract class Animal

class Dog @Inject constructor() : Animal()

class Cat : Animal()
```

```kotlin
/**
 * Create by Carry /11/29 14:55:43
 */

@Module
@InstallIn(ActivityComponent::class)
class AnimalModule {
    @Provides
    fun providerCat() = Cat()
}
```

```kotlin
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    
    @Inject
    lateinit var cat: Cat
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Log.d("SplashActivity", "cat:$cat")
    }
}
```

很简单，用法与Dagger十分相似，只是多了一个@InstallIn注解并通过参数指明这个Module受ActivityComponent管理。这样这个模块就受Hilt容器管理了。我们就能使用这些值了。

（2）使用 @Binds 注入接口实例

```kotlin
abstract class Animal

class Dog @Inject constructor() : Animal()

class Cat : Animal()

class DogImpl : Animal()
```

```kotlin
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    
    @Inject lateinit var dog2: Animal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Log.d("SplashActivity", "dog2:$dog2")
    }
}
```
可以看到这里dog2是一个Animal类型，而Animal是一个抽象类无法通过构造注入，此时@Binds就派上用场了

```kotlin
@Module
@InstallIn(ActivityComponent::class)
abstract class AbsModule {
    @Binds
    abstract fun providerDog(dogImpl: DogImpl): Animal
}
```
用法与dagger几乎很像也是多了InstallIn注解。

到这里我们还需要为DogImpl提供实例，一种方式是为DogImpl构造添加@Inject注解。另外一种是通过@Provides提供实例：

```kotlin
@Module
@InstallIn(ActivityComponent::class)
class AnimalModule {
    @Provides
    fun providerDogImpl() = DogImpl()
}
```

（3）为同一类型提供多个绑定

模块中是不允许两个方法具有相同的返回值的举个🌰

```kotlin
@Module
@InstallIn(ActivityComponent::class)
class AnimalModule {
    @Provides
    fun providerCat(): Cat {
        val result = Cat()
        Log.d("我的测试1", "hashCode1 $result")
        return result
    }
    
    @Provides
    fun providerSpecialCat(): Cat {
        val result = Cat()
        Log.d("我的测2", "hashCode2 $result")
        return result
    }
}
```
这样的代码在编译期间就会报错 com.carry.app.hilt.entity.Cat is bound multiple times:

那么Hilt是怎样解决这个问题的呢？答案是添加限定符注解

```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SpecialCat
```

在绑定对象时与注入对象实例时来标注即可

```kotlin
@Module
@InstallIn(ActivityComponent::class)
class AnimalModule {
    //默认方式
    @Provides
    fun providerCat(): Cat {
        val result = Cat()
        Log.d("我的测试1", "hashCode1 $result")
        return result
    }
    // 绑定对象时进行标注
    @SpecialCat
    @Provides
    fun providerSpecialCat(): Cat {
        val result = Cat()
        Log.d("我的测2", "hashCode2 $result")
        return result
    }
}
```

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var cat: Cat

    @SpecialCat
    @Inject
    lateinit var cat2: Cat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "animal1:$cat")
        Log.d("MainActivity", "animal2:$cat2")
        /**
         * 我的测试1           D   hashCode1 com.carry.app.hilt.entity.Cat@3219d60
         * 我的测2            D   hashCode2 com.carry.app.hilt.entity.Cat@b837219
         * MainActivity      D  animal1:com.carry.app.hilt.entity.Cat@3219d60
         * MainActivity      D  animal2:com.carry.app.hilt.entity.Cat@b837219
         * */
    }
}
```
可见限定符使用成功





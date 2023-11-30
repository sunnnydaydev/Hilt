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

（3）Hilt 中的预定义限定符

即使通过@Provider、@Binds我们还是无法获取到activity、Application的context的。Hilt想到了这点提供了两个注解

- @ActivityContext
- @ApplicationContext


```kotlin
data class BannerAdapter @Inject constructor(@ActivityContext val context: Context)
data class ImageAdapter @Inject constructor(@ApplicationContext val context: Context)
```

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var bannerAdapter: BannerAdapter

    @Inject
    lateinit var imageAdapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "adapter1:$bannerAdapter")
        Log.d("MainActivity", "adapter2:$imageAdapter")
        //adapter1:BannerAdapter(context=com.carry.app.hilt.ui.MainActivity@8c8df92)
        //adapter2:ImageAdapter(context=com.carry.app.hilt.MyApplication@dc45142)
    }
}
```

关于预置Qualifier其实还有一个隐藏的小技巧，就是对于Application和Activity这两个类型，Hilt也是给它们预置好了注入功能。也就是说，如果你的某个类依赖于Application或者Activity，不需要想办法为这两个类提供依赖注入的实例，Hilt自动就能识别它们。如下所示：

```kotlin
data class ApplicationAdapter @Inject constructor(val context: Application)
data class ActivityAdapter @Inject constructor(val activity: Activity)
```

注意必须是Application和Activity这两个类型，即使是声明它们的子类型，编译都无法通过。

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var applicationAdapter: ApplicationAdapter

    @Inject
    lateinit var activityAdapter: ActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "adapter1:$applicationAdapter")
        Log.d("MainActivity", "adapter2:$activityAdapter")
    }
}
```

我们可能会碰到这样的场景？在自定义的MyApplication中定义了一些api，但是通过Hilt获取到Application的对象后每次都要强转一下比较繁琐，此时我们可以这样做：

```kotlin
/**
 * Create by SunnyDay /11/30 20:44:25
 */
@Module
@InstallIn(SingletonComponent::class)//指定module受app容器管理，app中都可以使用
class AppModule {
    @Provides
    fun providerMyApplication(application: Application) = application as MyApplication
}
```

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var application:MyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "application:$application")
    }
}
```

# Hilt支持的切入点

Hilt大幅简化了Dagger2的用法，使得我们不用通过@Component注解去编写桥接层的逻辑，但是也因此限定了注入功能只能从几个Android固定的入口点开始：

- Application
- Activity
- Fragment
- View
- Service
- BroadcastReceiver

其中，只有Application这个入口点是使用@HiltAndroidApp注解来声明的，其他的所有入口点，都是用@AndroidEntryPoint注解来声明的。

上面都是使用activity作为Hilt注入点来举例的，这里就看看其他android组件是怎样使用的

###### 1、Fragment 🌰

```kotlin

```

```kotlin
@AndroidEntryPoint
class TestFragment : Fragment() {

    @Inject
    lateinit var cat:Cat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("TestFragment","cat:${cat}")
        return inflater.inflate(R.layout.fragment_test, container, false)
    }
    
}
```

```kotlin
@AndroidEntryPoint
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        supportFragmentManager.beginTransaction().replace(R.id.fl_container,TestFragment()).commit()
    }
}
```

这里其实就需要注意一点就可以了，fragment依附的activity也要标记@AndroidEntryPoint，否则你会得到如下编译错误：

IllegalStateException: Hilt Fragments must be attached to an @AndroidEntryPoint Activity. 

Found: class com.carry.app.hilt.ui.TestActivity

好了view、service、BroadcastReceiver都是类似使用 @AndroidEntryPoint即可。那么我们要是使用ViewModel该如何做呢？这个不属于上述6个入口点。

###### 2、ViewModel 栗子

```kotlin
abstract class Animal
class Fish:Animal()
```

```kotlin
/**
 * Create by SunnyDay /11/30 21:36:07
 */
@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {
    @Provides
    fun providerFish() = Fish()
}
```

```kotlin
@AndroidEntryPoint
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        supportFragmentManager.beginTransaction().replace(R.id.fl_container,TestFragment()).commit()
    }
}
```

```kotlin
@AndroidEntryPoint
class TestFragment : Fragment() {

    private lateinit var viewModel: TestViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_test, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[TestViewModel::class.java]
    }
}
```

```kotlin
@HiltViewModel
data class TestViewModel @Inject constructor(val fish: Fish) : ViewModel() {
    init {
        Log.d("TestViewModel","fish:$fish")
    }
}
```

可见很简单，为TestViewModel添加@HiltViewModel注解即可。

通过Provides提供对象时容器可以指定为ViewModelComponent

###### 3、其他

ContentProvider 等等

# Hilt组件

前面使用了ViewModelComponent、ActivityComponent这些是啥呢？或许我们可以猜到了一些，这些是Hilt提供的容器。就拿SingletonComponent来说这个是一个全局的容器，它负责
管理着app中的其他容器。而ActivityComponent就是所有的activity的容器，在dagger中我们需要创建一个ActivityComponent接口，然后每个activity需要注入字段时首先需要在
这个接口中定义一个注入方法。并且还要让ActivityComponent受SingletonComponent管理，这是一件繁琐的事情，现在看来Hilt真是简便多了。

接下来看看Hilt提供了哪些容器，对应哪些安卓类，生命周期是怎样的，以及默认提供的作用域范围：

| 容器 | 容器对应Android 类      | 容器创建时机| 容器销毁时机| 提供的容器内单例注解
|--------| -------------| ----|  ---| ---|
| SingletonComponent | Application |Application#onCreate()|Application 已销毁|@Singleton
| ActivityRetainedComponent | 不适用 |Activity#onCreate()|Activity#onDestroy()|@ActivityRetainedScoped
| ViewModelComponent | ViewModel |ViewModel 已创建|ViewModel 已销毁|@ViewModelScoped
| ActivityComponent | Activity |Activity#onCreate()|Activity#onDestroy()|@ActivityScoped
| FragmentComponent | Fragment |Fragment#onAttach()|Fragment#onDestroy()|@FragmentScoped
| ViewComponent | View |View#super()|View 已销毁|@ViewScoped
| ViewWithFragmentComponent | 带有 @WithFragmentBindings 注解的 View |View#super()|View 已销毁|@ViewScoped
| ServiceComponent | Service |Service#onCreate()|Service#onDestroy()|@ServiceScoped



todo

# 参考

[官方文档](https://developer.android.google.cn/training/dependency-injection/hilt-android?hl=zh-cn#multiple-bindings)

[Jetpack新成员，一篇文章带你玩转Hilt和依赖注入](https://mp.weixin.qq.com/s/dAhwLiYeFizdMRu_nN6Y7Q)






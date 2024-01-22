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

###### 3、View 栗子

```kotlin
class Dog @Inject constructor() : Animal()
```

```kotlin
@AndroidEntryPoint
class MyTextView constructor(
    private val cs: Context,
    private val attributes: AttributeSet?
) :
    AppCompatTextView(
        cs, attributes
    ) {

    @Inject
    lateinit var dog: Dog

}
```

在activity中使用自定义的View

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.carry.app.hilt.ui.ViewActivity">

    <com.carry.app.hilt.ui.MyTextView
        android:id="@+id/myTextView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:text="测试" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

```kotlin
@AndroidEntryPoint
class ViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        val myTextView: MyTextView = findViewById(R.id.myTextView)
        myTextView.text = myTextView.dog.javaClass.simpleName
        // 注意view依赖的activity或者fragment要添加AndroidEntryPoint否则会报错的
        //IllegalStateException: class com.carry.app.hilt.ui.MyTextView, Hilt view must be attached to an @AndroidEntryPoint Fragment or Activity.
    }
}
```


###### 4、如何为不支持的类进行依赖注入

Hilt支持常见android类注入，有些情况下我们可能需要把某些实例注入到不支持的类中，这时我们可以使用@EntryPoint，举个🌰

```kotlin
/**
 * Create by SunnyDay /12/07 20:40:48
 */

@EntryPoint
@InstallIn(SingletonComponent::class)
interface IUnSupport {
    fun getDog():Dog
}
```

如上使用 @EntryPoint 注解创建入口点。

Hilt 并不直接支持自定义类UnSupport，如果您希望 自定义类UnSupport 使用 Hilt 来获取某些依赖项，需要为所需的每个绑定类型定义一个带有 @EntryPoint 注解的接口并添加限定符。然后，添加 @InstallIn 以指定要在其中安装入口点的组件。


```kotlin
class UnSupport(val context: Context) {
    lateinit var dog: Dog
    init {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(context,IUnSupport::class.java)
        dog = hiltEntryPoint.getDog()
        Log.d("TAG-TEST","my test:${dog}")
        //my test:com.carry.app.hilt.entity.Dog@6354b8d
    }
}
```

如上使用EntryPointAccessors访问入口点

```kotlin
@AndroidEntryPoint
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        supportFragmentManager.beginTransaction().replace(R.id.fl_container,TestFragment()).commit()
        UnSupport(applicationContext)
    }
}
```

上述例子中必须使用ApplicationContext参数，因为我们入口点安装到了SingletonComponent中，若是安装到ActivityComponent我们应该使用ActivityContext

好了到了这里就知道怎样在ContentProvider使用Hilt管理的依赖项了。


###### 注意点

- Hilt 仅支持扩展 ComponentActivity 的 activity，如 AppCompatActivity。
- Hilt 仅支持扩展 androidx.Fragment 的 Fragment
- Hilt 注入的字段不能为私有字段。尝试使用 Hilt 注入私有字段会导致编译错误。
- Hilt setter注入的字段不能直接在init块中使用否则crash
```kotlin

@HiltViewModel
class HomeFragmentViewModel @Inject constructor() : HomeFragmentContract.ViewModel() {

    @Inject
    lateinit var repository: HomeRepository

    init {
        /**
         *  如下直接打log这样会crash：
         *  Timber.d("HomeFragmentViewModel->${repository.localDataSource.getLocalData()}")
         *  如何避免crash呢，有如下几种方式：
         *  1、delay后使用
         *  2、viewModelScope就不会crash
         *          viewModelScope.launch {
         *             Timber.d("HomeFragmentViewModel->${repository.localDataSource.getLocalData()}")
         *         }
         *  3、通过构造注入也不会crash。
         *  结论：Hilt setter注入的字段 不能直接通过init调用。需要延迟调用或者在对应的Scope组件中调用。
         */
        viewModelScope.launch {
            Timber.d("HomeFragmentViewModel->${repository.localDataSource.getLocalData()}")
        }
    }

    override fun onViewEvent(event: HomeFragmentContract.ViewEvent) {

    }
}

```

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


默认情况下，Hilt 中的所有绑定都未限定作用域。这意味着，每当应用请求绑定时，Hilt 都会创建所需类型的一个新实例。 Hilt 也允许将绑定的作用域限定为特定组件。Hilt 只为绑定作用域限定到的组件的每个实例创建一次限定作用域的绑定，对该绑定的所有请求共享同一实例。

将模块安装到组件后，其绑定就可以用作该组件中其他绑定的依赖项，也可以用作组件层次结构中该组件下的任何子组件中其他绑定的依赖项：

![](https://gitee.com/sunnnydaydev/my-pictures/raw/master/github/di/hilt.png)

默认情况下，如果您在视图中执行字段注入，ViewComponent 可以使用 ActivityComponent 中定义的绑定。如果您还需要使用 FragmentComponent 中定义的绑定并且视图是 fragment 的一部分，应将 @WithFragmentBindings 注解和 @AndroidEntryPoint 一起使用。

###### 1、ActivityComponent与ActivityRetainedComponent的区别

ActivityComponent组件与单个 Activity 的生命周期绑定。每个 Activity 都有一个相应的 ActivityComponent，它在该 Activity 的生命周期内创建和销毁。这意味着该组件中的依赖项的生命周期与相应的 Activity 相关联。当 Activity 被销毁时，ActivityComponent 中的依赖项也将被销毁。

ActivityRetainedComponent组件用于在多个 Activity 之间保持共享的依赖项，其生命周期比单个 Activity 更长。通常，ActivityRetainedComponent 绑定到一个被标记为 @EntryPoint 的类，这个类是一个能够提供在多个 Activity 之间共享的依赖项的入口点。这样，当一个 Activity 被销毁时，与 ActivityRetainedComponent 相关的依赖项仍然存在，以便下一个 Activity 可以共享它们。

# 多模块中使用Hilt

[多模块中使用Hilt](./多模块中使用hilt.md)

# Dagger to Hilt 

[测试Dagger2Hilt](./测试Dagger2Hilt.md)

# 参考

[官方文档](https://developer.android.google.cn/training/dependency-injection/hilt-android?hl=zh-cn#multiple-bindings)

[Jetpack新成员，一篇文章带你玩转Hilt和依赖注入](https://mp.weixin.qq.com/s/dAhwLiYeFizdMRu_nN6Y7Q)

[Dagger to Hilt官方文档](https://developers.google.cn/codelabs/android-dagger-to-hilt?hl=zh-cn#0)






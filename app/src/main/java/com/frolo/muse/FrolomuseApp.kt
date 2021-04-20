package com.frolo.muse

import android.app.Activity
import android.os.Build
import android.os.StrictMode
import android.os.strictmode.Violation
import android.widget.Toast
import androidx.multidex.MultiDexApplication
import com.frolo.muse.admob.AdMobs
import com.frolo.muse.di.AppComponent
import com.frolo.muse.di.DaggerAppComponent
import com.frolo.muse.di.impl.navigator.NavigatorImpl
import com.frolo.muse.di.modules.*
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.PlayerImpl
import com.frolo.muse.engine.PlayerWrapper
import com.frolo.muse.engine.audiofx.AudioFx_Impl
import com.frolo.muse.navigator.NavigatorWrapper
import com.frolo.muse.ui.base.BaseActivity
import com.frolo.muse.ui.base.FragmentNavigator
import com.frolo.muse.ui.main.MainActivity
import com.google.android.gms.ads.MobileAds
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import io.reactivex.plugins.RxJavaPlugins


class FrolomuseApp : MultiDexApplication() {

    lateinit var appComponent: AppComponent
        private set

    private val preferences by lazy { appComponent.providePreferences() }
    private val eventLogger by lazy { appComponent.provideEventLogger() }

    private val activityWatcher by lazy {
        FrolomuseActivityWatcher(preferences, eventLogger)
    }

    private val playerWrapper = PlayerWrapper()
    private val navigatorWrapper = NavigatorWrapper()

    val foregroundActivity: Activity? get() = activityWatcher.foregroundActivity

    override fun onCreate() {
        super.onCreate()

        appComponent = buildAppComponent()

        registerActivityLifecycleCallbacks(activityWatcher)

        setupStrictMode()

        setupRxPlugins()

        setupFirebaseRemoteConfigs()

        initAdMob()
    }

    private fun buildAppComponent(): AppComponent =
        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .playerModule(PlayerModule(playerWrapper, BuildConfig.DEBUG))
            .localDataModule(LocalDataModule())
            .remoteDataModule(RemoteDataModule())
            .navigationModule(NavigationModule(navigatorWrapper))
            .eventLoggerModule(EventLoggerModule(BuildConfig.DEBUG))
            .networkModule(NetworkModule())
            .miscModule(MiscModule())
            .build()

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {

            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val executor = HandlerExecutor(mainLooper)
                        val listener = StrictMode.OnThreadViolationListener { violation ->
                            showViolation(violation)
                        }
                        penaltyListener(executor, listener)
                    } else {
                        this
                    }
                }
                .build()
                .also { StrictMode.setThreadPolicy(it) }

            StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .setClassInstanceLimit(PlayerImpl::class.java, 1)
                .setClassInstanceLimit(AudioFx_Impl::class.java, 1)
                .penaltyLog()
                .run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val executor = HandlerExecutor(mainLooper)
                        val listener = StrictMode.OnVmViolationListener { violation ->
                            showViolation(violation)
                        }
                        penaltyListener(executor, listener)
                    } else {
                        this
                    }
                }
                .build()
                .also { StrictMode.setVmPolicy(it) }
        }
    }

    private fun showViolation(violation: Violation?) {
        if (BuildConfig.DEBUG) {
            foregroundActivity?.also { context ->
                Toast.makeText(context, violation?.toString().orEmpty(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupRxPlugins() {
        RxJavaPlugins.setErrorHandler { err ->
            // Default error consumer
            eventLogger.log(err)
            (activityWatcher.foregroundActivity as? BaseActivity)?.let { activity ->
                activity.runOnUiThread {
                    activity.postError(err)
                }
            }
        }
    }

    private fun setupFirebaseRemoteConfigs() {
        val instance = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(60 * 60 * 24) // 24 hours
            .build()
        instance.setConfigSettingsAsync(configSettings)
        instance.setDefaultsAsync(R.xml.firebase_remote_config_default)
    }

    private fun initAdMob() {
        if (AdMobs.shouldInitializeOnColdStart()) {
            MobileAds.initialize(this)
        }
    }

    fun onPlayerConnected(player: Player) {
        playerWrapper.attachBase(player)
    }

    fun onPlayerDisconnected() {
        playerWrapper.detachBase()
    }

    fun onFragmentNavigatorCreated(fragmentNavigator: FragmentNavigator) {
        navigatorWrapper.attachBase(NavigatorImpl(fragmentNavigator as MainActivity))
    }

    fun onFragmentNavigatorDestroyed() {
        navigatorWrapper.detachBase()
    }

}
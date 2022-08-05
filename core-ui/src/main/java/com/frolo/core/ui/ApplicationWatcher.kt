package com.frolo.core.ui

import android.app.Activity
import android.content.Context
import com.frolo.core.ui.activity.ActivityWatcher

object ApplicationWatcher {
    @JvmStatic
    val applicationContext: Context get() {
        return ApplicationWatcherImpl.instance.requireApplicationContext()
    }

    @JvmStatic
    val activityWatcher: ActivityWatcher get() {
        return ApplicationWatcherImpl.instance.activityWatcher
    }

    @JvmStatic
    val foregroundActivity: Activity? get() = activityWatcher.foregroundActivity
}
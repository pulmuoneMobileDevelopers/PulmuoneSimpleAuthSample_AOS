package com.android.pulmuone.sample.utils

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

class ForegroundDetector(application: Application) : ActivityLifecycleCallbacks {
    internal enum class State { None, Foreground, Background }

    private var state: State? = null
    private var application: Application? = null
    private var listener: Listener? = null
    private var isChangingConfigurations = false
    private var running = 0

    interface Listener {
        fun onBecameForeground()
        fun onBecameBackground()
    }

    init {
        instance = this
        this.application = application
        application.registerActivityLifecycleCallbacks(this)
    }

    fun unregisterCallbacks() {
        application!!.unregisterActivityLifecycleCallbacks(this)
    }

    fun addListener(listener: Listener?) {
        state = State.None
        this.listener = listener
    }

    fun removeListener() {
        state = State.None
    }

    val isBackground: Boolean
        get() = state == State.Background
    val isForeground: Boolean
        get() = state == State.Foreground

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        if (++running == 1 && !isChangingConfigurations) {
            state = State.Foreground
            if (listener != null) listener!!.onBecameForeground()
        }
    }

    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {
        isChangingConfigurations = activity.isChangingConfigurations
        if (--running == 0 && !isChangingConfigurations) {
            state = State.Background
            if (listener != null) listener!!.onBecameBackground()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        var instance: ForegroundDetector? = null
            private set
    }
}
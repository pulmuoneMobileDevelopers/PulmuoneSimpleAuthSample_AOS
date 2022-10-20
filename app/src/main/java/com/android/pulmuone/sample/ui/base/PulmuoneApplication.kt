package com.android.pulmuone.sample.ui.base

import android.app.Application
import android.content.Context
import android.util.Log
import com.android.pulmuone.sample.utils.Constants.KEY_INTERVAL
import com.android.pulmuone.sample.utils.ForegroundDetector
import com.android.pulmuone.sample.utils.PreferenceManager

class PulmuoneApplication : Application() {

    private var foregroundDetector: ForegroundDetector? = null
    private var start = 0L
    private var end = 0L

    companion object {
        lateinit var instance: PulmuoneApplication
        private set

        fun getContext(): Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        foregroundDetector = ForegroundDetector(this@PulmuoneApplication)
        foregroundDetector!!.addListener(object : ForegroundDetector.Listener {
            override fun onBecameForeground() {
                Log.d("CFSApplication:::", "Became Foreground")
                end = System.currentTimeMillis()
                if (start != 0L) {
                    Log.d("CFSApplication:::", "Interval:::" + ((end - start) / 1000))
                    val interval = (end - start) / 1000
                    // 백그라운드 전환 후 포어그라운드 전환 시 차이 저장
                    PreferenceManager().setLong(applicationContext, KEY_INTERVAL, interval)
                    start = 0L
                    end = 0L
                }
            }

            override fun onBecameBackground() {
                Log.d("CFSApplication:::", "Became Background")
                start = System.currentTimeMillis()
            }

        })
    }

    override fun onTerminate() {
        super.onTerminate()
        foregroundDetector!!.unregisterCallbacks()
    }
}
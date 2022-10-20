package com.android.pulmuone.sample.ui.intro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.android.pulmuone.sample.utils.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.android.pulmuone.sample.R
import com.android.pulmuone.sample.ui.bio.BioModuleActivity
import com.android.pulmuone.sample.utils.Constants.BIO_AUTH
import com.android.pulmuone.sample.utils.Constants.KEY_INTRO
import com.android.pulmuone.sample.utils.Constants.KEY_PIN_CODE_EMPTY
import com.android.pulmuone.sample.utils.Constants.PIN_AUTH
import com.android.pulmuone.sample.utils.Constants.PIN_CODE_STATUS
import com.android.pulmuone.sample.utils.Constants.SIMPLE_AUTH_STATUS
import com.android.pulmuone.sample.ui.main.MainActivity
import com.android.pulmuone.sample.ui.pin.view.PinModuleActivity

class IntroActivity : AppCompatActivity() {
    var statusAuth: String? = null

    /*
       Handler
     */
    var mHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        init()
        start()
    }

    private fun init() {
        statusAuth = PreferenceManager().getString(this, SIMPLE_AUTH_STATUS)
        assert(statusAuth != null)
        if (statusAuth == "") PreferenceManager().setString(this, SIMPLE_AUTH_STATUS, "none")
        statusAuth = PreferenceManager().getString(this, SIMPLE_AUTH_STATUS)
        if(PreferenceManager().getString(this, PIN_CODE_STATUS) == "")
            PreferenceManager().setString(this, PIN_CODE_STATUS, KEY_PIN_CODE_EMPTY)
    }

    /*
       다음 화면으로 이동을 위해 Handler 사용
     */
    private fun start() {
        mHandler = Handler(mainLooper)
        mHandler!!.postDelayed(mRunnable, 1500)
    }

    private val mRunnable = Runnable {
        when (statusAuth) {
            BIO_AUTH -> {
                startActivity(Intent(this, BioModuleActivity::class.java).putExtra("from", KEY_INTRO))
                overridePendingTransition(0, 0)
                finish()
            }
            PIN_AUTH -> {
                startActivity(Intent(this, PinModuleActivity::class.java).putExtra("from", KEY_INTRO))
                overridePendingTransition(0, 0)
                finish()
            }
            else -> {
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(0, 0)
                finish()
            }
        }
    }
}
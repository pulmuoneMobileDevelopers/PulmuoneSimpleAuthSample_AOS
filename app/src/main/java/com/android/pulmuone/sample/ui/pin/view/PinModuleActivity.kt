package com.android.pulmuone.sample.ui.pin.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.pulmuone.sample.R
import com.android.pulmuone.sample.utils.Constants.KEY_INTRO
import com.android.pulmuone.sample.ui.main.MainActivity
import com.android.pulmuone.sample.ui.base.FragmentTransitionManager
import kotlinx.android.synthetic.main.activity_pinmodule.*

class PinModuleActivity : AppCompatActivity() {

    private lateinit var fragment: PinCodeFragment
    private var pinRegistered: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pinmodule)

        intent.getStringExtra("from")?.let {
            Log.d("PinModuleActivity:::", it)
            pinRegistered = it
        }
        fragment = PinCodeFragment.newInstance()

        FragmentTransitionManager()
            .changeFragmentOnActivity(
                this,
                R.id.pinContainer,
                fragment,
                false
            )

        init()
    }

    private fun init() {
        img_cancel.let {
            it?.setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(0, 0)
                finish()
            }
        }
    }

    override fun onBackPressed() {

        /*
            do not work - delete super.onBackPressed()
         */
//        super.onBackPressed()
    }
}
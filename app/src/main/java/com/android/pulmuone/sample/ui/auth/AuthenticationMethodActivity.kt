package com.android.pulmuone.sample.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.pulmuone.biomodule.aos.BioManager
import com.android.pulmuone.sample.R
import com.android.pulmuone.sample.utils.Constants.KEY_AUTH_METHOD
import com.android.pulmuone.sample.ui.bio.BioModuleActivity
import com.android.pulmuone.sample.ui.pin.view.PinModuleActivity
import com.android.pulmuone.dialog.DefaultDialog
import com.android.pulmuone.sample.ui.main.MainActivity
import com.android.pulmuone.sample.utils.Constants.DEVICE_NOT_ENROLLED
import com.android.pulmuone.sample.utils.Constants.DEVICE_NOT_SUPPORTED

class AuthenticationMethodActivity : AppCompatActivity() {

    private var defaultDialog: DefaultDialog? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        /*
            Handle the Intent
         */
        if (result.resultCode == Activity.RESULT_OK) { }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticationmethod)

        val btnBio = findViewById<ConstraintLayout>(R.id.btnBio)
        btnBio.setOnClickListener {
            val bioManager = BioManager.getInstance(this)
            if (bioManager.isSupportBiometrics().success) {
                startActivity(
                    Intent(this, BioModuleActivity::class.java)
                        .putExtra("from", KEY_AUTH_METHOD)
                )
                overridePendingTransition(0, 0)
                finish()
            } else {
                val msg: String?
                when {
                    bioManager.isSupportBiometrics().errorType.toString() == DEVICE_NOT_ENROLLED -> {
                        msg = getString(R.string.device_not_enrolled)
                        defaultDialog = DefaultDialog(
                            this,
                            "",
                            msg,
                            false,
                            _ok = {
                                /*
                                    ?????? ???????????? ??????
                                 */
                                getContent.launch(Intent(Settings.ACTION_BIOMETRIC_ENROLL))
                            },
                            _cancel = {},
                            _singleOk = {},
                            "??????",
                            "??????",
                            ""
                        )
                        if (!(this as Activity).isFinishing) defaultDialog?.show()
                    }
                    bioManager.isSupportBiometrics().errorType.toString() == DEVICE_NOT_SUPPORTED -> {
                        msg = getString(R.string.device_not_supported)
                        defaultDialog = DefaultDialog(
                            this,
                            "",
                            msg,
                            true,
                            _ok = {},
                            _cancel = {},
                            _singleOk = {},
                            "",
                            "",
                            "??????"
                        )
                        if (!(this as Activity).isFinishing) defaultDialog?.show()
                    }
                    else -> {
                        msg = getString(R.string.auth_not_use)
                        defaultDialog = DefaultDialog(
                            this,
                            "",
                            msg,
                            true,
                            _ok = {},
                            _cancel = {},
                            _singleOk = {},
                            "",
                            "",
                            "??????"
                        )
                        if (!(this as Activity).isFinishing) defaultDialog?.show()
                    }
                }
            }
        }

        val btnPin = findViewById<ConstraintLayout>(R.id.btnPin)
        btnPin.setOnClickListener {
            startActivity(
                Intent(this, PinModuleActivity::class.java)
                    .putExtra("from", KEY_AUTH_METHOD)
            )
            overridePendingTransition(0, 0)
            finish()
        }

        val imgCancel = findViewById<ImageView>(R.id.img_cancel)
        imgCancel.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }
}
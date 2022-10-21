package com.android.pulmuone.sample.ui.bio

import android.app.Activity
import android.content.Intent
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.pulmuone.sample.R
import com.android.pulmuone.sample.utils.Constants
import com.android.pulmuone.sample.utils.Constants.BIO_AUTH
import com.android.pulmuone.sample.utils.Constants.KEY_INTRO
import com.android.pulmuone.sample.utils.Constants.SIMPLE_AUTH_STATUS
import com.android.pulmuone.sample.utils.PreferenceManager
import com.android.pulmuone.sample.ui.dialog.DefaultDialog
import com.android.pulmuone.sample.ui.main.MainActivity
import com.pulmuone.biomodule.aos.BioManager
import com.pulmuone.biomodule.aos.interfaces.BioResult
import kotlinx.android.synthetic.main.activity_biomodule.*
import com.pulmuone.toast.aos.GentleToast

class BioModuleActivity : AppCompatActivity() {

    private var bioManager: BioManager? = null
    private var bioRegistered: String? = null
    private var bioErrorCode = -1
    private var defaultDialog: DefaultDialog? = null

    override fun onResume() {
        super.onResume()

        Log.d("BioModuleActivity:::", "onResume()")
        Log.d("BioModuleActivity:::", "BioErrorCode=$bioErrorCode")

        if (bioErrorCode == BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED) {
            callBioManager()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("BioModuleActivity:::", "onPause()")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biomodule)

        init()
        callBioManager()
    }

    override fun onBackPressed() {
        /*
            do not work - delete super.onBackPressed()
        */
//        super.onBackPressed()
    }

    private fun init() {
        bioManager = BioManager.getInstance(this)
        intent.getStringExtra("from")?.let {
            Log.d("BioModuleActivity:::", it)
            bioRegistered = it
        }

        when {
            bioManager!!.isRegisterBiometrics() -> {
                bio_toolbar_title.let {
                    it?.setText(getString(R.string.bio_auth))
                }
            }
            else -> {
                bio_toolbar_title.let {
                    it?.setText(getString(R.string.bio_reg))
                }
            }
        }

        img_cancel.let {
            it?.setOnClickListener {
                when (bioRegistered) {
                    KEY_INTRO -> {
                        startActivity(Intent(this, MainActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    else -> {
                        overridePendingTransition(0, 0)
                        finish()
                    }
                }
            }
        }

        when {
            bioManager!!.isSupportBiometrics().success -> {
                Log.d("BioModuleActivity:::", "Biometrics Support")
            }
            else -> {
                Log.d("BioModuleActivity:::", "Biometrics Not Support")
            }
        }

        tv_bio_re_auth.let {
            it?.setOnClickListener {
                callBioManager()
            }
        }

        tv_bio_sub_title.let {
            it?.setText(getString(R.string.auth_with_bio))
        }
    }

    private fun callBioManager() {
        if (bioManager != null) {
            intent.getStringExtra("from")?.let {
                bioRegistered = it
            }
            if (bioManager!!.isRegisterBiometrics()) {
                bioManager!!.signBiometrics(
                    this@BioModuleActivity,
                    getString(R.string.bio_auth),
                    "",
                    "",
                    getString(R.string.dialog_update_btn_no_text)
                ) { (success, errorType, errorCode, errorMsg): BioResult ->
                    Log.d("BioModuleActivity:::", "Biometrics Authentication Success:::$success")
                    Log.d("BioModuleActivity:::", "ErrorCode:::$errorCode")
                    Log.d("BioModuleActivity:::", "ErrorType:::${errorType.name}")
                    Log.d("BioModuleActivity:::", "ErrorType:::${errorType.code}")
                    Log.d("BioModuleActivity:::", "ErrorMsg:::$errorMsg")

                    bioErrorCode = errorCode!!
                    PreferenceManager().setLong(this, Constants.KEY_INTERVAL, 0)

                    when {
                        success -> {
                            GentleToast.with(this)
                                .shortToast(getString(R.string.auth_complete))
                                .setTextColor(R.color.colorToastText)
                                .setBackgroundColor(R.color.colorToastBg)
                                .setBackgroundRadius(4)
                                .setTextSize(14)
                                .setAlpha(153) // 60%
                                .show()
                            when (bioRegistered) {
                                KEY_INTRO -> {
                                    startActivity(Intent(this, MainActivity::class.java))
                                    overridePendingTransition(0, 0)
                                    finish()
                                }
                                else -> {
                                    overridePendingTransition(0, 0)
                                    finish()
                                }
                            }
                        }
                        else -> {
                            if(errorCode != 0
                                && errorCode != BiometricPrompt.BIOMETRIC_ERROR_CANCELED
                                && errorCode != BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT
                                && errorCode != BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT_PERMANENT
                                && errorCode != BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS
                            ) {
                                GentleToast.with(this)
                                    .shortToast(errorMsg)
                                    .setTextColor(R.color.colorErrText)
                                    .setBackgroundColor(R.color.colorErrBg)
                                    .setBackgroundRadius(4)
                                    .setTextSize(14)
                                    .show()
                            }
                            when(errorCode) {
                                /*
                                  지문이 일치하지 않을 때
                                 */
                                0 -> {}

                                /*
                                  The hardware is unavailable. Try again later.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {}

                                /*
                                  Error state returned when the sensor was unable to process the current image.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_UNABLE_TO_PROCESS -> {}

                                /*
                                  Error state returned when the current request has been running too long. This is intended to
                                  prevent programs from waiting for the biometric sensor indefinitely. The timeout is platform
                                  and sensor-specific, but is generally on the order of 30 seconds.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_TIMEOUT -> {}

                                /*
                                  Error state returned for operations like enrollment; the operation cannot be completed
                                  because there's not enough storage remaining to complete the operation.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_NO_SPACE -> {}

                                /*
                                  The operation was canceled because the biometric sensor is unavailable. For example, this may
                                  happen when the user is switched, the device is locked or another pending operation prevents
                                  or disables it.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_CANCELED -> {
                                    if(defaultDialog != null && defaultDialog!!.isShowing) {
                                        defaultDialog!!.dismiss()
                                    }
                                    defaultDialog = DefaultDialog(
                                        this,
                                        "",
                                        "$errorMsg\n다시 인증을 진행하시겠습니까?",
                                        false,
                                        _ok = {
                                            callBioManager()
                                        },
                                        _cancel = {
                                            startActivity(Intent(this@BioModuleActivity, MainActivity::class.java))
                                            overridePendingTransition(0, 0)
                                            finish()
                                        },
                                        _singleOk = {},
                                        "확인",
                                        "취소",
                                        ""
                                    )
                                    if (!(this as Activity).isFinishing) defaultDialog?.show()
                                }

                                /*
                                  BIOMETRIC_ERROR_LOCKOUT
                                  > The operation was canceled because the API is locked out due to too many attempts.
                                  This occurs after 5 failed attempts, and lasts for 30 seconds.

                                  BIOMETRIC_ERROR_LOCKOUT_PERMANENT
                                  > The operation was canceled because BIOMETRIC_ERROR_LOCKOUT occurred too many times.
                                  Biometric authentication is disabled until the user unlocks with strong authentication
                                  (PIN/Pattern/Password)

                                  BIOMETRIC_ERROR_NO_BIOMETRICS
                                  > The user does not have any biometrics enrolled.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT, BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT_PERMANENT, BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS -> {
                                    if(defaultDialog != null && defaultDialog!!.isShowing) {
                                        defaultDialog!!.dismiss()
                                    }
                                    defaultDialog = DefaultDialog(
                                        this,
                                        "",
                                        getString(R.string.device_lock_out),
                                        true,
                                        _ok = { },
                                        _cancel = {},
                                        _singleOk = {
                                            startActivity(Intent(this@BioModuleActivity, MainActivity::class.java))
                                            overridePendingTransition(0, 0)
                                            finish()
                                        },
                                        "",
                                        "",
                                        "확인"
                                    )
                                    if (!(this as Activity).isFinishing) defaultDialog?.show()
                                }
                                /*
                                  OEMs should use this constant if there are conditions that do not fit under any of the other
                                  publicly defined constants, and must provide appropriate strings for these
                                  errors to the {@link BiometricPrompt.AuthenticationCallback#onAuthenticationError(int,
                                  CharSequence)} callback. OEMs should expect that the error message will be shown to users.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_VENDOR -> {}

                                /*
                                  The user canceled the operation. Upon receiving this, applications should use alternate
                                  authentication (e.g. a password). The application should also provide the means to return to
                                  biometric authentication, such as a "use <biometric>" button.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED -> {}

                                /*
                                  The device does not have a biometric sensor.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_HW_NOT_PRESENT -> {}

                                /*
                                  The user pressed the negative button. This is a placeholder that is currently only used
                                  by the support library.
                                  @hide
                                 */
                                13 -> { // BIOMETRIC_ERROR_NEGATIVE_BUTTON -> 사용자가 '취소'버튼 클릭
                                    startActivity(Intent(this@BioModuleActivity, MainActivity::class.java))
                                    overridePendingTransition(0, 0)
                                    finish()
                                }

                                /*
                                  The device does not have pin, pattern, or password set up. See
                                  {@link BiometricPrompt.Builder#setAllowedAuthenticators(int)},
                                  {@link Authenticators#DEVICE_CREDENTIAL}, and {@link BiometricManager#canAuthenticate(int)}.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_NO_DEVICE_CREDENTIAL -> {}

                                /*
                                  A security vulnerability has been discovered and the sensor is unavailable until a
                                  security update has addressed this issue. This error can be received if for example,
                                  authentication was requested with {@link Authenticators#BIOMETRIC_STRONG}, but the
                                  sensor's strength can currently only meet {@link Authenticators#BIOMETRIC_WEAK}.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {}
                            }
                        }
                    }
                }
            } else {
                bioManager!!.registerBiometrics(
                    this@BioModuleActivity,
                    getString(R.string.bio_reg),
                    "",
                    "",
                    getString(R.string.dialog_update_btn_no_text)
                ) { (success, errorType, errorCode, errorMsg): BioResult ->
                    Log.d("BioModuleActivity:::", "Biometrics Authentication Success:::$success")
                    Log.d("BioModuleActivity:::", "ErrorCode:::$errorCode")
                    Log.d("BioModuleActivity:::", "ErrorType:::${errorType.name}")
                    Log.d("BioModuleActivity:::", "ErrorType:::${errorType.code}")
                    Log.d("BioModuleActivity:::", "ErrorMsg:::$errorMsg")

                    bioErrorCode = errorCode!!
                    PreferenceManager().setLong(this, Constants.KEY_INTERVAL, 0)

                    when {
                        success -> {
                            GentleToast.with(this)
                                .shortToast(getString(R.string.bio_registered))
                                .setTextColor(R.color.colorToastText)
                                .setBackgroundColor(R.color.colorToastBg)
                                .setBackgroundRadius(4)
                                .setTextSize(14)
                                .setAlpha(153) // 60%
                                .show()

                            PreferenceManager().setString(this, SIMPLE_AUTH_STATUS, BIO_AUTH)
                            startActivity(Intent(this@BioModuleActivity, MainActivity::class.java))
                            overridePendingTransition(0, 0)
                            finish()
                        }
                        else -> {
                            if(errorCode != 0
                                && errorCode != BiometricPrompt.BIOMETRIC_ERROR_CANCELED
                                && errorCode != BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT
                                && errorCode != BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT_PERMANENT
                                && errorCode != BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS
                            ) {
                                GentleToast.with(this)
                                    .shortToast(errorMsg)
                                    .setTextColor(R.color.colorErrText)
                                    .setBackgroundColor(R.color.colorErrBg)
                                    .setBackgroundRadius(4)
                                    .setTextSize(14)
                                    .show()
                            }
                            when(errorCode) {
                                /*
                                  지문이 일치하지 않을 때
                                 */
                                0 -> {}

                                /*
                                  The hardware is unavailable. Try again later.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {}

                                /*
                                  Error state returned when the sensor was unable to process the current image.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_UNABLE_TO_PROCESS -> {}

                                /*
                                  Error state returned when the current request has been running too long. This is intended to
                                  prevent programs from waiting for the biometric sensor indefinitely. The timeout is platform
                                  and sensor-specific, but is generally on the order of 30 seconds.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_TIMEOUT -> {}

                                /*
                                  Error state returned for operations like enrollment; the operation cannot be completed
                                  because there's not enough storage remaining to complete the operation.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_NO_SPACE -> {}

                                /*
                                  The operation was canceled because the biometric sensor is unavailable. For example, this may
                                  happen when the user is switched, the device is locked or another pending operation prevents
                                  or disables it.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_CANCELED -> {
                                    if(defaultDialog != null && defaultDialog!!.isShowing) {
                                        defaultDialog!!.dismiss()
                                    }
                                    defaultDialog = DefaultDialog(
                                        this,
                                        "",
                                        "$errorMsg\n다시 인증을 진행하시겠습니까?",
                                        false,
                                        _ok = {
                                            callBioManager()
                                        },
                                        _cancel = {
                                            startActivity(Intent(this@BioModuleActivity, MainActivity::class.java))
                                            overridePendingTransition(0, 0)
                                            finish()
                                        },
                                        _singleOk = {},
                                        "확인",
                                        "취소",
                                        ""
                                    )
                                    if (!(this as Activity).isFinishing) defaultDialog?.show()
                                }

                                /*
                                  BIOMETRIC_ERROR_LOCKOUT
                                  > The operation was canceled because the API is locked out due to too many attempts.
                                  This occurs after 5 failed attempts, and lasts for 30 seconds.

                                  BIOMETRIC_ERROR_LOCKOUT_PERMANENT
                                  > The operation was canceled because BIOMETRIC_ERROR_LOCKOUT occurred too many times.
                                  Biometric authentication is disabled until the user unlocks with strong authentication
                                  (PIN/Pattern/Password)

                                  BIOMETRIC_ERROR_NO_BIOMETRICS
                                  > The user does not have any biometrics enrolled.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT, BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT_PERMANENT, BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS -> {
                                    if(defaultDialog != null && defaultDialog!!.isShowing) {
                                        defaultDialog!!.dismiss()
                                    }
                                    defaultDialog = DefaultDialog(
                                        this,
                                        "",
                                        getString(R.string.device_lock_out),
                                        true,
                                        _ok = { },
                                        _cancel = {},
                                        _singleOk = {
                                            startActivity(Intent(this@BioModuleActivity, MainActivity::class.java))
                                            overridePendingTransition(0, 0)
                                            finish()
                                        },
                                        "",
                                        "",
                                        "확인"
                                    )
                                    if (!(this as Activity).isFinishing) defaultDialog?.show()
                                }
                                /*
                                  OEMs should use this constant if there are conditions that do not fit under any of the other
                                  publicly defined constants, and must provide appropriate strings for these
                                  errors to the {@link BiometricPrompt.AuthenticationCallback#onAuthenticationError(int,
                                  CharSequence)} callback. OEMs should expect that the error message will be shown to users.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_VENDOR -> {}

                                /*
                                  The user canceled the operation. Upon receiving this, applications should use alternate
                                  authentication (e.g. a password). The application should also provide the means to return to
                                  biometric authentication, such as a "use <biometric>" button.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED -> {}

                                /*
                                  The device does not have a biometric sensor.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_HW_NOT_PRESENT -> {}

                                /*
                                  The user pressed the negative button. This is a placeholder that is currently only used
                                  by the support library.
                                  @hide
                                 */
                                13 -> { // BIOMETRIC_ERROR_NEGATIVE_BUTTON -> 사용자가 '취소'버튼 클릭
                                    startActivity(Intent(this@BioModuleActivity, MainActivity::class.java))
                                    overridePendingTransition(0, 0)
                                    finish()
                                }

                                /*
                                  The device does not have pin, pattern, or password set up. See
                                  {@link BiometricPrompt.Builder#setAllowedAuthenticators(int)},
                                  {@link Authenticators#DEVICE_CREDENTIAL}, and {@link BiometricManager#canAuthenticate(int)}.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_NO_DEVICE_CREDENTIAL -> {}

                                /*
                                  A security vulnerability has been discovered and the sensor is unavailable until a
                                  security update has addressed this issue. This error can be received if for example,
                                  authentication was requested with {@link Authenticators#BIOMETRIC_STRONG}, but the
                                  sensor's strength can currently only meet {@link Authenticators#BIOMETRIC_WEAK}.
                                 */
                                BiometricPrompt.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}
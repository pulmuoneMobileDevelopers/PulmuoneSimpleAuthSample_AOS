package com.pulmuone.biomodule.aos

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.pulmuone.biomodule.aos.interfaces.BioErrorType
import com.pulmuone.biomodule.aos.interfaces.BioInterface
import com.pulmuone.biomodule.aos.interfaces.BioResult
import com.pulmuone.biomodule.aos.util.LogUtil

class BioManager : BioInterface {
    companion object {
        private lateinit var biometricManager: BiometricManager
        private lateinit var sharedPreferences: SharedPreferences

        /**
         * 디버그 여부 기본은 false
         */
        private var isDebug = false

        @Volatile
        private var instance: BioManager? = null

        @JvmStatic
        fun getInstance(context: Context): BioManager = instance ?: synchronized(this) {
            init(context)
            instance ?: BioManager().also {
                instance = it
            }
        }

        private fun init(context: Context){
            biometricManager = BiometricManager.from(context)
            sharedPreferences = context.applicationContext.getSharedPreferences(Constants.TAG, Context.MODE_PRIVATE)
        }

        var debug
            set(value){this.isDebug = value}
            get() = this.isDebug
    }

    override fun isSupportBiometrics(): BioResult {
        val result = when(biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BioErrorType.Success
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BioErrorType.DeviceNotEnrolled
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BioErrorType.DeviceNotSupported
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BioErrorType.DeviceNotSupported
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BioErrorType.DeviceNotSupported
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BioErrorType.DeviceNotAvailable
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BioErrorType.Error
            else -> BioErrorType.Error
        }

        if(debug) LogUtil.d("result : $result")
        return BioResult(result == BioErrorType.Success, result)
    }

    override fun registerBiometrics(activity: FragmentActivity,
                                    title: String, subTitle: String?,
                                    description: String?,
                                    negativeButtonText: String,
                                    callBack: (BioResult) -> Unit) {

        checkRegisterBiometrics(true).let {
            if(it){
                callBack(BioResult(false, BioErrorType.ExistBiometrics, errorMsg = "이미 등록된 생체인증 정보가 존재 합니다."))
                return
            }
        }

        isSupportBiometrics().let {
            if(!it.success){
                callBack(it)
                return
            }
        }

        showSignAlert(activity, title, subTitle, description, negativeButtonText, callBack)
    }

    override fun signBiometrics(activity: FragmentActivity,
                                title: String,
                                subTitle: String?,
                                description: String?,
                                negativeButtonText: String,
                                callBack: (BioResult) -> Unit) {

        checkRegisterBiometrics(false).let {
            if(it) {
                callBack(BioResult(false, BioErrorType.AppBioNotRegister, errorMsg = "생체정보 등록후 이용 가능합니다."))
                return
            }
        }

        if(!isUseBiometrics()){
            callBack(BioResult(false, BioErrorType.AppBioNotRegister, errorMsg = "앱내 생체인증 사용이 OFF 입니다."))
            return
        }

        showSignAlert(activity, title, subTitle, description, negativeButtonText, callBack)
    }

    private fun createBioPromptInfo(title: String,
                                    subTitle: String?,
                                    description: String?,
                                    negativeButtonText: String) : BiometricPrompt.PromptInfo{
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subTitle)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonText)
            //.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG) // 지문 Only 해당 코드 주석시 안면 인증도 노출됨
            .build()
    }

    private fun showSignAlert(activity: FragmentActivity,
                              title: String,
                              subTitle: String?,
                              description: String?,
                              negativeButtonText: String,
                              callBack: (BioResult) -> Unit){

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (debug) LogUtil.e("onAuthenticationError -> errorCode : $errorCode, errString : $errString")

                    val bioError = when (errorCode) {
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BioErrorType.DeviceLockout
                        BiometricPrompt.ERROR_LOCKOUT -> BioErrorType.DeviceLockout
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> BioErrorType.Cancel
                        BiometricPrompt.ERROR_USER_CANCELED -> BioErrorType.Cancel
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> BioErrorType.DeviceNotEnrolled
                        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> BioErrorType.DeviceNotEnrolled
                        BiometricPrompt.ERROR_HW_UNAVAILABLE -> BioErrorType.DeviceNotSupported
                        BiometricPrompt.ERROR_HW_NOT_PRESENT -> BioErrorType.DeviceNotSupported
                        else -> BioErrorType.Error
                    }

                    callBack(BioResult(false, bioError, errorCode, errString.toString()))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (debug) LogUtil.d("onAuthenticationSucceeded -> result : ${result.authenticationType}")
                    changeUseBiometrics(true)
                    setRegisterBiometrics()
                    callBack(BioResult(true, BioErrorType.Success, errorMsg = "생체인증 성공"))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    if (debug) LogUtil.e("onAuthenticationFailed")
                    callBack(BioResult(false, BioErrorType.Error, errorMsg = BioErrorType.Error.name))
                }
            }
        )

        biometricPrompt.authenticate(createBioPromptInfo(title, subTitle, description, negativeButtonText))
    }

    private fun checkRegisterBiometrics(isCheck : Boolean) : Boolean = isRegisterBiometrics() == isCheck

    override fun moveSetting(context: Context) {
        try {
            context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS).addFlags(FLAG_ACTIVITY_NEW_TASK))
        } catch (error: ActivityNotFoundException) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(FLAG_ACTIVITY_NEW_TASK))
        }
    }

    override fun isRegisterBiometrics(): Boolean  {
        sharedPreferences.getBoolean(Constants.isRegisterBiometrics, false).let {
            if(debug) LogUtil.d("isRegisterBiometrics : $it")
            return it
        }
    }

    fun setRegisterBiometrics(): Boolean =
        sharedPreferences.edit().putBoolean(Constants.isRegisterBiometrics, true).commit()

    override fun removeBiometrics(): Boolean =
        sharedPreferences.edit().putBoolean(Constants.isRegisterBiometrics, false).commit()

    override fun isUseBiometrics(): Boolean{
        sharedPreferences.getBoolean(Constants.isUseBiometrics, false).let {
            if(debug) LogUtil.d("isUseBiometrics : $it")
            return it
        }
    }

    override fun changeUseBiometrics(isUse: Boolean): Boolean =
        sharedPreferences.edit().putBoolean(Constants.isUseBiometrics, isUse).commit()

}
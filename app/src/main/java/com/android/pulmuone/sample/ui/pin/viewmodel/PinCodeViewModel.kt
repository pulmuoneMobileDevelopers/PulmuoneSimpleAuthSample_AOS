package com.android.pulmuone.sample.ui.pin.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.pulmuone.sample.R
import com.android.pulmuone.sample.ui.base.PulmuoneApplication
import com.android.pulmuone.sample.utils.Constants
import com.android.pulmuone.sample.utils.Constants.AUTH_MAX_FAIL_COUNT
import com.android.pulmuone.sample.utils.Constants.KEY_PIN_CODE_AUTH
import com.android.pulmuone.sample.utils.Constants.KEY_PIN_CODE_CONFIRM
import com.android.pulmuone.sample.utils.Constants.KEY_PIN_CODE_EMPTY
import com.android.pulmuone.sample.utils.Constants.KEY_PIN_CODE_FAILED
import com.android.pulmuone.sample.utils.Constants.PIN_AUTH
import com.android.pulmuone.sample.utils.Constants.PIN_CODE_AUTH_FAIL_COUNT
import com.android.pulmuone.sample.utils.Constants.PIN_CODE_NUM
import com.android.pulmuone.sample.utils.Constants.PIN_CODE_STATUS
import com.android.pulmuone.sample.utils.Constants.SIMPLE_AUTH_STATUS
import com.android.pulmuone.sample.utils.PreferenceManager
import com.android.pulmuone.sample.ui.pin.NumPadListener
import com.android.pulmuone.sample.utils.AndroidKeyStoreUtil
import com.pulmuone.toast.aos.GentleToast
import kotlin.properties.Delegates
import java.util.*

class PinCodeViewModel : ViewModel() {

    val pinCode = MutableLiveData<String>()
    val securedPinCode = MutableLiveData<String>()
    val pinCodeTitle = MutableLiveData<String>()
    val shouldCloseLiveData = MutableLiveData<Void>()
    val shouldMoveLiveData = MutableLiveData<Void>()

    lateinit var tempPinCode : String
    lateinit var pinCodeStatus: String
    var pinCodeAuthFailCount by Delegates.notNull<Int>()

    /*
        ViewModel 인스턴스가 소멸되기 전에 호출됨
     */
    override fun onCleared() {
        super.onCleared()
        Log.d("PinCodeViewModel:::", "PinCodeViewModel instance about to be destroyed")
    }

    val numPadListener = object : NumPadListener {
        override fun onNumberClicked(number: Char) {
            AndroidKeyStoreUtil.init(PulmuoneApplication.getContext())
            val existingPinCode = pinCode.value ?: ""
            val newPassCode = existingPinCode + number
            pinCode.postValue(newPassCode)

            if (newPassCode.length == 4) {
                Timer().schedule(object: TimerTask(){
                    override fun run() {
                        when (pinCodeStatus) {
                            /*
                                간편비밀번호 생성
                             */
                            KEY_PIN_CODE_EMPTY -> {
                                tempPinCode = newPassCode
                                PreferenceManager().setString(PulmuoneApplication.getContext(), PIN_CODE_STATUS, KEY_PIN_CODE_CONFIRM)
                                initPinCodeTitle(PulmuoneApplication.getContext())
                                pinCode.postValue("")
                            }

                            /*
                                간편비밀번호 확인
                             */
                            KEY_PIN_CODE_CONFIRM -> {
                                if(tempPinCode == newPassCode) {
                                    PreferenceManager().setString(PulmuoneApplication.getContext(), PIN_CODE_NUM, AndroidKeyStoreUtil.encrypt(newPassCode))

                                    /*
                                        간편비밀번호 저장 후 앱 Activity 종료 처리
                                     */
                                    PreferenceManager().setString(PulmuoneApplication.getContext(), PIN_CODE_STATUS, KEY_PIN_CODE_AUTH)
                                    PreferenceManager().setString(PulmuoneApplication.getContext(), SIMPLE_AUTH_STATUS, PIN_AUTH)
                                    PreferenceManager().setInt(PulmuoneApplication.getContext(), PIN_CODE_AUTH_FAIL_COUNT, 0)
                                    shouldCloseLiveData.postValue(null)

                                    showToast(
                                        PulmuoneApplication.getContext().getString(R.string.pin_set),
                                        R.color.colorToastText,
                                        R.color.colorToastBg,
                                        153
                                    )
                                } else {
                                    var authFailCount = PreferenceManager().getInt(PulmuoneApplication.getContext(), PIN_CODE_AUTH_FAIL_COUNT)
                                    if(authFailCount == -1) { authFailCount = 0 }
                                    PreferenceManager().setString(PulmuoneApplication.getContext(), PIN_CODE_STATUS, KEY_PIN_CODE_CONFIRM)
                                    initPinCodeTitle(PulmuoneApplication.getContext(), KEY_PIN_CODE_CONFIRM)

                                    /*
                                     * 간편비밀번호 인증실패 시 실패 카운트 노티
                                     */
                                    authFailCount++
                                    if(authFailCount < AUTH_MAX_FAIL_COUNT) {
                                        PreferenceManager().setInt(PulmuoneApplication.getContext(), PIN_CODE_AUTH_FAIL_COUNT, authFailCount)
                                        authFailCount = PreferenceManager().getInt(PulmuoneApplication.getContext(), PIN_CODE_AUTH_FAIL_COUNT)
                                        var msg: String? = null
                                        when (authFailCount) {
                                            1 -> msg = PulmuoneApplication.getContext().getString(R.string.five_per_one)
                                            2 -> msg = PulmuoneApplication.getContext().getString(R.string.five_per_two)
                                            3 -> msg = PulmuoneApplication.getContext().getString(R.string.five_per_three)
                                            4 -> msg = PulmuoneApplication.getContext().getString(R.string.five_per_four)
                                            5 -> msg = PulmuoneApplication.getContext().getString(R.string.five_per_five)
                                        }

                                        showToast(
                                            PulmuoneApplication.getContext().getString(R.string.pin_not_match) + msg,
                                            R.color.colorErrText,
                                            R.color.colorErrBg,
                                            255
                                        )
                                    }

                                    /*
                                     * 간편비밀번호 5번 인증실패 시 인증 및 interval 초기화 후 로그인 페이지로 이동
                                     */
                                    if(authFailCount == AUTH_MAX_FAIL_COUNT) {
                                        PreferenceManager().setString(PulmuoneApplication.getContext(), PIN_CODE_STATUS, KEY_PIN_CODE_EMPTY)
                                        PreferenceManager().setInt(PulmuoneApplication.getContext(), PIN_CODE_AUTH_FAIL_COUNT, 0)

                                        showToast(
                                            PulmuoneApplication.getContext().getString(R.string.auth_fail),
                                            R.color.colorErrText,
                                            R.color.colorErrBg,
                                            255
                                        )
                                        shouldCloseLiveData.postValue(null)
                                    }
                                }
                                pinCode.postValue("")
                            }

                            /*
                                간편비밀번호 인증
                             */
                            KEY_PIN_CODE_AUTH -> {
                                val registeredPinCode = if(PreferenceManager().getString(PulmuoneApplication.getContext(), PIN_CODE_NUM) != "") {
                                    PreferenceManager().getString(PulmuoneApplication.getContext(), PIN_CODE_NUM)?.let { AndroidKeyStoreUtil.decrypt(it).trim() }
                                } else { "" }
                                if(registeredPinCode != null && registeredPinCode != "") {
                                    if(newPassCode == registeredPinCode) {
                                        showToast(
                                            PulmuoneApplication.getContext().getString(R.string.auth_complete),
                                            R.color.colorToastText,
                                            R.color.colorToastBg,
                                            153
                                        )

                                        PreferenceManager().setInt(PulmuoneApplication.getContext(), PIN_CODE_AUTH_FAIL_COUNT, 0)
                                        PreferenceManager().setString(PulmuoneApplication.getContext(), PIN_CODE_STATUS, KEY_PIN_CODE_AUTH)
                                        PreferenceManager().setLong(PulmuoneApplication.getContext(), Constants.KEY_INTERVAL, 0)
                                        shouldMoveLiveData.postValue(null)
                                    } else {
                                        PreferenceManager().setString(PulmuoneApplication.getContext(), PIN_CODE_STATUS, KEY_PIN_CODE_FAILED)
                                        PreferenceManager().setInt(PulmuoneApplication.getContext(), PIN_CODE_AUTH_FAIL_COUNT, 1)
                                        initPinCodeTitle(PulmuoneApplication.getContext())
                                        showToast(
                                            PulmuoneApplication.getContext().getString(R.string.failed_pin_number_count) + PulmuoneApplication.getContext().getString(R.string.five_per_one),
                                            R.color.colorErrText,
                                            R.color.colorErrBg,
                                            255
                                        )
                                    }
                                    pinCode.postValue("")
                                }
                            }

                            /*
                                간편비밀번호 실패
                             */
                            KEY_PIN_CODE_FAILED -> {
                                val registeredPinCode = if(PreferenceManager().getString(PulmuoneApplication.getContext(), PIN_CODE_NUM) != "") {
                                    PreferenceManager().getString(PulmuoneApplication.getContext(), PIN_CODE_NUM)?.let { AndroidKeyStoreUtil.decrypt(it).trim() }
                                } else { "" }
                                if(registeredPinCode != null && registeredPinCode != "") {
                                    var authFailCount = PreferenceManager().getInt(PulmuoneApplication.getContext(), PIN_CODE_AUTH_FAIL_COUNT)
                                    if(newPassCode == registeredPinCode) {
                                        showToast(
                                            PulmuoneApplication.getContext().getString(R.string.auth_complete),
                                            R.color.colorToastText,
                                            R.color.colorToastBg,
                                            153
                                        )

                                        PreferenceManager().setInt(PulmuoneApplication.getContext(), PIN_CODE_AUTH_FAIL_COUNT, 0)
                                        PreferenceManager().setString(PulmuoneApplication.getContext(), PIN_CODE_STATUS, KEY_PIN_CODE_AUTH)
                                        PreferenceManager().setLong(PulmuoneApplication.getContext(), Constants.KEY_INTERVAL, 0)
                                        shouldMoveLiveData.postValue(null)
                                    } else {
                                        if(authFailCount < AUTH_MAX_FAIL_COUNT) { authFailCount++ }
                                        PreferenceManager().setString(PulmuoneApplication.getContext(), PIN_CODE_STATUS, KEY_PIN_CODE_FAILED)
                                        PreferenceManager().setInt(PulmuoneApplication.getContext(), PIN_CODE_AUTH_FAIL_COUNT, authFailCount)
                                        initPinCodeTitle(PulmuoneApplication.getContext())

                                        /*
                                            간편비밀번호 인증실패 시 실패 카운트 노티
                                         */
                                        if(authFailCount < AUTH_MAX_FAIL_COUNT) {
                                            var msg: String? = null
                                            when (authFailCount) {
                                                2 -> msg = PulmuoneApplication.getContext().getString(R.string.five_per_two)
                                                3 -> msg = PulmuoneApplication.getContext().getString(R.string.five_per_three)
                                                4 -> msg = PulmuoneApplication.getContext().getString(R.string.five_per_four)
                                                5 -> msg = PulmuoneApplication.getContext().getString(R.string.five_per_five)
                                            }

                                            showToast(
                                                PulmuoneApplication.getContext().getString(R.string.failed_pin_number_count) + msg,
                                                R.color.colorErrText,
                                                R.color.colorErrBg,
                                                255
                                            )
                                        }

                                        if(authFailCount == AUTH_MAX_FAIL_COUNT) {
                                            PreferenceManager().setString(PulmuoneApplication.getContext(), PIN_CODE_STATUS, KEY_PIN_CODE_AUTH)
                                            PreferenceManager().setInt(PulmuoneApplication.getContext(), PIN_CODE_AUTH_FAIL_COUNT, 0)
                                            PreferenceManager().setLong(PulmuoneApplication.getContext(), Constants.KEY_INTERVAL, 0)
                                            showToast(
                                                PulmuoneApplication.getContext().getString(R.string.auth_fail),
                                                R.color.colorErrText,
                                                R.color.colorErrBg,
                                                255
                                            )
                                            shouldCloseLiveData.postValue(null)
                                        }
                                    }
                                }
                                pinCode.postValue("")
                            }
                        }
                    }
                }, 500)
            }
        }

        override fun onEraseClicked() {
            val droppedLast = pinCode.value?.dropLast(1) ?: ""
            pinCode.postValue(droppedLast)
        }

        override fun onClearClicked() {
            pinCode.postValue("")
        }
    }

    fun initPinCodeTitle(context: Context) {
        pinCodeStatus = PreferenceManager().getString(context, PIN_CODE_STATUS).toString()

        when (pinCodeStatus) {
            KEY_PIN_CODE_EMPTY -> pinCodeTitle.postValue(context.getString(R.string.enter_new_pin))     // 새로운 간편비밀번호 설정
            KEY_PIN_CODE_CONFIRM -> pinCodeTitle.postValue(context.getString(R.string.enter_again))     // 새로운 간편비밀번호 설정 시 확인
            KEY_PIN_CODE_AUTH -> pinCodeTitle.postValue(context.getString(R.string.enter_pin_number))       // 간편비밀번호 인증
            KEY_PIN_CODE_FAILED -> pinCodeTitle.postValue(context.getString(R.string.enter_pin_number))     // 간편비밀번호 인증실패
        }
    }

    fun initPinCodeTitle(context: Context, pinCodeStatus: String) {
        when (pinCodeStatus) {
            KEY_PIN_CODE_EMPTY -> pinCodeTitle.postValue(context.resources.getString(R.string.enter_new_pin))   // 새로운 간편비밀번호 설정
            KEY_PIN_CODE_CONFIRM -> pinCodeTitle.postValue(context.resources.getString(R.string.enter_again))   // 새로운 간편비밀번호 설정 시 확인
            KEY_PIN_CODE_AUTH -> pinCodeTitle.postValue(context.resources.getString(R.string.enter_pin_number))     // 간편비밀번호 인증
            KEY_PIN_CODE_FAILED -> pinCodeTitle.postValue(context.resources.getString(R.string.enter_pin_number))   // 간편비밀번호 인증실패
        }
    }

    fun showToast(strMsg: String, textColor: Int, bgColor: Int, alpha: Int) {
        android.os.Handler(Looper.getMainLooper()).postDelayed(kotlinx.coroutines.Runnable {
            GentleToast.with(PulmuoneApplication.getContext())
                .shortToast(strMsg)
                .setTextColor(textColor)
                .setBackgroundColor(bgColor)
                .setBackgroundRadius(4)
                .setTextSize(14)
                .setAlpha(alpha)
                .show()
        }, 0)
    }
}
package com.pulmuone.biomodule.aos.interfaces

import android.content.Context
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.FragmentActivity

interface BioInterface {
    /**
     * Device 생체인증 지원 여부 조회 API
     * @return 생체인증결과
     */
    fun isSupportBiometrics(): BioResult

    /**
     * Device 설정화면 이동 API
     */
    fun moveSetting(context: Context)

    /**
     * 서비스앱 생체인증 등록 여부 조회 API
     */
    fun isRegisterBiometrics(): Boolean

    /**
     * 서비스앱 생체인증 등록 API
     * @param activity 사용할 액티비티
     * @param title 타이틀(Require)
     * @param subTitle 서브타이틀(Option)
     * @param description 상세설명(Option)
     * @param negativeButtonText 취소버튼텍스트(Require)
     * @param callBack 성공여부에 대한 콜백
     */
    fun registerBiometrics(activity: FragmentActivity,
                           title: String,
                           subTitle: String?,
                           description: String?,
                           negativeButtonText: String,
                           callBack: (BioResult) -> Unit)

    /**
     * 서비스앱 생체인증 서명 API
     * @param activity 사용할 액티비티
     * @param title 타이틀(Require)
     * @param subTitle 서브타이틀(Option)
     * @param description 상세설명(Option)
     * @param negativeButtonText 취소버튼텍스트(Require)
     * @param callBack 성공여부에 대한 콜백
     */
    fun signBiometrics(
        activity: FragmentActivity,
        title: String,
        subTitle: String?,
        description: String?,
        negativeButtonText: String,
        callBack: (BioResult) -> Unit
    )

    /**
     * 생체인증 여부 삭제
     */
    fun removeBiometrics(): Boolean

    /**
     * 서비스앱 생체인증 사용 여부 조회 API
     */
    fun isUseBiometrics(): Boolean

    /**
     * 서비스앱 생체인증 사용 여부 변경 API
     */
    fun changeUseBiometrics(isUse: Boolean): Boolean
}
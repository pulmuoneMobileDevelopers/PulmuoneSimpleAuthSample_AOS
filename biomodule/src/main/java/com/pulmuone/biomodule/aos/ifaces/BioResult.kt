package com.pulmuone.biomodule.aos.interfaces

/**
 * 에러타입 정의
 */
enum class BioErrorType(val code : Int){
    /**
     * 성공
     */
    Success(0),

    /**
     * 사용자가 인증을 취소 하였습니다.
     */
    Cancel(1),

    /**
     * 생체인증 시도한 실패 횟수가 너무 많아 생체인증을 사용할수 없습니다.
     */
    DeviceLockout(2),

    /**
     * 기기에 등록된 생체 인증 정보가 없습니다. 기기에 생체인증 등록후 이용 가능합니다.
     */
    DeviceNotEnrolled(3),

    /**
     * 기기에서 생체 인식을 사용할 수 없습니다.
     */
    DeviceNotAvailable(4),

    /**
     * 생체인증을 지원하지 않는 기기입니다.
     */
    DeviceNotSupported(5),

    /**
     * 앱에 등록된 생체인증 정보가 없습니다. 앱에 생체인증 등록후 이용해 주세요.
     */
    AppBioNotRegister(6),

    /**
     * 이미 등록된 생체인증 정보가 존재 합니다.
     */
    ExistBiometrics(7),

    /**
     * 앱내 생체인증 사용 여부 Off 상태
     */
    OptionOff(8),

    /**
     * 공통 에러(이외 에러포함)
     */
    Error(9)
}

/**
 * 생체 인증,등록 결과
 * @property success 성공여부
 * @property errorType 에러타입
 * @property errorCode 에러코드
 * @property errorMsg 에러메시지
 */
data class BioResult(
    val success : Boolean = false,
    val errorType : BioErrorType = BioErrorType.Error,
    val errorCode : Int? = BioErrorType.Success.code,
    val errorMsg : String? = ""
)
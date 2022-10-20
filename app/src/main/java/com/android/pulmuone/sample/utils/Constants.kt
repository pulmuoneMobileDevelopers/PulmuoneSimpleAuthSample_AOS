package com.android.pulmuone.sample.utils

object Constants {
    const val PIN_CODE_NUM = "pin_code_num"                             // 간편 비밀번호(4자리)
    const val SIMPLE_AUTH_STATUS = "simple_auth_status"                 // 간편인증 상태
    const val KEY_INTERVAL = "interval"                                 // Background 에서 Foreground 로 전환 시 인터벌
    const val KEY_MAIN = "main"
    const val KEY_INTRO = "intro"
    const val KEY_AUTH_METHOD = "auth_method"
    const val PIN_CODE_STATUS = "pin_code_status"                       // 간편 비밀번호 상태
    const val KEY_PIN_CODE_EMPTY = "pin_code_empty"                     // 앱 최초 실행, 간편 비밀번호 초기화, 설정-앱-데이터 삭제 시
    const val KEY_PIN_CODE_CONFIRM = "pin_code_confirm"                 // 간편비밀번호 설정 시 입력한 비밀번호 확인
    const val KEY_PIN_CODE_AUTH = "pin_code_auth"                       // 간편비밀번호 인증
    const val KEY_PIN_CODE_FAILED = "pin_code_auth_fialed"              // 간편비밀번호 인증 실패
    const val PIN_CODE_AUTH_FAIL_COUNT = "pin_code_auth_fail_count"     // 간편비밀번호 인증 실패 횟수
    const val AUTH_MAX_FAIL_COUNT = 5                                   // 간편비밀번호 최대 실패 횟수
    const val BIO_AUTH = "bio"                                          // 생체 인증
    const val PIN_AUTH = "pin"                                          // 간편비밀번호 인증
}
package com.pulmuone.permission

import android.Manifest


object PermissionConstants {
    const val UNKNOWN_ALLOW_INSTALL = "unknown_allow_install"
    const val APP_UP_APP = "app_up_app"
    const val PICTURE_IN_PICTURE = "picture_in_picture"
    const val PASS_PERMISSION = "pass_permission"

    /**
     * 퍼미션 그룹 맵
     * */
    val PermissionGroupMap = mapOf(
        Manifest.permission_group.CALENDAR to arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
        ),
        Manifest.permission_group.CAMERA to arrayOf(
            Manifest.permission.CAMERA,
        ),
        Manifest.permission_group.CONTACTS to arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.GET_ACCOUNTS,
        ),
        Manifest.permission_group.LOCATION to arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ),
        Manifest.permission_group.MICROPHONE to arrayOf(
            Manifest.permission.RECORD_AUDIO,
        ),
        Manifest.permission_group.PHONE to arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.ADD_VOICEMAIL,
            Manifest.permission.USE_SIP,
            Manifest.permission.ACCESS_CHECKIN_PROPERTIES,
        ),
        Manifest.permission_group.SENSORS to arrayOf(
            Manifest.permission.BODY_SENSORS,
        ),
        Manifest.permission_group.SMS to arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_WAP_PUSH,
            Manifest.permission.RECEIVE_MMS,
        ),
        Manifest.permission_group.STORAGE to arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    ) as HashMap<String, Array<String>>
}

enum class PermissionStatus {
    FIRST,  // 최초 요청
    SECOND, // 두번째 요청
    DENY,   // 두번이상 거부
    GRANT   // 허용
}

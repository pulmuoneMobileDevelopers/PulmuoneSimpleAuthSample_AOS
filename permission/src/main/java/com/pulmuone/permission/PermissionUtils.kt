package com.pulmuone.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

/**
 * 권한 리스트 전체 허용이 되어있는지 여부
 */
fun Context.isPermissionAllGranted(list: Array<String>): Boolean {
    list.forEach {
        when {
            it == PermissionConstants.UNKNOWN_ALLOW_INSTALL -> {
                if (!isAllowUnknownApp()) {
                    return false
                }
            }
            it == PermissionConstants.APP_UP_APP -> {
                if (!isAllowAppUpApp()) {
                    return false
                }
            }
            it == PermissionConstants.PICTURE_IN_PICTURE -> {
                // 그냥 통과
            }
            it == PermissionConstants.PASS_PERMISSION -> {
                // 그냥 통과
            }
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED -> {
                return false
            }
        }
    }
    return true
}

/**
 * 권한 허용 여부
 */
fun Context.isPermissionGranted(data: PermissionData): Boolean {
    return ContextCompat.checkSelfPermission(this, data.permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * 권한 리스트 중 한개라도 권한 거부가 있는지 여부
 */
fun Activity.shouldShowRequestPermissionsRationale(list: ArrayList<PermissionData>): Boolean {
    list.forEach {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, it.permission)) {
            return true
        }
    }
    return false
}

/**
 * 퍼미션 전용 문자열 배열 세팅
 */
fun ArrayList<PermissionData>.changeStringArray(): Array<String> {
    val permissionList = arrayListOf<String>()
    this.forEach {
        permissionList.add(it.permission)
    }
    return permissionList.toArray(arrayOfNulls<String>(permissionList.size))
}

/**
 * 앱 상세 설정 이동 인텐트 정보
 */
fun Context.setDetailSettingIntent() {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    intent.data = Uri.fromParts("package", packageName, null)
    startActivity(intent)
}

/**
 * 권한 허용 여부 Status 확인 (단일 권한)
 */
fun Activity.getGrantedStatus(permission: String, preference: SharedPreferences): PermissionStatus {
    when (permission) {
        PermissionConstants.UNKNOWN_ALLOW_INSTALL -> {
            return when (isAllowUnknownApp()) {
                true -> PermissionStatus.GRANT
                false -> PermissionStatus.DENY
            }
        }
        PermissionConstants.APP_UP_APP -> {
            return when (isAllowAppUpApp()) {
                true -> PermissionStatus.GRANT
                false -> PermissionStatus.DENY
            }
        }
        PermissionConstants.PICTURE_IN_PICTURE -> {
            return PermissionStatus.GRANT
        }
        PermissionConstants.PASS_PERMISSION -> {
            return PermissionStatus.GRANT
        }
        else -> return if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            when (preference.getInt(permission, 0)) {
                0 -> {
                    preference.edit().putInt(permission, 1).apply()
                    PermissionStatus.FIRST
                }
                1 -> {
                    preference.edit().putInt(permission, 2).apply()
                    PermissionStatus.SECOND
                }
                else -> {
                    PermissionStatus.DENY
                }
            }
        } else {
            preference.edit().putInt(permission, 2).apply()
            PermissionStatus.GRANT
        }
    }
}

/**
 * 출처를 알수없는 앱 허용 되어 있는지 체크
 */
fun Context.isAllowUnknownApp(): Boolean {
    return try {
        var isNonPlayAppAllowed = true
        // 오레오(8.0) 이전
        isNonPlayAppAllowed = if (Build.VERSION.SDK_INT < 26) {
            Settings.Secure.getInt(contentResolver, Settings.Global.INSTALL_NON_MARKET_APPS) == 1
        } else {
            val packageManager = packageManager
            packageManager.canRequestPackageInstalls()
        }
        isNonPlayAppAllowed
    } catch (e: Exception) {
        true
    }
}

/**
 * 출처를 알수없는 앱 허용 설정 화면으로 이동
 */
fun Context.moveAllowUnknownSetting() {
    // 오레오(8.0) 이전
    if (Build.VERSION.SDK_INT < 26) {
        startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
    }
    // 오래오(8.0) 이후
    else {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
}

/**
 * 다른 앱 위에 그리기 허용 되어 있는지 체크
 */
fun Context.isAllowAppUpApp(): Boolean {
    return Settings.canDrawOverlays(this)
}

/**
 * 다른 앱 위에 그리기 허용 설정 화면으로 이동
 */
fun Context.moveAllowAppUpAppSetting() {
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    intent.data = Uri.parse("package:$packageName")
    startActivity(intent)
}

/**
 * SnackBar 호출
 * */
fun Context.showSnackBar(view: View, permission: String) {
    val snackBar = Snackbar.make(view, this.defaultPermissionData(permission).mainText + " 권한 요청을 허용해주세요.", Snackbar.LENGTH_LONG)
    snackBar.setAction("설정") {
        when (permission) {
            PermissionConstants.UNKNOWN_ALLOW_INSTALL -> {
                moveAllowUnknownSetting()
            }
            PermissionConstants.APP_UP_APP -> {
                moveAllowAppUpAppSetting()
            }
            PermissionConstants.PICTURE_IN_PICTURE -> {
                // 권한 필요 없음
            }
            else -> {
                setDetailSettingIntent()
            }
        }
    }
    snackBar.show()
}


/**
 * 그룹 퍼미션 명 가져오기
 */
fun String.searchGroupPermission(): String {
    PermissionConstants.PermissionGroupMap.forEach { map ->
        map.value.forEach { permissionString ->
            if (this == permissionString) {
                return map.key.split(".").last()
            }
        }
    }
    return this.split(".").last()
}

/**
 * 그룹 퍼미션 명 가져오기 (없으면 퍼미션명 그대로 반환)
 */
fun String.searchGroupPermissionName(): String {
    PermissionConstants.PermissionGroupMap.forEach { map ->
        map.value.forEach { permissionString ->
            if (this == permissionString) {
                return map.key
            }
        }
    }
    return this
}

/**
 * 같은 퍼미션 그룹인지 비교
 * @param permissionA 퍼미션 A
 * @param permissionB 퍼미션 B
 * @return 같은그룹 여부
 * */
fun isSamePermissionGroup(permissionA: String, permissionB: String): Boolean {
    var groupA = ""
    var groupB = ""
    PermissionConstants.PermissionGroupMap.forEach { map ->
        map.value.forEach { permissionString ->
            if (permissionString == permissionA) {
                groupA = map.key
            }
            if (permissionString == permissionB) {
                groupB = map.key
            }
            if (groupA != "" && groupB != "") {
                return@forEach
            }
        }
    }
    // 하나라도 그룹권한에 속하지 않는게 있으면 다른 퍼미션 그룹이라고 처리한다
    return if (groupA == "" || groupB == "")
        false
    else
        groupA == groupB
}

/**
 * 퍼미션 array 리스트에서 동일 그룹을 제외한 리스트 반환
 *
 * 필수권한 리스트의 경우 permissionList 만 이용,
 * 선택권한 리스트의 경우 addedPermissionList에 필수권한 리스트를 추가한다.
 *
 * @param permissionList 퍼미션 리스트
 * @param addedPermissionList 추가적으로 비교할 리스트
 *
 * */
fun classifyPermissionGroup(permissionList: Array<String>, addedPermissionList: Array<String> = arrayOf()): ArrayList<String> {
    val addedList = ArrayList<String>()

    for (i in permissionList.indices) {
        var isUnique = true
        for (j in i + 1 until permissionList.size) {
            if (isSamePermissionGroup(permissionList[i], permissionList[j])) {
                // 같은 그룹이면 패스한다
                isUnique = false
            }
            if (j == permissionList.size - 1) {
                if (isUnique) {

                    // 추가로 감별할 리스트
                    if (addedPermissionList.isNotEmpty()) {
                        for (k in addedPermissionList.indices) {
                            if (isSamePermissionGroup(permissionList[i], addedPermissionList[k])) {
                                isUnique = false
                            }
                        }
                    }
                    // 마지막까지 같은그룹이 없으면 추가한다.
                    if (isUnique) {
                        addedList.add(permissionList[i])
                    }
                }
            }
        }
        if (i == permissionList.size - 1) {
            // 추가로 감별할 리스트
            if (addedPermissionList.isNotEmpty()) {
                for (k in addedPermissionList.indices) {
                    if (isSamePermissionGroup(permissionList[i], addedPermissionList[k])) {
                        isUnique = false
                    }
                }
            }
            // 마지막 항목이면 무조건 추가
            if (isUnique) {
                addedList.add(permissionList[i])
            }
        }
    }
    return addedList
}

/**
 * 기본 퍼미션 내용
 * @param permission 퍼미션
 * @return 기본 세팅된 퍼미션 내용 데이터
 * */
fun Context.defaultPermissionData(permission: String): PermissionData {

    // 퍼미션 그룹에 속하는 퍼미션일 경우 Default 세팅 값 반환
    PermissionConstants.PermissionGroupMap.forEach { map ->
        map.value.forEach { comparePermission ->
            if (permission == comparePermission) {
                when (map.key) {
                    // 캘린더 읽기/쓰기
                    Manifest.permission_group.CALENDAR -> {
                        return PermissionData(
                            iconImage = R.drawable.img_vector_call,
                            mainText = this.getString(R.string.permissions_title_calendar),
                            description = this.getString(R.string.permissions_description_calendar),
                            permission = permission,
                        )
                    }
                    // 카메라
                    Manifest.permission_group.CAMERA -> {
                        return PermissionData(
                            iconImage = R.drawable.img_vector_camera,
                            mainText = this.getString(R.string.permissions_title_camera),
                            description = this.getString(R.string.permissions_description_camera),
                            permission = permission,
                        )
                    }
                    // 연락처 읽기/쓰기
                    Manifest.permission_group.CONTACTS -> {
                        return PermissionData(
                            iconImage = R.drawable.img_vector_contacts,
                            mainText = this.getString(R.string.permissions_title_contacts),
                            description = this.getString(R.string.permissions_description_contacts),
                            permission = permission,
                        )
                    }
                    // 위치 정보
                    Manifest.permission_group.LOCATION -> {
                        return PermissionData(
                            iconImage = R.drawable.img_vector_gps,
                            mainText = this.getString(R.string.permissions_title_gps),
                            description = this.getString(R.string.permissions_description_gps),
                            permission = permission,
                        )
                    }
                    // 오디오 녹음
                    Manifest.permission_group.MICROPHONE -> {
                        return PermissionData(
                            iconImage = R.drawable.img_vector_voice_record,
                            mainText = this.getString(R.string.permissions_title_voice_record),
                            description = this.getString(R.string.permissions_description_voice_record),
                            permission = permission,
                        )
                    }
                    // 전화상태, 전화걸기, 전화목록 읽기/쓰기, 보이스메일 추가
                    Manifest.permission_group.PHONE -> {
                        return PermissionData(
                            iconImage = R.drawable.img_vector_call,
                            mainText = this.getString(R.string.permissions_title_call),
                            description = this.getString(R.string.permissions_description_call),
                            permission = permission,
                        )
                    }
                    // 센서
                    Manifest.permission_group.SENSORS -> {
                        return PermissionData(
                            iconImage = R.drawable.img_vector_motion_sensor,
                            mainText = this.getString(R.string.permissions_title_sensor),
                            description = this.getString(R.string.permissions_description_sensor),
                            permission = permission,
                        )
                    }
                    // SMS 보내기/받기/일기
                    Manifest.permission_group.SMS -> {
                        return PermissionData(
                            iconImage = R.drawable.img_vector_sms,
                            mainText = this.getString(R.string.permissions_title_sms),
                            description = this.getString(R.string.permissions_description_sms),
                            permission = permission,
                        )
                    }
                    // 저장소 읽기/쓰기
                    Manifest.permission_group.STORAGE -> {
                        return PermissionData(
                            iconImage = R.drawable.img_vector_file,
                            mainText = this.getString(R.string.permissions_title_file),
                            description = this.getString(R.string.permissions_description_file),
                            permission = permission,
                        )
                    }
                }
            }
        }
    }

    // 출처를 알 수 없는 앱
    if (permission == PermissionConstants.UNKNOWN_ALLOW_INSTALL) {
        return PermissionData(
            iconImage = R.drawable.img_vector_unknown_app,
            mainText = this.getString(R.string.permissions_title_unknown_app),
            description = this.getString(R.string.permissions_description_unknown_app),
            permission = permission,
        )
    }
    // 앱 위에 그리기
    if (permission == PermissionConstants.APP_UP_APP) {
        return PermissionData(
            iconImage = R.drawable.img_vector_app_up_app,
            mainText = this.getString(R.string.permissions_title_app_up_app),
            description = this.getString(R.string.permissions_description_app_up_app),
            permission = permission,
        )
    }
    // 그림 속 그림
    if (permission == PermissionConstants.PICTURE_IN_PICTURE) {
        return PermissionData(
            iconImage = R.drawable.img_vector_pip,
            mainText = this.getString(R.string.permissions_title_pip),
            description = this.getString(R.string.permissions_description_pip),
            permission = permission,
        )
    }

    // 그 이외 선택되지 않는 항목일 경우
    return PermissionData(
        iconImage = R.drawable.img_vector_smile,
        mainText = "(기본)문구 수정 필요",
        description = "(기본)문구 수정 필요",
        permission = permission,
    )
}

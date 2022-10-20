package com.pulmuone.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 권한 리스트 전체 허용 여부
 */
fun Context.isPermissionAllGranted(list: ArrayList<PermissionData>): Boolean {
    list.forEach {
        if (it.permission == PermissionConstants.UNKNOWN_ALLOW_INSTALL) {
            if (!isAllowUnknownApp()) {
                return false
            }
        }
        else if (ActivityCompat.checkSelfPermission(this, it.permission) != PackageManager.PERMISSION_GRANTED) {
            return false
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
fun Activity.setDetailSettingIntent() {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    intent.data = Uri.fromParts("package", packageName, null)
    startActivity(intent)
}

/**
 * 권한 허용 여부 Status 확인 (단일 권한)
 */
fun Activity.getGrantedStatus(permission: String, preference: SharedPreferences): PermissionStatus {
    if (permission == PermissionConstants.UNKNOWN_ALLOW_INSTALL) {
        return when (isAllowUnknownApp()) {
            true -> PermissionStatus.GRANT
            false -> PermissionStatus.DENY
        }
    } else
        return if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
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
//fun Activity.getPermissionGrantStatus(permission: String, preference: SharedPreferences): PermissionStatus {
//    return if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
//            Log.d("TAG", "bk $permission : SECOND")
//            PermissionStatus.SECOND
//        } else {
//            val isFirstCheck = preference.getBoolean(permission, true)
//            if (isFirstCheck) {
//                Log.d("TAG", "bk $permission : FIRST")
//                preference.edit().putBoolean(permission, false).apply()
//                PermissionStatus.FIRST
//            } else {
//                Log.d("TAG", "bk $permission : DENY")
//                PermissionStatus.DENY
//            }
//        }
//    } else {
//        Log.d("TAG", "bk $permission : GRANT")
//        PermissionStatus.GRANT
//    }
//}

///**
// * 권한 허용 여부 Status 확인 (멀티 권한)
// */
//fun Activity.getMultiplePermissionStatus(list: ArrayList<PermissionData>, preference: SharedPreferences): PermissionStatus {
//    return if (!isPermissionAllGranted(list)) {
//        if (shouldShowRequestPermissionsRationale(list)) {
//            PermissionStatus.SECOND
//        } else {
//            val isFirst = preference.getBoolean(isFirstRequirePermissionRequest, true)
//            if (isFirst) {
//                preference.edit().putBoolean(isFirstRequirePermissionRequest, false).apply()
//                list.forEach {
//                    preference.edit().putBoolean(it.permission, false).apply()
//                }
//                PermissionStatus.FIRST
//            } else {
//                PermissionStatus.DENY
//            }
//        }
//    } else {
//        PermissionStatus.GRANT
//    }
//}

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
fun Activity.moveAllowUnknownSetting() {
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

enum class PermissionStatus {
    FIRST,  // 최초 요청
    SECOND, // 두번째 요청
    DENY,   // 두번이상 거부
    GRANT   // 허용
}

class PermissionUtils {
    companion object {
        const val isFirstRequirePermissionRequest   = "isFirstRequirePermissionRequest" // 필수권한 최초 요청
    }
}
package com.pulmuone.permission

import android.content.Context
import androidx.lifecycle.ViewModel
import com.pulmuone.permission.PermissionAdaptorItemType.*

class PermissionViewModel: ViewModel() {
    var fragmentData: PermissionFragmentData = PermissionFragmentData()

    fun setPermList(context: Context?):ArrayList<PermissionAdaptorData> {
        fragmentData.apply {
            val list = ArrayList<PermissionAdaptorData>()
            list.apply {
                clear()
                // 필수 권한
                if (requirePermissionList != null && requirePermissionList.size != 0) {
                    add(
                        PermissionAdaptorData(
                            itemType = TITLE,
                            title = context?.getString(R.string.title_require_permission),
                        )
                    )
                    requirePermissionList.forEach { data ->
                        add(
                            PermissionAdaptorData(
                                itemType = CONTENTS,
                                mainText = data.mainText,
                                subText = data.description,
                                iconImage = data.iconImage,
                            )
                        )
                    }
                }
                // 선택 권한
                if (optionalPermissionList != null && optionalPermissionList.size != 0) {
                    add(
                        PermissionAdaptorData(
                            itemType = TITLE,
                            title = context?.getString(R.string.title_optional_permission),
                        )
                    )
                    optionalPermissionList.forEach { data ->
                        add(
                            PermissionAdaptorData(
                                itemType = CONTENTS,
                                mainText = data.mainText,
                                subText = data.description,
                                iconImage = data.iconImage,
                            )
                        )
                    }
                }
                // 접근권한 안내
                add(
                    PermissionAdaptorData(
                        itemType = TITLE,
                        title = context?.getString(R.string.title_setting_permission),
                    )
                )
                add(
                    PermissionAdaptorData(
                        itemType = SETTING,
                        settingText = howToSettingText
                    )
                )
            }
            return list
        }
    }
}

/***
 * @param requirePermissionList 필수 권한 항목
 * @param optionalPermissionList 선택 권한 항목
 * @param howToSettingText 권한 항목 설정 변경 방법
 * @param onClick 확인버튼 클릭 리스너
 */
data class PermissionFragmentData(
    private val permissionList : ArrayList<PermissionAdaptorData> = arrayListOf(),
    val requirePermissionList : ArrayList<PermissionData>? = arrayListOf(),
    val optionalPermissionList : ArrayList<PermissionData>? = arrayListOf(),
    val howToSettingText: String? = "",
    val onClick: (() -> Unit)? = null,
) {
    fun setTotalPermissionList(list : ArrayList<PermissionAdaptorData> ) {
        permissionList.clear()
        list.forEach { data ->
            permissionList.add(data)
        }
    }

    fun getList():ArrayList<PermissionAdaptorData> = permissionList
}

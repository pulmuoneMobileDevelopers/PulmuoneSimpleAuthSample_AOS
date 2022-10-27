package com.pulmuone.permission

import androidx.annotation.DrawableRes

/***
 * 권한 목록 데이터
 * @param iconImage 아이콘 이미지
 * @param mainText 권한항목의 대제목
 * @param description 권한항목의 소제목
 * @param permission 권한 명
 *
 */
data class PermissionData(
    @DrawableRes var iconImage: Int = R.drawable.img_vector_smile,
    var mainText: String = "",
    var description: String = "",
    val permission: String,
)


package com.pulmuone.permission

import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import com.pulmuone.permission.base.BaseRecyclerAdapter
import com.pulmuone.permission.base.BaseViewHolder
import com.pulmuone.permission.databinding.ItemPermissionTitleBinding


class PermissionRecyclerViewAdapter(
    @LayoutRes val layoutResId: Int = R.layout.item_permission_title,
    private val bindingVariableId: Int = BR.permissionData,
) : BaseRecyclerAdapter<PermissionAdaptorData, ItemPermissionTitleBinding>(
    layoutResId,
    bindingVariableId
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ItemPermissionTitleBinding> {
        return when(viewType) {
            TITLE -> PermissionAListViewHolder(
                layoutResId = R.layout.item_permission_title,
                parent = parent,
                bindingVariableId = bindingVariableId,
                viewType = viewType,
            )
            SETTING -> PermissionAListViewHolder(
                layoutResId = R.layout.item_permission_contents_setting,
                parent = parent,
                bindingVariableId = bindingVariableId,
                viewType = viewType,
            )
            else -> PermissionAListViewHolder (
                layoutResId = R.layout.item_permission_contents,
                parent = parent,
                bindingVariableId = bindingVariableId,
                viewType = viewType,
            )
        }
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ItemPermissionTitleBinding>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)

        if ((holder as PermissionAListViewHolder).viewType == CONTENTS )
            items[position].iconImage.let {
                holder.itemView.findViewById<ImageView>(R.id.iv_icon).setImageResource(it)
            }
    }

    class PermissionAListViewHolder(
        @LayoutRes layoutResId: Int,
        parent: ViewGroup,
        val bindingVariableId: Int,
        val viewType: Int,
    ) : BaseViewHolder<ItemPermissionTitleBinding>(
        layoutResId = layoutResId,
        parent = parent,
        bindingVariableId = bindingVariableId
    )

    override fun getItemViewType(position: Int): Int =
        when (items[position].itemType) {
            PermissionAdaptorItemType.TITLE -> TITLE
            PermissionAdaptorItemType.SETTING -> SETTING
            else -> CONTENTS
        }

    companion object {
        const val TITLE = 0
        const val CONTENTS = 1
        const val SETTING = 2
    }
}

/**
 * RecyclerView 에서 사용할 데이터
 * @param itemType 자식 뷰의 타입
 * @param title 리스트의 큰 제목
 * @param mainText 권한 내용 중 메인 내용
 * @param subText 권한 내용 중 서브 내용
 * @param settingText 마지막 설정 변경 방법
 * @param iconImage 아이콘 이미지
 * */
data class PermissionAdaptorData(

    var itemType: PermissionAdaptorItemType = PermissionAdaptorItemType.CONTENTS,
    val title: String? = "",
    val mainText: String? = "",
    val subText: String? = "",
    val settingText: String? = "",
    @DrawableRes val iconImage: Int = R.drawable.ic_launcher_foreground,
)

enum class PermissionAdaptorItemType {
    TITLE,
    CONTENTS,
    SETTING
}

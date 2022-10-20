package com.pulmuone.permission

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.FragmentManager
import com.pulmuone.permission.databinding.FragmentPermissionBinding
import com.pulmuone.permission.base.BaseFixBottomSheetFragment
import com.pulmuone.permission.base.createViewModel
import com.pulmuone.permission.base.setAnimation

/**
 * 권한 안내 팝업
 * */
class PermissionFragment: BaseFixBottomSheetFragment<FragmentPermissionBinding>(
    R.layout.fragment_permission
) {
    private val viewModel by lazy {
        createViewModel { PermissionViewModel() }
    }

    //TODO: - 확인 버튼 등 팀장님이 공간을 좀 어떻게 하면 좋겠다고 하는데 (한번에 보이게) 혹은 버튼 비활성화 활성화. 좀생각해보기
    // 스크롤 없이
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // 시작시 애니메이션
            setAnimation(
                view = binding.clBottomView,
                resId = R.anim.slide_in_bottom
            )

            //데이터 바인딩
            data = viewModel.fragmentData

            // recyclerView 데이터 세팅
            viewModel.fragmentData.setTotalPermissionList(viewModel.setPermList(context))

            rvList.adapter = PermissionRecyclerViewAdapter()
            (rvList.adapter as PermissionRecyclerViewAdapter).setData(
                viewModel.fragmentData.getList()
            )

            // 확인 버튼
            btnConfirm.setOnClickListener {
                // 종료 시 애니메이션
                setAnimation(
                    view = binding.clBottomView,
                    resId = R.anim.slide_out_bottom,
                    endListener = {
                        viewModel.fragmentData.onClick?.invoke()
                        dismissAllowingStateLoss()
                    }
                )
            }
        }
    }

    /**
     * 다이얼로그 호출
     * @param manager 프래그먼트 매니저
     * @param data 퍼미션 안내 팝업 관련 데이터
     * */
    fun showBottomSheet(manager: FragmentManager, data: PermissionFragmentData) {
        super.show(manager, tag)
        viewModel.fragmentData = data
    }

    companion object {
        private const val TAG = "PermissionFragment"
    }

}




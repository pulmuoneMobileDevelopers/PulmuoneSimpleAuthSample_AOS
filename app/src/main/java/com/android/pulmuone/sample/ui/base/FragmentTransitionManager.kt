package com.android.pulmuone.sample.ui.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class FragmentTransitionManager {
    fun changeFragmentOnActivity(
        activity: FragmentActivity,
        containerID: Int,
        fragment: Fragment,
        addBackStack: Boolean
    ) {
        activity
            .supportFragmentManager
            .beginTransaction()
            .replace(containerID, fragment)
            .apply {
                if(addBackStack) {
                    addToBackStack(null)
                }
            }
            .commit()
    }

    fun changeFragmentInFragment(
        containerID: Int,
        parentFragment: Fragment,
        childFragment: Fragment,
        addBackStack: Boolean
    ) {
        parentFragment
            .childFragmentManager
            .beginTransaction()
            .replace(containerID, childFragment)
            .apply {
                if(addBackStack) {
                    addToBackStack(null)
                }
            }
            .commit()
    }
}
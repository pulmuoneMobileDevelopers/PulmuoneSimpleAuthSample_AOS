package com.pulmuone.permission.base

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


/**
 * @param view 애니메이션 적용할 뷰
 * @param resId 애니메이션 리소스 아이디
 * @param endListener 애니메이션 종료 후 수행할 block listener
 * */
fun Fragment.setAnimation(view: View, @AnimRes resId: Int, endListener: ((Boolean) -> Unit)? = null) {
    val animation = AnimationUtils.loadAnimation(context, resId)
    animation.setAnimationListener(object: Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationEnd(animation: Animation?) {
            endListener?.invoke(true)
        }
    })
    view.startAnimation(animation)
}


inline fun <reified T : ViewModel> Fragment.createViewModel(
    crossinline func: () -> T
): T {
    return ViewModelProvider(this, object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>): T = func() as T
    })[T::class.java]
}
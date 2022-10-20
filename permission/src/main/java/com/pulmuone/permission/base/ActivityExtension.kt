package com.pulmuone.permission.base

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <reified T : ViewModel> AppCompatActivity.createViewModel(
    crossinline func: () -> T
): T {
    return ViewModelProvider(this, object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>): T = func() as T
    })[T::class.java]
}
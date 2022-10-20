package com.android.pulmuone.sample.ui.pin.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PinCodeViewModelFactory(
    private val sharedPreferences: SharedPreferences
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")

        if(modelClass.isAssignableFrom(PinCodeViewModel::class.java)) {
            return PinCodeViewModel(sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}
package com.android.pulmuone.sample.ui.pin.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PinCodeViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")

        if(modelClass.isAssignableFrom(PinCodeViewModel::class.java)) {
            return PinCodeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}
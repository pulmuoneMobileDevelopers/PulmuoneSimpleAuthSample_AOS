package com.android.pulmuone.sample.ui.pin

interface NumPadListener {

    fun onNumberClicked(number: Char)

    fun onEraseClicked()

    fun onClearClicked()
}
package com.android.pulmuone.sample.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import com.android.pulmuone.sample.R
import com.android.pulmuone.sample.databinding.DialogDefaultBinding

class DefaultDialog (
    context: Context,
    _title: String,
    _message: String,
    _isSingleButton: Boolean = false,
    _ok: () -> Unit,
    _cancel: () -> Unit,
    _singleOk: () -> Unit,
    _okText: String,
    _cancelText: String,
    _singleOkText: String
) :  Dialog(context), View.OnClickListener {

    private var mBinding: DialogDefaultBinding? = null
    private val binding get() = mBinding

    private val ok = _ok
    private val cancel = _cancel
    private val singleOk = _singleOk
    private var title = _title
    private var message = _message
    private var isSingleButton = _isSingleButton
    private var okText = _okText
    private var cancelText = _cancelText
    private var singleOkText = _singleOkText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBinding = DialogDefaultBinding.inflate(layoutInflater)
        binding?.let { setContentView(it.root) }
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        binding?.let { it.tvMessage.text = message }
        binding?.btnOk?.setOnClickListener(this)

        binding?.let { it.btnOk.text = okText }
        binding?.btnCancel?.setOnClickListener(this)

        binding?.let { it.btnCancel.text = cancelText }
        binding?.btnSingleOk?.setOnClickListener(this)

        binding?.let { it.btnSingleOk.text = singleOkText }
        binding?.btnSingleOk?.setOnClickListener(this)

        if (isSingleButton) {
            binding?.let { it.btnOk.visibility = View.GONE }
            binding?.let { it.btnCancel.visibility = View.GONE }
            binding?.let { it.btnSingleOk.visibility = View.VISIBLE }
        }else {
            binding?.let { it.btnOk.visibility = View.VISIBLE }
            binding?.let { it.btnCancel.visibility = View.VISIBLE }
            binding?.let { it.btnSingleOk.visibility = View.GONE }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_cancel -> {
                cancel.invoke()
                dismiss()
            }
            R.id.btn_ok -> {
                ok.invoke()
                dismiss()
            }

            R.id.btn_single_ok -> {
                singleOk.invoke()
                dismiss()
            }
        }
    }

    override fun show() {
        if (this.isShowing) return
        super.show()
    }
}
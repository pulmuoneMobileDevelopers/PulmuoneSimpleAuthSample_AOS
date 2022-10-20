package com.pulmuone.permission.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<B : ViewDataBinding>(
    @LayoutRes layoutResId: Int,
    parent: ViewGroup,
    private val bindingVariableId: Int? = null
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
), LifecycleOwner {
    private val lifecycleRegistry: LifecycleRegistry by lazy {
        LifecycleRegistry(this)
    }
    protected val binding: B = DataBindingUtil.bind(itemView)!!

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    fun onBind(item: Any?) {
        binding.lifecycleOwner = this

        try {
            bindingVariableId?.let {
                binding.setVariable(it, item)
            }

            initialize(item)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected open fun initialize(item: Any?) {
        // empty
    }

    fun onViewAttachedToWindow() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }
}
package com.pulmuone.permission.base

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

open class BaseRecyclerAdapter<ITEM, B : ViewDataBinding>(
    @LayoutRes private val layoutResId: Int,
    private val bindingVariableId: Int? = null
) : RecyclerView.Adapter<BaseViewHolder<B>>() {

    val items = mutableListOf<ITEM>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        object : BaseViewHolder<B>(
            layoutResId = layoutResId,
            parent = parent,
            bindingVariableId = bindingVariableId
        ) {}

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BaseViewHolder<B>, position: Int) {
        holder.onBind(items[position])
    }

    override fun onViewAttachedToWindow(holder: BaseViewHolder<B>) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttachedToWindow()
    }

    open fun setData(items: List<ITEM>?) {
        items?.let {
            this.items.run {
                clear()
                addAll(it)
                notifyDataSetChanged()
            }
        }
    }

    open fun clearData() {
        items.clear()
        notifyDataSetChanged()
    }
}

package cz.lhoracek.databinding.binding

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import cz.lhoracek.databinding.BR

@BindingAdapter("items", "itemLayout", "onItemClick")
fun <T> LinearLayout.bindList(items: List<T> = emptyList(), layout: Int?, onItemClick: (String)-> Unit = {}) {
    layout?.let {
        this.removeAllViews()
        items.forEach {
            val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(context), layout, this, true)
            binding.setVariable(BR.item, it)
            binding.setVariable(BR.clickHandler, onItemClick)
            binding.executePendingBindings()
            val lp = binding.root.layoutParams as LinearLayout.LayoutParams
            lp.weight = 1.0f
            lp.width = 0
            binding.root.layoutParams = lp
        }
    }
}
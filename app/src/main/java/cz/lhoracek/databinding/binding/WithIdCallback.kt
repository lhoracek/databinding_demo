package cz.lhoracek.databinding.binding

import androidx.recyclerview.widget.DiffUtil
import cz.lhoracek.databinding.model.WithId

class WithIdCallback<T> : DiffUtil.ItemCallback<T>() where T:WithId {
    override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: T, newItem: T) = true
}
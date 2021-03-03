package cz.lhoracek.databinding.binding

import androidx.recyclerview.widget.DiffUtil
import cz.lhoracek.databinding.model.WithId

class WithIdCallback : DiffUtil.ItemCallback<WithId>() {
    override fun areItemsTheSame(oldItem: WithId, newItem: WithId) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: WithId, newItem: WithId) = true
}
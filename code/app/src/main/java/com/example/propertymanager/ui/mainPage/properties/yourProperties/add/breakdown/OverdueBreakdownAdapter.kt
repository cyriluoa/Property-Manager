package com.example.propertymanager.ui.mainPage.properties.yourProperties.add.breakdown

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanager.data.model.OverdueItem
import com.example.propertymanager.databinding.ItemOverdueBreakdownBinding

class OverdueBreakdownAdapter :
    ListAdapter<OverdueItem, OverdueBreakdownAdapter.OverdueViewHolder>(DiffCallback()) {

    inner class OverdueViewHolder(val binding: ItemOverdueBreakdownBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverdueViewHolder {
        val binding = ItemOverdueBreakdownBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OverdueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OverdueViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvOverdueLabel.text = item.label


        holder.binding.tvOverdueAmount.text = String.format("%.2f", item.amount)
        val formattedAmount = String.format("%.2f", item.amount)
        holder.binding.tvOverdueAmount.text = "$$formattedAmount"

    }

    class DiffCallback : DiffUtil.ItemCallback<OverdueItem>() {
        override fun areItemsTheSame(oldItem: OverdueItem, newItem: OverdueItem) =
            oldItem.label == newItem.label

        override fun areContentsTheSame(oldItem: OverdueItem, newItem: OverdueItem) =
            oldItem == newItem
    }
}

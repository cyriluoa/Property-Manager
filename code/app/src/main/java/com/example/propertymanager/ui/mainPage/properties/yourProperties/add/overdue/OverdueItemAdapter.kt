package com.example.propertymanager.ui.mainPage.properties.yourProperties.add.overdue



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanager.data.model.OverdueItem
import com.example.propertymanager.databinding.ItemOverdueAmountBinding

class OverdueItemAdapter(
    private val onDeleteClick: (OverdueItem) -> Unit
) : ListAdapter<OverdueItem, OverdueItemAdapter.OverdueItemViewHolder>(DiffCallback) {

    inner class OverdueItemViewHolder(private val binding: ItemOverdueAmountBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OverdueItem) {
            binding.tvOverdueLabel.text = item.label
            val formattedAmount = String.format("%.2f", item.amount)
            binding.tvOverdueAmount.text = "$$formattedAmount"

            binding.btnDelete.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverdueItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOverdueAmountBinding.inflate(inflater, parent, false)
        return OverdueItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OverdueItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<OverdueItem>() {
        override fun areItemsTheSame(oldItem: OverdueItem, newItem: OverdueItem): Boolean {
            return oldItem.label == newItem.label // Customize if needed
        }

        override fun areContentsTheSame(oldItem: OverdueItem, newItem: OverdueItem): Boolean {
            return oldItem == newItem
        }
    }
}

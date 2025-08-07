package com.example.propertymanager.ui.mainPage.properties.yourProperties

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.propertymanager.R
import com.example.propertymanager.data.model.ContractState
import com.example.propertymanager.data.model.DisplayProperty
import com.example.propertymanager.data.model.PropertyState
import com.example.propertymanager.databinding.ItemPropertyCardBinding

class PropertyListAdapter(
    private val onClick: (DisplayProperty) -> Unit
) : ListAdapter<DisplayProperty, PropertyListAdapter.PropertyViewHolder>(DiffCallback()) {

    inner class PropertyViewHolder(private val binding: ItemPropertyCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DisplayProperty) = with(binding) {
            tvPropertyTitle.text = item.propertyName
            tvPropertyStatus.text = item.status
            tvCurrentClient.text = item.currentClientName
            tvAmountDueMonth.text = "$${item.dueThisMonth}"
            tvTotalAmountDue.text = "$${item.totalDue}"

            tvPropertyStatus.setBackgroundResource(getStatusBadge(item.status))

            if (!item.imageUrl.isNullOrEmpty()) {
                ivPropertyImage.visibility = View.VISIBLE
                Glide.with(root.context).load(item.imageUrl).into(ivPropertyImage)
                llNoImageOverlay.visibility = View.GONE
            } else {
                llNoImageOverlay.visibility = View.VISIBLE
            }

            btnContracts.setOnClickListener { onClick(item) }
        }

        private fun getStatusBadge(state: String): Int {
            return when (state) {
                PropertyState.VACANT.toString() -> R.drawable.status_badge_yellow
                PropertyState.OCCUPIED.toString() ->  R.drawable.status_badge_green
                else -> R.drawable.status_badge_red

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemPropertyCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PropertyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<DisplayProperty>() {
        override fun areItemsTheSame(oldItem: DisplayProperty, newItem: DisplayProperty): Boolean {
            return oldItem.propertyId == newItem.propertyId
        }

        override fun areContentsTheSame(oldItem: DisplayProperty, newItem: DisplayProperty): Boolean {
            return oldItem == newItem
        }
    }
}

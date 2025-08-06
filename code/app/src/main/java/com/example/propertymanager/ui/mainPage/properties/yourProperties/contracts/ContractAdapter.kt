package com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanager.R
import com.example.propertymanager.databinding.ItemContractBinding
import com.example.propertymanager.data.model.Contract
import java.text.SimpleDateFormat
import java.util.*

class ContractAdapter(
    private val onViewPaymentsClick: (Contract) -> Unit
) : ListAdapter<Contract, ContractAdapter.ContractViewHolder>(ContractDiffCallback()) {

    inner class ContractViewHolder(private val binding: ItemContractBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contract: Contract) {
            binding.tvContractDuration.text = "${contract.startDate} - ${contract.endDate}"
            binding.tvContractLength.text = "${contract.contractLengthMonths} months"
            binding.tvContractStatus.text = contract.contractState.name

            contract.createdAt?.let {
                val formatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it.toDate())
                binding.tvCreatedAt.text = formatted
            } ?: run {
                binding.tvCreatedAt.text = "-"
            }

            binding.tvOverdueItems.text = "${contract.preContractOverdueAmounts.size} items"

            val badgeRes = when (contract.contractState.name) {
                "ACTIVE" -> R.drawable.status_badge_yellow
                "OVER" -> R.drawable.status_badge_yellow
                "CANCELLED" -> R.drawable.status_badge_yellow
                else -> R.drawable.status_badge_yellow
            }

            binding.tvContractStatus.setBackgroundResource(badgeRes)

            binding.btnViewBills.setOnClickListener {
                Log.d("ContractAdapter","Button clicked")
                onViewPaymentsClick(contract)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder {
        val binding = ItemContractBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContractViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class ContractDiffCallback : DiffUtil.ItemCallback<Contract>() {
        override fun areItemsTheSame(oldItem: Contract, newItem: Contract): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contract, newItem: Contract): Boolean {
            return oldItem == newItem
        }
    }
}

package com.example.propertymanager.ui.mainPage.payments.client


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.propertymanager.R
import com.example.propertymanager.data.model.ClientPropertyContract
import com.example.propertymanager.databinding.ItemClientPropertyContractCardBinding

class ClientPropertiesAdapter(
    private val onViewBillsClick: (ClientPropertyContract) -> Unit
) : ListAdapter<ClientPropertyContract, ClientPropertiesAdapter.ContractViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder {
        val binding = ItemClientPropertyContractCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContractViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContractViewHolder(
        private val binding: ItemClientPropertyContractCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contract: ClientPropertyContract) = with(binding) {
            tvPropertyName.text = contract.propertyName
            tvContractStatus.text = contract.contractState.name

            // Badge background could be dynamic if needed:
            // tvContractStatus.setBackgroundResource(getBadgeDrawable(contract.contractState))

            tvOwnerName.text = contract.ownerName
            tvStartDate.text = contract.startDate
            tvEndDate.text = contract.endDate
            tvContractLength.text = "${contract.contractLengthMonths} months"

            // Handle image visibility
            if (!contract.propertyImageUrl.isNullOrEmpty()) {
                ivPropertyImage.visibility = View.VISIBLE
                llNoImageOverlay.visibility = View.GONE

                Glide.with(ivPropertyImage.context)
                    .load(contract.propertyImageUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .into(ivPropertyImage)
            } else {
                ivPropertyImage.visibility = View.GONE
                llNoImageOverlay.visibility = View.VISIBLE
            }

            binding.btnViewBills.setOnClickListener {
                onViewBillsClick(contract)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ClientPropertyContract>() {
        override fun areItemsTheSame(
            oldItem: ClientPropertyContract,
            newItem: ClientPropertyContract
        ): Boolean = oldItem.contractId == newItem.contractId

        override fun areContentsTheSame(
            oldItem: ClientPropertyContract,
            newItem: ClientPropertyContract
        ): Boolean = oldItem == newItem
    }
}

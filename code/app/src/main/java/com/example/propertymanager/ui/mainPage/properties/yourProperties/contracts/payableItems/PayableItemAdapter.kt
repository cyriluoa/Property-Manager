package com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts.payableItems



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanager.data.model.PayableItem
import com.example.propertymanager.data.model.PayableState
import com.example.propertymanager.databinding.ItemPayableItemBinding
import java.text.NumberFormat

import java.util.*

class PayableItemAdapter(
    private val onViewPaymentsClicked: (PayableItem) -> Unit
) : ListAdapter<PayableItem, PayableItemAdapter.PayableViewHolder>(DiffCallback()) {

    inner class PayableViewHolder(private val binding: ItemPayableItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PayableItem) = with(binding) {

            tvPayableTitle.text = when (item.type.name) {
                "MONTHLY" -> "Month ${item.monthIndex?.plus(1) ?: "-"} Rent"
                "PRE_CONTRACT_OVERDUE" -> item.overdueItemLabel ?: "Pre-contract Dues"
                else -> "Payable Item"
            }

            tvPayableStatus.text = item.status.name
            tvStartDate.text = item.startDate
            tvDueDate.text = item.dueDate
            tvFullAmount.text = item.amountDue.toString() + " INR"
            tvTotalPaid.text = item.totalPaid.toString() + " INR"
            tvLeftToPay.text = (item.amountDue - item.totalPaid).toString() + " INR"

            btnViewBills.visibility =
                if (item.status == PayableState.NOT_APPLIED_YET) View.GONE else View.VISIBLE

            btnViewBills.setOnClickListener {
                onViewPaymentsClicked(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayableViewHolder {
        val binding = ItemPayableItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PayableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PayableViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<PayableItem>() {
        override fun areItemsTheSame(oldItem: PayableItem, newItem: PayableItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PayableItem, newItem: PayableItem) = oldItem == newItem
    }



}

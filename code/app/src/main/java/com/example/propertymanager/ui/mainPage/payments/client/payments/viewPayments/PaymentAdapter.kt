package com.example.propertymanager.ui.mainPage.payments.client.payments.viewPayments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanager.R
import com.example.propertymanager.data.model.Mode
import com.example.propertymanager.data.model.Payment
import com.example.propertymanager.data.model.PaymentState
import com.example.propertymanager.databinding.ItemPaymentBinding
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class PaymentAdapter(
    private val mode: Mode,
    private val loadingCardIds: Set<String> = emptySet(),
    private val onApproveClicked: (Payment) -> Unit,
    private val onDenyClicked: (Payment) -> Unit,
    private val onViewImageClicked: (Payment) -> Unit
) : ListAdapter<Payment, PaymentAdapter.PaymentViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPaymentBinding.inflate(inflater, parent, false)
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PaymentViewHolder(private val binding: ItemPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: Payment) {
            val isLoading = loadingCardIds.contains(payment.id)
            binding.apply {
                tvAmountPaid.text = "₹${payment.amountPaid}"
                tvTimestamp.text = formatDate(payment.timestamp)
                if(payment.notes == ""){
                    tvNotes.visibility = View.GONE
                }
                else{
                    tvNotes.visibility = View.VISIBLE
                    tvNotes.text = payment.notes
                }


                tvPaymentState.text = payment.paymentState.name
                tvPaymentState.setBackgroundResource(getStatusBadge(payment.paymentState))

                // Show buttons only if pending and in OWNER_MODE
                layoutActionButtons.visibility =
                    if (payment.paymentState == PaymentState.PENDING && mode == Mode.OWNER_MODE) View.VISIBLE else View.GONE

                btnViewImage.visibility = if (payment.proofUrl.isNullOrBlank()) View.GONE else View.VISIBLE

                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                btnApprove.isEnabled = !isLoading
                btnDeny.isEnabled = !isLoading
                btnViewImage.isEnabled = !isLoading

                btnApprove.setOnClickListener { onApproveClicked(payment) }
                btnDeny.setOnClickListener { onDenyClicked(payment) }
                btnViewImage.setOnClickListener { onViewImageClicked(payment) }
            }
        }

        private fun getStatusBadge(status: PaymentState): Int {
            return when (status) {
                PaymentState.PENDING -> R.drawable.status_badge_yellow
                PaymentState.APPROVED -> R.drawable.status_badge_green
                PaymentState.DENIED -> R.drawable.status_badge_red
            }
        }

        private fun formatDate(timestamp: Timestamp?): String {
            if (timestamp == null) return "-"
            val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            return sdf.format(timestamp.toDate())
        }

    }

    class DiffCallback : DiffUtil.ItemCallback<Payment>() {
        override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem == newItem
        }
    }
}

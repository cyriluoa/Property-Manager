package com.example.propertymanager.ui.mainPage.requests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.databinding.ItemRequestBinding
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ClientRequestAdapter(
    private val onItemClicked: (ClientRequest) -> Unit
) : ListAdapter<ClientRequest, ClientRequestAdapter.ClientRequestViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientRequestViewHolder {
        val binding = ItemRequestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ClientRequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClientRequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ClientRequestViewHolder(
        private val binding: ItemRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(request: ClientRequest) {
            binding.tvPropertyName.text = request.propertyName
            binding.tvOwnerName.text = "Owner: ${request.ownerName}"
            binding.tvStatus.text = request.status.replaceFirstChar { it.uppercase() }

            val formattedTime = formatTimestamp(request.timestamp)
            binding.tvTimestamp.text = formattedTime

            binding.btnViewContract.setOnClickListener {
                onItemClicked(request)
            }
        }

        private fun formatTimestamp(timestamp: Timestamp): String {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            sdf.timeZone = TimeZone.getDefault()
            return sdf.format(timestamp.toDate())
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ClientRequest>() {
        override fun areItemsTheSame(oldItem: ClientRequest, newItem: ClientRequest): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ClientRequest, newItem: ClientRequest): Boolean {
            return oldItem == newItem
        }
    }
}

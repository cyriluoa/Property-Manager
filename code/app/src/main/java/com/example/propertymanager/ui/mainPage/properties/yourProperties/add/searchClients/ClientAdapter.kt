package com.example.propertymanager.ui.mainPage.properties.yourProperties.add.searchClients

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.propertymanager.R
import com.example.propertymanager.data.model.User
import com.example.propertymanager.databinding.ItemClientBinding
import com.google.android.material.card.MaterialCardView

class ClientAdapter(
    private val onUserSelected: (User) -> Unit
) : ListAdapter<User, ClientAdapter.ClientViewHolder>(DiffCallback()) {

    private var selectedUserId: String? = null

    inner class ClientViewHolder(private val binding: ItemClientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvUsername.text = user.username
            binding.tvFullname.text = user.fullName
            if(user.photoUrl != null){
                binding.ivProfilePicture.setPadding(0, 0, 0, 0)
                Glide.with(binding.root)
                    .load(user.photoUrl)
                    .placeholder(R.drawable.ic_gallery)
                    .error(R.drawable.ic_camera)
                    .circleCrop()
                    .into(binding.ivProfilePicture)
            }

            val isSelected = user.uid == selectedUserId

            (binding.root as? MaterialCardView)?.setCardBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isSelected) R.color.accent_light_blue else R.color.card_background
                )
            )

            binding.root.setOnClickListener {
                if (user.uid != selectedUserId) {
                    selectedUserId = user.uid
                    notifyDataSetChanged() // rebinds entire list to update selection UI
                    onUserSelected(user)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val binding = ItemClientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.uid == newItem.uid

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }
}

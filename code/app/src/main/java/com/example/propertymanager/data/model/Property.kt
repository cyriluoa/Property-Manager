package com.example.propertymanager.data.model

import com.google.firebase.Timestamp

data class Property(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val ownerId: String = "",
    val currentContractId: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val location: String? = null
)

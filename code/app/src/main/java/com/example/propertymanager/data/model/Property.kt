package com.example.propertymanager.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize


@Parcelize
data class Property(
    val id: String = "",
    val name: String = "",
    val imageUrl: String ?= null,
    val ownerId: String = "",
    val currentContractId: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
) : Parcelable

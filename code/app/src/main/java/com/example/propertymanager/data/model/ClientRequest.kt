package com.example.propertymanager.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClientRequest(
    val id: String = "",
    val clientId: String = "",
    val ownerId: String = "",
    val propertyId: String = "",
    val contractId: String = "",
    val ownerName: String = "",
    val propertyName: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "pending" // <-- New field
) : Parcelable

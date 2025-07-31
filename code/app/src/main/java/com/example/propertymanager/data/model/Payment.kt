package com.example.propertymanager.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize


@Parcelize
data class Payment(
    val id: String = "",
    val amountPaid: Double = 0.0,
    val timestamp: Timestamp = Timestamp.now(),
    val clientId: String = "",
    val proofUrl: String? = null,
    val referenceNumber: String? = null,
    val verified: Boolean = false,
    val notes: String = "",
    val ownerId: String = ""
) : Parcelable


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
    val ownerId: String = "",
    val proofUrl: String? = null,
    val notes: String = "",
    val paymentState: PaymentState = PaymentState.PENDING,
    val propertyName: String = "", // NEW
    val paymentLabel: String = "", // NEW
) : Parcelable



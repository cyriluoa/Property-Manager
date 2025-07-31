package com.example.propertymanager.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contract(
    val id: String = "",
    val clientId: String = "",
    val startDate:  String = "",
    val contractLengthMonths: Int = 0,
    val monthlyRentBreakdown: List<RentBreakdown> = emptyList(),
    val preContractOverdueAmounts: List<OverdueItem> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null,
    val hasClientAccepted: Boolean = false,
    val notes: String = ""
) : Parcelable



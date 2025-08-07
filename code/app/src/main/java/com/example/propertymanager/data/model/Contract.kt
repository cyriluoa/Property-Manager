package com.example.propertymanager.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import java.sql.Time

@Parcelize
data class Contract(
    val id: String = "",
    val clientId: String = "",
    val startDate:  String = "",
    val endDate: String = "",
    val contractLengthMonths: Int = 0,
    val monthlyRentBreakdown: List<RentBreakdown> = emptyList(),
    val preContractOverdueAmounts: List<OverdueItem> = emptyList(),
    val createdAt: Timestamp? = null,
    val cancelledAt: Timestamp?= null,
    val notes: String = "",
    val contractState: ContractState = ContractState.PENDING
) : Parcelable



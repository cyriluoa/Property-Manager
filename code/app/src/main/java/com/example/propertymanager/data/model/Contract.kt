package com.example.propertymanager.data.model

import com.google.firebase.Timestamp

data class Contract(
    val id: String = "",
    val clientId: String = "",
    val startDate: Timestamp? = null,
    val contractLengthMonths: Int = 0,
    val monthlyRentBreakdown: Map<String, Double> = emptyMap(), // "2025-08" to amount
    val preContractOverdueAmounts: Map<String, Double> = emptyMap(),       // "Deposit" to amount
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null,
    val notes: String? = null
)


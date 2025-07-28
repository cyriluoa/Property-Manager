package com.example.propertymanager.data.model

import com.google.firebase.Timestamp


data class Payment(
    val id: String = "",
    val type: String = "",                 // "monthly_rent" or "overdue"
    val label: String = "",               // Display label
    val month: String? = null,            // e.g., "2025-08"
    val overdueLabel: String? = null,     // e.g., "Initial Deposit"
    val amountPaid: Double = 0.0,
    val method: String = "",              // E.g., "Cash", "UPI"
    val timestamp: Timestamp? = null,
    val paidBy: String = "",
    val notes: String? = null,
    val paymentProofUrl: String? = null
)

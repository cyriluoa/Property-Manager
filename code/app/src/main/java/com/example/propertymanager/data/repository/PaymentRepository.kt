package com.example.propertymanager.data.repository

import com.example.propertymanager.data.firebase.PaymentManager
import com.example.propertymanager.data.model.Payment
import jakarta.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentManager: PaymentManager
) {
    fun createPayment(
        payment: Payment,
        propertyId: String,
        contractId: String,
        payableItemId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        paymentManager.makePayment(payment, propertyId, contractId, payableItemId, onSuccess, onFailure)
    }
}

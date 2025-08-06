package com.example.propertymanager.data.repository

import com.example.propertymanager.data.firebase.ContractManager
import com.example.propertymanager.data.firebase.PayableItemManager
import com.example.propertymanager.data.firebase.PaymentManager
import com.example.propertymanager.data.model.Payment
import jakarta.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentManager: PaymentManager,
    private val payableItemManager: PayableItemManager,
    private val contractManager: ContractManager
) {
    fun createPayment(
        payment: Payment,
        propertyId: String,
        contractId: String,
        payableItemId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        paymentManager.makePayment(
            payment,
            propertyId,
            contractId,
            payableItemId,
            onSuccess,
            onFailure
        )
    }

    fun fetchPaymentsForPayableItem(
        propertyId: String,
        contractId: String,
        payableItemId: String,
        onSuccess: (List<Payment>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        paymentManager.fetchPaymentsForPayableItem(propertyId, contractId, payableItemId, onSuccess, onFailure)
    }


    fun markPaymentApproved(
        propertyId: String,
        contractId: String,
        payableItemId: String,
        paymentId: String,
        paymentAmount: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        paymentManager.approvePayment(
            propertyId, contractId, payableItemId, paymentId,
            onSuccess = {
                payableItemManager.approvePaymentAndUpdateContractIfNeeded(
                    propertyId, contractId, payableItemId, paymentAmount,
                    onSuccess,
                    onFailure
                )
            },
            onFailure
        )
    }




    fun markPaymentDenied(
        propertyId: String,
        contractId: String,
        payableItemId: String,
        paymentId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        paymentManager.denyPayment(
            propertyId,
            contractId,
            payableItemId,
            paymentId,
            onSuccess,
            onFailure
        )
    }


}
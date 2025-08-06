package com.example.propertymanager.data.firebase

import com.example.propertymanager.data.model.Payment
import jakarta.inject.Inject
import java.util.UUID
import javax.inject.Singleton


@Singleton
class PaymentManager @Inject constructor(): FirestoreManager() {


    fun makePayment(
        payment: Payment,
        propertyId: String,
        contractId: String,
        payableItemId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val paymentId = UUID.randomUUID().toString()
        val paymentWithId = payment.copy(id = paymentId)

        val paymentRef = db
            .collection("properties")
            .document(propertyId)
            .collection("contracts")
            .document(contractId)
            .collection("payableItems")
            .document(payableItemId)
            .collection("payments")
            .document(paymentId)

        paymentRef.set(paymentWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun fetchPaymentsForPayableItem(
        propertyId: String,
        contractId: String,
        payableItemId: String,
        onSuccess: (List<Payment>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val paymentsRef = db
            .collection("properties")
            .document(propertyId)
            .collection("contracts")
            .document(contractId)
            .collection("payableItems")
            .document(payableItemId)
            .collection("payments")

        paymentsRef.get()
            .addOnSuccessListener { snapshot ->
                val payments = snapshot.documents.mapNotNull { it.toObject(Payment::class.java) }
                onSuccess(payments)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }



}
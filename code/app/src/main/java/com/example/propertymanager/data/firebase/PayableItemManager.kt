package com.example.propertymanager.data.firebase


import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.PayableItem
import com.example.propertymanager.data.model.PayableItemType
import com.example.propertymanager.data.model.PayableStatus
import com.example.propertymanager.data.model.Property
import jakarta.inject.Inject
import java.util.UUID
import javax.inject.Singleton

@Singleton
class PayableItemManager @Inject constructor() : FirestoreManager() {

    fun createPayableItem(
        propertyId: String,
        contractId: String,
        payableItem: PayableItem,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("properties").document(propertyId)
            .collection("contracts").document(contractId)
            .collection("payableItems").document(payableItem.id)
            .set(payableItem)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun createAllPayableItems(
        property: Property,
        contract: Contract,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val batch = db.batch()
        val contractRef = db.collection("properties").document(property.id).collection("contracts").document(contract.id)

        // Monthly rent → NOT_APPLIED_YET
        contract.monthlyRentBreakdown.forEachIndexed { index, rentItem ->
            val itemId = UUID.randomUUID().toString()
            val itemRef = contractRef.collection("payableItems").document(itemId)

            val item = PayableItem(
                id = itemId,
                type = PayableItemType.MONTHLY,
                monthIndex = index,
                dueDate = rentItem.dueDate, // assumes RentBreakdown has a dueDate field
                amountDue = rentItem.amount,
                status = PayableStatus.NOT_APPLIED_YET,
                clientId = contract.clientId,
                ownerId = property.ownerId
            )

            batch.set(itemRef, item)
        }

        // Pre-contract overdues → OVERDUE
        contract.preContractOverdueAmounts.forEach { overdueItem ->
            val itemId = UUID.randomUUID().toString()
            val itemRef = contractRef.collection("payableItems").document(itemId)

            val item = PayableItem(
                id = itemId,
                type = PayableItemType.PRE_CONTRACT_OVERDUE,
                dueDate = contract.startDate,
                overdueItemLabel = overdueItem.label,
                amountDue = overdueItem.amount,
                status = PayableStatus.OVERDUE,
                clientId = contract.clientId,
                ownerId = property.ownerId
            )

            batch.set(itemRef, item)
        }

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}

package com.example.propertymanager.data.firebase


import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.PayableItem
import com.example.propertymanager.data.model.PayableItemType
import com.example.propertymanager.data.model.PayableState
import com.example.propertymanager.data.model.Property
import jakarta.inject.Inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        // Monthly rent → NOT_APPLIED_YET
        contract.monthlyRentBreakdown.forEachIndexed { index, rentItem ->
            val itemId = UUID.randomUUID().toString()
            val itemRef = contractRef.collection("payableItems").document(itemId)

            val startDateStr = rentItem.dueDate
            val startDate = dateFormat.parse(startDateStr)

            val dueDate: String = if (index + 1 < contract.monthlyRentBreakdown.size) {
                val nextDateStr = contract.monthlyRentBreakdown[index + 1].dueDate
                val nextDate = dateFormat.parse(nextDateStr)
                val cal = Calendar.getInstance().apply {
                    time = nextDate
                    add(Calendar.DATE, -1)
                }
                dateFormat.format(cal.time)
            } else {
                // Last item → add 1 month to startDate, then -1 day
                val cal = Calendar.getInstance().apply {
                    time = startDate!!
                    add(Calendar.MONTH, 1)
                    add(Calendar.DATE, -1)
                }
                dateFormat.format(cal.time)
            }

            val item = PayableItem(
                id = itemId,
                type = PayableItemType.MONTHLY,
                monthIndex = index,
                startDate = startDateStr,
                dueDate = dueDate,
                amountDue = rentItem.amount,
                status = PayableState.NOT_APPLIED_YET,
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
                startDate = contract.startDate,
                dueDate = contract.startDate,
                overdueItemLabel = overdueItem.label,
                amountDue = overdueItem.amount,
                status = PayableState.OVERDUE,
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

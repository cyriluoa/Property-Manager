package com.example.propertymanager.data.firebase


import android.util.Log
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.ContractState
import com.example.propertymanager.data.model.PayableItem
import com.example.propertymanager.data.model.PayableItemType
import com.example.propertymanager.data.model.PayableState
import com.example.propertymanager.data.model.Property
import com.google.firebase.firestore.ListenerRegistration
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

    fun listenToPayableItems(
        propertyId: String,
        contractId: String,
        onMonthlyItems: (List<PayableItem>) -> Unit,
        onOverdueItems: (List<PayableItem>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        val query = db.collection("properties")
            .document(propertyId)
            .collection("contracts")
            .document(contractId)
            .collection("payableItems")

        return query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val allItems = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(PayableItem::class.java)
                    } catch (e: Exception) {
                        Log.e("PayableItemParseError", "Error parsing item ${doc.id}: ${e.message}")
                        null
                    }
                }


                val monthlyItems = allItems
                    .filter { it.type == PayableItemType.MONTHLY }
                    .sortedBy { it.monthIndex ?: Int.MAX_VALUE }

                Log.d("Listen to payable items", monthlyItems.size.toString())

                val overdueItems = allItems.filter { it.type == PayableItemType.PRE_CONTRACT_OVERDUE }

                onMonthlyItems(monthlyItems)
                onOverdueItems(overdueItems)
            }
        }
    }


    fun approvePaymentAndUpdateContractIfNeeded(
        propertyId: String,
        contractId: String,
        payableItemId: String,
        paymentAmount: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val TAG = "approvePayment"

        val contractRef = db.collection("properties")
            .document(propertyId)
            .collection("contracts")
            .document(contractId)

        val payableItemsRef = db.collection("properties")
            .document(propertyId)
            .collection("contracts")
            .document(contractId)
            .collection("payableItems")

        val targetItemRef = payableItemsRef.document(payableItemId)

        Log.d(TAG, "Fetching all payable items...")

        payableItemsRef.get()
            .addOnSuccessListener { snapshot ->
                val allItemDocs = snapshot.documents

                Log.d(TAG, "Fetched ${allItemDocs.size} payable items. Starting transaction...")

                db.runTransaction { transaction ->
                    Log.d(TAG, "Transaction started")

                    // Step 1: Read the target item
                    val targetSnap = transaction.get(targetItemRef)
                    val currentItem = targetSnap.toObject(PayableItem::class.java)
                        ?: throw Exception("PayableItem not found")

                    Log.d(TAG, "Current item: ${currentItem.id}, totalPaid: ${currentItem.totalPaid}, amountDue: ${currentItem.amountDue}")

                    // Step 2: Calculate new totals
                    val newTotalPaid = currentItem.totalPaid + paymentAmount
                    val isFullyPaid = newTotalPaid >= currentItem.amountDue

                    var updatedStatus = currentItem.status
                    if (isFullyPaid) {
                        updatedStatus = when (currentItem.status) {
                            PayableState.DUE -> PayableState.PAID
                            PayableState.OVERDUE -> PayableState.PAID_LATE
                            else -> currentItem.status
                        }
                    }

                    Log.d(TAG, "New total paid: $newTotalPaid, fully paid: $isFullyPaid, new status: $updatedStatus")

                    // Step 3: Check all other items' statuses
                    var allPaid = true
                    for ((i, doc) in allItemDocs.withIndex()) {
                        val id = doc.id

                        if (id == payableItemId) {
                            // Use the calculated status for this one
                            Log.d(TAG, "Skipping re-fetch of current item $id; using updated status: $updatedStatus")
                            if (updatedStatus != PayableState.PAID && updatedStatus != PayableState.PAID_LATE) {
                                Log.d(TAG, "Item $id is not fully paid (new status = $updatedStatus). Breaking out.")
                                allPaid = false
                                break
                            }
                            continue
                        }

                        val ref = payableItemsRef.document(id)
                        val snap = transaction.get(ref)
                        val state = snap.getString("status")

                        Log.d(TAG, "Doc #$i ($id) status = $state")

                        if (state != PayableState.PAID.name && state != PayableState.PAID_LATE.name) {
                            Log.d(TAG, "Item $id is not fully paid. Breaking out.")
                            allPaid = false
                            break
                        }
                    }

                    Log.d(TAG, "All items fully paid? $allPaid")

                    // Step 4: Perform updates
                    transaction.update(targetItemRef, mapOf(
                        "totalPaid" to newTotalPaid,
                        "isFullyPaid" to isFullyPaid,
                        "status" to updatedStatus.name
                    ))

                    Log.d(TAG, "Updated target payable item ${currentItem.id}")

                    if (allPaid) {
                        transaction.update(contractRef, "contractState", ContractState.COMPLETELY_PAID_OFF.name)
                        Log.d(TAG, "Updated contract state to COMPLETELY_PAID_OFF")
                    }

                }.addOnSuccessListener {
                    Log.d(TAG, "Transaction successful")
                    onSuccess()
                }.addOnFailureListener {
                    Log.e(TAG, "Transaction failed", it)
                    onFailure(it)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to fetch payable items before transaction", it)
                onFailure(it)
            }
    }


}



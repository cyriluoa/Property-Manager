package com.example.propertymanager.data.repository

import com.example.propertymanager.data.firebase.PayableItemManager
import com.example.propertymanager.data.model.PayableItem
import com.google.firebase.firestore.ListenerRegistration
import jakarta.inject.Inject
import javax.inject.Singleton


@Singleton
class PayableItemRepository @Inject constructor(
    private val payableItemManager: PayableItemManager
){

    fun listenToPayableItems(
        propertyId: String,
        contractId: String,
        onMonthlyItems: (List<PayableItem>) -> Unit,
        onOverdueItems: (List<PayableItem>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return payableItemManager.listenToPayableItems(
            propertyId,
            contractId,
            onMonthlyItems,
            onOverdueItems,
            onError
        )
    }

}
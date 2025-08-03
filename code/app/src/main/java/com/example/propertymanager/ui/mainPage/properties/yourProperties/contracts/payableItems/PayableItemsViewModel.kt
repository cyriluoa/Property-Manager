package com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts.payableItems

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.model.PayableItem
import com.example.propertymanager.data.repository.PayableItemRepository
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class PayableItemsViewModel @Inject constructor(
    private val repository: PayableItemRepository
) : ViewModel() {

    private val _monthlyItems = MutableLiveData<List<PayableItem>>()
    val monthlyItems: LiveData<List<PayableItem>> = _monthlyItems

    private val _overdueItems = MutableLiveData<List<PayableItem>>()
    val overdueItems: LiveData<List<PayableItem>> = _overdueItems

    private var listenerRegistration: ListenerRegistration? = null

    fun startListening(propertyId: String, contractId: String) {
        listenerRegistration = repository.listenToPayableItems(
            propertyId,
            contractId,
            onMonthlyItems = { _monthlyItems.postValue(it) },
            onOverdueItems = { _overdueItems.postValue(it) },
            onError = { Log.e("PayableItemsViewModel", "Listen error", it) }
        )
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}

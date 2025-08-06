package com.example.propertymanager.ui.mainPage.payments.client.payments.viewPayments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.model.Payment
import com.example.propertymanager.data.model.PaymentState
import com.example.propertymanager.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class SharedPaymentsViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _allPayments = MutableLiveData<List<Payment>>()
    val allPayments: LiveData<List<Payment>> = _allPayments

    private val _pendingPayments = MutableLiveData<List<Payment>>()
    val pendingPayments: LiveData<List<Payment>> = _pendingPayments

    private val _approvedPayments = MutableLiveData<List<Payment>>()
    val approvedPayments: LiveData<List<Payment>> = _approvedPayments

    private val _deniedPayments = MutableLiveData<List<Payment>>()
    val deniedPayments: LiveData<List<Payment>> = _deniedPayments

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchPayments(propertyId: String, contractId: String, payableItemId: String) {
        _loading.value = true
        paymentRepository.fetchPaymentsForPayableItem(
            propertyId, contractId, payableItemId,
            onSuccess = { payments ->
                _loading.postValue(false)
                _allPayments.postValue(payments)
                splitPayments(payments)
            },
            onFailure = { e ->
                _loading.postValue(false)
                _error.postValue(e.message)
            }
        )
    }

    fun refresh(propertyId: String, contractId: String, payableItemId: String) {
        fetchPayments(propertyId, contractId, payableItemId)
    }

    private fun splitPayments(payments: List<Payment>) {
        _pendingPayments.value = payments.filter { it.paymentState == PaymentState.PENDING }
        _approvedPayments.value = payments.filter { it.paymentState == PaymentState.APPROVED }
        _deniedPayments.value = payments.filter { it.paymentState == PaymentState.DENIED }
    }
}


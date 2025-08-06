package com.example.propertymanager.ui.mainPage.payments.client.payments.viewPayments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.model.Payment
import com.example.propertymanager.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class PendingPaymentsViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    fun approvePayment(
        propertyId: String,
        contractId: String,
        payableItemId: String,
        payment: Payment,
        onComplete: (Exception?) -> Unit
    ) {
        setLoading(true)
        paymentRepository.markPaymentApproved(
            propertyId, contractId, payableItemId, payment.id, payment.amountPaid,
            onSuccess = {
                setLoading(false)
                onComplete(null)
            },
            onFailure = { error ->
                setLoading(false)
                onComplete(error)
            }
        )
    }

    fun denyPayment(
        propertyId: String,
        contractId: String,
        payableItemId: String,
        paymentId: String,
        onComplete: (Exception?) -> Unit
    ) {
        setLoading(true)
        paymentRepository.markPaymentDenied(
            propertyId, contractId, payableItemId, paymentId,
            onSuccess = {
                setLoading(false)
                onComplete(null)
            },
            onFailure = { error ->
                setLoading(false)
                onComplete(error)
            }
        )
    }
}


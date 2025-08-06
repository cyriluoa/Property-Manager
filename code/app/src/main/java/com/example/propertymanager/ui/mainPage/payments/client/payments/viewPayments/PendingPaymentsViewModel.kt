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

    private val _loadingCards = MutableLiveData<Set<String>>(emptySet())
    val loadingCards: LiveData<Set<String>> = _loadingCards

    private fun setCardLoading(id: String, isLoading: Boolean) {
        val current = _loadingCards.value ?: emptySet()
        _loadingCards.value = if (isLoading) current + id else current - id
    }

    fun approvePayment(
        propertyId: String,
        contractId: String,
        payableItemId: String,
        payment: Payment,
        onComplete: (Exception?) -> Unit
    ) {
        setCardLoading(payment.id, true)
        paymentRepository.markPaymentApproved(
            propertyId, contractId, payableItemId, payment.id, payment.amountPaid,
            onSuccess = {
                setCardLoading(payment.id, false)
                onComplete(null)
            },
            onFailure = { error ->
                setCardLoading(payment.id, false)
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
        setCardLoading(paymentId, true)
        paymentRepository.markPaymentDenied(
            propertyId, contractId, payableItemId, paymentId,
            onSuccess = {
                setCardLoading(paymentId, false)
                onComplete(null)
            },
            onFailure = { error ->
                setCardLoading(paymentId, false)
                onComplete(error)
            }
        )
    }

}

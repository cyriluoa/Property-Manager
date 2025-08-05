package com.example.propertymanager.ui.mainPage.payments.client.payments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.model.Payment
import com.example.propertymanager.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _paymentResult = MutableLiveData<Result<Unit>>()
    val paymentResult: LiveData<Result<Unit>> = _paymentResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun makePayment(
        payment: Payment,
        propertyId: String,
        contractId: String,
        payableItemId: String
    ) {
        _loading.value = true

        paymentRepository.createPayment(
            payment,
            propertyId,
            contractId,
            payableItemId,
            onSuccess = {
                _loading.postValue(false)
                _paymentResult.postValue(Result.success(Unit))
            },
            onFailure = { exception ->
                _loading.postValue(false)
                _paymentResult.postValue(Result.failure(exception))
            }
        )
    }
}

package com.example.propertymanager.ui.mainPage.payments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.propertymanager.data.model.ClientPropertyContract
import com.example.propertymanager.data.model.ContractState
import com.example.propertymanager.data.repository.PropertyRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PaymentsViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    private val _clientContracts = MutableLiveData<List<ClientPropertyContract>>()
    val clientContracts: LiveData<List<ClientPropertyContract>> get() = _clientContracts

    // Split into 6 filtered lists
    val activeContracts: LiveData<List<ClientPropertyContract>> = _clientContracts.map {
        it.filter { contract -> contract.contractState == ContractState.ACTIVE }
    }

    val acceptedContracts: LiveData<List<ClientPropertyContract>> = _clientContracts.map {
        it.filter { contract -> contract.contractState == ContractState.ACCEPTED }
    }

    val cancelledContracts: LiveData<List<ClientPropertyContract>> = _clientContracts.map {
        it.filter { contract -> contract.contractState == ContractState.CANCELLED }
    }

    val deniedContracts: LiveData<List<ClientPropertyContract>> = _clientContracts.map {
        it.filter { contract -> contract.contractState == ContractState.DENIED }
    }

    val expiredContracts: LiveData<List<ClientPropertyContract>> = _clientContracts.map {
        it.filter { contract -> contract.contractState == ContractState.OVER }
    }

    val paidOffContracts: LiveData<List<ClientPropertyContract>> = _clientContracts.map {
        it.filter { contract -> contract.contractState == ContractState.COMPLETELY_PAID_OFF }
    }

    val totalContracts: LiveData<Int> = _clientContracts.map { it.size }

    private val _error = MutableLiveData<Exception>()
    val error: LiveData<Exception> get() = _error

    private var listenerRegistration: ListenerRegistration? = null

    fun refreshClientContracts() {
        propertyRepository.fetchClientPropertyContracts(
            onComplete = { contracts -> _clientContracts.value = contracts },
            onError = { exception -> _error.value = exception }
        )
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}


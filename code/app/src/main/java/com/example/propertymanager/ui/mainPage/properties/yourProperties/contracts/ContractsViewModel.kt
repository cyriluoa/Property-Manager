package com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.ContractState
import com.example.propertymanager.data.repository.ContractRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class ContractsViewModel @Inject constructor(
    private val repository: ContractRepository
) : ViewModel() {

    private val _activeContract = MutableLiveData<Contract?>()
    val activeContract: LiveData<Contract?> = _activeContract

    private val _inactiveContracts = MutableLiveData<List<Contract>>()
    val inactiveContracts: LiveData<List<Contract>> = _inactiveContracts

    fun loadContracts(propertyId: String) {
        repository.listenToContracts(
            propertyId = propertyId,
            onSuccess = { contracts ->
                _activeContract.value = contracts.find { it.contractState == ContractState.ACTIVE }
                _inactiveContracts.value = contracts.filter { it.contractState != ContractState.ACTIVE }
            },
            onFailure = {
                _activeContract.value = null
                _inactiveContracts.value = emptyList()
            }
        )
    }
}

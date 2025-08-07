package com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.ContractState
import com.example.propertymanager.data.model.PropertyState
import com.example.propertymanager.data.repository.ContractRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import java.util.Calendar
import java.util.TimeZone

@HiltViewModel
class ContractsViewModel @Inject constructor(
    private val repository: ContractRepository
) : ViewModel() {

    private val _activeContract = MutableLiveData<Contract?>()
    val activeContract: LiveData<Contract?> = _activeContract

    private val _inactiveContracts = MutableLiveData<List<Contract>>()
    val inactiveContracts: LiveData<List<Contract>> = _inactiveContracts

    private val _cancelResult = MutableLiveData<Result<Unit>>()
    val cancelResult: LiveData<Result<Unit>> = _cancelResult

    private val _canAddContract = MutableLiveData(false)
    val canAddContract: LiveData<Boolean> = _canAddContract

    fun loadContracts(
        propertyId: String,
        currentContractId: String?,
        propertyStatus: String
    ) {
        repository.listenToContracts(
            propertyId = propertyId,
            onSuccess = { contracts ->
                _activeContract.value = contracts.find { it.contractState == ContractState.ACTIVE }
                _inactiveContracts.value = contracts.filter { it.contractState != ContractState.ACTIVE }

                _canAddContract.value = isAddAllowed(contracts, currentContractId, propertyStatus)
            },
            onFailure = {
                _activeContract.value = null
                _inactiveContracts.value = emptyList()
                _canAddContract.value = false
            }
        )
    }

    private fun isAddAllowed(
        contracts: List<Contract>,
        currentContractId: String?,
        propertyStatus: String
    ): Boolean {
        if (currentContractId != null || propertyStatus != PropertyState.VACANT.name) {
            return false
        }

        val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))

        for (contract in contracts) {
            when (contract.contractState) {
                ContractState.DENIED, ContractState.CANCELLED, ContractState.OVER -> continue

                ContractState.COMPLETELY_PAID_OFF -> {
                    val parts = contract.startDate.split("-")
                    if (parts.size != 3) return false

                    val (day, month, year) = parts.map { it.toInt() }

                    val deadline = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month - 1)
                        set(Calendar.DAY_OF_MONTH, day)
                        set(Calendar.HOUR_OF_DAY, 8)
                        set(Calendar.MINUTE, 6)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    if (now.before(deadline)) return false
                }

                else -> return false
            }
        }

        return true
    }

    fun cancelContract(propertyId: String, contractId: String) {
        repository.cancelContract(
            propertyId,
            contractId,
            onSuccess = {
                _cancelResult.postValue(Result.success(Unit))
            },
            onFailure = { exception ->
                _cancelResult.postValue(Result.failure(exception))
            }
        )
    }
}


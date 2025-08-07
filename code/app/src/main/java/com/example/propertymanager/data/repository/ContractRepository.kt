package com.example.propertymanager.data.repository

import com.example.propertymanager.data.firebase.ContractManager
import com.example.propertymanager.data.firebase.PropertyManager
import com.example.propertymanager.data.model.Contract
import jakarta.inject.Inject
import javax.inject.Singleton


@Singleton
class ContractRepository @Inject constructor(
    private val contractManager: ContractManager,
    private val propertyManager: PropertyManager
) {

    fun listenToContracts(
        propertyId: String,
        onSuccess: (List<Contract>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        contractManager.listenToContracts(propertyId, onSuccess, onFailure)
    }

    fun cancelContract(
        propertyId: String,
        contractId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        contractManager.markContractCancelled(
            propertyId = propertyId,
            contractId = contractId,
            onSuccess = {
                propertyManager.removeCurrentContract(
                    propertyId = propertyId,
                    onSuccess = {
                        onSuccess()
                    },
                    onFailure = { propError ->
                        onFailure(propError)
                    }
                )
            },
            onFailure = { contractError ->
                onFailure(contractError)
            }
        )
    }
}

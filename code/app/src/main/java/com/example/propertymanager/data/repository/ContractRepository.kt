package com.example.propertymanager.data.repository

import com.example.propertymanager.data.firebase.ContractManager
import com.example.propertymanager.data.model.Contract
import jakarta.inject.Inject
import javax.inject.Singleton


@Singleton
class ContractRepository @Inject constructor(
    private val contractManager: ContractManager
) {
    fun listenToContracts(
        propertyId: String,
        onSuccess: (List<Contract>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        contractManager.listenToContracts(propertyId, onSuccess, onFailure)
    }
}

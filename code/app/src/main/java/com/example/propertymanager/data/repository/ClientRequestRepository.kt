package com.example.propertymanager.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.propertymanager.data.firebase.ClientRequestManager
import com.example.propertymanager.data.firebase.ContractManager
import com.example.propertymanager.data.firebase.PropertyManager
import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.Property
import jakarta.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRequestRepository @Inject constructor(
    private val manager: ClientRequestManager,
    private val propertyManager: PropertyManager,
    private val contractManager: ContractManager
) {

    private val _requests = MutableLiveData<List<ClientRequest>>()
    val requests: LiveData<List<ClientRequest>> get() = _requests

    private val _pendingCount = MutableLiveData(0)
    val pendingCount: LiveData<Int> get() = _pendingCount

    private val _acceptedCount = MutableLiveData(0)
    val acceptedCount: LiveData<Int> get() = _acceptedCount

    private val _deniedCount = MutableLiveData(0)
    val deniedCount: LiveData<Int> get() = _deniedCount

    fun listenToRequestsForClient(clientId: String) {
        manager.listenToRequestsForClient(
            clientId = clientId,
            onSuccess = { list ->
                _requests.value = list
                calculateStats(list)
            },
            onFailure = { e ->
                // Handle error (log or toast)
            }
        )
    }

    fun removeListener() {
        manager.removeListener()
    }

    private fun calculateStats(requests: List<ClientRequest>) {
        val pending = requests.count { it.status == "pending" }
        val accepted = requests.count { it.status == "accepted" }
        val denied = requests.count { it.status == "denied" }

        _pendingCount.postValue(pending)
        _acceptedCount.postValue(accepted)
        _deniedCount.postValue(denied)
    }

    fun getPropertyAndContractForRequest(
        propertyId: String,
        contractId: String,
        onSuccess: (Property, Contract) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        propertyManager.getPropertyById(propertyId, onSuccess = { property ->
            contractManager.getContractById(propertyId, contractId, onSuccess = { contract ->
                onSuccess(property, contract)
            }, onFailure = onFailure)
        }, onFailure = onFailure)
    }
}
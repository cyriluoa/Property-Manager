package com.example.propertymanager.ui.mainPage.profile.requests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.Property
import com.example.propertymanager.data.repository.ClientRequestRepository
import com.example.propertymanager.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject


@HiltViewModel
class RequestsViewModel @Inject constructor(
    private val repository: ClientRequestRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val requests: LiveData<List<ClientRequest>> = repository.requests

    private val _clientUsername = MutableLiveData<String>()
    val clientUsername: LiveData<String> = _clientUsername

    val pendingCount: LiveData<Int> get() = repository.pendingCount
    val acceptedCount: LiveData<Int> get() = repository.acceptedCount
    val deniedCount: LiveData<Int> get() = repository.deniedCount

    fun fetchClientUsername(clientId: String) {
        userRepository.getUsernameByUid(
            clientId,
            onSuccess = { username ->
                _clientUsername.value = username
            },
            onFailure = {
                _clientUsername.value = "Unknown"
            }
        )
    }

    fun startListening(clientId: String) {
        repository.listenToRequestsForClient(clientId)
    }

    fun stopListening() {
        repository.removeListener()
    }

    fun fetchPropertyAndContract(
        request: ClientRequest,
        onSuccess: (Property, Contract) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.getPropertyAndContractForRequest(
            request.propertyId,
            request.contractId,
            onSuccess,
            onFailure
        )
    }

}

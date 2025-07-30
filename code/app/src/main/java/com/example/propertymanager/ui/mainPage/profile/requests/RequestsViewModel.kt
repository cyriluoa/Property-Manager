package com.example.propertymanager.ui.mainPage.profile.requests

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.data.repository.ClientRequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject


@HiltViewModel
class RequestsViewModel @Inject constructor(
    private val repository: ClientRequestRepository
) : ViewModel() {

    val requests: LiveData<List<ClientRequest>> = repository.requests

    val pendingCount: LiveData<Int> get() = repository.pendingCount
    val acceptedCount: LiveData<Int> get() = repository.acceptedCount
    val deniedCount: LiveData<Int> get() = repository.deniedCount

    fun startListening(clientId: String) {
        repository.listenToRequestsForClient(clientId)
    }

    fun stopListening() {
        repository.removeListener()
    }
}

package com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.repository.PropertyRepository
import com.example.propertymanager.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class AddContractDraftViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    private val _ownerUsername = MutableLiveData<String>()
    val ownerUsername: LiveData<String> = _ownerUsername

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchOwnerUsername(ownerId: String) {
        userRepository.getUsernameByUid(
            uid = ownerId,
            onSuccess = { username -> _ownerUsername.value = username ?: "Unknown" },
            onFailure = { _ownerUsername.value = "Unknown"}
        )
    }

    fun submitContractToProperty(
        contract: Contract,
        clientRequest: ClientRequest,
        propertyId: String,
        ownerId: String
    ) {
        _isLoading.value = true

        propertyRepository.updatePropertyWithNewContractAndSendOutClientRequest(
            propertyId = propertyId,
            ownerId = ownerId,
            contract = contract,
            clientRequest = clientRequest,
            onSuccess = {
                _isLoading.postValue(false)
                _successMessage.postValue("Contract added successfully")
            },
            onFailure = { error ->
                _isLoading.postValue(false)
                _errorMessage.postValue(error.message ?: "Unknown error occurred")
            }
        )
    }
}

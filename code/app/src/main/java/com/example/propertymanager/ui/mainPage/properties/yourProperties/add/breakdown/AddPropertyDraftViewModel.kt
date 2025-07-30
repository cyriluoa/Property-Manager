package com.example.propertymanager.ui.mainPage.properties.yourProperties.add.breakdown

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.Property
import com.example.propertymanager.data.repository.PropertyRepository
import com.example.propertymanager.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class AddPropertyDraftViewModel @Inject constructor(
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
            ownerId,
            onSuccess = { username ->
                _ownerUsername.value = username
            },
            onFailure = {
                _ownerUsername.value = "Unknown"
            }
        )
    }

    fun submitPropertyWithContractAndRequest(
        property: Property,
        contract: Contract,
        clientRequest: ClientRequest
    ) {
        _isLoading.value = true

        propertyRepository.createFullPropertyFlow(
            property = property,
            contract = contract,
            clientRequest = clientRequest,
            onSuccess = {
                _isLoading.value = false
                _successMessage.value = "Successfully created property and contract and send request to client"
            },
            onFailure = { error ->
                _isLoading.value = false
                _errorMessage.value = error.message ?: "Something went wrong"
            }
        )
    }
}


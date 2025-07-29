package com.example.propertymanager.ui.mainPage.properties.yourProperties.add.breakdown

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class AddPropertyDraftViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _ownerUsername = MutableLiveData<String>()
    val ownerUsername: LiveData<String> = _ownerUsername

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
}

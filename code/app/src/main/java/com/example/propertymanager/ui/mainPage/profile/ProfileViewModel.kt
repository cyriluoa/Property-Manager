package com.example.propertymanager.ui.mainPage.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.model.User
import com.example.propertymanager.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadUserData() {
        userRepository.getCurrentUserInfo(
            onSuccess = {
                _user.postValue(it)
            },
            onFailure = {
                _error.postValue(it.message)
            }
        )
    }

    fun signOut() {
        userRepository.signOut()
    }

}

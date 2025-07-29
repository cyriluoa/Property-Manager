package com.example.propertymanager.ui.mainPage.profile

import android.util.Log
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

    private var hasLoaded = false // ✅ Caching flag

    fun loadUserData() {
        if (hasLoaded) return // ✅ Prevent multiple fetches

        userRepository.getCurrentUserInfo(
            onSuccess = {
                _user.postValue(it)
                hasLoaded = true
            },
            onFailure = {
                _error.postValue(it.message)
            }
        )
    }

    fun signOut(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRepository.signOut(
            onSuccess = {
                _user.value = null
                hasLoaded = false
                onSuccess()
            },
            onFailure = { error ->
                Log.e("SignOut", "Error signing out: ${error.message}")
                onFailure(error)
            }
        )
    }


}


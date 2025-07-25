package com.example.propertymanager.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _errorMessage.value = "Username and password must not be empty"
            return
        }

        _isLoading.value = true
        authRepository.getEmailFromUsername(
            username,
            onSuccess = { email ->
                authRepository.loginUser(
                    email = email,
                    password = password,
                    onSuccess = {
                        _isLoading.value = false
                        _loginSuccess.value = true
                    },
                    onFailure = { loginError ->
                        _isLoading.value = false
                        _errorMessage.value = loginError.message
                    }
                )
            },
            onFailure = { emailError ->
                _isLoading.value = false
                _errorMessage.value = "Username not found"
            }
        )
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

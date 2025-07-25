package com.example.propertymanager.ui.createAccount

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _usernameAvailable = MutableLiveData<Boolean>()
    val usernameAvailable: LiveData<Boolean> = _usernameAvailable

    private val _accountCreated = MutableLiveData<Boolean>()
    val accountCreated: LiveData<Boolean> = _accountCreated

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun createAccount(fullName: String, username: String, email: String, password: String) {
        _loading.value = true
        _errorMessage.value = null

        authRepository.signUpUser(
            fullName,
            username,
            email,
            password,
            onSuccess = {
                _loading.postValue(false)
                _accountCreated.postValue(true)
            },
            onFailure = { e ->
                _loading.postValue(false)
                _errorMessage.postValue(e.message)
                Log.e("CreateAccountVM", "SignUp Error", e)
            }
        )
    }

    fun checkUsernameAvailability(username: String) {

        authRepository.isUsernameAvailable(username) { isAvailable ->
            _usernameAvailable.postValue(isAvailable)
        }

    }
}

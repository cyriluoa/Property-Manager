package com.example.propertymanager.ui.mainPage.properties.yourProperties.add.searchClients

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.model.User
import com.example.propertymanager.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class SearchClientsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> get() = _allUsers

    private val _filteredUsers = MutableLiveData<List<User>>()
    val filteredUsers: LiveData<List<User>> get() = _filteredUsers

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    fun fetchAllUsers() {
        userRepository.getAllUsers(
            onSuccess = { users ->
                _allUsers.value = users
                _filteredUsers.value = users
                _message.value = "Users loaded successfully ✅"
            },
            onFailure = {
                _message.value = "Failed to load users: ${it.message}"
            }
        )
    }

    fun filterUsers(query: String) {
        val lower = query.trim().lowercase()
        val filtered = _allUsers.value?.filter {
            it.username.lowercase().contains(lower)
        } ?: emptyList()
        _filteredUsers.value = filtered

        if (query.isNotBlank() && filtered.isEmpty()) {
            _message.value = "No users found matching \"$query\""
        } else {
            _message.value = null
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

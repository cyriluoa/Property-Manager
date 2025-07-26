package com.example.propertymanager.data.repository

import com.example.propertymanager.data.firebase.UserManager
import com.example.propertymanager.data.model.User
import jakarta.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userManager: UserManager
) {
    fun getCurrentUserInfo(
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userManager.getCurrentUserInformation(onSuccess, onFailure)
    }

    fun signOut() {
        userManager.signOutUser()
    }

}

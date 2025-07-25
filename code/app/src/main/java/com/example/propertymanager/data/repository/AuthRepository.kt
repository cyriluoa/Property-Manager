package com.example.propertymanager.data.repository

import com.example.propertymanager.data.firebase.AuthManager
import com.example.propertymanager.data.firebase.UserManager
import com.example.propertymanager.data.model.User

import jakarta.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthRepository @Inject constructor(
    private val authManager: AuthManager,
    private val userManager: UserManager
) {

    fun signUpUser(
        fullName: String,
        username: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authManager.createUserWithEmailAndPassword(
            email,
            password,
            onSuccess = { uid ->
                val newUser = User(
                    fullName = fullName,
                    username = username,
                    email = email
                )
                userManager.createUserRecord(uid, newUser,
                    onSuccess = onSuccess,
                    onFailure = { error ->
                        // ⚠️ Rollback: Delete user from Auth if Firestore creation failed
                        authManager.deleteUserFromAuth(
                            uid,
                            onSuccess = { onFailure(error) }, // return original Firestore failure
                            onFailure = { deleteError ->
                                // Combine both errors if needed
                                onFailure(Exception("Firestore failed: ${error.message}. Also failed to delete auth user: ${deleteError.message}"))
                            }
                        )
                    }
                )
            },
            onFailure = onFailure
        )
    }


    fun loginUser(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authManager.signInWithEmailAndPassword(email, password, onSuccess, onFailure)
    }

    fun isUsernameAvailable(
        username: String,
        onResult: (Boolean) -> Unit
    ) {
        userManager.isUsernameAvailable(username, onResult)
    }

    fun getEmailFromUsername(
        username: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userManager.getEmailByUsername(username, onSuccess, onFailure)
    }


}

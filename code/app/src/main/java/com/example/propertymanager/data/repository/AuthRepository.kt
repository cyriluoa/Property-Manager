package com.example.propertymanager.data.repository

import com.example.propertymanager.data.firebase.AuthManager
import com.example.propertymanager.data.firebase.UserManager
import com.example.propertymanager.data.model.User
import com.example.propertymanager.sharedPrefs.FcmTokenManager

import jakarta.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthRepository @Inject constructor(
    private val authManager: AuthManager,
    private val userManager: UserManager,
    private val fcmTokenManager: FcmTokenManager
) {

    fun signUpUser(
        fullName: String,
        username: String,
        email: String,
        password: String,
        profileImageUrl: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        authManager.createUserWithEmailAndPassword(
            email,
            password,
            onSuccess = { uid ->
                fcmTokenManager.getCurrentToken(
                    onTokenReceived = { token ->
                        val newUser = User(
                            uid = uid,
                            fullName = fullName,
                            username = username,
                            email = email,
                            photoUrl = profileImageUrl,
                            fcmTokens = listOf(token)
                        )

                        userManager.createUserRecord(
                            uid,
                            newUser,
                            onSuccess = onSuccess,
                            onFailure = { error ->
                                // Firestore creation failed — rollback Auth
                                authManager.deleteUserFromAuth(
                                    uid,
                                    onSuccess = { onFailure(error) },
                                    onFailure = { deleteError ->
                                        onFailure(
                                            Exception("Firestore failed: ${error.message}. Also failed to delete Auth user: ${deleteError.message}")
                                        )
                                    }
                                )
                            }
                        )
                    },
                    onFailure = { tokenError ->
                        // FCM token fetch failed — rollback Auth
                        authManager.deleteUserFromAuth(
                            uid,
                            onSuccess = { onFailure(tokenError) },
                            onFailure = { deleteError ->
                                onFailure(
                                    Exception("Token fetch failed: ${tokenError.message}. Also failed to delete Auth user: ${deleteError.message}")
                                )
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
        authManager.signInWithEmailAndPassword(
            email,
            password,
            onSuccess = { uid ->
                fcmTokenManager.getCurrentToken(
                    onTokenReceived = { token ->
                        userManager.addFcmToken(
                            uid,
                            token,
                            onSuccess = {
                                onSuccess(uid)
                            },
                            onFailure = { firestoreError ->
                                onFailure(Exception("Logged in but failed to store token: ${firestoreError.message}"))
                            }
                        )
                    },
                    onFailure = { tokenError ->
                        onFailure(Exception("Logged in but failed to fetch FCM token: ${tokenError.message}"))
                    }
                )
            },
            onFailure = onFailure
        )
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

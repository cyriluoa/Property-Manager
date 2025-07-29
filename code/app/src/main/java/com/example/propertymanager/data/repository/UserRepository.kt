package com.example.propertymanager.data.repository

import com.example.propertymanager.data.firebase.UserManager
import com.example.propertymanager.data.model.User
import com.example.propertymanager.sharedPrefs.FcmTokenManager
import jakarta.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userManager: UserManager,
    private val fcmTokenManager: FcmTokenManager
) {
    fun getCurrentUserInfo(
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userManager.getCurrentUserInformation(onSuccess, onFailure)
    }

    fun getUsernameByUid(
        uid: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userManager.getUsernameByUid(uid, onSuccess, onFailure)
    }




    fun signOut(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val token = fcmTokenManager.getSavedToken()

        if (token == null) {
            // Token doesn't exist locally, just sign out
            userManager.signOutUser()
            onSuccess()
            return
        }

        val uid = userManager.getCurrentUserUid()
        if (uid == null) {
            onFailure(Exception("No current user UID found"))
            return
        }

        userManager.removeFcmToken(
            uid = uid,
            token = token,
            onSuccess = {
                fcmTokenManager.clearSavedToken()
                userManager.signOutUser()
                onSuccess()
            },
            onFailure = { e ->
                onFailure(Exception("Failed to remove FCM token: ${e.message}"))
            }
        )
    }



    fun getAllUsers(
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userManager.getAllUsers(onSuccess, onFailure)
    }

}

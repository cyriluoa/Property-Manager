package com.example.propertymanager.data.firebase


import com.example.propertymanager.data.model.User
import com.example.propertymanager.utils.Constants
import jakarta.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor() : FirestoreManager() {

    fun createUserRecord( uid: String, user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {

        val userRef = db.collection(Constants.USERS_COLLECTION).document(uid)
        val usernameRef = db.collection(Constants.USERNAMES_COLLECTION).document(user.username)

        val usernameMap = mapOf(Constants.FIELD_EMAIL to user.email)

        db.runBatch { batch ->
            batch.set(userRef, user) // ✅ write full user object
            batch.set(usernameRef, usernameMap) // ✅ just a simple map
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { e ->
            onFailure(e)
        }
    }

    fun isUsernameAvailable(
        username: String,
        onResult: (Boolean) -> Unit
    ) {
        db.collection(Constants.USERNAMES_COLLECTION)
            .document(username)
            .get()
            .addOnSuccessListener { doc ->
                onResult(!doc.exists()) // Available if no doc found
            }
            .addOnFailureListener {
                onResult(false) // Fail safe: assume not available
            }
    }

    fun getEmailByUsername(
        username: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(Constants.USERNAMES_COLLECTION)
            .document(username)
            .get()
            .addOnSuccessListener { doc ->
                val email = doc.getString(Constants.FIELD_EMAIL)
                if (!email.isNullOrEmpty()) {
                    onSuccess(email)
                } else {
                    onFailure(Exception("Email not found for username"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getCurrentUserInformation(
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrEmpty()) {
            onFailure(Exception("User not logged in"))
            return
        }

        db.collection(Constants.USERS_COLLECTION)
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val user = doc.toObject(User::class.java)
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onFailure(Exception("User data is null"))
                    }
                } else {
                    onFailure(Exception("User document not found"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getAllUsers(
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUid = auth.currentUser?.uid
        db.collection(Constants.USERS_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents
                    .mapNotNull { it.toObject(User::class.java) }
                    .filter { it.uid != currentUid } // ✅ Filter out current user
                onSuccess(users)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }


    fun signOutUser() {
        auth.signOut()
    }






}

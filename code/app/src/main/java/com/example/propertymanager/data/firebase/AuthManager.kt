package com.example.propertymanager.data.firebase

import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import jakarta.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(): FirestoreManager() {



    fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: (String) -> Unit, // returns UID
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    onSuccess(uid)
                } else {
                    onFailure(Exception("UID is null"))
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: (String) -> Unit, // returns UID
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    onSuccess(uid)
                } else {
                    onFailure(Exception("UID is null"))
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun deleteUserFromAuth(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val functions = Firebase.functions
        val data = hashMapOf("uid" to uid)

        functions
            .getHttpsCallable("deleteUserByUid")
            .call(data)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

}

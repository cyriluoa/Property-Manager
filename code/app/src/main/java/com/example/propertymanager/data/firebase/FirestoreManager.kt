package com.example.propertymanager.data.firebase


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


open class FirestoreManager {

    protected val db = FirebaseFirestore.getInstance()
    protected val auth =  FirebaseAuth.getInstance()

    protected val storage = FirebaseStorage.getInstance()

    fun getCurrentUserUid(): String?{
        return auth.currentUser?.uid
    }
}
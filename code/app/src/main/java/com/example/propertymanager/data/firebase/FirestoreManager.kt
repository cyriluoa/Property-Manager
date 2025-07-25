package com.example.propertymanager.data.firebase


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



open class FirestoreManager {

    protected val db = FirebaseFirestore.getInstance()
    protected val auth =  FirebaseAuth.getInstance()
}
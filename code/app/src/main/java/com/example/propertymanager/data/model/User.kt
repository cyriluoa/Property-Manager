package com.example.propertymanager.data.model

import com.google.firebase.Timestamp

data class User(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val createdAt: Timestamp = Timestamp.now()
)

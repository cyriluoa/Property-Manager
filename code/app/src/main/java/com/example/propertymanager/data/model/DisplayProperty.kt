package com.example.propertymanager.data.model

data class DisplayProperty(
    val propertyId: String,
    val propertyName: String,
    val imageUrl: String?,
    val status: String, // e.g.  "OCCUPIED", "VACANT"
    val currentClientName: String,
    val dueThisMonth: Double,
    val totalDue: Double,
    val ownerId: String
)


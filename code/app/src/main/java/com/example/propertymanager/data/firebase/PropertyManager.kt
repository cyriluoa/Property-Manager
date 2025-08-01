package com.example.propertymanager.data.firebase


import com.example.propertymanager.data.model.Property
import com.example.propertymanager.data.model.PropertyState
import com.google.firebase.Timestamp
import jakarta.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyManager  @Inject constructor(): FirestoreManager(){

    fun createProperty(property: Property, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val propertyWithTimestamps = property.copy(
            createdAt = property.createdAt ?: Timestamp.now(),
            updatedAt = property.updatedAt ?: Timestamp.now()
        )
        db.collection("properties").document(property.id)
            .set(propertyWithTimestamps)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateCurrentContractId(propertyId: String, contractId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("properties").document(propertyId)
            .update("currentContractId", contractId, "updatedAt", Timestamp.now())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getPropertyById(propertyId: String, onSuccess: (Property) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("properties").document(propertyId).get()
            .addOnSuccessListener { doc ->
                doc.toObject(Property::class.java)?.let { onSuccess(it) }
                    ?: onFailure(Exception("Property not found"))
            }
            .addOnFailureListener { onFailure(it) }
    }


    fun deleteProperty(propertyId: String) {
        db.collection("properties").document(propertyId).delete()
    }

    fun listenToOwnerProperties(
        ownerId: String,
        onSnapshot: (List<Property>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("properties")
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    onSnapshot(emptyList())
                    return@addSnapshotListener
                }

                val properties = snapshot.toObjects(Property::class.java)
                onSnapshot(properties)
            }
    }

    fun removeCurrentContract(propertyId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("properties").document(propertyId)
            .update(
                mapOf(
                    "currentContractId" to null,
                    "status" to PropertyState.VACANT.name,
                    "updatedAt" to Timestamp.now()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun setPropertyStateOccupied(propertyId: String, contractId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("properties").document(propertyId)
            .update(
                mapOf(
                    "currentContractId" to contractId,
                    "status" to PropertyState.OCCUPIED.name,
                    "updatedAt" to Timestamp.now()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

}
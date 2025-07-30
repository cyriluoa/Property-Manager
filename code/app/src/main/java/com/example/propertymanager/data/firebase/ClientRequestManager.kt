package com.example.propertymanager.data.firebase

import com.example.propertymanager.data.model.ClientRequest
import jakarta.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRequestManager  @Inject constructor(): FirestoreManager() {


    fun createClientRequest(request: ClientRequest, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("client_requests").document(request.id)
            .set(request)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteClientRequest(requestId: String) {
        db.collection("client_requests").document(requestId).delete()
    }
}

package com.example.propertymanager.data.firebase

import com.example.propertymanager.data.model.ClientRequest
import com.google.firebase.firestore.ListenerRegistration
import jakarta.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRequestManager  @Inject constructor(): FirestoreManager() {


    private var listenerRegistration: ListenerRegistration? = null


    fun createClientRequest(request: ClientRequest, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("client_requests").document(request.id)
            .set(request)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun listenToRequestsForClient(
        clientId: String,
        onSuccess: (List<ClientRequest>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        listenerRegistration?.remove() // Remove old listener if exists

        listenerRegistration = db.collection("client_requests")
            .whereEqualTo("clientId", clientId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val list = snapshots.documents.mapNotNull { it.toObject(ClientRequest::class.java) }
                    onSuccess(list)
                }
            }
    }

    fun removeListener() {
        listenerRegistration?.remove()
    }

    fun deleteClientRequest(requestId: String) {
        db.collection("client_requests").document(requestId).delete()
    }
}

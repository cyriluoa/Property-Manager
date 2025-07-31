package com.example.propertymanager.data.firebase

import com.example.propertymanager.data.model.Contract
import com.google.firebase.Timestamp
import jakarta.inject.Inject
import javax.inject.Singleton


@Singleton
class ContractManager  @Inject constructor(): FirestoreManager() {

    fun createContract(propertyId: String, contract: Contract, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val contractWithTimestamp = contract.copy(createdAt = contract.createdAt ?: Timestamp.now())
        db.collection("properties").document(propertyId)
            .collection("contracts").document(contract.id)
            .set(contractWithTimestamp)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getContractById(propertyId: String, contractId: String, onSuccess: (Contract) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("properties").document(propertyId)
            .collection("contracts").document(contractId).get()
            .addOnSuccessListener { doc ->
                doc.toObject(Contract::class.java)?.let { onSuccess(it) }
                    ?: onFailure(Exception("Contract not found"))
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun markClientAccepted(propertyId: String, contractId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("properties").document(propertyId)
            .collection("contracts").document(contractId)
            .update("hasClientAccepted", true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }



    fun deleteContract(propertyId: String, contractId: String) {
        db.collection("properties").document(propertyId)
            .collection("contracts").document(contractId)
            .delete()
    }
}
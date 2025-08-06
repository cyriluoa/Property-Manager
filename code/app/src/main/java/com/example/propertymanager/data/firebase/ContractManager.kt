package com.example.propertymanager.data.firebase

import android.util.Log
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.ContractState
import com.example.propertymanager.data.model.PayableState
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

    fun markContractAccepted(propertyId: String, contractId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("properties").document(propertyId)
            .collection("contracts").document(contractId)
            .update("contractState", ContractState.ACCEPTED.name)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun markContractDenied(propertyId: String, contractId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("properties").document(propertyId)
            .collection("contracts").document(contractId)
            .update("contractState", ContractState.DENIED.name)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }




    fun deleteContract(propertyId: String, contractId: String) {
        db.collection("properties").document(propertyId)
            .collection("contracts").document(contractId)
            .delete()
    }

    fun listenToContracts(
        propertyId: String,
        onSuccess: (List<Contract>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("properties").document(propertyId)
            .collection("contracts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val contracts = snapshot.documents.mapNotNull { it.toObject(Contract::class.java) }
                    onSuccess(contracts)
                }
            }
    }
}
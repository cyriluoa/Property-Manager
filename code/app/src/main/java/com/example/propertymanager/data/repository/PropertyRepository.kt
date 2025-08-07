package com.example.propertymanager.data.repository

import android.util.Log
import com.example.propertymanager.data.firebase.ClientRequestManager
import com.example.propertymanager.data.firebase.ContractManager
import com.example.propertymanager.data.firebase.PayableItemManager
import com.example.propertymanager.data.firebase.PropertyManager
import com.example.propertymanager.data.firebase.UserManager
import com.example.propertymanager.data.model.ClientPropertyContract
import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.DisplayProperty
import com.example.propertymanager.data.model.Property
import com.google.firebase.firestore.ListenerRegistration
import jakarta.inject.Inject
import javax.inject.Singleton


@Singleton
class PropertyRepository @Inject constructor(
    private val propertyManager: PropertyManager,
    private val contractManager: ContractManager,
    private val clientRequestManager: ClientRequestManager,
    private val payableItemManager: PayableItemManager,
    private val userManager: UserManager
) {

    fun createFullPropertyFlow(
        property: Property,
        contract: Contract,
        clientRequest: ClientRequest,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        propertyManager.createProperty(property, {
            contractManager.createContract(property.id, contract, {
                payableItemManager.createAllPayableItems(property, contract, {
                    clientRequestManager.createClientRequest(clientRequest, {
                        onSuccess()
                    }, { err3 ->
                        // rollback everything
                        contractManager.deleteContract(property.id, contract.id)
                        propertyManager.deleteProperty(property.id)
                        onFailure(err3)
                    })
                }, { err2_5 ->
                    // rollback contract and property
                    contractManager.deleteContract(property.id, contract.id)
                    propertyManager.deleteProperty(property.id)
                    onFailure(err2_5)
                })
            }, { err2 ->
                propertyManager.deleteProperty(property.id)
                onFailure(err2)
            })
        }, { err1 ->
            onFailure(err1)
        })
    }

    fun listenToDisplayPropertiesForOwner(
        ownerId: String,
        onUpdate: (List<DisplayProperty>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        propertyManager.listenToOwnerProperties(ownerId, { propertyList ->
            if (propertyList.isEmpty()) {
                onUpdate(emptyList())
                return@listenToOwnerProperties
            }

            val displayList = mutableListOf<DisplayProperty>()
            val total = propertyList.size
            var completed = 0
            var failed = false

            propertyList.forEach { property ->
                val currentContractId = property.currentContractId

                if (currentContractId != null) {
                    contractManager.getContractById(
                        propertyId = property.id,
                        contractId = currentContractId,
                        onSuccess = { contract ->
                            val clientId = contract.clientId
                            val status = property.status.toString()

                            userManager.getUsernameByUid(clientId, { clientName ->
                                displayList.add(
                                    DisplayProperty(
                                        propertyId = property.id,
                                        propertyName = property.name,
                                        imageUrl = property.imageUrl,
                                        status = status,
                                        currentClientName = clientName,
                                        dueThisMonth = 0.0,
                                        totalDue = 0.0,
                                        ownerId = property.ownerId,
                                        currentContractId = property.currentContractId
                                    )
                                )
                                completed++
                                if (completed == total && !failed) onUpdate(displayList)
                            }, { userErr ->
                                failed = true
                                Log.e("Level 1",userErr.message.toString())
                                onError(userErr)
                            })
                        },
                        onFailure = { err ->
                            failed = true
                            onError(err)
                            Log.e("Level 2",err.message.toString())
                        }
                    )
                } else {
                    displayList.add(
                        DisplayProperty(
                            propertyId = property.id,
                            propertyName = property.name,
                            imageUrl = property.imageUrl,
                            status = "VACANT",
                            currentClientName = "-",
                            dueThisMonth = 0.0,
                            totalDue = 0.0,
                            ownerId = property.ownerId
                        )
                    )
                    completed++
                    if (completed == total && !failed) onUpdate(displayList)
                }
            }
        }, onError)
    }

    fun updatePropertyWithNewContractAndSendOutClientRequest(
        propertyId: String,
        ownerId: String,
        contract: Contract,
        clientRequest: ClientRequest,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Step 1: Create contract under this property
        contractManager.createContract(propertyId, contract, {
            // Step 2: Create payable items (monthly + overdue)
            payableItemManager.createAllPayableItems(property = Property(id = propertyId, ownerId = ownerId), contract = contract, {
                // Step 3: Update property.currentContractId
                propertyManager.updateCurrentContractId(propertyId, contract.id, {
                    // Step 4: Create client request
                    clientRequestManager.createClientRequest(clientRequest, {
                        onSuccess()
                    }, { err4 ->
                        // Rollback only currentContractId and contract? (Cannot delete contract safely if payable items already committed)
                        onFailure(err4)
                    })
                }, { err3 ->
                    // Rollback contract if property update fails
                    contractManager.deleteContract(propertyId, contract.id)
                    onFailure(err3)
                })
            }, { err2 ->
                // Rollback contract if payable items fail
                contractManager.deleteContract(propertyId, contract.id)
                onFailure(err2)
            })
        }, { err1 ->
            onFailure(err1)
        })
    }


    fun fetchClientPropertyContracts(
        onComplete: (List<ClientPropertyContract>) -> Unit,
        onError: (Exception) -> Unit
    ){
        return propertyManager.fetchClientPropertyContractsFromCloudFunction(onComplete,onError)
    }

}
package com.example.propertymanager.data.repository

import com.example.propertymanager.data.firebase.ClientRequestManager
import com.example.propertymanager.data.firebase.ContractManager
import com.example.propertymanager.data.firebase.PayableItemManager
import com.example.propertymanager.data.firebase.PropertyManager
import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.Property
import jakarta.inject.Inject
import javax.inject.Singleton


@Singleton
class PropertyRepository @Inject constructor(
    private val propertyManager: PropertyManager,
    private val contractManager: ContractManager,
    private val clientRequestManager: ClientRequestManager,
    private val payableItemManager: PayableItemManager
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

}
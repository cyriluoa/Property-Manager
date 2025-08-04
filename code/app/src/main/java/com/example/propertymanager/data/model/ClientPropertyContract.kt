package com.example.propertymanager.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize


@Parcelize
data class ClientPropertyContract(
    // Property fields
    val propertyId: String = "",
    val propertyName: String = "",
    val propertyImageUrl: String? = null,
    val ownerId: String = "",
    val ownerName: String = "",
    val propertyStatus: PropertyState = PropertyState.VACANT,
    val propertyCreatedAt: Timestamp? = null,
    val propertyUpdatedAt: Timestamp? = null,

    // Contract fields
    val contractId: String = "",
    val clientId: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val contractLengthMonths: Int = 0,
    val monthlyRentBreakdown: List<RentBreakdown> = emptyList(),
    val preContractOverdueAmounts: List<OverdueItem> = emptyList(),
    val contractCreatedAt: Timestamp? = null,
    val contractNotes: String = "",
    val contractState: ContractState = ContractState.PENDING
) : Parcelable{
    companion object {
        fun fromMap(map: Map<String, Any?>): ClientPropertyContract {
            return ClientPropertyContract(
                propertyId = map["propertyId"] as? String ?: "",
                propertyName = map["propertyName"] as? String ?: "",
                propertyImageUrl = map["propertyImageUrl"] as? String,
                ownerId = map["ownerId"] as? String ?: "",
                ownerName = map["ownerName"] as? String ?: "",
                propertyStatus = PropertyState.valueOf(map["propertyStatus"] as? String ?: "VACANT"),
                propertyCreatedAt = (map["propertyCreatedAt"] as? com.google.firebase.Timestamp),
                propertyUpdatedAt = (map["propertyUpdatedAt"] as? com.google.firebase.Timestamp),

                contractId = map["contractId"] as? String ?: "",
                clientId = map["clientId"] as? String ?: "",
                startDate = map["startDate"] as? String ?: "",
                endDate = map["endDate"] as? String ?: "",
                contractLengthMonths = (map["contractLengthMonths"] as? Number)?.toInt() ?: 0,
                monthlyRentBreakdown = (map["monthlyRentBreakdown"] as? List<Map<String, Any>>)?.map {
                    RentBreakdown.fromMap(it)
                } ?: emptyList(),
                preContractOverdueAmounts = (map["preContractOverdueAmounts"] as? List<Map<String, Any>>)?.map {
                    OverdueItem.fromMap(it)
                } ?: emptyList(),
                contractCreatedAt = (map["contractCreatedAt"] as? com.google.firebase.Timestamp),
                contractNotes = map["contractNotes"] as? String ?: "",
                contractState = ContractState.valueOf(map["contractState"] as? String ?: "PENDING")
            )
        }
    }



}
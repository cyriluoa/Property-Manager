package com.example.propertymanager.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RentBreakdown(
    var dueDate: String = "", // Format: dd-MM-yyyy
    var amount: Double = 0.0
) : Parcelable {
    companion object {
        fun fromMap(map: Map<String, Any?>): RentBreakdown {
            return RentBreakdown(
                dueDate = map["dueDate"] as? String ?: "",
                amount = (map["amount"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}

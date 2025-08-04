package com.example.propertymanager.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OverdueItem(
    val label: String = "",
    val amount: Double = 0.0
) : Parcelable {
    companion object {
        fun fromMap(map: Map<String, Any?>): OverdueItem {
            return OverdueItem(
                label = map["label"] as? String ?: "",
                amount = (map["amount"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}


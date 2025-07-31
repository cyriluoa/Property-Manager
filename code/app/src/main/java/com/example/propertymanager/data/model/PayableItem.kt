package com.example.propertymanager.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class PayableItem(
    val id: String = "",
    val type: PayableItemType = PayableItemType.MONTHLY,
    val monthIndex: Int? = null, // for monthly rent only
    val overdueItemLabel: String? = null, // for pre-contract dues
    val dueDate: String = "", // DD-MM-YYYY
    val amountDue: Double = 0.0,
    val totalPaid: Double = 0.0,
    val isFullyPaid: Boolean = false,
    val status: PayableStatus = PayableStatus.NOT_APPLIED_YET,
    val createdAt: Timestamp = Timestamp.now(),
    val clientId: String = "",
    val ownerId: String = ""
) : Parcelable

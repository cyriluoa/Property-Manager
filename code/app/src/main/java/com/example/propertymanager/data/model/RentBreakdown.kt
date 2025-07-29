package com.example.propertymanager.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RentBreakdown(
    var dueDate: String = "", // Format:dd-MM-yyyy
    var amount: Double = 0.0
) : Parcelable
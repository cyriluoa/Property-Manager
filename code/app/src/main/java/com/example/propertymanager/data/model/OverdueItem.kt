package com.example.propertymanager.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OverdueItem(
    val label: String = "",
    val amount: Double = 0.0
) : Parcelable


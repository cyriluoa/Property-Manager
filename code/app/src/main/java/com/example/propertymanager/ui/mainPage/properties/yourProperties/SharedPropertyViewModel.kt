package com.example.propertymanager.ui.mainPage.properties.yourProperties

import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class SharedPropertyViewModel @Inject constructor() : ViewModel() {
    var selectedClient: User? = null
}

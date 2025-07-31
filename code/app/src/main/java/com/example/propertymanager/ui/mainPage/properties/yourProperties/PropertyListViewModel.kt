package com.example.propertymanager.ui.mainPage.properties.yourProperties

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.model.DisplayProperty
import com.example.propertymanager.data.repository.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class PropertyListViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    private val _displayProperties = MutableLiveData<List<DisplayProperty>>()
    val displayProperties: LiveData<List<DisplayProperty>> = _displayProperties

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun startListening(ownerId: String) {
        propertyRepository.listenToDisplayPropertiesForOwner(
            ownerId = ownerId,
            onUpdate = { list ->
                _displayProperties.postValue(list)
            },
            onError = { err ->
                _error.postValue(err.message)
            }
        )
    }
}

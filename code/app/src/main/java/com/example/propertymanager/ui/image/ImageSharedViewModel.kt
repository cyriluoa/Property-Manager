package com.example.propertymanager.ui.image

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class ImageSharedViewModel @Inject constructor() : ViewModel() {

    private val _profileImageUrl = MutableLiveData<String?>()
    val profileImageUrl: LiveData<String?> = _profileImageUrl

    fun setProfileImageUrl(url: String) {
        _profileImageUrl.value = url
    }

    fun clear() {
        _profileImageUrl.value = null
    }
}

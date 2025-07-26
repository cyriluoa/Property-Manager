package com.example.propertymanager.ui.image

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class ImageSharedViewModel @Inject constructor() : ViewModel() {

    private val _profileImageUri = MutableLiveData<Uri?>()
    val profileImageUri: LiveData<Uri?> = _profileImageUri

    private val _profileImageUrl = MutableLiveData<String?>()
    val profileImageUrl: LiveData<String?> = _profileImageUrl

    fun setProfileImageUri(uri: Uri) {
        _profileImageUri.value = uri
    }

    fun setProfileImageUrl(url: String) {
        _profileImageUrl.value = url
    }

    fun clear() {
        _profileImageUri.value = null
        _profileImageUrl.value = null
    }
}


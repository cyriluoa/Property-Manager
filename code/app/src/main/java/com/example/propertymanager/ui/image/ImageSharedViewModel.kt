package com.example.propertymanager.ui.image

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class ImageSharedViewModel @Inject constructor() : ViewModel() {

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> = _imageUri

    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: LiveData<String?> = _imageUrl

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    fun setImageUrl(url: String) {
        _imageUrl.value = url
    }

    fun clear() {
        _imageUri.value = null
        _imageUrl.value = null
    }
}


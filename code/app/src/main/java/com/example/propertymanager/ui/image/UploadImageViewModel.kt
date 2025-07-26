package com.example.propertymanager.ui.image

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.propertymanager.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

@HiltViewModel
class UploadImageViewModel @Inject constructor(
    private val imageRepository: ImageRepository
) : ViewModel() {

    private var compressedImageFile: File? = null

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun compressImageAndSet(uri: Uri, context: Context,maxImageSize: Int) {
        _loading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val compressedFile = imageRepository.compressImage(uri, context, maxImageSize)
                compressedImageFile = compressedFile
                withContext(Dispatchers.Main) {
                    _loading.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _loading.value = false
                }
            }
        }
    }

    fun uploadCompressedImage(
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val file = compressedImageFile
        if (file == null) {
            onFailure(Exception("No image selected"))
            return
        }

        _loading.value = true

        imageRepository.uploadImage(file,
            onSuccess = { downloadUrl ->
                _loading.value = false
                onSuccess(downloadUrl)
            },
            onFailure = { e ->
                _loading.value = false
                onFailure(e)
            }
        )
    }
}

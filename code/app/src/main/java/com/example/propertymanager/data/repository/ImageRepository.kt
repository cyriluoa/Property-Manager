package com.example.propertymanager.data.repository

import android.content.Context
import android.net.Uri
import com.example.propertymanager.data.firebase.ImageManager
import jakarta.inject.Inject
import java.io.File
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    private val imageManager: ImageManager
) {
    fun compressImage(uri: Uri, context: Context, maxImageSize: Int): File {
        return imageManager.compressImage(uri, context, maxImageSize)
    }

    fun uploadImage(
        file: File,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        imageManager.uploadImageToFirebaseStorage(file, onSuccess, onFailure)
    }
}

package com.example.propertymanager.data.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import jakarta.inject.Inject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Singleton

@Singleton
class ImageManager @Inject constructor(): FirestoreManager (){

    fun compressImage(uri: Uri, context: Context, maxImageSize: Int): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open input stream from URI")

        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(compressedFile)

        // Compress to target 256KB or lower by quality
        var quality = 90
        var byteArray: ByteArray

        do {
            val byteArrayStream = ByteArrayOutputStream()
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayStream)
            byteArray = byteArrayStream.toByteArray()
            quality -= 5
        } while (byteArray.size > maxImageSize && quality > 10)

        outputStream.write(byteArray)
        outputStream.flush()
        outputStream.close()

        return compressedFile
    }

    fun uploadImageToFirebaseStorage(
        file: File,
        path: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storageRef = storage.reference
        val fileRef = storageRef.child("${path}/${UUID.randomUUID()}.jpg")

        fileRef.putFile(Uri.fromFile(file))
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener { uri -> onSuccess(uri.toString()) }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }
}

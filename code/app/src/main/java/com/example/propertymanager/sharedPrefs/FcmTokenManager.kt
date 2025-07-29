package com.example.propertymanager.sharedPrefs



import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TOKEN_KEY = "device_fcm_token"
        private const val TAG = "FcmTokenManager"
    }

    fun getCurrentToken(onTokenReceived: (String) -> Unit, onFailure: (Exception) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                saveTokenLocally(token)
                onTokenReceived(token)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch FCM token", e)
                onFailure(e)
            }
    }

    fun saveTokenLocally(token: String) {
        sharedPrefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getSavedToken(): String? = sharedPrefs.getString(TOKEN_KEY, null)

    fun clearSavedToken() {
        sharedPrefs.edit().remove(TOKEN_KEY).apply()
    }
}

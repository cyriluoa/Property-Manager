package com.example.propertymanager.ui.mainPage.profile.requests

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.propertymanager.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RequestsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        // Show RequestsFragment initially
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RequestsFragment())
                .commit()
        }
    }
}

package com.example.propertymanager.ui.mainPage.properties.yourProperties

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.propertymanager.R
import com.example.propertymanager.databinding.ActivityYourPropertiesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class YourPropertiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityYourPropertiesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYourPropertiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, PropertyListFragment())
                .commit()
        }
    }
}


package com.example.propertymanager.ui.mainPage.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.propertymanager.R
import com.example.propertymanager.databinding.ActivityMainPageBinding
import com.example.propertymanager.ui.mainPage.payments.PaymentsFragment
import com.example.propertymanager.ui.mainPage.profile.ProfileFragment
import com.example.propertymanager.ui.mainPage.properties.PropertiesFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)




        val openProfileTab = intent.getBooleanExtra("openProfileTab", false)
        if(openProfileTab){
            loadFragment(ProfileFragment())
        }
        else{
            loadFragment(PropertiesFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_properties -> {
                    loadFragment(PropertiesFragment())
                    true
                }
                R.id.nav_payments -> {
                    loadFragment(PaymentsFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment)
            .commit()
    }
}
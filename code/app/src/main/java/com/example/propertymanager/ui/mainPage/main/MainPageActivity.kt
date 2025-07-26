package com.example.propertymanager.ui.mainPage.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.propertymanager.R
import com.example.propertymanager.data.model.MainPageTabs
import com.example.propertymanager.databinding.ActivityMainPageBinding
import com.example.propertymanager.ui.mainPage.payments.PaymentsFragment
import com.example.propertymanager.ui.mainPage.profile.ProfileFragment
import com.example.propertymanager.ui.mainPage.properties.PropertiesFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainPageBinding
    private var activeFragment: Fragment? = null
    private var currentTab: MainPageTabs = MainPageTabs.PROPERTIES

    private val propertiesFragment by lazy { PropertiesFragment() }
    private val paymentsFragment by lazy { PaymentsFragment() }
    private val profileFragment by lazy { ProfileFragment() }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // First-time add the initial fragment
        val openProfileTab = intent.getBooleanExtra("openProfileTab", false)
        val initialFragment = if (openProfileTab) profileFragment else propertiesFragment

        supportFragmentManager.beginTransaction()
            .add(R.id.main_fragment_container, initialFragment)
            .commit()
        activeFragment = initialFragment

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_properties -> {
                    switchFragment(propertiesFragment, MainPageTabs.PROPERTIES)
                    true
                }
                R.id.nav_payments -> {
                    switchFragment(paymentsFragment, MainPageTabs.PAYMENTS)
                    true
                }
                R.id.nav_profile -> {
                    switchFragment(profileFragment, MainPageTabs.PROFILE)
                    true
                }
                else -> false
            }
        }

        // Set selected item initially
        binding.bottomNav.selectedItemId = if (openProfileTab) R.id.nav_profile else R.id.nav_properties
    }

    private fun switchFragment(targetFragment: Fragment, targetTab: MainPageTabs) {
        if (targetTab == currentTab) return

        val transaction = supportFragmentManager.beginTransaction()

        val enterAnim = if (targetTab.ordinal > currentTab.ordinal) {
            R.anim.slide_in_right
        } else {
            R.anim.slide_in_left
        }

        val exitAnim = if (targetTab.ordinal > currentTab.ordinal) {
            R.anim.slide_out_left
        } else {
            R.anim.slide_out_right
        }

        transaction.setCustomAnimations(enterAnim, exitAnim)

        activeFragment?.let { transaction.hide(it) }

        if (!targetFragment.isAdded) {
            transaction.add(R.id.main_fragment_container, targetFragment)
        } else {
            transaction.show(targetFragment)
        }

        transaction.commit()
        activeFragment = targetFragment
        currentTab = targetTab
    }


}

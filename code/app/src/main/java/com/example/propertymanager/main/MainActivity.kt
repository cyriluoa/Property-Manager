package com.example.propertymanager.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.propertymanager.R
import com.example.propertymanager.databinding.ActivityMainBinding
import com.example.propertymanager.sharedPrefs.Prefs
import com.example.propertymanager.ui.login.LoginFragment
import com.example.propertymanager.ui.mainPage.main.MainPageActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser
        val rememberMe = Prefs.getRememberMe(this)

        if (user != null && rememberMe) {
            // User is already logged in and opted for remember me
            startActivity(Intent(this, MainPageActivity::class.java))
            finish()
        } else if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_left, R.anim.slide_out_right
                )
                .replace(binding.fragmentContainer.id, LoginFragment())
                .commit()
        }
    }


    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(binding.fragmentContainer.id)
        if (currentFragment is LoginFragment) {
            finish()
        } else {
            super.onBackPressed()
        }
    }
}

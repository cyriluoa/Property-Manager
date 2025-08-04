package com.example.propertymanager.ui.mainPage.payments.client

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.propertymanager.databinding.ActivityClientBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val state = intent.getStringExtra("CONTRACT_STATE") ?: return

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, ClientPropertiesFragment.newInstance(state))

            .commit()
    }
}


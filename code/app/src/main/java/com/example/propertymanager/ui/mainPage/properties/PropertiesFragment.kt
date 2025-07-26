package com.example.propertymanager.ui.mainPage.properties

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.propertymanager.databinding.FragmentPropertiesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PropertiesFragment : Fragment() {

    private var _binding: FragmentPropertiesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPropertiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.cardYourProperties.setOnClickListener {
//            startActivity(Intent(requireContext(), YourPropertiesActivity::class.java))
//        }
//
//        binding.cardYourTenants.setOnClickListener {
//            startActivity(Intent(requireContext(), YourTenantsActivity::class.java))
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
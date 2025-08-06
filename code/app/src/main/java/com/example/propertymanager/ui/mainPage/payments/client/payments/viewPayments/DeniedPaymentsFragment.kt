package com.example.propertymanager.ui.mainPage.payments.client.payments.viewPayments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.propertymanager.databinding.FragmentPaymentsDeniedBinding

class DeniedPaymentsFragment : Fragment() {

    private var _binding: FragmentPaymentsDeniedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentsDeniedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Setup RecyclerView & SwipeRefresh here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

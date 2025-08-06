package com.example.propertymanager.ui.mainPage.payments.client.payments.viewPayments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.propertymanager.R
import com.example.propertymanager.data.model.Mode
import com.example.propertymanager.databinding.FragmentPaymentTabsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FragmentPaymentTabs : Fragment() {

    private var _binding: FragmentPaymentTabsBinding? = null
    private val binding get() = _binding!!

    private val tabTitles = listOf("Pending", "Approved", "Denied")
    private val tabColors = listOf(R.color.warning_yellow, R.color.success_green, R.color.error_red)


    private val propertyId: String by lazy {
        requireArguments().getString(ARG_PROPERTY_ID) ?: error("Missing propertyId")
    }
    private val contractId: String by lazy {
        requireArguments().getString(ARG_CONTRACT_ID) ?: error("Missing contractId")
    }
    private val payableItemId: String by lazy {
        requireArguments().getString(ARG_PAYABLE_ITEM_ID) ?: error("Missing payableItemId")
    }
    private val propertyName: String by lazy {
        requireArguments().getString(ARG_PROPERTY_NAME) ?: error("Missing propertyName")
    }
    private val paymentLabel: String by lazy {
        requireArguments().getString(ARG_PAYMENT_LABEL) ?: error("Missing paymentLabel")
    }
    private val clientId: String by lazy {
        requireArguments().getString(ARG_CLIENT_ID) ?: error("Missing clientId")
    }
    private val ownerId: String by lazy {
        requireArguments().getString(ARG_OWNER_ID) ?: error("Missing ownerId")
    }

    private val mode: Mode by lazy {
        Mode.valueOf(requireArguments().getString(ARG_MODE) ?: Mode.CLIENT_MODE.name)
    }

    companion object {
        private const val ARG_PROPERTY_ID = "property_id"
        private const val ARG_CONTRACT_ID = "contract_id"
        private const val ARG_PAYABLE_ITEM_ID = "payable_item_id"
        private const val ARG_PROPERTY_NAME = "property_name"
        private const val ARG_PAYMENT_LABEL = "payment_label"
        private const val ARG_CLIENT_ID = "client_id"
        private const val ARG_OWNER_ID = "owner_id"
        private const val ARG_MODE = "mode"

        fun newInstance(
            propertyId: String,
            contractId: String,
            payableItemId: String,
            propertyName: String,
            paymentLabel: String,
            clientId: String,
            ownerId: String,
            mode: Mode
        ): FragmentPaymentTabs {
            return FragmentPaymentTabs().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROPERTY_ID, propertyId)
                    putString(ARG_CONTRACT_ID, contractId)
                    putString(ARG_PAYABLE_ITEM_ID, payableItemId)
                    putString(ARG_PROPERTY_NAME, propertyName)
                    putString(ARG_PAYMENT_LABEL, paymentLabel)
                    putString(ARG_CLIENT_ID, clientId)
                    putString(ARG_OWNER_ID, ownerId)
                    putString(ARG_MODE, mode.name)
                }
            }
        }
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentTabsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = PaymentsPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val color = ContextCompat.getColor(requireContext(), tabColors[tab.position])
                binding.tabLayout.setTabTextColors(Color.WHITE, color)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.propertymanager.ui.mainPage.payments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.propertymanager.databinding.FragmentPaymentsBinding
import com.example.propertymanager.ui.mainPage.payments.client.ClientActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentsFragment : Fragment() {

    private var _binding: FragmentPaymentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaymentsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupRefresh()
        viewModel.refreshClientContracts()
        setupClickListeners()

        binding.cardViewAllContracts.setOnClickListener {
            Toast.makeText(requireContext(), "View All Properties clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to contract list
        }
    }

    private fun setupObservers() {
        viewModel.clientContracts.observe(viewLifecycleOwner) {
            Log.d("PaymentsFragment", "Received ${it.size} contracts")
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.totalContracts.observe(viewLifecycleOwner) {
            binding.tvTotalProperties.text = it.toString()
        }

        viewModel.activeContracts.observe(viewLifecycleOwner) {
            binding.tvActiveContracts.text = it.size.toString()
        }

        viewModel.paidOffContracts.observe(viewLifecycleOwner) {
            binding.tvPaidOffContracts.text = it.size.toString()
        }

        viewModel.acceptedContracts.observe(viewLifecycleOwner) {
            binding.tvAcceptedContracts.text = it.size.toString()
        }

        viewModel.expiredContracts.observe(viewLifecycleOwner) {
            binding.tvExpiredContracts.text = it.size.toString()
        }

        viewModel.cancelledContracts.observe(viewLifecycleOwner) {
            binding.tvCancelledContracts.text = it.size.toString()
        }

        viewModel.deniedContracts.observe(viewLifecycleOwner) {
            binding.tvDeniedContracts.text = it.size.toString()
        }

        viewModel.error.observe(viewLifecycleOwner) { exception ->
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        binding.cardActiveContracts.setOnClickListener {
            openClientContractsActivity("ACTIVE")
        }
        binding.cardAcceptedContracts.setOnClickListener {
            openClientContractsActivity("ACCEPTED")
        }
        binding.cardCancelledContracts.setOnClickListener {
            openClientContractsActivity("CANCELLED")
        }
        binding.cardDeniedContracts.setOnClickListener {
            openClientContractsActivity("DENIED")
        }
        binding.cardExpiredContracts.setOnClickListener {
            openClientContractsActivity("EXPIRED")
        }
        binding.cardPaidOffContracts.setOnClickListener {
            openClientContractsActivity("PAID_OFF")
        }
    }

    private fun openClientContractsActivity(state: String) {
        val intent = Intent(requireContext(), ClientActivity::class.java)
        intent.putExtra("CONTRACT_STATE", state)
        startActivity(intent)
    }



    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshClientContracts()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshClientContracts() // Re-fetch on return to screen
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


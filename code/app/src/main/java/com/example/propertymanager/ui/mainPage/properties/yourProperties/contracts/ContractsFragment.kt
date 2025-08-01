package com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.ContractState
import com.example.propertymanager.databinding.FragmentContractsBinding
import com.example.propertymanager.databinding.ItemContractBinding

class ContractsFragment : Fragment() {

    private var _binding: FragmentContractsBinding? = null
    private val binding get() = _binding!!

    private lateinit var inactiveAdapter: ContractAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContractsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInactiveRecycler()
        loadMockContracts()
    }

    private fun setupInactiveRecycler() {
        inactiveAdapter = ContractAdapter()
        binding.rvInactiveContracts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = inactiveAdapter
        }
    }

    private fun loadMockContracts() {
        val mockInactiveContracts = listOf(
            Contract(
                id = "c1",
                clientId = "client1",
                startDate = "01-01-2023",
                endDate = "01-01-2024",
                contractLengthMonths = 12,
                createdAt = null,
                contractState = ContractState.OVER,
                preContractOverdueAmounts = listOf(),
                notes = ""
            ),
            Contract(
                id = "c2",
                clientId = "client2",
                startDate = "01-02-2023",
                endDate = "01-02-2024",
                contractLengthMonths = 12,
                createdAt = null,
                contractState = ContractState.OVER,
                preContractOverdueAmounts = listOf(),
                notes = ""
            )
        )

        val activeContract = Contract(
            id = "c3",
            clientId = "client3",
            startDate = "01-03-2024",
            endDate = "01-12-2025",
            contractLengthMonths = 12,
            createdAt = null,
            contractState = ContractState.ACTIVE,
            preContractOverdueAmounts = listOf(),
            notes = ""
        )

        // Bind Active Contract
        if (activeContract.contractState == ContractState.ACTIVE) {
            val activeBinding = ItemContractBinding.bind(binding.layoutActiveContract.rootView)
            bindContractToCard(activeBinding, activeContract)
            binding.layoutActiveContract.visibility = View.VISIBLE
            binding.tvNoActiveContract.visibility = View.GONE
        } else {
            binding.layoutActiveContract.visibility = View.GONE
            binding.tvNoActiveContract.visibility = View.VISIBLE
        }

        // Bind Inactive Contracts
        if (mockInactiveContracts.isNotEmpty()) {
            inactiveAdapter.submitList(mockInactiveContracts)
            binding.tvNoInactiveContracts.visibility = View.GONE
        } else {
            binding.tvNoInactiveContracts.visibility = View.VISIBLE
        }
    }

    private fun bindContractToCard(cardBinding: ItemContractBinding, contract: Contract) {
        cardBinding.tvContractDuration.text = "${contract.startDate} - ${contract.endDate}"
        cardBinding.tvContractLength.text = "${contract.contractLengthMonths} months"
        cardBinding.tvCreatedAt.text = "N/A"
        cardBinding.tvOverdueItems.text = "${contract.preContractOverdueAmounts.size} items"
        cardBinding.tvContractStatus.text = contract.contractState.name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

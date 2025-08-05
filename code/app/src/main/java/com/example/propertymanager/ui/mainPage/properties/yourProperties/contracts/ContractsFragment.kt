package com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanager.R
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.Mode
import com.example.propertymanager.databinding.FragmentContractsBinding
import com.example.propertymanager.databinding.ItemContractBinding
import com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts.payableItems.PayableItemsFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class ContractsFragment : Fragment() {

    private var _binding: FragmentContractsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContractsViewModel by viewModels()

    private val propertyId: String by lazy {
        requireArguments().getString(ARG_PROPERTY_ID) ?: error("Property ID not passed")
    }



    companion object {
        private const val ARG_PROPERTY_ID = "property_id"

        fun newInstance(propertyId: String): ContractsFragment {
            val fragment = ContractsFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_PROPERTY_ID, propertyId)
            }
            return fragment
        }
    }

    private lateinit var inactiveAdapter: ContractAdapter

    private val onContractClick: (Contract) -> Unit = { selectedContract ->
        Log.d("ContractsFragment", "View bills clicked for contract: ${selectedContract.id}")

        val fragment = PayableItemsFragment.newInstance(propertyId, selectedContract.id, Mode.OWNER_MODE)

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContractsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInactiveRecycler()
        viewModel.loadContracts(propertyId)
        observeViewModel()
    }

    private fun setupInactiveRecycler() {
        inactiveAdapter = ContractAdapter(onContractClick)
        binding.rvInactiveContracts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = inactiveAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.activeContract.observe(viewLifecycleOwner) { activeContract ->
            if (activeContract != null) {
                bindContractToCard(binding.layoutActiveContract, activeContract,onContractClick)
                binding.layoutActiveContract.root.visibility = View.VISIBLE
                binding.tvNoActiveContract.visibility = View.GONE
            } else {
                binding.layoutActiveContract.root.visibility = View.GONE
                binding.tvNoActiveContract.visibility = View.VISIBLE
            }
        }

        viewModel.inactiveContracts.observe(viewLifecycleOwner) { contracts ->
            if (contracts.isNotEmpty()) {
                inactiveAdapter.submitList(contracts)
                binding.tvNoInactiveContracts.visibility = View.GONE
            } else {
                binding.tvNoInactiveContracts.visibility = View.VISIBLE
            }
        }
    }

    private fun bindContractToCard(
        cardBinding: ItemContractBinding,
        contract: Contract,
        onClick: (Contract) -> Unit
    ) {
        cardBinding.tvContractDuration.text = "${contract.startDate} - ${contract.endDate}"
        cardBinding.tvContractLength.text = "${contract.contractLengthMonths} months"

        val createdAtText = contract.createdAt?.let {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it.toDate())
        } ?: "N/A"
        cardBinding.tvCreatedAt.text = createdAtText

        cardBinding.tvOverdueItems.text = "${contract.preContractOverdueAmounts.size} items"
        cardBinding.tvContractStatus.text = contract.contractState.name

        cardBinding.btnViewBills.setOnClickListener {
            onClick(contract)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


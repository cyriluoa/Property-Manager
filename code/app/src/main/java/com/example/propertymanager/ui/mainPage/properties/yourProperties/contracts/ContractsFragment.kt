package com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts

import android.app.AlertDialog
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
import com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts.add.AddContractFragment
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

    private val propertyName: String by lazy {
        requireArguments().getString(ARG_PROPERTY_NAME) ?: error("Property Name not passed")
    }

    private val ownerId: String by lazy {
        requireArguments().getString(ARG_OWNER_ID) ?: error("Owner ID not passed")
    }

    private val currentContractId: String? by lazy {
        requireArguments().getString(ARG_CURRENT_CONTRACT_ID)
    }

    private val propertyStatus: String by lazy {
        requireArguments().getString(ARG_PROPERTY_STATUS) ?: error("Property status not passed")
    }

    companion object {
        private const val ARG_PROPERTY_ID = "property_id"
        private const val ARG_PROPERTY_NAME = "property_name"
        private const val ARG_OWNER_ID = "owner_id"
        private const val ARG_CURRENT_CONTRACT_ID = "current_contract_id"
        private const val ARG_PROPERTY_STATUS = "property_status"

        fun newInstance(
            propertyId: String,
            propertyName: String,
            ownerId: String,
            currentContractId: String?,
            propertyStatus: String
        ): ContractsFragment {
            return ContractsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROPERTY_ID, propertyId)
                    putString(ARG_PROPERTY_NAME, propertyName)
                    putString(ARG_OWNER_ID, ownerId)
                    putString(ARG_CURRENT_CONTRACT_ID, currentContractId)
                    putString(ARG_PROPERTY_STATUS, propertyStatus)
                }
            }
        }
    }

    private lateinit var inactiveAdapter: ContractAdapter

    private val onContractClick: (Contract) -> Unit = { selectedContract ->
        val fragment = PayableItemsFragment.newInstance(
            propertyId = propertyId,
            contractId = selectedContract.id,
            mode = Mode.OWNER_MODE,
            ownerId = ownerId,
            clientId = selectedContract.clientId,
            propertyName = propertyName,
            contractState = selectedContract.contractState
        )

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

        setupSwipeToRefresh()
        setupInactiveRecycler()
        observeViewModel()

        viewModel.loadContracts(propertyId, currentContractId, propertyStatus)

        binding.fabAddContract.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, AddContractFragment.newInstance(propertyId,propertyName,ownerId))
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupInactiveRecycler() {
        inactiveAdapter = ContractAdapter(onContractClick)
        binding.rvInactiveContracts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = inactiveAdapter
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshContracts.setOnRefreshListener {
            viewModel.loadContracts(propertyId, currentContractId, propertyStatus)
        }

        viewModel.activeContract.observe(viewLifecycleOwner) {
            binding.swipeRefreshContracts.isRefreshing = false
        }

        viewModel.inactiveContracts.observe(viewLifecycleOwner) {
            binding.swipeRefreshContracts.isRefreshing = false
        }
    }

    private fun observeViewModel() {
        viewModel.activeContract.observe(viewLifecycleOwner) { activeContract ->
            if (activeContract != null) {
                bindContractToCard(binding.layoutActiveContract, activeContract, onContractClick)
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

        viewModel.canAddContract.observe(viewLifecycleOwner) { canAdd ->
            binding.fabAddContract.visibility = if (canAdd) View.VISIBLE else View.GONE
        }
    }

    private fun bindContractToCard(
        cardBinding: ItemContractBinding,
        contract: Contract,
        onClick: (Contract) -> Unit
    ) {
        cardBinding.tvContractDuration.text = "${contract.startDate} - ${contract.endDate}"
        cardBinding.tvContractLength.text = "${contract.contractLengthMonths} months"
        cardBinding.tvCreatedAt.text = contract.createdAt?.let {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it.toDate())
        } ?: "N/A"
        cardBinding.llCancelButton.visibility = View.VISIBLE
        cardBinding.tvOverdueItems.text = "${contract.preContractOverdueAmounts.size} items"
        cardBinding.tvContractStatus.text = contract.contractState.name
        cardBinding.tvContractStatus.setBackgroundResource(R.drawable.status_badge_green)

        cardBinding.btnViewBills.setOnClickListener { onClick(contract) }

        cardBinding.btnCancelContract.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cancel Contract")
                .setMessage("Are you sure you want to cancel this contract?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.cancelContract(propertyId, contract.id)
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



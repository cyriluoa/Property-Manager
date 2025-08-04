package com.example.propertymanager.ui.mainPage.payments.client

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanager.R
import com.example.propertymanager.databinding.FragmentClientPropertiesBinding
import com.example.propertymanager.ui.mainPage.payments.PaymentsViewModel
import com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts.payableItems.PayableItemsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClientPropertiesFragment : Fragment() {

    private lateinit var binding: FragmentClientPropertiesBinding
    private val viewModel: PaymentsViewModel by activityViewModels()
    private lateinit var adapter: ClientPropertiesAdapter
    private var contractState: String? = null

    companion object {
        fun newInstance(state: String): ClientPropertiesFragment {
            val fragment = ClientPropertiesFragment()
            val bundle = Bundle().apply {
                putString("CONTRACT_STATE", state)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contractState = arguments?.getString("CONTRACT_STATE")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClientPropertiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ClientPropertiesAdapter { selectedContract ->
            // Handle click
            val fragment = PayableItemsFragment.newInstance(
                propertyId = selectedContract.propertyId,
                contractId = selectedContract.contractId
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

        binding.rvContracts.adapter = adapter
        binding.rvContracts.layoutManager = LinearLayoutManager(requireContext())
        binding.tvHeader.text = "${contractState?.replace("_", " ")} Properties"

        setupSwipeRefresh()
        viewModel.refreshClientContracts()
        observeContracts()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshClientContracts()
        }

        // Optional: stop refreshing when data is loaded
        viewModel.clientContracts.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.error.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = false
        }
    }


    private fun observeContracts() {
        Log.d("observeContracts",contractState.toString())
        when (contractState) {
            "ACTIVE" -> viewModel.activeContracts.observe(viewLifecycleOwner) { adapter.submitList(it) }
            "ACCEPTED" -> viewModel.acceptedContracts.observe(viewLifecycleOwner) { adapter.submitList(it) }
            "CANCELLED" -> viewModel.cancelledContracts.observe(viewLifecycleOwner) { adapter.submitList(it) }
            "DENIED" -> viewModel.deniedContracts.observe(viewLifecycleOwner) { adapter.submitList(it) }
            "EXPIRED" -> viewModel.expiredContracts.observe(viewLifecycleOwner) { adapter.submitList(it) }
            "PAID_OFF" -> viewModel.paidOffContracts.observe(viewLifecycleOwner) { adapter.submitList(it) }
        }
    }


}

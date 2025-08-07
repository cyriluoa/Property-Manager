package com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts.add

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.Property
import com.example.propertymanager.databinding.FragmentAddPropertyDraftBinding
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.breakdown.OverdueBreakdownAdapter
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.breakdown.RentBreakdownAdapter
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class AddContractDraftFragment : Fragment() {

    private var _binding: FragmentAddPropertyDraftBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddContractDraftViewModel by viewModels()

    private lateinit var contract: Contract
    private lateinit var clientRequest: ClientRequest
    private lateinit var propertyId: String

    private lateinit var ownerId: String

    private lateinit var rentBreakdownAdapter: RentBreakdownAdapter
    private lateinit var overdueAdapter: OverdueBreakdownAdapter

    companion object {
        private const val ARG_CONTRACT = "contract"
        private const val ARG_CLIENT_REQUEST = "client_request"
        private const val ARG_PROPERTY_ID = "property_id"

        private const val ARG_OWNER_ID = "owner_id"



        fun newInstance(propertyId: String, contract: Contract, clientRequest: ClientRequest, ownerId: String) =
            AddContractDraftFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CONTRACT, contract)
                    putParcelable(ARG_CLIENT_REQUEST, clientRequest)
                    putString(ARG_PROPERTY_ID, propertyId)
                    putString(ARG_OWNER_ID, ownerId)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contract = it.getParcelable(ARG_CONTRACT) ?: error("Missing contract data")
            clientRequest = it.getParcelable(ARG_CLIENT_REQUEST) ?: error("Missing client request data")
            propertyId = it.getString(ARG_PROPERTY_ID) ?: error("Missing property ID")
            ownerId = it.getString(ARG_OWNER_ID) ?: error("Missing owner ID")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPropertyDraftBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide property card for contract-only flow
        binding.cardProperty.visibility = View.GONE

        setupAdapters()
        setupNoteListener()

        viewModel.fetchOwnerUsername(ownerId)

        // UI state
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnConfirm.isEnabled = !loading
        }

        viewModel.successMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            parentFragmentManager.popBackStack()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnConfirm.setOnClickListener {
            val notes = binding.etNotes.text?.toString()?.trim() ?: ""
            val updatedBreakdown = rentBreakdownAdapter.getUpdatedList()

            val updatedContract = contract.copy(
                notes = notes,
                createdAt = Timestamp.now(),
                monthlyRentBreakdown = updatedBreakdown
            )

            val updatedRequest = clientRequest.copy(
                ownerName = viewModel.ownerUsername.value ?: "Unknown"
            )

            viewModel.submitContractToProperty(updatedContract, updatedRequest, propertyId, ownerId)

            Log.d("Submit", "Contract: $updatedContract")
            Log.d("Submit", "ClientRequest: $updatedRequest")
        }
    }

    private fun setupNoteListener() {
        binding.etNotes.doOnTextChanged { text, _, _, _ ->
            binding.tvCharacterCount.text = "${text?.length ?: 0}/500"
        }
    }

    private fun setupAdapters() {
        rentBreakdownAdapter = RentBreakdownAdapter(requireContext())
        binding.rvRentBreakdown.adapter = rentBreakdownAdapter
        binding.rvRentBreakdown.layoutManager = LinearLayoutManager(requireContext())
        rentBreakdownAdapter.submitList(contract.monthlyRentBreakdown)

        overdueAdapter = OverdueBreakdownAdapter()
        binding.rvPcOverdues.adapter = overdueAdapter
        binding.rvPcOverdues.layoutManager = LinearLayoutManager(requireContext())

        if (contract.preContractOverdueAmounts.isEmpty()) {
            binding.tvNoPcOverdues.visibility = View.VISIBLE
            binding.rvPcOverdues.visibility = View.GONE
        } else {
            binding.tvNoPcOverdues.visibility = View.GONE
            binding.rvPcOverdues.visibility = View.VISIBLE
            overdueAdapter.submitList(contract.preContractOverdueAmounts)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

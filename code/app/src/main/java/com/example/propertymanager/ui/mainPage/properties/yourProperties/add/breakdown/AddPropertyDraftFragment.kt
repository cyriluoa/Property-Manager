package com.example.propertymanager.ui.mainPage.properties.yourProperties.add.breakdown

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
import com.bumptech.glide.Glide
import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.Property
import com.example.propertymanager.databinding.FragmentAddPropertyDraftBinding
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class AddPropertyDraftFragment : Fragment() {
    private var _binding: FragmentAddPropertyDraftBinding? = null
    private val binding get() = _binding!!
    private lateinit var property: Property
    private lateinit var contract: Contract
    private lateinit var clientName: String
    private lateinit var rentBreakdownAdapter: RentBreakdownAdapter
    private lateinit var overdueAdapter: OverdueBreakdownAdapter

    private val viewModel: AddPropertyDraftViewModel by viewModels()



    companion object {
        fun newInstance(property: Property, contract: Contract, clientName: String) =
            AddPropertyDraftFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("property", property)
                    putParcelable("contract", contract)
                    putString("clientName", clientName)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            property = bundle.getParcelable("property") ?: error("Missing property data")
            contract = bundle.getParcelable("contract") ?: error("Missing contract data")
            clientName = bundle.getString("clientName") ?: "Unknown"
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

        setupPropertyCard()
        setupNoteListeners()
        setupAdapters()

        // 👇 Fetch and observe owner's username
        viewModel.ownerUsername.observe(viewLifecycleOwner) { username ->
            binding.tvPropertyOwner.text ="Owner " + username
        }
        viewModel.fetchOwnerUsername(property.ownerId)

        // 🔁 Observers for UI feedback
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnConfirm.isEnabled = !isLoading
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            parentFragmentManager.popBackStack()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errMsg ->
            Toast.makeText(requireContext(), errMsg, Toast.LENGTH_LONG).show()
        }


        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnConfirm.setOnClickListener {
            val notes = binding.etNotes.text?.toString()?.trim() ?: ""
            val updatedRentBreakdown = rentBreakdownAdapter.getUpdatedList()

            // Generate IDs
            val propertyId = UUID.randomUUID().toString()
            val contractId = UUID.randomUUID().toString()
            val clientRequestId = UUID.randomUUID().toString()

            // Add timestamp + id to contract
            val finalContract = contract.copy(
                id = contractId,
                notes = notes,
                createdAt = Timestamp.now(),
                monthlyRentBreakdown = updatedRentBreakdown
            )

            // Add contract id to property
            val finalProperty = property.copy(
                id = propertyId,
                currentContractId = contractId,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            // Set up client request (ownerName can be observed from ViewModel)
            val ownerName = viewModel.ownerUsername.value ?: "Unknown"
            val clientRequest = ClientRequest(
                id = clientRequestId,
                clientId = finalContract.clientId,
                ownerId = finalProperty.ownerId,
                propertyId = propertyId,
                contractId = contractId,
                ownerName = ownerName,
                propertyName = finalProperty.name
            )

            // ✅ Now pass these 3 objects to your ViewModel
            viewModel.submitPropertyWithContractAndRequest(
                finalProperty,
                finalContract,
                clientRequest
            )

            Log.d("Submit", "Property: $finalProperty")
            Log.d("Submit", "Contract: $finalContract")
            Log.d("Submit", "ClientRequest: $clientRequest")
        }

    }


    private fun setupPropertyCard() {
        binding.tvPropertyName.text =  property.name
        binding.tvPropertyClient.text = "Client: " + clientName

        if (!property.imageUrl.isNullOrEmpty()) {
            binding.ivPropertyImage.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(property.imageUrl)
                .into(binding.ivPropertyImage)
        }
    }

    private fun setupNoteListeners() {
        binding.etNotes.doOnTextChanged { text, _, _, _ ->
            val count = text?.length ?: 0
            binding.tvCharacterCount.text = "$count/500"
        }
    }

    private fun setupAdapters() {
        // Rent Breakdown Adapter
        rentBreakdownAdapter = RentBreakdownAdapter(requireContext())
        binding.rvRentBreakdown.adapter = rentBreakdownAdapter
        binding.rvRentBreakdown.layoutManager = LinearLayoutManager(requireContext())
        rentBreakdownAdapter.submitList(contract.monthlyRentBreakdown)

        // Overdue Adapter (read-only)
        overdueAdapter = OverdueBreakdownAdapter()
        binding.rvPcOverdues.adapter = overdueAdapter
        binding.rvPcOverdues.layoutManager =  LinearLayoutManager(requireContext())

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


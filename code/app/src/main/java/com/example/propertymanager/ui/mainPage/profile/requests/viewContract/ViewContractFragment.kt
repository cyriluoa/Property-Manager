package com.example.propertymanager.ui.mainPage.profile.requests.viewContract

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.Property
import com.example.propertymanager.databinding.FragmentAddPropertyDraftBinding
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.breakdown.AddPropertyDraftViewModel
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.breakdown.OverdueBreakdownAdapter
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.breakdown.RentBreakdownAdapter
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class ViewContractFragment : Fragment() {
    private var _binding: FragmentAddPropertyDraftBinding? = null
    private val binding get() = _binding!!
    private lateinit var property: Property
    private lateinit var contract: Contract
    private lateinit var clientName: String

    private lateinit var ownerName: String
    private lateinit var rentBreakdownAdapter: RentBreakdownAdapter
    private lateinit var overdueAdapter: OverdueBreakdownAdapter



    companion object {
        fun newInstance(property: Property, contract: Contract, clientName: String, ownerName: String) =
            ViewContractFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("property", property)
                    putParcelable("contract", contract)
                    putString("clientName", clientName)
                    putString("ownerName",ownerName)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            property = bundle.getParcelable("property") ?: error("Missing property data")
            contract = bundle.getParcelable("contract") ?: error("Missing contract data")
            clientName = bundle.getString("clientName") ?: "Unknown"
            ownerName = bundle.getString("ownerName") ?: "Unknown"

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


        setUpViewContractViewChanges()
        setupPropertyCard()
        setupNoteListeners()
        setupAdapters()

    }


    private fun setUpViewContractViewChanges(){
        binding.layoutConfirmCancel.visibility = View.GONE
        binding.layoutAcceptDeny.visibility = View.VISIBLE
        binding.btnBack.visibility = View.VISIBLE


    }


    private fun setupPropertyCard() {
        binding.tvPropertyName.text = property.name
        binding.tvPropertyOwner.text = "Owner: " + ownerName
        binding.tvPropertyClient.text = "Client: " + clientName

        if (!property.imageUrl.isNullOrEmpty()) {
            binding.ivPropertyImage.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(property.imageUrl)
                .into(binding.ivPropertyImage)
        }
    }

    private fun setupNoteListeners() {
        // Set the notes
        binding.etNotes.setText(contract.notes)

        // Disable editing
        binding.etNotes.isFocusable = false
        binding.etNotes.isClickable = false
        binding.etNotes.isCursorVisible = false
        binding.etNotes.isLongClickable = false

        // Update character count
        val count = contract.notes.length
        binding.tvCharacterCount.text = "$count/500"
    }


    private fun setupAdapters() {
        // Rent Breakdown Adapter
        rentBreakdownAdapter = RentBreakdownAdapter(requireContext(), readOnly = true)
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
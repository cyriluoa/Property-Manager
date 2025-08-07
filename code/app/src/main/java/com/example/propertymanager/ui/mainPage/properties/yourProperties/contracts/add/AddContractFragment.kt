package com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts.add




import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.propertymanager.R
import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.OverdueItem
import com.example.propertymanager.data.model.RentBreakdown
import com.example.propertymanager.databinding.DialogOverdueInputBinding
import com.example.propertymanager.databinding.FragmentAddPropertyBinding
import com.example.propertymanager.ui.mainPage.properties.yourProperties.SharedPropertyViewModel
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.breakdown.AddPropertyDraftFragment
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.overdue.OverdueItemAdapter
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.searchClients.SearchClientsFragment
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@AndroidEntryPoint
class AddContractFragment : Fragment() {
    private var _binding: FragmentAddPropertyBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedPropertyViewModel by activityViewModels()
    private var selectedStartDate: LocalDate? = null

    private val propertyId: String by lazy {
        arguments?.getString(ARG_PROPERTY_ID) ?: throw IllegalArgumentException("Property ID is required")
    }

    private val propertyName: String by lazy {
        requireArguments().getString(ARG_PROPERTY_NAME) ?: error("Property Name not passed")
    }

    private val ownerId: String by lazy {
        requireArguments().getString(ARG_OWNER_ID) ?: error("Owner ID not passed")
    }



    companion object {
        private const val ARG_PROPERTY_ID = "property_id"
        private const val ARG_PROPERTY_NAME = "property_name"
        private const val ARG_OWNER_ID = "owner_id"

        fun newInstance(propertyId: String, propertyName: String, ownerId: String): AddContractFragment {
            val fragment = AddContractFragment()
            val args = Bundle()
            args.putString(ARG_PROPERTY_ID, propertyId)
            args.putString(ARG_OWNER_ID, ownerId)
            args.putString(ARG_PROPERTY_NAME, propertyName)

            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPropertyBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Update header text and hide property/image UI
        binding.tvHeader.text = "Add Contract"
        binding.tvGreeting.visibility = View.GONE
        binding.llImageUpload.visibility = View.GONE
        binding.etPropertyName.visibility = View.GONE

        setupDatePicker()
        setupClientSearch()
        updateSelectedClient()

        // Setup overdue item list
        val overdueItems = mutableListOf<OverdueItem>()
        lateinit var adapter: OverdueItemAdapter

        adapter = OverdueItemAdapter { itemToDelete ->
            overdueItems.remove(itemToDelete)
            adapter.submitList(overdueItems.toList())
        }
        binding.rvOverdueItems.adapter = adapter

        binding.cardAddOverdue.setOnClickListener {
            showOverdueDialog { newItem ->
                overdueItems.add(newItem)
                adapter.submitList(overdueItems.toList())
            }
        }


        binding.seeBreakdown.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            val contractLength = binding.etRentStartDate.text.toString().trim().toInt()
            val rentAmount = binding.etMonthlyRent.text.toString().trim().toDouble()
            val clientId = sharedViewModel.selectedClient?.uid ?: return@setOnClickListener
            val clientName = sharedViewModel.selectedClient?.username ?: return@setOnClickListener

            val contractId = UUID.randomUUID().toString()
            val clientRequestId = UUID.randomUUID().toString()

            val rentBreakdown = mutableListOf<RentBreakdown>()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val date = selectedStartDate!!

            for (i in 0 until contractLength) {
                val year = date.year + (date.monthValue - 1 + i) / 12
                val month = (date.monthValue - 1 + i) % 12 + 1
                val day = date.dayOfMonth

                val targetDate = try {
                    LocalDate.of(year, month, day)
                } catch (e: Exception) {
                    LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)
                }

                rentBreakdown.add(RentBreakdown(targetDate.format(formatter), rentAmount))
            }

            val formattedStartDate = date.format(formatter)
            val formattedEndDate = date.plusMonths(contractLength.toLong()).format(formatter)

            val contract = Contract(
                id = contractId,
                clientId = clientId,
                startDate = formattedStartDate,
                endDate = formattedEndDate,
                contractLengthMonths = contractLength,
                monthlyRentBreakdown = rentBreakdown,
                preContractOverdueAmounts = adapter.currentList.toList()
            )

            val clientRequest = ClientRequest(
                id = clientRequestId,
                clientId = clientId,
                ownerId = ownerId,
                propertyId = propertyId,
                contractId = contractId,
                propertyName = propertyName,
            )

            val nextFragment = AddContractDraftFragment.newInstance(propertyId,contract,clientRequest,ownerId)

            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, nextFragment)
                .addToBackStack(null)
                .commit()


            Log.d("Contract", "Built: $contract")
        }


        // See breakdown click removed for now
    }

    private fun setupClientSearch() {
        val openSearchFragment = {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SearchClientsFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.etClientSearch.setOnClickListener { openSearchFragment() }
        binding.btnSearchClients.setOnClickListener { openSearchFragment() }
    }

    private fun updateSelectedClient() {
        sharedViewModel.selectedClient?.let {
            binding.etClientSearch.setText(it.username)
        }
    }

    override fun onResume() {
        super.onResume()
        updateSelectedClient()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupDatePicker() {
        binding.etContractStart.setOnClickListener {
            val today = LocalDate.now()
            val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(todayMillis)
                .build()
            datePicker.addOnPositiveButtonClickListener { selectedMillis ->
                val selectedDate = Instant.ofEpochMilli(selectedMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                selectedStartDate = selectedDate
                binding.etContractStart.setText(
                    selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                )
            }
            datePicker.show(parentFragmentManager, "contract_start_date_picker")
        }
    }

    private fun showOverdueDialog(onItemAdded: (OverdueItem) -> Unit) {
        val dialogBinding = DialogOverdueInputBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnAdd.setOnClickListener {
            val label = dialogBinding.etOverdueLabel.text.toString().trim()
            val amount = dialogBinding.etOverdueAmount.text.toString().toDoubleOrNull()

            if (label.isEmpty()) {
                dialogBinding.etOverdueLabel.error = "Enter label"
                return@setOnClickListener
            }
            if (amount == null) {
                dialogBinding.etOverdueAmount.error = "Enter valid amount"
                return@setOnClickListener
            }

            onItemAdded(OverdueItem(label, amount))
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateInputs(): Boolean {
        val today = LocalDate.now()

        if (selectedStartDate == null) {
            Toast.makeText(requireContext(), "Select contract start date", Toast.LENGTH_SHORT).show()
            return false
        } else if (!selectedStartDate!!.isAfter(today)) {
            Toast.makeText(requireContext(), "Start date must be after today", Toast.LENGTH_SHORT).show()
            return false
        }

        val months = binding.etRentStartDate.text.toString().toIntOrNull()
        if (months == null || months < 1) {
            Toast.makeText(requireContext(), "Enter valid contract length", Toast.LENGTH_SHORT).show()
            return false
        }

        val rent = binding.etMonthlyRent.text.toString().toDoubleOrNull()
        if (rent == null || rent <= 0) {
            Toast.makeText(requireContext(), "Enter valid rent amount", Toast.LENGTH_SHORT).show()
            return false
        }

        val client = binding.etClientSearch.text.toString().trim()
        if (client.isEmpty()) {
            Toast.makeText(requireContext(), "Select a client", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

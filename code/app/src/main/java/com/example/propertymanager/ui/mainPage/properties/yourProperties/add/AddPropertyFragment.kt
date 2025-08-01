package com.example.propertymanager.ui.mainPage.properties.yourProperties.add

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
import com.example.propertymanager.data.firebase.FirestoreManager
import com.example.propertymanager.data.model.Contract
import com.example.propertymanager.data.model.OverdueItem
import com.example.propertymanager.data.model.Property
import com.example.propertymanager.data.model.RentBreakdown
import com.example.propertymanager.databinding.DialogOverdueInputBinding
import com.example.propertymanager.databinding.FragmentAddPropertyBinding
import com.example.propertymanager.ui.image.ImageSharedViewModel
import com.example.propertymanager.ui.image.UploadImageFragment
import com.example.propertymanager.ui.mainPage.properties.yourProperties.SharedPropertyViewModel
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.breakdown.AddPropertyDraftFragment
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.overdue.OverdueItemAdapter
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.searchClients.SearchClientsFragment
import com.example.propertymanager.utils.Constants
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AddPropertyFragment : Fragment() {
    private var _binding: FragmentAddPropertyBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedPropertyViewModel by activityViewModels()
    private var selectedStartDate: LocalDate? = null

    private val imageSharedViewModel: ImageSharedViewModel by activityViewModels()



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
        setupDatePicker()
        setupClientSearch()
        updateSelectedClient()

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

        binding.btnCamera.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, UploadImageFragment.newInstance(Constants.PATH_PROPERTIES))
                .addToBackStack(null)
                .commit()
        }

        imageSharedViewModel.imageUrl.observe(viewLifecycleOwner) { url ->
            Log.d("URL outside if", url.toString())
            if (!url.isNullOrEmpty()) {
                Log.d("URL", url.toString())
                binding.btnCamera.visibility = View.GONE
                binding.tvImageStatus.text = "Image Uploaded successfully"
            } else {
                // Optional: reset UI if there's no image
                binding.btnCamera.visibility = View.VISIBLE
                binding.tvImageStatus.text = "Upload Image"
            }
        }


        binding.seeBreakdown.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener
            val firestoreManager = FirestoreManager()

            val propertyName = binding.etPropertyName.text.toString().trim()
            val contractLength = binding.etRentStartDate.text.toString().trim().toInt()
            val rentAmount = binding.etMonthlyRent.text.toString().trim().toDouble()
            val imageUrl = imageSharedViewModel.imageUrl.value
            val clientId = sharedViewModel.selectedClient?.uid ?: return@setOnClickListener
            val clientName = sharedViewModel.selectedClient?.username ?: return@setOnClickListener

            val ownerId = firestoreManager.getCurrentUserUid() ?: return@setOnClickListener

            // Create rent breakdown
            val rentBreakdown = mutableListOf<RentBreakdown>()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            var date = selectedStartDate!!
            for (i in 0 until contractLength) {
                val year = date.year + (date.monthValue - 1 + i) / 12
                val month = (date.monthValue - 1 + i) % 12 + 1
                val day = date.dayOfMonth

                val targetDate = try {
                    LocalDate.of(year, month, day)
                } catch (e: Exception) {
                    LocalDate.of(year, month, 1).plusMonths(1).minusDays(1) // Last day of month
                }

                rentBreakdown.add(RentBreakdown(targetDate.format(formatter), rentAmount))
            }

            val formattedStartDate = selectedStartDate!!.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            val formattedEndDate = selectedStartDate!!.plusMonths(contractLength.toLong())
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))


            val contract = Contract(
                clientId = clientId,
                startDate = formattedStartDate,
                endDate = formattedEndDate,
                contractLengthMonths = contractLength,
                monthlyRentBreakdown = rentBreakdown,
                preContractOverdueAmounts = overdueItems
            )


            val property = Property(

                name = propertyName,
                ownerId = ownerId,
                imageUrl = imageUrl
            )


            val nextFragment = AddPropertyDraftFragment.newInstance(property, contract, clientName)

            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, nextFragment)
                .addToBackStack(null)
                .commit()
        }



    }

    private fun setupClientSearch() {
        val openSearchFragment = {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right
                )
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupDatePicker() {
        binding.etContractStart.setOnClickListener {
            val today = LocalDate.now()
            val zoneId = ZoneId.systemDefault()
            val todayMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Contract Start Date")
                .setTheme(R.style.DatePickerTheme)
                .setSelection(todayMillis)
                .build()

            datePicker.addOnPositiveButtonClickListener { selectedMillis ->
                val selectedDate = Instant.ofEpochMilli(selectedMillis)
                    .atZone(zoneId)
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
        val binding = DialogOverdueInputBinding.inflate(layoutInflater)

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setCancelable(false)
            .create()

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.btnAdd.setOnClickListener {
            val label = binding.etOverdueLabel.text?.toString()?.trim()
            val amountText = binding.etOverdueAmount.text?.toString()?.trim()

            if (label.isNullOrEmpty()) {
                binding.etOverdueLabel.error = "Enter label"
                return@setOnClickListener
            }
            if (amountText.isNullOrEmpty()) {
                binding.etOverdueAmount.error = "Enter amount"
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null) {
                binding.etOverdueAmount.error = "Enter a valid number"
                return@setOnClickListener
            }

            onItemAdded(OverdueItem(label, amount))
            dialog.dismiss()
        }

        dialog.show()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateInputs(): Boolean {

        val propertyName = binding.etPropertyName.text?.toString()?.trim()
        val contractStartText = binding.etContractStart.text?.toString()?.trim()
        val monthsText = binding.etRentStartDate.text?.toString()?.trim()
        val clientName = binding.etClientSearch.text?.toString()?.trim()
        val rentText = binding.etMonthlyRent.text?.toString()?.trim()

        // Property Name
        if (propertyName.isNullOrEmpty()) {
            Toast.makeText(requireContext(),"Property name cannot be empty", Toast.LENGTH_SHORT ).show()
            return false
        }

        // Start Date
        val today = LocalDate.now()
        if (selectedStartDate == null) {
            Toast.makeText(requireContext(), "Select contract start date", Toast.LENGTH_SHORT ).show()
            return false
        } else if (!selectedStartDate!!.isAfter(today)) {
            Toast.makeText(requireContext(), "Start date must be after today", Toast.LENGTH_SHORT ).show()
            return false
        }

        // Contract Length
        val months = monthsText?.toIntOrNull()
        if (months == null || months < 1) {
            Toast.makeText(requireContext(),"Enter valid contract length (min 1 month)", Toast.LENGTH_SHORT ).show()
            return false
        }

        // Client Name
        if (clientName.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select a client", Toast.LENGTH_SHORT ).show()
            return false
        }

        // Rent Amount
        val rent = rentText?.toDoubleOrNull()
        if (rent == null || rent <= 0) {
            Toast.makeText(requireContext(),"Enter a valid rent amount", Toast.LENGTH_SHORT ).show()
            return false
        }

        return true
    }


    override fun onResume() {
        super.onResume()
        updateSelectedClient()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
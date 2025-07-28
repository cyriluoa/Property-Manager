package com.example.propertymanager.ui.mainPage.properties.yourProperties.add

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.propertymanager.R
import com.example.propertymanager.data.model.OverdueItem
import com.example.propertymanager.databinding.DialogOverdueInputBinding
import com.example.propertymanager.databinding.FragmentAddPropertyBinding
import com.example.propertymanager.ui.mainPage.properties.yourProperties.SharedPropertyViewModel
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.overdue.OverdueItemAdapter
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.searchClients.SearchClientsFragment
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
            val amount = binding.etOverdueAmount.text?.toString()?.trim()

            if (label.isNullOrEmpty()) {
                binding.etOverdueLabel.error = "Enter label"
                return@setOnClickListener
            }
            if (amount.isNullOrEmpty()) {
                binding.etOverdueAmount.error = "Enter amount"
                return@setOnClickListener
            }

            onItemAdded(OverdueItem(label, amount))
            dialog.dismiss()
        }

        dialog.show()
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
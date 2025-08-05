package com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts.payableItems

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanager.data.model.Mode
import com.example.propertymanager.databinding.FragmentPayableItemsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PayableItemsFragment : Fragment() {

    private var _binding: FragmentPayableItemsBinding? = null
    private val binding get() = _binding!!

    private lateinit var monthlyAdapter: PayableItemAdapter
    private lateinit var overdueAdapter: PayableItemAdapter

    private val viewModel: PayableItemsViewModel by viewModels()

    private val propertyId: String by lazy {
        requireArguments().getString(ARG_PROPERTY_ID) ?: error("Missing propertyId")
    }

    private val contractId: String by lazy {
        requireArguments().getString(ARG_CONTRACT_ID) ?: error("Missing contractId")
    }

    private val mode: Mode by lazy {
        Mode.valueOf(requireArguments().getString(ARG_MODE) ?: Mode.CLIENT_MODE.name)
    }


    companion object {
        private const val ARG_PROPERTY_ID = "property_id"
        private const val ARG_CONTRACT_ID = "contract_id"

        private const val ARG_MODE = "mode"

        fun newInstance(propertyId: String, contractId: String, mode: Mode): PayableItemsFragment {
            return PayableItemsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROPERTY_ID, propertyId)
                    putString(ARG_CONTRACT_ID, contractId)
                    putString(ARG_MODE, mode.name)
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayableItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        observeViewModel()
        viewModel.startListening(propertyId, contractId)
    }

    private fun setupAdapters() {
        monthlyAdapter = PayableItemAdapter(
            mode,
            onViewPaymentsClicked = { /* handle view */ },
            onMakePaymentClicked = { /* handle make payment */ }
        )

        overdueAdapter = PayableItemAdapter(
            mode,
            onViewPaymentsClicked = { /* handle view */ },
            onMakePaymentClicked = { /* handle make payment */ }
        )


        binding.rvMonthlyRent.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = monthlyAdapter
        }

        binding.rvPreContractOverdue.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = overdueAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.monthlyItems.observe(viewLifecycleOwner) { items ->
            monthlyAdapter.submitList(items)
            binding.tvNoMonthlyRent.visibility =
                if (items.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.overdueItems.observe(viewLifecycleOwner) { items ->
            overdueAdapter.submitList(items)
            binding.tvNoPreContractOverdue.visibility =
                if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

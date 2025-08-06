package com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts.payableItems

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanager.R
import com.example.propertymanager.data.model.Mode
import com.example.propertymanager.data.model.PayableItemType
import com.example.propertymanager.databinding.FragmentPayableItemsBinding
import com.example.propertymanager.ui.image.ImageSharedViewModel
import com.example.propertymanager.ui.mainPage.payments.client.payments.MakePaymentFragment
import com.example.propertymanager.ui.mainPage.payments.client.payments.viewPayments.FragmentPaymentTabs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PayableItemsFragment : Fragment() {

    private var _binding: FragmentPayableItemsBinding? = null
    private val binding get() = _binding!!

    private lateinit var monthlyAdapter: PayableItemAdapter
    private lateinit var overdueAdapter: PayableItemAdapter

    private val viewModel: PayableItemsViewModel by viewModels()

    private val imageSharedViewModel: ImageSharedViewModel by activityViewModels()


    private val propertyId: String by lazy {
        requireArguments().getString(ARG_PROPERTY_ID) ?: error("Missing propertyId")
    }

    private val contractId: String by lazy {
        requireArguments().getString(ARG_CONTRACT_ID) ?: error("Missing contractId")
    }

    private val mode: Mode by lazy {
        Mode.valueOf(requireArguments().getString(ARG_MODE) ?: Mode.CLIENT_MODE.name)
    }

    private val ownerId: String by lazy {
        requireArguments().getString(ARG_OWNER_ID) ?: error("Missing ownerId")
    }
    private val clientId: String by lazy {
        requireArguments().getString(ARG_CLIENT_ID) ?: error("Missing clientId")
    }
    private val propertyName: String by lazy {
        requireArguments().getString(ARG_PROPERTY_NAME) ?: error("Missing propertyName")
    }


    companion object {
        private const val ARG_PROPERTY_ID = "property_id"
        private const val ARG_CONTRACT_ID = "contract_id"
        private const val ARG_MODE = "mode"
        private const val ARG_OWNER_ID = "owner_id"
        private const val ARG_CLIENT_ID = "client_id"
        private const val ARG_PROPERTY_NAME = "property_name"

        fun newInstance(
            propertyId: String,
            contractId: String,
            mode: Mode,
            ownerId: String,
            clientId: String,
            propertyName: String
        ): PayableItemsFragment {
            return PayableItemsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROPERTY_ID, propertyId)
                    putString(ARG_CONTRACT_ID, contractId)
                    putString(ARG_MODE, mode.name)
                    putString(ARG_OWNER_ID, ownerId)
                    putString(ARG_CLIENT_ID, clientId)
                    putString(ARG_PROPERTY_NAME, propertyName)
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
            onViewPaymentsClicked = { payableItem ->
                val label = if (payableItem.type == PayableItemType.PRE_CONTRACT_OVERDUE) {
                    payableItem.overdueItemLabel ?: "Pre Contract - Overdue Item"
                } else {
                    "Month ${payableItem.monthIndex?.plus(1)}'s Rent"
                }

                val fragment = FragmentPaymentTabs.newInstance(
                    propertyId = propertyId,
                    contractId = contractId,
                    payableItemId = payableItem.id,
                    propertyName = propertyName,
                    paymentLabel = label,
                    clientId = clientId,
                    ownerId = ownerId
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
            ,
            onMakePaymentClicked = { payableItem ->

                val label = if (payableItem.type == PayableItemType.PRE_CONTRACT_OVERDUE) {
                    payableItem.overdueItemLabel ?: "Pre Contract - Overdue Item"
                } else {
                    "Month ${payableItem.monthIndex?.plus(1)}'s Rent"
                }

                val fragment = MakePaymentFragment.newInstance(
                    propertyId = propertyId,
                    contractId = contractId,
                    payableItemId = payableItem.id,
                    amountLeft = payableItem.amountDue - payableItem.totalPaid,
                    clientId = clientId,
                    ownerId = ownerId,
                    propertyName = propertyName,
                    paymentLabel = label
                )


                imageSharedViewModel.clear()
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right
                    )
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

        )

        overdueAdapter = PayableItemAdapter(
            mode,
            onViewPaymentsClicked = { payableItem ->
                val label = if (payableItem.type == PayableItemType.PRE_CONTRACT_OVERDUE) {
                    payableItem.overdueItemLabel ?: "Pre Contract - Overdue Item"
                } else {
                    "Month ${payableItem.monthIndex?.plus(1)}'s Rent"
                }

                val fragment = FragmentPaymentTabs.newInstance(
                    propertyId = propertyId,
                    contractId = contractId,
                    payableItemId = payableItem.id,
                    propertyName = propertyName,
                    paymentLabel = label,
                    clientId = clientId,
                    ownerId = ownerId
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
            ,
            onMakePaymentClicked = { payableItem ->
                val label = if (payableItem.type == PayableItemType.PRE_CONTRACT_OVERDUE) {
                    payableItem.overdueItemLabel ?: "Pre Contract - Overdue Item"
                } else {
                    "Month ${payableItem.monthIndex?.plus(1)}"
                }

                val fragment = MakePaymentFragment.newInstance(
                    propertyId = propertyId,
                    contractId = contractId,
                    payableItemId = payableItem.id,
                    amountLeft = payableItem.amountDue - payableItem.totalPaid,
                    clientId = clientId,
                    ownerId = ownerId,
                    propertyName = propertyName,
                    paymentLabel = label
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

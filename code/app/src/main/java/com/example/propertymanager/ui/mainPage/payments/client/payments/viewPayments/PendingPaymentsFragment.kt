package com.example.propertymanager.ui.mainPage.payments.client.payments.viewPayments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.propertymanager.R
import com.example.propertymanager.data.model.Mode
import com.example.propertymanager.databinding.DialogPaymentProofBinding
import com.example.propertymanager.databinding.FragmentPaymentsPendingBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PendingPaymentsFragment : Fragment() {

    private var _binding: FragmentPaymentsPendingBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PaymentAdapter

    private val propertyId by lazy { requireArguments().getString(ARG_PROPERTY_ID)!! }
    private val contractId by lazy { requireArguments().getString(ARG_CONTRACT_ID)!! }
    private val payableItemId by lazy { requireArguments().getString(ARG_PAYABLE_ITEM_ID)!! }
    private val mode by lazy { Mode.valueOf(requireArguments().getString(ARG_MODE)!!) }

    private val sharedViewModel: SharedPaymentsViewModel by activityViewModels()



    companion object {
        private const val ARG_PROPERTY_ID = "property_id"
        private const val ARG_CONTRACT_ID = "contract_id"
        private const val ARG_PAYABLE_ITEM_ID = "payable_item_id"
        private const val ARG_MODE = "mode"

        fun newInstance(
            propertyId: String,
            contractId: String,
            payableItemId: String,
            mode: Mode
        ): PendingPaymentsFragment {
            return PendingPaymentsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROPERTY_ID, propertyId)
                    putString(ARG_CONTRACT_ID, contractId)
                    putString(ARG_PAYABLE_ITEM_ID, payableItemId)
                    putString(ARG_MODE, mode.name)
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentsPendingBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PaymentAdapter(
            mode = mode,
            onApproveClicked = { payment ->
//                sharedViewModel.updatePaymentState(
//                    propertyId, contractId, payableItemId, payment.id, PaymentState.APPROVED
//                )
            },
            onDenyClicked = { payment ->
//                sharedViewModel.updatePaymentState(
//                    propertyId, contractId, payableItemId, payment.id, PaymentState.DENIED
//                )
            },
            onViewImageClicked = { payment ->
                showImageDialog(payment.proofUrl)
            }
        )

        binding.rvPending.adapter = adapter
        binding.rvPending.layoutManager = LinearLayoutManager(requireContext())

        sharedViewModel.pendingPayments.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.swipeRefresh.setOnRefreshListener {
            sharedViewModel.refresh(propertyId, contractId, payableItemId)
        }

        sharedViewModel.loading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }

        if (sharedViewModel.pendingPayments.value == null)
            sharedViewModel.fetchPayments(propertyId, contractId, payableItemId)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showImageDialog(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No image available", Toast.LENGTH_SHORT).show()
            return
        }

        val binding: DialogPaymentProofBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_payment_proof,
            null,
            false
        )

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.color.light_gray)
            .error(R.drawable.ic_expired)
            .into(binding.ivPaymentProof)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }



}

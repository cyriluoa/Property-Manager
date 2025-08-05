package com.example.propertymanager.ui.mainPage.payments.client.payments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.propertymanager.R
import com.example.propertymanager.data.model.Payment
import com.example.propertymanager.data.model.PaymentState
import com.example.propertymanager.databinding.FragmentMakePaymentBinding
import com.example.propertymanager.ui.image.ImageSharedViewModel
import com.example.propertymanager.ui.image.UploadImageFragment
import com.example.propertymanager.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MakePaymentFragment : Fragment() {

    private var _binding: FragmentMakePaymentBinding? = null
    private val binding get() = _binding!!

    private val imageSharedViewModel: ImageSharedViewModel by activityViewModels()

    private val viewModel: PaymentViewModel by viewModels()




    private val propertyId: String by lazy {
        requireArguments().getString(ARG_PROPERTY_ID) ?: error("Missing propertyId")
    }

    private val contractId: String by lazy {
        requireArguments().getString(ARG_CONTRACT_ID) ?: error("Missing contractId")
    }

    private val payableItemId: String by lazy {
        requireArguments().getString(ARG_PAYABLE_ITEM_ID) ?: error("Missing payableItemId")
    }

    private val amountLeft: Double by lazy {
        requireArguments().getDouble(ARG_AMOUNT_LEFT)
    }

    private val clientId: String by lazy {
        requireArguments().getString(ARG_CLIENT_ID) ?: error("Missing clientId")
    }
    private val ownerId: String by lazy {
        requireArguments().getString(ARG_OWNER_ID) ?: error("Missing ownerId")
    }
    private val propertyName: String by lazy {
        requireArguments().getString(ARG_PROPERTY_NAME) ?: error("Missing propertyName")
    }

    private val paymentLabel: String by lazy {
        requireArguments().getString(ARG_PAYMENT_LABEL) ?: error("Missing paymentLabel")
    }


    companion object {
        private const val ARG_PROPERTY_ID = "property_id"
        private const val ARG_CONTRACT_ID = "contract_id"
        private const val ARG_PAYABLE_ITEM_ID = "payable_item_id"
        private const val ARG_AMOUNT_LEFT = "amount_left"

        private const val ARG_CLIENT_ID = "client_id"
        private const val ARG_OWNER_ID = "owner_id"
        private const val ARG_PROPERTY_NAME = "property_name"

        private const val ARG_PAYMENT_LABEL = "payment_label"

        fun newInstance(
            propertyId: String,
            contractId: String,
            payableItemId: String,
            amountLeft: Double,
            clientId: String,
            ownerId: String,
            propertyName: String,
            paymentLabel: String // <-- NEW
        ): MakePaymentFragment {
            return MakePaymentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROPERTY_ID, propertyId)
                    putString(ARG_CONTRACT_ID, contractId)
                    putString(ARG_PAYABLE_ITEM_ID, payableItemId)
                    putDouble(ARG_AMOUNT_LEFT, amountLeft)
                    putString(ARG_CLIENT_ID, clientId)
                    putString(ARG_OWNER_ID, ownerId)
                    putString(ARG_PROPERTY_NAME, propertyName)
                    putString(ARG_PAYMENT_LABEL, paymentLabel) // <-- NEW
                }
            }
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMakePaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvAmountLeft.text = "₹${amountLeft}"
        binding.tvPropertyName.text = "${propertyName}"

        setupListeners()
        observeViewModel()




    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSubmitPayment.isEnabled = !isLoading
        }

        viewModel.paymentResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Payment submitted successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack() // Or navigate somewhere
            }

            result.onFailure { error ->
                Toast.makeText(requireContext(), "Failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        imageSharedViewModel.imageUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                binding.cardImagePreview.visibility = View.VISIBLE

                Glide.with(this)
                    .load(it)
                    .into(binding.ivProofPreview)
            } ?: run {
                binding.cardImagePreview.visibility = View.GONE
            }
        }
    }


    private fun setupListeners() {
        val amountEditText = binding.etPaymentAmount

        amountEditText.doOnTextChanged { text, _, _, _ ->
            val input = text.toString().toDoubleOrNull()

            when {
                input == null || input <= 0.0 -> {
                    binding.llAmountValidation.visibility = View.GONE
                    binding.llAcceptableAmount.visibility = View.GONE
                }
                input > amountLeft -> {
                    binding.llAmountValidation.visibility = View.VISIBLE
                    binding.llAcceptableAmount.visibility = View.GONE
                }
                else -> {
                    binding.llAmountValidation.visibility = View.GONE
                    binding.llAcceptableAmount.visibility = View.VISIBLE
                }
            }
        }

        binding.llImageUpload.setOnClickListener {
            val uploadFragment = UploadImageFragment.newInstance(Constants.PATH_PAYMENT_PROOFS)

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, uploadFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.ivRemoveImage.setOnClickListener {
            binding.cardImagePreview.visibility = View.GONE
            imageSharedViewModel.clear()
        }


        binding.btnSubmitPayment.setOnClickListener {
            val input = amountEditText.text.toString().toDoubleOrNull()

            if (input == null || input <= 0.0 || input > amountLeft) {
                Toast.makeText(requireContext(), "Enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val proofUrl = imageSharedViewModel.imageUrl.value

            val payment = Payment(
                amountPaid = input,
                clientId = clientId,
                ownerId = ownerId,
                proofUrl = proofUrl,
                notes = binding.etNotes.text.toString(),
                paymentState = PaymentState.PENDING,
                propertyName = propertyName,
                paymentLabel = paymentLabel
            )

            viewModel.makePayment(payment, propertyId, contractId, payableItemId)


        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

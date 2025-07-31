package com.example.propertymanager.ui.mainPage.profile.requests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.propertymanager.R
import com.example.propertymanager.databinding.FragmentRequestsBinding
import com.example.propertymanager.ui.mainPage.profile.requests.viewContract.ViewContractFragment
import com.example.propertymanager.ui.mainPage.requests.ClientRequestAdapter
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestsFragment : Fragment() {

    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RequestsViewModel by viewModels()
    private lateinit var adapter: ClientRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ClientRequestAdapter { request ->
            viewModel.fetchPropertyAndContract(
                request,
                onSuccess = { property, contract ->
                    val nextFragment = ViewContractFragment.newInstance(property,contract,"You",request.ownerName)
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right
                        )
                        .replace(R.id.fragment_container, nextFragment)
                        .addToBackStack(null)
                        .commit()
                    // TODO: navigate to contract details screen
                },
                onFailure = {
                    Toast.makeText(requireContext(), "Failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            )
        }


        binding.rvRequests.adapter = adapter

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            viewModel.startListening(currentUserId)
        }

        viewModel.requests.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.pendingCount.observe(viewLifecycleOwner) {
            binding.tvPendingCount.text = it.toString()
        }

        viewModel.acceptedCount.observe(viewLifecycleOwner) {
            binding.tvAcceptedCount.text = it.toString()
        }

        viewModel.deniedCount.observe(viewLifecycleOwner) {
            binding.tvDeniedCount.text = it.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.stopListening()
    }
}



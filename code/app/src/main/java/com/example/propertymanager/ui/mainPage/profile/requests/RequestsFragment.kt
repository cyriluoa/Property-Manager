package com.example.propertymanager.ui.mainPage.profile.requests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.databinding.FragmentRequestsBinding
import com.example.propertymanager.ui.mainPage.requests.ClientRequestAdapter
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestsFragment : Fragment() {

    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!

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
            Toast.makeText(requireContext(), "Clicked: ${request.propertyName}", Toast.LENGTH_SHORT).show()
            // TODO: navigate to contract details later
        }

        binding.rvRequests.adapter = adapter


        val dummyRequests = listOf(
            ClientRequest(
                id = "r1",
                clientId = "client123",
                ownerId = "owner456",
                propertyId = "prop1",
                contractId = "con1",
                ownerName = "John Smith",
                propertyName = "Luxury Villa Downtown",
                timestamp = Timestamp.now()
            ),
            ClientRequest(
                id = "r2",
                clientId = "client789",
                ownerId = "owner123",
                propertyId = "prop2",
                contractId = "con2",
                ownerName = "Alice Johnson",
                propertyName = "Cozy Cottage East",
                status = "accepted",
                timestamp = Timestamp.now()
            )
        )

        adapter.submitList(dummyRequests)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


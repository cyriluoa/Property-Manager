package com.example.propertymanager.ui.mainPage.properties.yourProperties.add.searchClients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.propertymanager.data.model.User
import com.example.propertymanager.databinding.FragmentSearchClientBinding
import com.example.propertymanager.ui.mainPage.properties.yourProperties.SharedPropertyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchClientsFragment : Fragment() {

    private var _binding: FragmentSearchClientBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchClientsViewModel by viewModels()
    private val sharedViewModel: SharedPropertyViewModel by activityViewModels()

    private lateinit var adapter: ClientAdapter
    private var selectedUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ClientAdapter { user ->
            selectedUser = user
        }

        binding.rvClients.adapter = adapter

        binding.etSearch.doAfterTextChanged {
            viewModel.filterUsers(it.toString())
        }

        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        binding.btnConfirm.setOnClickListener {
            selectedUser?.let { user ->
                sharedViewModel.selectedClient = user
                parentFragmentManager.popBackStack()
            }
        }

        viewModel.filteredUsers.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
            binding.llEmptyState.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }


        viewModel.fetchAllUsers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



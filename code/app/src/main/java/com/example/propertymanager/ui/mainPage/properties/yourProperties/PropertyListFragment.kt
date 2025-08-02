package com.example.propertymanager.ui.mainPage.properties.yourProperties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanager.R
import com.example.propertymanager.databinding.FragmentPropertyListBinding
import com.example.propertymanager.ui.image.ImageSharedViewModel
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.AddPropertyFragment
import com.example.propertymanager.ui.mainPage.properties.yourProperties.contracts.ContractsFragment
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PropertyListFragment : Fragment() {

    private var _binding: FragmentPropertyListBinding? = null
    private val binding get() = _binding!!

    private val imageSharedViewModel: ImageSharedViewModel by activityViewModels()

    private val viewModel: PropertyListViewModel by viewModels()

    private lateinit var adapter: PropertyListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPropertyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PropertyListAdapter { clickedItem ->
            val fragment = ContractsFragment.newInstance(clickedItem.propertyId)

            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }


        binding.rvProperties.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProperties.adapter = adapter

        binding.fabAddProperty.setOnClickListener {
            imageSharedViewModel.clear()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, AddPropertyFragment())
                .addToBackStack(null)
                .commit()
        }

        // 🔄 Start listening to live Firestore data
        val ownerId = FirebaseAuth.getInstance().currentUser?.uid
        if (ownerId != null) {
            viewModel.startListening(ownerId)
        }

        viewModel.displayProperties.observe(viewLifecycleOwner) { propertyList ->
            adapter.submitList(propertyList)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



package com.example.propertymanager.ui.mainPage.properties.yourProperties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanager.R
import com.example.propertymanager.data.model.DisplayProperty
import com.example.propertymanager.databinding.FragmentPropertyListBinding
import com.example.propertymanager.ui.image.ImageSharedViewModel
import com.example.propertymanager.ui.mainPage.properties.yourProperties.add.AddPropertyFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PropertyListFragment : Fragment() {

    private var _binding: FragmentPropertyListBinding? = null
    private val binding get() = _binding!!

    private val imageSharedViewModel: ImageSharedViewModel by activityViewModels()

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
            Toast.makeText(requireContext(), "Clicked: ${clickedItem.propertyName}", Toast.LENGTH_SHORT).show()
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

        // 🔧 For now, show dummy data
        val testList = listOf(
            DisplayProperty(
                propertyId = "1",
                propertyName = "Sunset Villa Apartment",
                imageUrl = null,
                status = "PENDING",
                currentTenantName = "John Smith",
                dueThisMonth = 1200.0,
                totalDue = 3600.0
            ),

            DisplayProperty(
                propertyId = "2",
                propertyName = "Sunset Villa Apartment",
                imageUrl = null,
                status = "PENDING",
                currentTenantName = "John Smitho",
                dueThisMonth = 1200.0,
                totalDue = 2400.0
            )


        )

        adapter.submitList(testList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


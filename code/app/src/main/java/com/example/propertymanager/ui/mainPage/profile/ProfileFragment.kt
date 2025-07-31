package com.example.propertymanager.ui.mainPage.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.propertymanager.R
import com.example.propertymanager.databinding.FragmentProfileBinding
import com.example.propertymanager.main.MainActivity
import com.example.propertymanager.ui.mainPage.profile.requests.RequestsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var username: String = "Unknown"

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel.loadUserData()

        profileViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                // Optional: Clear UI if needed
                binding.tvFullName.text = ""
                binding.tvUsername.text = ""
                binding.ivProfilePicture.setImageResource(R.drawable.ic_white_person)
                binding.ivProfilePicture.setPadding(24, 24, 24, 24)
                return@observe
            }

            binding.tvFullName.text = user.fullName
            binding.tvUsername.text = user.username

            if (!user.photoUrl.isNullOrEmpty()) {
                binding.ivProfilePicture.setPadding(0, 0, 0, 0)
                Glide.with(this)
                    .load(user.photoUrl)
                    .placeholder(R.drawable.ic_gallery)
                    .error(R.drawable.ic_camera)
                    .circleCrop()
                    .into(binding.ivProfilePicture)
            } else {
                binding.ivProfilePicture.setImageResource(R.drawable.ic_white_person)
                binding.ivProfilePicture.setPadding(24, 24, 24, 24)
            }
        }


        profileViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        binding.btnSignOut.setOnClickListener {
            profileViewModel.signOut(
                onSuccess = {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), "Logout failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }

        binding.btnRequests.setOnClickListener {
            val intent = Intent(requireContext(), RequestsActivity::class.java)
            startActivity(intent)
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


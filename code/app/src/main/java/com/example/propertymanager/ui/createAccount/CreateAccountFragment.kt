package com.example.propertymanager.ui.createAccount

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.propertymanager.databinding.FragmentCreateAccountBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.propertymanager.R
import com.example.propertymanager.ui.image.ImageSharedViewModel
import com.example.propertymanager.ui.image.UploadImageFragment

@AndroidEntryPoint
class CreateAccountFragment : Fragment() {

    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateAccountViewModel by viewModels()

    private val imageSharedViewModel: ImageSharedViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.ivAddPhoto.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, UploadImageFragment())
                .addToBackStack(null)
                .commit()
        }

        // Observe loading state
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnCreateAccount.isEnabled = !isLoading
        }

        // Username availability check
        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val username = s.toString().trim()
                if (username.isNotEmpty()) {
                    viewModel.checkUsernameAvailability(username)
                } else {
                    binding.tvUsernameAvailability.text = ""
                    binding.tvUsernameAvailability.visibility = View.GONE
                }
            }
        })

        viewModel.usernameAvailable.observe(viewLifecycleOwner) { isAvailable ->
            binding.tvUsernameAvailability.apply {
                visibility = View.VISIBLE
                text = if (isAvailable) "Username is available" else "Username is taken"
                setTextColor(resources.getColor(
                    if (isAvailable) android.R.color.holo_green_dark else android.R.color.holo_red_dark,
                    null
                ))
            }
        }

        binding.btnCreateAccount.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (name.isEmpty() || username.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()
            ) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                Toast.makeText(
                    requireContext(),
                    "Password must be 5+ chars, include upper/lowercase and a special character",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val profileImageUrl = imageSharedViewModel.profileImageUrl.value
            viewModel.createAccount(name, username, email, password, profileImageUrl) // ✅ Pass it
        }


        viewModel.accountCreated.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Account created successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }

        imageSharedViewModel.profileImageUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                // Show local preview image
                binding.ivProfilePicture.setPadding(0, 0, 0, 0)
                binding.ivProfilePicture.clearColorFilter()

                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivProfilePicture)

            } else {
                // Fall back to download URL
                imageSharedViewModel.profileImageUrl.value?.let { url ->
                    binding.ivProfilePicture.setPadding(0, 0, 0, 0)
                    binding.ivProfilePicture.clearColorFilter()

                    Glide.with(this)
                        .load(url)
                        .into(binding.ivProfilePicture)
                } ?: run {
                    // Still nothing → show placeholder
                    binding.ivProfilePicture.setImageResource(R.drawable.ic_person)
                    binding.ivProfilePicture.setPadding(24, 24, 24, 24)
                    binding.ivProfilePicture.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                }
            }
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isValidPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z]).{5,}")
        return regex.matches(password)
    }
}



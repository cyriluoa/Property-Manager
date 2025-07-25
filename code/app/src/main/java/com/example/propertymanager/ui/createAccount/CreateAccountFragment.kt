package com.example.propertymanager.ui.createAccount

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.propertymanager.databinding.FragmentCreateAccountBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.viewModels

@AndroidEntryPoint
class CreateAccountFragment : Fragment() {

    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateAccountViewModel by viewModels()


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

            viewModel.createAccount(name, username, email, password)
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



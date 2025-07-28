package com.example.propertymanager.ui.image

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.propertymanager.databinding.FragmentUploadImageBinding
import com.example.propertymanager.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.DecimalFormat

@AndroidEntryPoint
class UploadImageFragment : Fragment() {

    private var _binding: FragmentUploadImageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UploadImageViewModel by viewModels()
    private val imageSharedViewModel: ImageSharedViewModel by activityViewModels()

    private var selectedImageUri: Uri? = null
    private lateinit var cameraImageUri: Uri


    private var imagePath: String? = null

    companion object {
        private const val ARG_IMAGE_PATH = "arg_image_path"

        fun newInstance(path: String): UploadImageFragment {
            val fragment = UploadImageFragment()
            val args = Bundle()
            args.putString(ARG_IMAGE_PATH, path)
            fragment.arguments = args
            return fragment
        }
    }


    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                setSelectedImage(it)
                viewModel.compressImageAndSet(it, requireContext(), Constants.MAX_PFP_SIZE)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                setSelectedImage(cameraImageUri)
                viewModel.compressImageAndSet(cameraImageUri, requireContext(), Constants.MAX_PFP_SIZE)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imagePath = arguments?.getString(ARG_IMAGE_PATH)


        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                launchCamera()
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
            }
        }

        binding.btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnConfirm.setOnClickListener {
            selectedImageUri?.let {
                viewModel.uploadCompressedImage(
                    imagePath = imagePath,
                    onSuccess = { url ->
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Uploaded Successfully", Toast.LENGTH_SHORT).show()
                            imageSharedViewModel.setImageUrl(url)
                            selectedImageUri?.let { uri ->
                                imageSharedViewModel.setImageUri(uri)
                            }

                            parentFragmentManager.popBackStack()
                        }
                    },
                    onFailure = { e ->
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } ?: Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show()
        }


        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnRemoveImage.setOnClickListener {
            resetImageSelection()
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnConfirm.isEnabled = !isLoading
            binding.btnCamera.isEnabled = !isLoading
            binding.btnGallery.isEnabled = !isLoading
            binding.btnRemoveImage.isEnabled = !isLoading
        }

    }

    private fun setSelectedImage(uri: Uri) {
        selectedImageUri = uri
        binding.btnConfirm.isEnabled = true

        binding.llNoImagePlaceholder.visibility = View.GONE
        binding.ivImagePreview.visibility = View.VISIBLE
        binding.llImageDetails.visibility = View.VISIBLE

        Glide.with(this)
            .load(uri)
            .into(binding.ivImagePreview)

        binding.tvImageSize.text = "Size: ${getFileSizeInKB(requireContext(),uri)} KB"
    }

    private fun getFileSizeInKB(context: Context, uri: Uri): String {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val sizeIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.SIZE) ?: -1
            cursor?.moveToFirst()
            val sizeInBytes = if (sizeIndex != -1) cursor?.getLong(sizeIndex) ?: 0L else 0L
            cursor?.close()

            val sizeInKB = sizeInBytes / 1024.0
            DecimalFormat("#.##").format(sizeInKB)
        } catch (e: Exception) {
            "?"
        }
    }


    private fun resetImageSelection() {
        selectedImageUri = null
        binding.btnConfirm.isEnabled = false
        binding.llNoImagePlaceholder.visibility = View.VISIBLE
        binding.ivImagePreview.visibility = View.GONE
        binding.llImageDetails.visibility = View.GONE
    }

    private fun launchCamera() {
        val imageFile = createImageFile()
        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            imageFile
        )
        cameraLauncher.launch(cameraImageUri)
    }

    private fun createImageFile(): File {
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${System.currentTimeMillis()}", ".jpg", storageDir)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

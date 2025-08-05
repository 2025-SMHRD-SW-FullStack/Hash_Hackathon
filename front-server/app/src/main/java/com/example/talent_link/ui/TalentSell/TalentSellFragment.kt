package com.example.talent_link.ui.TalentSell

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.talent_link.databinding.FragmentTalentSellBinding
import com.example.talent_link.ui.Talentsell.TalentSellViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.InputStream

class TalentSellFragment : Fragment() {

    private var _binding: FragmentTalentSellBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private val viewModel: TalentSellViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    val inputStream: InputStream? =
                        requireContext().contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.ivPreview.setImageBitmap(bitmap)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTalentSellBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            imagePickerLauncher.launch(intent)
        }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()
            val price = binding.etPrice.text.toString()

            if (title.isBlank() || content.isBlank() || price.isBlank()) {
                Toast.makeText(requireContext(), "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // JSON 문자열을 직접 작성
            val json = """
                {
                    "title": "$title",
                    "content": "$content",
                    "price": "$price"
                }
            """.trimIndent()

            val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json)

            val imagePart = selectedImageUri?.let { uri ->
                val file = File(requireContext().cacheDir, "upload.jpg").apply {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    inputStream?.use { writeBytes(it.readBytes()) }
                }
                val reqFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                MultipartBody.Part.createFormData("image", file.name, reqFile)
            }

            viewModel.uploadTalentSell(requestBody, imagePart)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

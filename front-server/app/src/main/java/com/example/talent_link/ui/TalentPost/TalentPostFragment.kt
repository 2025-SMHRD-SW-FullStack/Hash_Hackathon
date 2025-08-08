package com.example.talent_link.ui.TalentPost

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.talent_link.MainActivity
import com.example.talent_link.R
import com.example.talent_link.databinding.FragmentTalentPostBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class TalentPostFragment : Fragment() {

    private var _binding: FragmentTalentPostBinding? = null
    private val binding get() = _binding!!

    private var selectedType: String = "팝니다" // 기본값

    private var selectedImageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private val viewModel: TalentPostViewModel by lazy {
        ViewModelProvider(
            this,
            TalentPostViewModelFactory(requireContext())
        )[TalentPostViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupImagePicker()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTalentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        highlightTypeButton(selectedType)

        binding.btnSell.setOnClickListener {
            selectedType = "팝니다"
            highlightTypeButton(selectedType)
            binding.etPrice.hint = "가격 입력 (숫자만)"
        }

        binding.btnBuy.setOnClickListener {
            selectedType = "삽니다"
            highlightTypeButton(selectedType)
            binding.etPrice.hint = "희망 예산 입력 (숫자만)"
        }

        binding.placeholderLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }

        binding.ivPreview.setOnClickListener {
            // 이미지가 선택된 후에도 다시 갤러리를 열 수 있도록 합니다.
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val description = binding.etContent.text.toString()
            val priceOrBudget = binding.etPrice.text.toString()

            if (title.isBlank() || description.isBlank() || priceOrBudget.isBlank()) {
                Toast.makeText(requireContext(), "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✨ 수정된 부분: JSONObject를 사용하여 안전하게 JSON 생성
            val jsonObject = JSONObject().apply {
                put("title", title)
                put("description", description)
                if (selectedType == "팝니다") {
                    put("price", priceOrBudget.toIntOrNull() ?: 0)
                } else {
                    put("budget", priceOrBudget.toIntOrNull() ?: 0)
                }
            }
            val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())


            val imagePart = selectedImageUri?.let { uri ->
                val file = File(requireContext().cacheDir, "upload.jpg").apply {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    inputStream?.use { writeBytes(it.readBytes()) }
                }
                val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", file.name, reqFile)
            }

            val onResult: (Boolean) -> Unit = { success ->
                if (success) {
                    Toast.makeText(requireContext(), "등록 완료!", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.nav)?.selectedItemId = R.id.btnHome
                } else {
                    Toast.makeText(requireContext(), "등록 실패", Toast.LENGTH_SHORT).show()
                }
            }

            if (selectedType == "팝니다") {
                viewModel.uploadTalentSell(requestBody, imagePart, onResult)
            } else {
                viewModel.uploadTalentBuy(requestBody, imagePart, onResult)
            }
        }
    }

    private fun highlightTypeButton(type: String) {
        val green = ContextCompat.getColor(requireContext(), R.color.market_green)
        val gray = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        val white = ContextCompat.getColor(requireContext(), android.R.color.white)

        if (type == "팝니다") {
            binding.btnSell.setBackgroundColor(green)
            binding.btnSell.setTextColor(white)
            binding.btnBuy.setBackgroundColor(white)
            binding.btnBuy.setTextColor(gray)
            binding.btnSubmit.text = "재능 판매 등록"
        } else {
            binding.btnSell.setBackgroundColor(white)
            binding.btnSell.setTextColor(gray)
            binding.btnBuy.setBackgroundColor(green)
            binding.btnBuy.setTextColor(white)
            binding.btnSubmit.text = "재능 구매 등록"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    binding.ivPreview.setImageURI(uri)
                    binding.ivPreview.visibility = View.VISIBLE
                    binding.placeholderLayout.visibility = View.GONE
                }
            }
        }
    }
}
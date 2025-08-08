package com.example.talent_link.ui.TalentPost

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.talent_link.MainActivity
import com.example.talent_link.R
import com.example.talent_link.databinding.FragmentTalentPostBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.InputStream

class TalentPostFragment : Fragment() {

    private var _binding: FragmentTalentPostBinding? = null
    private val binding get() = _binding!!

    private var selectedType: String = "팝니다" // 기본값

    private var selectedImageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private val viewModel: TalentPostViewModel by lazy { // 🟢 ViewModel 이름 변경
        ViewModelProvider(
            this,
            TalentPostViewModelFactory(requireContext()) // 🟢 Factory 이름 변경
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

        binding.typeSelectLayout.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnSell -> {
                        selectedType = "팝니다"
                        highlightTypeButton(selectedType)
                        binding.etPrice.hint = "가격 입력 (숫자만)"
                        binding.btnSubmit.text = "재능 판매 등록"
                    }

                    R.id.btnBuy -> {
                        selectedType = "삽니다"
                        highlightTypeButton(selectedType)
                        binding.etPrice.hint = "희망 예산 입력 (숫자만)"
                        binding.btnSubmit.text = "재능 구매 등록"
                    }
                }
            }
        }

        // 👈 초기 버튼 선택 상태 설정
        binding.typeSelectLayout.check(R.id.btnSell)

        binding.cardSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish() // 👈 Activity를 종료하는 코드로 변경
        }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val description = binding.etContent.text.toString()
            val priceOrBudget = binding.etPrice.text.toString()

            if (title.isBlank() || description.isBlank() || priceOrBudget.isBlank()) {
                Toast.makeText(requireContext(), "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🟢 "팝니다" / "삽니다"에 따라 다른 JSON 생성
            val jsonKeyForPrice = if (selectedType == "팝니다") "price" else "budget"
            val json = """
                {
                    "title": "$title",
                    "description": "$description",
                    "$jsonKeyForPrice": $priceOrBudget 
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

            val onResult: (Boolean) -> Unit = { success ->
                if (success) {
                    // 홈으로 바로 이동 -> Activity에 성공 결과 설정 후 종료
                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "등록 실패", Toast.LENGTH_SHORT).show()
                }
            }

            // 🟢 selectedType에 따라 다른 함수 호출
            if (selectedType == "팝니다") {
                viewModel.uploadTalentSell(requestBody, imagePart, onResult)
            } else {
                viewModel.uploadTalentBuy(requestBody, imagePart, onResult)
            }
        }
    }

    private fun highlightTypeButton(type: String) {
        val market_green = ContextCompat.getColor(requireContext(), R.color.market_green)
        val gray = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        val white = ContextCompat.getColor(requireContext(), android.R.color.white)

        if (type == "팝니다") {
            // '팝니다' 버튼을 선택된 스타일로 변경
            binding.btnSell.setBackgroundColor(market_green)
            binding.btnSell.setTextColor(white)
            // '삽니다' 버튼을 기본 스타일로 변경
            binding.btnBuy.setBackgroundColor(white)
            binding.btnBuy.setTextColor(gray)
        } else {
            // '팝니다' 버튼을 기본 스타일로 변경
            binding.btnSell.setBackgroundColor(white)
            binding.btnSell.setTextColor(gray)
            // '삽니다' 버튼을 선택된 스타일로 변경
            binding.btnBuy.setBackgroundColor(market_green)
            binding.btnBuy.setTextColor(white)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupImagePicker() {
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        selectedImageUri = uri

                        // 1. 선택한 이미지를 ivPreview에 설정
                        binding.ivPreview.setImageURI(uri)
                        // 2. ivPreview를 화면에 보이게 함
                        binding.ivPreview.visibility = View.VISIBLE
                        // 3. '이미지 추가' 플레이스홀더는 숨김
                        binding.placeholderLayout.visibility = View.GONE
                    }
                }
            }
    }
}
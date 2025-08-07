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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.talent_link.MainActivity
import com.example.talent_link.R
import com.example.talent_link.databinding.FragmentTalentSellBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.InputStream

class TalentPostFragment : Fragment() {

    private var _binding: FragmentTalentSellBinding? = null
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

        binding.btnSelectImage.setOnClickListener {
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
                    Toast.makeText(requireContext(), "등록 완료!", Toast.LENGTH_SHORT).show()
                    // MainActivity의 BottomNavigationView를 조작하여 홈으로 이동
                    (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.nav)?.selectedItemId = R.id.btnHome
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
}
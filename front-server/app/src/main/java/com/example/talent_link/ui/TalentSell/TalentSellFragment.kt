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
import com.example.talent_link.ui.TalentSell.TalentSellViewModel
import com.example.talent_link.ui.TalentSell.TalentSellViewModelFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.InputStream

class TalentSellFragment : Fragment() {

    private var _binding: FragmentTalentSellBinding? = null
    private val binding get() = _binding!!

    private var selectedType: String = "팝니다"

    private var selectedImageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    // **Factory를 써서 ViewModel 생성**
    private val viewModel: TalentSellViewModel by lazy {
        ViewModelProvider(
            this,
            TalentSellViewModelFactory(requireContext())
        )[TalentSellViewModel::class.java]
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

        val btnSell = binding.btnSell
        val btnBuy = binding.btnBuy

        // 초기 강조
        highlightTypeButton("팝니다")

        btnSell.setOnClickListener {
            selectedType = "팝니다"
            highlightTypeButton(selectedType)
        }

        btnBuy.setOnClickListener {
            selectedType = "삽니다"
            highlightTypeButton(selectedType)
        }


        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            imagePickerLauncher.launch(intent)
        }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val description = binding.etContent.text.toString()  // description으로 맞춤
            val price = binding.etPrice.text.toString()

            if (title.isBlank() || description.isBlank() || price.isBlank()) {
                Toast.makeText(requireContext(), "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 서버 DTO에 맞춰 JSON 키 값 수정 ("description"!!)
            val json = """
                {
                    "title": "$title",
                    "description": "$description",
                    "price": $price
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

            viewModel.uploadTalentSell(requestBody, imagePart) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "등록 완료!", Toast.LENGTH_SHORT).show()
//                    // MainActivity로 이동 (예: 현재 Fragment가 MainActivity의 일부가 아니라면)
//                    val intent = Intent(requireContext(), MainActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//                    startActivity(intent)
//                    requireActivity().finish()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "등록 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun highlightTypeButton(type: String) {
        val green = ContextCompat.getColor(requireContext(), R.color.market_green)
        val gray = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        val white = ContextCompat.getColor(requireContext(), android.R.color.white)

        // 팝니다 강조
        if (type == "팝니다") {
            binding.btnSell.setBackgroundColor(green)
            binding.btnSell.setTextColor(white)
            binding.btnBuy.setBackgroundColor(white)
            binding.btnBuy.setTextColor(gray)
        } else {
            binding.btnSell.setBackgroundColor(white)
            binding.btnSell.setTextColor(gray)
            binding.btnBuy.setBackgroundColor(green)
            binding.btnBuy.setTextColor(white)
        }
    }

}

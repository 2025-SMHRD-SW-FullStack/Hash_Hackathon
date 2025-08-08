package com.example.talent_link.ui.TalentPost

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.talent_link.R
import com.example.talent_link.databinding.FragmentTalentPostBinding
import com.example.talent_link.ui.Home.HomeRetrofitInstance
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class TalentPostFragment : Fragment() {

    private var _binding: FragmentTalentPostBinding? = null
    private val binding get() = _binding!!

    // 수정/생성 모드 관리
    private var mode = "create" // "create" or "edit"
    private var postId: Long = -1L
    private var selectedType: String = "sell" // 기본값

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

        val bundle = arguments ?: requireActivity().intent.getBundleExtra("fragment_bundle")

        bundle?.let {
            mode = it.getString("mode", "create")
            postId = it.getLong("id", -1L)
            selectedType = it.getString("type", "sell")

            if (mode == "edit") {
                binding.etTitle.setText(it.getString("title"))
                binding.etContent.setText(it.getString("description"))
                binding.etPrice.setText(it.getInt("price").toString())
                val imageUrl = it.getString("imageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    binding.ivPreview.visibility = View.VISIBLE
                    binding.placeholderLayout.visibility = View.GONE
                    Glide.with(this).load(imageUrl).into(binding.ivPreview)
                }
                binding.typeSelectLayout.visibility = View.GONE
            }
        }

        setupTypeButtons()
        setupImageClickListeners()
        setupToolbar()
        setupSubmitButton()
    }

    private fun setupTypeButtons() {
        if (selectedType == "buy") {
            binding.typeSelectLayout.check(R.id.btnBuy)
        } else {
            binding.typeSelectLayout.check(R.id.btnSell)
        }

        binding.typeSelectLayout.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnSell -> {
                        selectedType = "sell"
                        updateUIForType()
                    }
                    R.id.btnBuy -> {
                        selectedType = "buy"
                        updateUIForType()
                    }
                }
            }
        }
        updateUIForType()
    }

    private fun updateUIForType() {
        highlightTypeButton(selectedType)
        if (selectedType == "sell") {
            binding.etPrice.hint = "가격 입력 (숫자만)"
            binding.btnSubmit.text = if (mode == "edit") "재능 판매 수정" else "재능 판매 등록"
        } else {
            binding.etPrice.hint = "희망 예산 입력 (숫자만)"
            binding.btnSubmit.text = if (mode == "edit") "재능 구매 수정" else "재능 구매 등록"
        }
    }

    private fun setupImageClickListeners() {
        binding.cardSelectImage.setOnClickListener { openGallery() }
        binding.ivPreview.setOnClickListener { openGallery() }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        imagePickerLauncher.launch(intent)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val description = binding.etContent.text.toString()
            val priceOrBudget = binding.etPrice.text.toString()

            if (title.isBlank() || description.isBlank() || priceOrBudget.isBlank()) {
                Toast.makeText(requireContext(), "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jsonObject = JSONObject().apply {
                put("title", title)
                put("description", description)
                if (selectedType == "sell") {
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

            // ViewModel의 CoroutineScope를 사용하여 API 호출
            viewModel.submitPost(mode, selectedType, postId, requestBody, imagePart) { success ->
                if (success) {
                    val message = if (mode == "edit") "수정 완료!" else "등록 완료!"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
                } else {
                    val message = if (mode == "edit") "수정 실패" else "등록 실패"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun highlightTypeButton(type: String) {
        val white = ContextCompat.getColor(requireContext(), android.R.color.white)
        val gray = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)

        binding.btnSell.apply {
            backgroundTintList = ContextCompat.getColorStateList(requireContext(), if (type == "sell") R.color.market_green else R.color.white)
            setTextColor(if (type == "sell") white else gray)
        }
        binding.btnBuy.apply {
            backgroundTintList = ContextCompat.getColorStateList(requireContext(), if (type == "buy") R.color.market_green else R.color.white)
            setTextColor(if (type == "buy") white else gray)
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
                        binding.ivPreview.setImageURI(uri)
                        binding.ivPreview.visibility = View.VISIBLE
                        binding.placeholderLayout.visibility = View.GONE
                    }
                }
            }
    }
}
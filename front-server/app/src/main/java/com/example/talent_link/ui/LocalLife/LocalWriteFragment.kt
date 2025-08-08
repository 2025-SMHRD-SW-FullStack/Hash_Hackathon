package com.example.talent_link.ui.LocalLife

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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.talent_link.R
import com.example.talent_link.databinding.FragmentLocalWriteBinding
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class LocalWriteFragment : Fragment() {

    private var _binding: FragmentLocalWriteBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    companion object {
        const val REQUEST_KEY = "localWriteRequest"
        const val BUNDLE_KEY_SUCCESS = "isSuccess"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    binding.ivPreview.setImageURI(it)
                    binding.ivPreview.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocalWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        binding.btnAddImg.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }
    }

    private fun uploadPost() {
        val title = binding.editTitle.text.toString().trim()
        val content = binding.editContent.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(requireContext(), "제목과 내용을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val nickname = IdManager.getNickname(requireContext()) ?: "사용자"
        val address = "중흥3동" // TODO: 실제 주소 데이터 사용
        val jwt = "Bearer " + (TokenManager.getAccessToken(requireContext()) ?: "")

        // ✨ 수정된 부분: JSONObject를 사용하여 안전하게 JSON 생성
        val jsonObject = JSONObject().apply {
            put("title", title)
            put("content", content)
            put("writerNickname", nickname)
            put("address", address)
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

        lifecycleScope.launch {
            try {
                val response = LocalLifeRetrofitInstance.api.uploadPost(jwt, requestBody, imagePart)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_KEY_SUCCESS to true))
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "등록 실패: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_submit -> {
                    uploadPost()
                    true
                }
                else -> false
            }
        }
    }
}
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
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

    // 수정/생성 모드 관리
    private var mode = "create" // "create" or "edit"
    private var postId: Long = -1L

    private var selectedImageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>


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
                    binding.placeholderLayout.visibility = View.GONE
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

        // 👈 arguments나 intent extra에서 번들을 가져옵니다.
        val bundle = arguments ?: requireActivity().intent.getBundleExtra("fragment_bundle")

        bundle?.let {
            mode = it.getString("mode", "create")
            postId = it.getLong("id", -1L)
            if (mode == "edit") {
                binding.toolbar.title = "동네 생활 글 수정"
                binding.btnSubmit.text = "수정 완료"
                binding.editTitle.setText(it.getString("title"))
                binding.editContent.setText(it.getString("content"))
                // 이미지 로드
                val imageUrl = it.getString("imageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    binding.ivPreview.visibility = View.VISIBLE
                    binding.placeholderLayout.visibility = View.GONE
                    Glide.with(this).load(imageUrl).into(binding.ivPreview)
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.cardSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }

        binding.btnSubmit.setOnClickListener {
            uploadPost()
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
        val address = "중흥3동"
        val jwt = "Bearer " + (TokenManager.getAccessToken(requireContext()) ?: "")

        val jsonObject = JSONObject().apply {
            put("title", title)
            put("content", content)
            put("writerNickname", nickname) // 서버에서 인증정보로 덮어쓰므로 사실상 불필요
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
                val response = if (mode == "edit") {
                    LocalLifeRetrofitInstance.api.updatePost(postId, jwt, requestBody, imagePart)
                } else {
                    LocalLifeRetrofitInstance.api.uploadPost(jwt, requestBody, imagePart)
                }

                if (response.isSuccessful) {
                    val message = if (mode == "edit") "게시글이 수정되었습니다." else "게시글이 등록되었습니다."
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
                } else {
                    val error = response.errorBody()?.string()
                    Toast.makeText(requireContext(), "요청 실패: $error", Toast.LENGTH_SHORT).show()
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

}
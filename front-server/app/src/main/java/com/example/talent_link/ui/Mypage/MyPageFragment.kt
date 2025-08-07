package com.example.talent_link.ui.Mypage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.talent_link.data.network.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.talent_link.databinding.FragmentMyPageBinding
import com.example.talent_link.R
import com.example.talent_link.data.model.login.RefreshRequest
import com.example.talent_link.data.model.mypage.UserUpdateRequest
import com.example.talent_link.util.TokenManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!
    private var isEditing = false
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            Log.d("MyPageFragment", "선택한 이미지 Uri: $imageUri")

            imageUri?.let {
                uploadProfileImage(it)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MyPageFragment", "🟢 onViewCreated 호출됨")

        lifecycleScope.launch {
            fetchUserProfile()
        }

        // 리프레시 토큰으로 토큰 갱신 후 프로필 재호출 (이 부분은 그대로 유지)
        lifecycleScope.launch {
            try {
                val refreshToken = TokenManager.getRefreshToken(requireContext())
                if (refreshToken.isNullOrBlank()) {
                    Log.e("MyPageFragment", "리프레시 토큰 없음")
                    return@launch
                }

                Log.d("MyPageFragment", "📥 불러온 리프레시 토큰: $refreshToken")

                val request = RefreshRequest(token = refreshToken)
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.authService.refreshToken(request)
                }

                if (response.isSuccessful) {
                    val newAccessToken = response.body()?.accessToken ?: ""
                    val refreshToken = response.body()?.refreshToken
                    Log.d("MyPageFragment", "리프레시 성공, 새 토큰: $newAccessToken")

                    if (newAccessToken.isNotBlank()) {
                        TokenManager.saveTokens(requireContext(), newAccessToken, null)
                        withContext(Dispatchers.Main) {
                            fetchUserProfile()
                        }
                    } else {
                        Log.e("MyPageFragment", "새 액세스 토큰이 비어있음")
                    }
                } else {
                    Log.e("MyPageFragment", "리프레시 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MyPageFragment", "리프레시 오류: ${e.message}")
            }
        }


        binding.editIcon.setOnClickListener {
            isEditing = !isEditing
            binding.etMyUserNick.isEnabled = isEditing
            binding.etMyUserNick.isFocusableInTouchMode = isEditing

            if (isEditing) {
                binding.etMyUserNick.requestFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.etMyUserNick, InputMethodManager.SHOW_IMPLICIT)
            } else {
                // 수정 완료, 서버에 닉네임 업데이트 요청
                val newNick = binding.etMyUserNick.text.toString()
                val updateRequest = UserUpdateRequest(newNick)

                lifecycleScope.launch {
                    try {
                        // 토큰 직접 넘기지 말고 인터셉터에 맡김
                        // val token = TokenManager.getAccessToken(requireContext())
                        // val bearerToken = "Bearer $token" // <-- 삭제

                        val response = RetrofitClient.userService.updateNickname(updateRequest) // <-- 수정: updateNickname 호출

                        if (response.isSuccessful) {
                            Log.d("MyPageFragment", "닉네임 수정 성공")
                            // 성공 시 최신 정보 다시 가져오기 권장
                            fetchUserProfile()
                        } else {
                            Log.e("MyPageFragment", "닉네임 수정 실패: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("MyPageFragment", "닉네임 수정 오류: ${e.message}")
                    }
                }
            }
        }

        binding.ivMyUserProfile.setOnClickListener {
            openImagePicker()
        }

    }

    private fun getTokenFromSharedPrefs(): String {
        return TokenManager.getAccessToken(requireContext()) ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickImageLauncher.launch(intent)
    }

    private fun uploadProfileImage(imageUri: Uri) {
        lifecycleScope.launch {
            try {
                // 토큰 직접 넘기지 말고 인터셉터에 맡김
                // val token = "Bearer ${getTokenFromSharedPrefs()}" // <-- 삭제

                val multipartBody = prepareFilePart("file", imageUri)
                Log.d("MyPageFragment", "MultipartBody.Part 생성됨: name=${multipartBody.headers?.get("Content-Disposition")}")

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.userService.uploadProfileImage(multipartBody) // <-- 수정: 토큰 제거
                }

                Log.d("MyPageFragment", "서버 응답: ${response.body()?.toString()}")

                if (response.isSuccessful) {
                    val imageUrl = response.body()
                    Log.d("MyPageFragment", "서버에서 받은 이미지 URL: $imageUrl")
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.ivMyUserProfile)

                } else {
                    Log.e("MyPageFragment", "프로필 사진 업로드 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MyPageFragment", "프로필 사진 업로드 오류: ${e.message}")
            }
        }
    }

    private fun prepareFilePart(partName: String, fileUri: Uri): MultipartBody.Part {
        val contentResolver = requireContext().contentResolver

        val inputStream = contentResolver.openInputStream(fileUri)
            ?: throw IllegalArgumentException("Cannot open input stream from URI")

        val fileBytes = inputStream.readBytes()
        inputStream.close()

        val requestBody = fileBytes.toRequestBody(
            contentResolver.getType(fileUri)?.toMediaTypeOrNull()
        )

        val fileName = "upload_${System.currentTimeMillis()}.jpg"

        return MultipartBody.Part.createFormData(partName, fileName, requestBody)
    }

    private suspend fun fetchUserProfile() {
        Log.d("MyPageFragment", "📡 fetchUserProfile() 호출됨")
        try {
            val response = RetrofitClient.userService.getMyPageProfile()
            Log.d("MyPageFragment", "응답 코드: ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("MyPageFragment", "✅ 사용자 정보: $body")
                Log.d("MyPageFragment", "프로필 이미지 URL: ${body?.profileImageUrl}")

                // 프로필 이미지 뷰에 표시
                body?.profileImageUrl?.let { url ->
                    Glide.with(requireContext())
                        .load(url)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.ivMyUserProfile)
                }

                // 닉네임, 이메일 텍스트뷰에 표시 (예시)
                binding.etMyUserNick.setText(body?.nickname ?: "")
                // 이메일 표시용 뷰가 있으면 셋팅
                 binding.tvMyUserEmail.text = body?.email ?: ""

            } else {
                Log.w("MyPageFragment", "❌ 사용자 정보 없음. 에러 바디: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("MyPageFragment", "❌ 예외 발생: ${e.message}", e)
        }
    }



}

package com.example.talent_link.ui.Mypage

import android.app.AlertDialog
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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialog
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.talent_link.NoNavActivity
import com.example.talent_link.databinding.FragmentMyPageBinding
import com.example.talent_link.R
import com.example.talent_link.data.model.login.RefreshRequest
import com.example.talent_link.data.model.mypage.UserUpdateRequest
import com.example.talent_link.data.repository.UserRepository
import com.example.talent_link.ui.Auth.UserViewModel
import com.example.talent_link.ui.Auth.UserViewModelFactory
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class MyPageFragment : Fragment() {

    private lateinit var userViewModel: UserViewModel
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userRepository = UserRepository(RetrofitClient.userService)
        Log.d("MyPageFragment", "UserRepository 생성됨: $userRepository") // 확인용 로그

        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(requireActivity(), factory).get(UserViewModel::class.java)

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

        // 프로필 정보가 변경되면 UI 업데이트
        userViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.etMyUserNick.setText(it.nickname)
                binding.tvMyUserEmail.text = it.email

                val imageUrl = it.profileImageUrl ?: ""

                Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.profile_default_t)
                    .error(R.drawable.profile_default_t)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.ivMyUserProfile)
            }
        }



        // 프로필 불러오기 요청
        userViewModel.fetchUserProfile()


        binding.editIcon.setOnClickListener {
            isEditing = !isEditing
            binding.etMyUserNick.isEnabled = isEditing
            binding.etMyUserNick.isFocusableInTouchMode = isEditing

            if (isEditing) {
                binding.etMyUserNick.requestFocus()
                // 현재 텍스트 전체 선택 (커서가 끝이 아닌 전체 선택)
                binding.etMyUserNick.selectAll()

                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.etMyUserNick, InputMethodManager.SHOW_IMPLICIT)
            } else {
                val newNick = binding.etMyUserNick.text.toString()
                val updateRequest = UserUpdateRequest(newNick)

                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.userService.updateNickname(updateRequest)
                        Log.d("MyPageFragment", "updateNickname 응답 코드: ${response.code()}, 메시지: ${response.message()}")

                        if (response.isSuccessful) {
                            Log.d("MyPageFragment", "닉네임 수정 성공")
                            userViewModel.fetchUserProfile()
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

        binding.btnUserDelete.setOnClickListener {
            showWithdrawalConfirmationDialog()
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
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

    private fun showWithdrawalConfirmationDialog() {
        val dialog = AppCompatDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_withdrawal_confirmation, null)
        dialog.setContentView(view)

        // 👇 다이얼로그의 너비를 조절하는 코드 (핵심 수정 부분)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 다이얼로그 배경을 투명하게 만들어 둥근 모서리가 보이게 함
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnWithdraw = view.findViewById<Button>(R.id.btnWithdraw)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnWithdraw.setOnClickListener {
            // "탈퇴" 버튼 클릭 시 동작
            lifecycleScope.launch { // API 호출을 위해 코루틴 사용
                try {
                    val response = RetrofitClient.userService.withdrawUser()
                    if (response.isSuccessful) {
                        TokenManager.clearTokens(requireContext())
                        IdManager.clearAll(requireContext())

                        val intent = Intent(requireContext(), NoNavActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                        Toast.makeText(requireContext(), "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "회원 탈퇴에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }


    private fun logoutUser() {
        // 1. 기기에 저장된 모든 토큰과 사용자 정보를 삭제합니다.
        TokenManager.clearTokens(requireContext())
        IdManager.clearAll(requireContext()) // 1단계에서 추가한 함수 호출

        // 2. 로그인/인증 화면(NoNavActivity)으로 이동합니다.
        val intent = Intent(requireContext(), NoNavActivity::class.java)
        // 이전의 모든 Activity 기록을 지워서, 뒤로가기 버튼으로 다시 돌아오지 못하게 합니다.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
    }
}

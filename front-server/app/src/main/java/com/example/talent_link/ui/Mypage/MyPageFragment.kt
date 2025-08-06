package com.example.talent_link.ui.Mypage

import android.content.Context
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.example.talent_link.databinding.FragmentMyPageBinding
import com.example.talent_link.R


class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!
    private var isEditing = false



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // 토큰 불러오기
        val token = "Bearer ${getTokenFromSharedPrefs()}"


        // API 호출
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.userService.getMyPageProfile(token)
                }

                if (response.isSuccessful) {
                    val data = response.body()
                    // editText라서 setText 써야함
                    binding.etMyUserNick.setText(data?.nickname ?: "")
                    binding.tvMyUserEmail.text = data?.email

                    // 프로필 이미지 Glide로 불러오기 (예시)
                    val imageUrl = data?.profileImageUrl
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(imageUrl)
                            .into(binding.ivMyUserProfile)
                    } else {
                        binding.ivMyUserProfile.setImageResource(R.drawable.profile_clover)
                    }

                } else {
                    Log.e("UserService", "마이페이지 정보 불러오기 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("UserService", "API 호출 오류: ${e.message}")
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
                lifecycleScope.launch {
                    try {
                        val token = "Bearer ${getTokenFromSharedPrefs()}"
                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.userService.updateNickname(token, newNick)
                        }
                        if (response.isSuccessful) {
                            Log.d("MyPageFragment", "닉네임 수정 성공")
                        } else {
                            Log.e("MyPageFragment", "닉네임 수정 실패: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("MyPageFragment", "닉네임 수정 오류: ${e.message}")
                    }
                }
            }
        }

    }

    private fun getTokenFromSharedPrefs(): String {
        val prefs = requireContext().getSharedPreferences("auth", 0)
        return prefs.getString("accessToken", "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

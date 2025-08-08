package com.example.talent_link.ui.Auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.talent_link.data.model.mypage.MyPageResponse
import com.example.talent_link.data.network.RetrofitClient
import com.example.talent_link.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _userProfile = MutableLiveData<MyPageResponse?>()
    val userProfile: LiveData<MyPageResponse?> = _userProfile

    // UserViewModel.kt
    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.userService.getMyPageProfile()  // ✅ 여기
                if (response.isSuccessful) {
                    _userProfile.value = response.body()
                } else {
                    Log.e("UserViewModel", "📛 유저 정보 불러오기 실패: ${response.code()} / ${response.errorBody()?.string()}")
                    _userProfile.value = null
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "❌ 유저 정보 불러오기 오류: ${e.message}")
                _userProfile.value = null
            }
        }
    }


    // 필요 시 닉네임 수정, 이미지 업로드 함수도 추가 가능
}

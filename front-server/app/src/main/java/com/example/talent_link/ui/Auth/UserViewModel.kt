package com.example.talent_link.ui.Auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.talent_link.data.model.mypage.MyPageResponse
import com.example.talent_link.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _userProfile = MutableLiveData<MyPageResponse?>()
    val userProfile: LiveData<MyPageResponse?> = _userProfile

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val response = userRepository.getUserProfile()
                if (response.isSuccessful) {
                    _userProfile.value = response.body()
                } else {
                    _userProfile.value = null
                }
            } catch (e: Exception) {
                _userProfile.value = null
            }
        }
    }

    // 필요 시 닉네임 수정, 이미지 업로드 함수도 추가 가능
}

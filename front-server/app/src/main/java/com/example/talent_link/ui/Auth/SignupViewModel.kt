package com.example.talent_link.ui.Auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_link.data.repository.AuthRepository
import kotlinx.coroutines.launch
import android.net.Uri

class SignupViewModel(private val repository: AuthRepository) : ViewModel() {

    fun signup(email: String, pw: String, nickname: String, uri: Uri?) {
        viewModelScope.launch {
            val response = repository.signup(email, pw, nickname, uri)
            if (response.isSuccessful) {
                println("✅ 회원가입 성공")
            } else {
                println("❌ 실패: ${response.code()} / ${response.errorBody()?.string()}")
            }
        }
    }
}

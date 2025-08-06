package com.example.talent_link.ui.Auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.talent_link.data.repository.AuthRepository
import kotlinx.coroutines.launch
import android.net.Uri

class SignupViewModel(private val repository: AuthRepository) : ViewModel() {

    fun signup(
        email: String,
        password: String,
        confirmPassword: String,
        nickname: String,
        profileUri: Uri?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.signup(email, password, confirmPassword, nickname, profileUri)
                if (response.isSuccessful) {
                    println("✅ 회원가입 성공")
                    onSuccess()
                } else {
                    val error = response.errorBody()?.string() ?: "회원가입 실패"
                    println("❌ 회원가입 실패: $error")
                    onFailure(error)
                }
            } catch (e: Exception) {
                println("❌ 네트워크 에러: ${e.message}")
                onFailure("에러: ${e.message}")
            }
        }
    }
}


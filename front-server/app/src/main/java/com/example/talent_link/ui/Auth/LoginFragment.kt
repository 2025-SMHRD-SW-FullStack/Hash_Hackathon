package com.example.talent_link.ui.Auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.talent_link.AuthActivity
import com.example.talent_link.MainActivity
import com.example.talent_link.data.repository.AuthRepository
import com.example.talent_link.databinding.FragmentLoginBinding
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.JwtUtils
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        authRepository = AuthRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginEmail.text.toString()
            val password = binding.etLoginPw.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "이메일과 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = authRepository.login(email, password)

                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        val accessToken = loginResponse?.accessToken
                        Log.d("response",requireContext().toString())
                        if (accessToken != null) {
                            TokenManager.saveToken(requireContext(), accessToken)
                            val userId = JwtUtils.parseUserIdFromJwt(accessToken)
                            val nickname = loginResponse.user.nickname
                            IdManager.saveNickname(requireContext(), nickname)
                            IdManager.saveUserId(requireContext(), userId)

                            Log.d("토큰저장", "저장된 토큰: $accessToken")

                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "로그인 성공", Toast.LENGTH_SHORT).show()

                                // ✅ 홈 프래그먼트(HomeFragment)부터 띄우도록 명시
                                val intent = Intent(requireContext(), MainActivity::class.java)
                                intent.putExtra("fromLogin", true) // 요 줄이 핵심
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "토큰 없음", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                        Log.e("로그인 실패", response.errorBody()?.string() ?: "Unknown error")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "에러 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("LoginFragment", "Exception", e)
                }
            }
        }

        binding.tvSignUp.setOnClickListener {
            (activity as? AuthActivity)?.openSignUpFragment()
        }
    }
}

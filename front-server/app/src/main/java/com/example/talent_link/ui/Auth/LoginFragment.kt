package com.example.talent_link.ui.Auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.talent_link.MainActivity
import com.example.talent_link.NoNavActivity
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
                    Log.d("Login", "로그인 버튼 클릭, API 호출 시작")
                    val response = authRepository.login(email, password)

                    if (response.isSuccessful) {
                        Log.d("Login", "로그인 성공, 토큰 저장 시도")
                        val loginResponse = response.body()
                        Log.d("Login", "accessToken=${loginResponse?.accessToken}, refreshToken=${loginResponse?.refreshToken}")
                        val accessToken = loginResponse?.accessToken
                        val refreshToken = loginResponse?.refreshToken

                        Log.d("response",requireContext().toString())

                        if (accessToken != null && refreshToken != null) {
                            TokenManager.saveTokens(requireContext(), accessToken, refreshToken)
                            Log.d("토큰 저장", "✅ Access: $accessToken, Refresh: $refreshToken")

                            val userId = JwtUtils.parseUserIdFromJwt(accessToken)
                            val nickname = loginResponse.user.nickname
                            IdManager.saveNickname(requireContext(), nickname)
                            IdManager.saveUserId(requireContext(), userId)

                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "로그인 성공", Toast.LENGTH_SHORT).show()

                                // ✅ 홈 프래그먼트(HomeFragment)부터 띄우도록 명시
                                val intent = Intent(requireContext(), MainActivity::class.java)
                                intent.putExtra("fromLogin", true)
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        } else {
                            Log.e("토큰 저장 오류", "AccessToken 또는 RefreshToken이 null입니다.")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "토큰 파싱 실패", Toast.LENGTH_SHORT).show()
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
            (activity as? NoNavActivity)?.openSignUpFragment()
        }
    }
}

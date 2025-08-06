package com.example.talent_link.ui.Auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.talent_link.MainActivity
import com.example.talent_link.data.repository.AuthRepository
import com.example.talent_link.databinding.FragmentSignupBinding
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private val viewModel by lazy {
        SignupViewModel(AuthRepository(requireContext()))
    }

    private var imageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.ivProfile.setImageURI(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.ivProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.etSignUpEmail.text.toString().trim()
            val pw = binding.etSignUpPw.text.toString().trim()
            val pwCheck = binding.etSignUpPwCheck.text.toString().trim()
            val nickname = binding.etSignUpNick.text.toString().trim()

            if (email.isBlank() || pw.isBlank() || pwCheck.isBlank() || nickname.isBlank()) {
                Toast.makeText(requireContext(), "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pw != pwCheck) {
                Toast.makeText(requireContext(), "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.signup(
                email = email,
                password = pw,
                confirmPassword = pwCheck,
                nickname = nickname,
                profileUri = imageUri,
                onSuccess = {
                    // 🔥 로그인 후 토큰 저장
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val authRepo = AuthRepository(requireContext())
                            val response = authRepo.login(email, pw)

                            if (response.isSuccessful) {
                                val loginResponse = response.body()
                                val accessToken = loginResponse?.accessToken

                                if (accessToken != null) {
                                    TokenManager.saveToken(requireContext(), accessToken)

                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(requireContext(), "회원가입 + 로그인 성공", Toast.LENGTH_SHORT).show()
                                        moveToMain()
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(requireContext(), "로그인 실패: 토큰 없음", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "로그인 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "예외 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            )

        }
    }

    private fun moveToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.putExtra("fromLogin", true) // ✅ 자동로그인 명시
        startActivity(intent)
        requireActivity().finish()
    }
}

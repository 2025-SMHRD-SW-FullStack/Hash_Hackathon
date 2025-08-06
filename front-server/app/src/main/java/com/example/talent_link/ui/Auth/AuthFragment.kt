package com.example.talent_link.ui.Auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.talent_link.AuthActivity
import com.example.talent_link.MainActivity
import com.example.talent_link.databinding.FragmentAuthBinding

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 자동 로그인 체크
        val token = requireContext()
            .getSharedPreferences("auth", Context.MODE_PRIVATE)
            .getString("accessToken", null)

        if (!token.isNullOrBlank()) {
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
            return
        }

        // ✅ 비회원 → 로그인 버튼
        binding.btnLocal.setOnClickListener {
            (activity as? AuthActivity)?.openLoginFragment()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

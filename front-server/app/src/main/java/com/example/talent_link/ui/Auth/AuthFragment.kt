package com.example.talent_link.ui.Auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.talent_link.AuthActivity
import com.example.talent_link.R
import com.example.talent_link.databinding.FragmentAuthBinding

class AuthFragment : Fragment() {

    // 생명주기 관리용
    // _binding이 실제 바인딩 객체, nullable로 선언한 이유: onDestroyView()을 null로 만들기 위해
    private var _binding: FragmentAuthBinding? = null

    // non-null로 안전하게 접근
    // 앱 코드에서 쉽게 쓰기 위한 non-null getter
    // !! : 개발자가 이 시점에서는 무조건 null 아님 확신
    private val binding get() = _binding!!

    // 화면 구성
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // ViewBinding 초기화
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 이벤트/로직 초기화
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 버튼 클릭 리스너 연결
        super.onViewCreated(view, savedInstanceState)

        binding.btnLocal.setOnClickListener {
            (activity as? AuthActivity)?.openLoginFragment()
        }
    }

    override fun onDestroyView() {
        // 메모리 해제(view 파괴)
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}

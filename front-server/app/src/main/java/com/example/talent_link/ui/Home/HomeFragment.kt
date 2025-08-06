package com.example.talent_link.ui.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talent_link.MainActivity
import com.example.talent_link.databinding.FragmentHomeBinding
import com.example.talent_link.ui.TalentSell.TalentSellFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 글쓰기 버튼 클릭 시 TalentSellFragment로 이동
        binding.fabWrite.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireActivity() as MainActivity).getFrameLayoutId(), TalentSellFragment())
                .addToBackStack(null)
                .commit()
        }

        // RecyclerView 기본 셋팅
        binding.rvPostList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPostList.adapter = HomePostAdapter(dummyPostList()) // 추후 API로 대체
    }

    private fun dummyPostList(): List<String> {
        return listOf("글1", "글2", "글3", "글4")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

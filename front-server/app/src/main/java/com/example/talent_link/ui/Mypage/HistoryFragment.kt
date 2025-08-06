package com.example.talent_link.ui.Mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.talent_link.R
import com.example.talent_link.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    lateinit var binding: FragmentHistoryBinding
    private var postType: String = "sale"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        // 전달받은 값 가져오기
//        postType = arguments?.getString("type") ?: "sale"
//
//        // 제목 변경
//        binding.tvTitle.text = if (postType == "sale") "판매 내역" else "구매 내역"
//
//        // RecyclerView 세팅
//        adapter = PostAdapter()
//        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
//        binding.rvHistory.adapter = adapter
//
//        // 데이터 불러오기 (샘플 데이터 or 서버 연동)
//        loadPostData()
//    }
//
//    private fun loadPostData() {
//        val dummyList = when (postType) {
//            "sale" -> listOf("판매글1", "판매글2")
//            else -> listOf("구매글1", "구매글2")
//        }
//
//        adapter.submitList(dummyList)
//    }

}
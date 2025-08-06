package com.example.talent_link.ui.Favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talent_link.R

class FavoriteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_favorite, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 더미 데이터
        val sampleList = arrayListOf(
            FavoriteVO(R.drawable.home_icon, "강아지 산책대행", "서구 쌍촌동", "15,000원"),
            FavoriteVO(R.drawable.love_icon, "아이 돌봄", "북구 용봉동", "10,000원")
        )

        val adapter = FavoriteAdapter(requireContext(), sampleList) { clickedItem ->
            // 상세화면 이동은 나중에
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.favoriteRecy)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }


}
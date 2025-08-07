package com.example.talent_link.ui.Favorite

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talent_link.R
import com.example.talent_link.ui.Favorite.dto.FavoriteDeleteRequest
import com.example.talent_link.ui.Favorite.dto.FavoriteRequest
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {

    private lateinit var adapter: FavoriteAdapter
    private val favoriteList = ArrayList<FavoriteVO>()
    private lateinit var userId : String // 실제 로그인 유저의 ID로 변경
    private lateinit var jwt : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = IdManager.getUserId(requireContext()).toString()
        jwt = "Bearer "+ TokenManager.getToken(requireContext()) ?: ""

        val recyclerView = view.findViewById<RecyclerView>(R.id.favoriteRecy)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = FavoriteAdapter(requireContext(), favoriteList, { clickedItem ->
            // 상세화면 이동 등
        }, { item, position ->
            // 하트 토글 처리 (서버 연동)
            if (item.favorite) {
                // 하트 해제 → 삭제 요청
                lifecycleScope.launch {
                    val userId = IdManager.getUserId(requireContext()).toString()
                    val deleteReq = FavoriteDeleteRequest(
                        userId = userId,
                        sellId = if(item.type == "sell") item.id else null,
                        buyId = if(item.type == "buy") item.id else null
                    )
                    val response = FavoriteRetrofitInstance.api.deleteFavorite(jwt, deleteReq)
                    if (response.isSuccessful) {
                        item.favorite = false
                        adapter.notifyItemChanged(position)
                    }
                }
            } else {
                // 하트 추가 → 저장 요청
                lifecycleScope.launch {
                    val request = FavoriteRequest(
                        id = if (item.type == "sell") item.id else null,
                        type = item.type,
                        userId = userId,
                        writerNickname = null,
                        buyId = if (item.type == "buy") item.id else null,
                        sellId = if (item.type == "sell") item.id else null
                    )
                    val response = FavoriteRetrofitInstance.api.addFavorite(jwt,request)
                    if (response.isSuccessful) {
                        item.favorite = true
                        adapter.notifyItemChanged(position)
                    }
                }
            }
        })

        recyclerView.adapter = adapter
        loadFavorites()
    }

    private fun loadFavorites() {
        Log.d("FavoriteFragment", "loadFavorites() 호출됨. userId=$userId")
        lifecycleScope.launch {
            val response = FavoriteRetrofitInstance.api.getFavoriteList(jwt,userId)
            Log.d("response",response.toString())
            if (response.isSuccessful) {
                val data = response.body() ?: emptyList()
                favoriteList.clear()
                favoriteList.addAll(data.map {
                    FavoriteVO(
                        id = it.id,
                        img = getIconResByType(it.type),
                        title = it.title,
                        local = "", // 필요하면 서버 데이터에 맞게 변경
                        price = "",
                        favorite = true,
                        type = it.type
                    )
                })
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun getIconResByType(type: String): Int {
        return when (type) {
            "buy" -> R.drawable.home_icon
            "sell" -> R.drawable.love_icon
            else -> R.drawable.ic_launcher_background
        }
    }
}

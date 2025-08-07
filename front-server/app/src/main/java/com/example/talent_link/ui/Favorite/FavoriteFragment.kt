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
import com.example.talent_link.MainActivity
import com.example.talent_link.R
import com.example.talent_link.ui.Favorite.dto.FavoriteDeleteRequest
import com.example.talent_link.ui.Favorite.dto.FavoriteRequest
import com.example.talent_link.ui.Home.HomeRetrofitInstance
import com.example.talent_link.ui.TalentSell.TalentSellDetailFragment
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {

    private lateinit var adapter: FavoritePostAdapter
    private val favoriteList = ArrayList<FavoriteVO>()
    private lateinit var userId : String
    private lateinit var jwt : String
    private lateinit var favoriteRecy: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = IdManager.getUserId(requireContext()).toString()
        jwt = "Bearer "+ (TokenManager.getAccessToken(requireContext()) ?: "")

        favoriteRecy = view.findViewById(R.id.favoriteRecy)
        favoriteRecy.layoutManager = LinearLayoutManager(context)

        adapter = FavoritePostAdapter(favoriteList, { clickedItem ->
            val fragment = TalentSellDetailFragment.newInstance(
                id = if(clickedItem.type == "sell") clickedItem.sellId!! else clickedItem.buyId!!,
                type = clickedItem.type
            )
            parentFragmentManager.beginTransaction()
                .replace((requireActivity() as MainActivity).getFrameLayoutId(), fragment)
                .addToBackStack(null)
                .commit()
        }, { item, position ->
            // 관심 목록에서 제거
            lifecycleScope.launch {
                val deleteReq = FavoriteDeleteRequest(
                    userId = userId,
                    sellId = if(item.type == "sell") item.sellId else null,
                    buyId = if(item.type == "buy") item.buyId else null
                )
                val response = FavoriteRetrofitInstance.api.deleteFavorite(jwt, deleteReq)
                if (response.isSuccessful) {
                    favoriteList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    adapter.notifyItemRangeChanged(position, favoriteList.size)
                }
            }
        })

        favoriteRecy.adapter = adapter
        loadFavorites()
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            try {
                // 1. 내가 즐겨찾기한 목록 가져오기
                val favResponse = FavoriteRetrofitInstance.api.getFavoriteList(jwt, userId)
                if (!favResponse.isSuccessful) return@launch

                val favData = favResponse.body() ?: emptyList()
                val newFavoriteList = mutableListOf<FavoriteVO>()

                // 2. 각 즐겨찾기 항목의 상세 정보 가져오기
                for (fav in favData) {
                    val vo = if (fav.type == "sell" && fav.sellId != null) {
                        val detail = HomeRetrofitInstance.api.getTalentSellDetail(fav.sellId, jwt)
                        FavoriteVO(
                            id = fav.id,
                            imageUrl = detail.imageUrl,
                            title = detail.title,
                            location = "지역 정보", // API 응답에 지역 정보가 없으므로 임시값 사용
                            price = "₩${detail.price}",
                            isFavorite = true,
                            type = "sell",
                            sellId = fav.sellId,
                            buyId = null
                        )
                    } else if (fav.type == "buy" && fav.buyId != null) {
                        val detail = HomeRetrofitInstance.api.getTalentBuyDetail(fav.buyId, jwt)
                        FavoriteVO(
                            id = fav.id,
                            imageUrl = detail.imageUrl,
                            title = detail.title,
                            location = "지역 정보",
                            price = "희망가: ₩${detail.budget}",
                            isFavorite = true,
                            type = "buy",
                            sellId = null,
                            buyId = fav.buyId
                        )
                    } else {
                        null
                    }
                    vo?.let { newFavoriteList.add(it) }
                }

                // 3. 어댑터에 데이터 반영
                favoriteList.clear()
                favoriteList.addAll(newFavoriteList)
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Log.e("FavoriteFragment", "관심 목록 로딩 실패", e)
            }
        }
    }
}
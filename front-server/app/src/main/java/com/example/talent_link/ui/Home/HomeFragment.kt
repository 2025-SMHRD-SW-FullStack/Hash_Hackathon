package com.example.talent_link.ui.Home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talent_link.MainActivity
import com.example.talent_link.databinding.FragmentHomeBinding
import com.example.talent_link.ui.Favorite.dto.FavoriteDeleteRequest
import com.example.talent_link.ui.Favorite.dto.FavoriteRequest
import com.example.talent_link.ui.Favorite.FavoriteRetrofitInstance
import com.example.talent_link.ui.Home.dto.PostType
import com.example.talent_link.ui.Home.dto.toUiModel
import com.example.talent_link.ui.TalentSell.TalentSellDetailActivity
import com.example.talent_link.ui.TalentSell.TalentSellFragment
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HomePostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabWrite.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireActivity() as MainActivity).getFrameLayoutId(), TalentSellFragment())
                .addToBackStack(null)
                .commit()
        }

        adapter = HomePostAdapter(
            emptyList(),
            onItemClick = { post ->
                if (post.type == PostType.SELL) {
                    val intent = Intent(requireContext(), TalentSellDetailActivity::class.java)
                    intent.putExtra("id", post.id)
                    startActivity(intent)
                }
            },
            onFavoriteClick = { post, position ->
                val jwt = "Bearer "+ TokenManager.getToken(requireContext()) ?: ""
                val userId = IdManager.getUserId(requireContext()).toString()
                viewLifecycleOwner.lifecycleScope.launch {
                    if (post.isFavorite) {
                        // 즐겨찾기 해제
                        val deleteReq = FavoriteDeleteRequest(
                            userId = userId,
                            sellId = if (post.type == PostType.SELL) post.id else null,
                            buyId = if (post.type == PostType.BUY) post.id else null
                        )
                        FavoriteRetrofitInstance.api.deleteFavorite(jwt, deleteReq)
                        post.isFavorite = false
//                        adapter.notifyItemChanged(position)
                    } else {
                        // 즐겨찾기 추가
                        val req = FavoriteRequest(
                            id = if (post.type == PostType.SELL) post.id else null,
                            type = post.type.name.lowercase(),
                            userId = userId,
                            writerNickname = post.writerNickname,
                            buyId = if (post.type == PostType.BUY) post.id else null,
                            sellId = if (post.type == PostType.SELL) post.id else null
                        )
                        FavoriteRetrofitInstance.api.addFavorite(jwt, req)
                        post.isFavorite = true
                    }
                    adapter.notifyItemChanged(position)
                }
            }
        )
        binding.rvPostList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPostList.adapter = adapter

        loadAllPostsAndFavorites()
    }

    private fun loadAllPostsAndFavorites() {
        val jwt = "Bearer "+ TokenManager.getToken(requireContext()) ?: ""
        val userId = IdManager.getUserId(requireContext()).toString()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sellList = HomeRetrofitInstance.api.getTalentSellList(jwt)
                val buyList = HomeRetrofitInstance.api.getTalentBuyList(jwt)
                val favoriteResponse = FavoriteRetrofitInstance.api.getFavoriteList(jwt, userId)
                // 즐겨찾기 대상 글 id로 Set 만들기 (핵심!)
                val favoriteIds = favoriteResponse.body()
                    ?.mapNotNull { it.sellId ?: it.buyId }
                    ?.toSet() ?: emptySet()

                // 시간 포맷팅(초까지)
                val formatterIn = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                val formatterOut = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                val combinedList = (sellList.map { it.toUiModel() } + buyList.map { it.toUiModel() })
                    .onEach { it.isFavorite = favoriteIds.contains(it.id) }
                    .onEach { post ->
                        // 시간 포맷 바꿔서 저장
                        try {
                            post.createdAt = LocalDateTime.parse(post.createdAt, formatterIn).format(formatterOut)
                        } catch (_: Exception) { }
                    }
                    .sortedByDescending { LocalDateTime.parse(it.createdAt, formatterOut) }

                adapter.updateList(combinedList)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "글 목록 불러오기 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.talent_link.ui.Home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talent_link.MainActivity
import com.example.talent_link.R
import com.example.talent_link.databinding.FragmentHomeBinding
import com.example.talent_link.ui.Favorite.dto.FavoriteDeleteRequest
import com.example.talent_link.ui.Favorite.dto.FavoriteRequest
import com.example.talent_link.ui.Favorite.FavoriteRetrofitInstance
import com.example.talent_link.ui.Home.dto.HomePostUiModel
import com.example.talent_link.ui.Home.dto.PostType
import com.example.talent_link.ui.Home.dto.toUiModel
import com.example.talent_link.ui.TalentSell.TalentSellDetailFragment
import com.example.talent_link.ui.TalentSell.TalentPostFragment
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HomePostAdapter

    // '전체' 게시물 리스트를 저장할 변수
    private var combinedList = listOf<HomePostUiModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupRecyclerView()
        loadAllPostsAndFavorites()
    }

    // 클릭 리스너 설정
    private fun setupClickListeners() {
        binding.fabWrite.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireActivity() as MainActivity).getFrameLayoutId(), TalentPostFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnAll.setOnClickListener {
            filterPosts(null) // type이 null이면 전체
            updateButtonUI("all")
        }

        binding.btnSell.setOnClickListener {
            filterPosts(PostType.SELL) // 팝니다
            updateButtonUI("sell")
        }

        binding.btnBuy.setOnClickListener {
            filterPosts(PostType.BUY) // 삽니다
            updateButtonUI("buy")
        }
    }

    // 리사이클러뷰 설정
    private fun setupRecyclerView() {
        adapter = HomePostAdapter(
            emptyList(),
            onItemClick = { post ->
                val fragment = TalentSellDetailFragment.newInstance(post.id, post.type.name.lowercase())
                parentFragmentManager.beginTransaction()
                    .replace((requireActivity() as MainActivity).getFrameLayoutId(), fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onFavoriteClick = { post, position ->
                val jwt = "Bearer " + TokenManager.getAccessToken(requireContext()).orEmpty()
                val userId = IdManager.getUserId(requireContext()).toString()
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        if (post.isFavorite) {
                            // 즐겨찾기 해제
                            val deleteReq = FavoriteDeleteRequest(
                                userId = userId,
                                sellId = if (post.type == PostType.SELL) post.id else null,
                                buyId = if (post.type == PostType.BUY) post.id else null
                            )
                            FavoriteRetrofitInstance.api.deleteFavorite(jwt, deleteReq)
                            post.isFavorite = false
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
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "관심 목록 변경 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        binding.rvPostList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPostList.adapter = adapter
    }

    // 서버에서 모든 데이터 로드
    private fun loadAllPostsAndFavorites() {
        val jwt = "Bearer " + TokenManager.getAccessToken(requireContext()).orEmpty()
        val userId = IdManager.getUserId(requireContext()).toString()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 서버로부터 데이터 가져오기
                val sellList = HomeRetrofitInstance.api.getTalentSellList(jwt)
                val buyList = HomeRetrofitInstance.api.getTalentBuyList(jwt)
                val favoriteResponse = FavoriteRetrofitInstance.api.getFavoriteList(jwt, userId)
                val favoriteList = favoriteResponse.body() ?: emptyList()

                val favoriteKeySet = favoriteList.mapNotNull {
                    when {
                        it.type == "sell" && it.sellId != null -> "sell-${it.sellId}"
                        it.type == "buy" && it.buyId != null -> "buy-${it.buyId}"
                        else -> null
                    }
                }.toSet()

                val formatterIn = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                val formatterOut = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                // '팝니다'와 '삽니다' 리스트를 합치고, 관심 여부와 시간 포맷을 설정
                combinedList = (sellList.map { it.toUiModel() } + buyList.map { it.toUiModel() })
                    .onEach { post ->
                        val key = "${post.type.name.lowercase()}-${post.id}"
                        post.isFavorite = favoriteKeySet.contains(key)
                        try {
                            post.createdAt = LocalDateTime.parse(post.createdAt, formatterIn).format(formatterOut)
                        } catch (_: Exception) { }
                    }
                    .sortedByDescending { LocalDateTime.parse(it.createdAt, formatterOut) }

                // 처음에는 전체 목록을 보여줌
                filterPosts(null)
                updateButtonUI("all") // 기본으로 '전체' 버튼 활성화
            } catch (e: Exception) {
                Log.e("HomeFragment", "데이터 로드 실패", e)
                Toast.makeText(requireContext(), "글 목록을 불러오는데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 게시물 리스트 필터링
    private fun filterPosts(type: PostType?) {
        val filteredList = if (type == null) {
            combinedList // type이 null이면 전체 목록
        } else {
            combinedList.filter { it.type == type } // '삽니다' 또는 '팝니다' 필터링
        }
        adapter.updateList(filteredList)
    }

    // 버튼 UI 업데이트
    private fun updateButtonUI(selectedType: String) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.market_green)
        val defaultColor = Color.WHITE
        val selectedTextColor = Color.WHITE
        val defaultTextColor = Color.parseColor("#5D5D5D")

        binding.btnAll.apply {
            background.setTint(if (selectedType == "all") selectedColor else defaultColor)
            setTextColor(if (selectedType == "all") selectedTextColor else defaultTextColor)
        }
        binding.btnSell.apply {
            background.setTint(if (selectedType == "sell") selectedColor else defaultColor)
            setTextColor(if (selectedType == "sell") selectedTextColor else defaultTextColor)
        }
        binding.btnBuy.apply {
            background.setTint(if (selectedType == "buy") selectedColor else defaultColor)
            setTextColor(if (selectedType == "buy") selectedTextColor else defaultTextColor)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.talent_link.ui.Home

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talent_link.MainActivity
import com.example.talent_link.NoNavActivity
import com.example.talent_link.R
import com.example.talent_link.databinding.FragmentHomeBinding
import com.example.talent_link.ui.Favorite.dto.FavoriteDeleteRequest
import com.example.talent_link.ui.Favorite.dto.FavoriteRequest
import com.example.talent_link.ui.Favorite.FavoriteRetrofitInstance
import com.example.talent_link.ui.Home.dto.HomePostUiModel
import com.example.talent_link.ui.Home.dto.PostType
import com.example.talent_link.ui.Home.dto.toUiModel
import com.example.talent_link.ui.TalentPost.TalentPostDetailFragment
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HomePostAdapter
    private var selectedLocationIndex = 0

    private var combinedList = listOf<HomePostUiModel>()

    // ✅ 글쓰기/수정 후 결과를 받아 목록을 새로고침하기 위한 Launcher
    private val writePostLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 글 작성이 성공적으로 완료되면 목록을 새로고침합니다.
            loadAllPostsAndFavorites()
        }
    }

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
        setupLocationSelector()
    }

    private fun setupClickListeners() {
        binding.fabWrite.setOnClickListener {
            val intent = Intent(requireContext(), NoNavActivity::class.java).apply {
                putExtra(NoNavActivity.EXTRA_FRAGMENT_TYPE, NoNavActivity.TYPE_TALENT_POST)
            }
            // ✅ startActivity 대신 launcher를 사용해 Activity를 시작합니다.
            writePostLauncher.launch(intent)
        }

        binding.btnAll.setOnClickListener {
            filterPosts(null)
            updateButtonUI("all")
        }

        binding.btnSell.setOnClickListener {
            filterPosts(PostType.SELL)
            updateButtonUI("sell")
        }

        binding.btnBuy.setOnClickListener {
            filterPosts(PostType.BUY)
            updateButtonUI("buy")
        }
    }

    private fun setupRecyclerView() {
        adapter = HomePostAdapter(
            emptyList(),
            onItemClick = { post ->
                val intent = Intent(requireContext(), NoNavActivity::class.java).apply {
                    putExtra(NoNavActivity.EXTRA_FRAGMENT_TYPE, NoNavActivity.TYPE_TALENT_DETAIL)
                    putExtra("id", post.id)
                    putExtra("type", post.type.name.lowercase())
                }
                startActivity(intent)
            },
            onFavoriteClick = { post, position ->
                val jwt = "Bearer " + TokenManager.getAccessToken(requireContext()).orEmpty()
                val userId = IdManager.getUserId(requireContext()).toString()
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        if (post.isFavorite) {
                            val deleteReq = FavoriteDeleteRequest(
                                userId = userId,
                                sellId = if (post.type == PostType.SELL) post.id else null,
                                buyId = if (post.type == PostType.BUY) post.id else null
                            )
                            FavoriteRetrofitInstance.api.deleteFavorite(jwt, deleteReq)
                            post.isFavorite = false
                        } else {
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

    private fun loadAllPostsAndFavorites() {
        val jwt = "Bearer " + TokenManager.getAccessToken(requireContext()).orEmpty()
        val userId = IdManager.getUserId(requireContext()).toString()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
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

                combinedList = (sellList.map { it.toUiModel() } + buyList.map { it.toUiModel() })
                    .onEach { post ->
                        val key = "${post.type.name.lowercase()}-${post.id}"
                        post.isFavorite = favoriteKeySet.contains(key)
                        try {
                            post.createdAt = LocalDateTime.parse(post.createdAt, formatterIn)
                                .format(formatterOut)
                        } catch (_: Exception) {
                        }
                    }
                    .sortedByDescending { LocalDateTime.parse(it.createdAt, formatterOut) }

                filterPosts(null)
                updateButtonUI("all")
            } catch (e: Exception) {
                Log.e("HomeFragment", "데이터 로드 실패", e)
                Toast.makeText(
                    requireContext(),
                    "글 목록을 불러오는데 실패했습니다: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun filterPosts(type: PostType?) {
        val filteredList = if (type == null) {
            combinedList
        } else {
            combinedList.filter { it.type == type }
        }
        adapter.updateList(filteredList)
    }

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

    private fun setupLocationSelector() {
        binding.locationSelectorLayout.setOnClickListener { anchorView ->
            // 1. 커스텀 레이아웃을 inflate 합니다.
            val inflater = LayoutInflater.from(requireContext())
            val popupView = inflater.inflate(R.layout.popup_location_menu, null)

            // 2. PopupWindow를 생성합니다.
            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true // 포커스를 받을 수 있도록 설정
            )
            popupWindow.elevation = 8f // 그림자 효과

            // 3. 각 메뉴 항목(LinearLayout)과 내부의 TextView, ImageView를 찾습니다.
            val optionChungjang = popupView.findViewById<LinearLayout>(R.id.option_chungjang)
            val textChungjang = popupView.findViewById<TextView>(R.id.text_chungjang)
            val checkChungjang = popupView.findViewById<ImageView>(R.id.check_chungjang)

            val optionGwangju = popupView.findViewById<LinearLayout>(R.id.option_gwangju)
            val textGwangju = popupView.findViewById<TextView>(R.id.text_gwangju)
            val checkGwangju = popupView.findViewById<ImageView>(R.id.check_gwangju)

            val optionAll = popupView.findViewById<LinearLayout>(R.id.option_all)
            val textAll = popupView.findViewById<TextView>(R.id.text_all)
            val checkAll = popupView.findViewById<ImageView>(R.id.check_all)

            val allTextViews = listOf(textChungjang, textGwangju, textAll)
            val allCheckImageViews = listOf(checkChungjang, checkGwangju, checkAll)

            // 4. 현재 선택된 항목에 따라 UI를 미리 설정합니다.
            allTextViews.forEachIndexed { index, textView ->
                if (index == selectedLocationIndex) {
                    textView.typeface = Typeface.DEFAULT_BOLD // 선택된 항목은 굵게
                    textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.market_green))
                    allCheckImageViews[index].visibility = View.VISIBLE // 체크 아이콘 보이기
                } else {
                    textView.typeface = Typeface.DEFAULT // 선택 안된 항목은 보통 굵기
                    textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    allCheckImageViews[index].visibility = View.GONE // 체크 아이콘 숨기기
                }
            }

            // 5. 각 항목에 대한 클릭 리스너를 설정합니다.
            val locations = arrayOf("충장동", "광주광역시", "전국", "동네 설정")
            optionChungjang.setOnClickListener {
                selectedLocationIndex = 0
                binding.tvCurrentLocation.text = locations[0]
                popupWindow.dismiss()
            }
            optionGwangju.setOnClickListener {
                selectedLocationIndex = 1
                binding.tvCurrentLocation.text = locations[1]
                popupWindow.dismiss()
            }
            optionAll.setOnClickListener {
                selectedLocationIndex = 2
                binding.tvCurrentLocation.text = locations[2]
                popupWindow.dismiss()
            }
            optionAll.setOnClickListener {
                selectedLocationIndex = 3
                binding.tvCurrentLocation.text = locations[3]
                popupWindow.dismiss()
            }


            // 6. PopupWindow를 가운데에 위치시킵니다.
            popupWindow.showAsDropDown(anchorView, 0, 0, Gravity.CENTER)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
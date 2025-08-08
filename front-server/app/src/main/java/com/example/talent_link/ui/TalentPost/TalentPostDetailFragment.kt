package com.example.talent_link.ui.TalentPost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.talent_link.Chat.ChatRetrofitInstance
import com.example.talent_link.R
import com.example.talent_link.databinding.FragmentTalentPostDetailBinding
import com.example.talent_link.ui.Chat.ChatRoomFragment
import com.example.talent_link.ui.Chat.dto.CreateChatRoomRequest
import com.example.talent_link.ui.Favorite.FavoriteRetrofitInstance
import com.example.talent_link.ui.Favorite.dto.FavoriteDeleteRequest
import com.example.talent_link.ui.Favorite.dto.FavoriteRequest
import com.example.talent_link.ui.Home.HomeRetrofitInstance
import com.example.talent_link.ui.Home.SharedFavoriteViewModel
import com.example.talent_link.ui.Home.dto.TalentBuyResponse
import com.example.talent_link.ui.Home.dto.TalentSellResponse
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.launch

class TalentPostDetailFragment : Fragment() {

    private var _binding: FragmentTalentPostDetailBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedFavoriteViewModel by activityViewModels()

    private var isFavorite: Boolean = false
    private var postId: Long = -1L
    private var type: String = "sell"

    private var lastLoadedDetail: Any? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTalentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postId = arguments?.getLong("id") ?: -1L
        type = arguments?.getString("type") ?: "sell"

        if (postId == -1L) {
            Toast.makeText(requireContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            activity?.onBackPressedDispatcher?.onBackPressed()
            return
        }

        checkFavoriteStatus(postId, type)
        loadDetail(postId, type)

        binding.imgDetailFavorite.setOnClickListener {
            if (isFavorite) {
                removeFavorite(postId, type)
            } else {
                addFavorite(postId, type)
            }
        }

        binding.btnChat.setOnClickListener {
            openOrCreateChatRoom()
        }

        binding.toolbarDetail.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit_post -> {
                    // TODO: 게시글 수정 로직 실행 (예: 수정 화면으로 이동)
                    Toast.makeText(requireContext(), "게시글 수정 선택", Toast.LENGTH_SHORT).show()
                    true // 이벤트 처리를 완료했음을 의미
                }
                R.id.menu_delete_post -> {
                    // TODO: 게시글 삭제 로직 실행 (예: 삭제 확인 다이얼로그 띄우기)
                    Toast.makeText(requireContext(), "게시글 삭제 선택", Toast.LENGTH_SHORT).show()
                    true // 이벤트 처리를 완료했음을 의미
                }
                else -> false // 다른 메뉴 아이템은 처리하지 않음
            }
        }
    }

    private fun loadDetail(id: Long, type: String) {
        lifecycleScope.launch {
            try {
                val jwt = "Bearer " + TokenManager.getAccessToken(requireContext())

                if (type == "sell") {
                    val detail = HomeRetrofitInstance.api.getTalentSellDetail(id, jwt)
                    lastLoadedDetail = detail
                    bindSellDetail(detail)
                } else { // "buy"
                    val detail = HomeRetrofitInstance.api.getTalentBuyDetail(id, jwt)
                    lastLoadedDetail = detail
                    bindBuyDetail(detail)
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "불러오기 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
    }

    private fun bindSellDetail(detail: TalentSellResponse) {
        binding.tvDetailTitle.text = detail.title
        binding.tvDetailContent.text = detail.description
        binding.tvDetailPrice.text = "₩${detail.price}"
        binding.tvDetailInfo.text = "${detail.writerNickname} · ${detail.createdAt}"
        updateImage(detail.imageUrl)
    }

    private fun bindBuyDetail(detail: TalentBuyResponse) {
        binding.tvDetailTitle.text = detail.title
        binding.tvDetailContent.text = detail.description
        binding.tvDetailPrice.text = "희망가: ₩${detail.budget}"
        binding.tvDetailInfo.text = "${detail.writerNickname} · ${detail.createdAt}"
        updateImage(detail.imageUrl)
    }

    private fun updateImage(imageUrl: String?) {
        if (imageUrl.isNullOrBlank()) {
            binding.imgDetailPost.visibility = View.GONE
        } else {
            binding.imgDetailPost.visibility = View.VISIBLE
            Glide.with(requireContext()).load(imageUrl).into(binding.imgDetailPost)
        }
    }

    private fun checkFavoriteStatus(postId: Long, type: String) {
        val jwt = "Bearer " + TokenManager.getAccessToken(requireContext())
        val userId = IdManager.getUserId(requireContext()).toString()
        lifecycleScope.launch {
            try {
                val response = FavoriteRetrofitInstance.api.getFavoriteList(jwt, userId)
                val favoriteList = response.body() ?: emptyList()
                isFavorite = favoriteList.any {
                    (type == "sell" && it.sellId == postId) ||
                            (type == "buy" && it.buyId == postId)
                }
                updateFavoriteIcon()
            } catch (_: Exception) { }
        }
    }

    private fun addFavorite(postId: Long, type: String) {
        val jwt = "Bearer " + TokenManager.getAccessToken(requireContext())
        val userId = IdManager.getUserId(requireContext()).toString()
        lifecycleScope.launch {
            try {
                val req = FavoriteRequest(
                    id = if (type == "sell") postId else null,
                    type = type,
                    userId = userId,
                    writerNickname = null,
                    buyId = if (type == "buy") postId else null,
                    sellId = if (type == "sell") postId else null
                )
                FavoriteRetrofitInstance.api.addFavorite(jwt, req)
                isFavorite = true
                updateFavoriteIcon()
                sharedViewModel.notifyFavoriteChanged(postId, type)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "즐겨찾기 추가 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeFavorite(postId: Long, type: String) {
        val jwt = "Bearer " + TokenManager.getAccessToken(requireContext())
        val userId = IdManager.getUserId(requireContext()).toString()
        lifecycleScope.launch {
            try {
                val req = FavoriteDeleteRequest(
                    userId = userId,
                    sellId = if (type == "sell") postId else null,
                    buyId = if (type == "buy") postId else null
                )
                FavoriteRetrofitInstance.api.deleteFavorite(jwt, req)
                isFavorite = false
                updateFavoriteIcon()
                sharedViewModel.notifyFavoriteChanged(postId, type)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "즐겨찾기 삭제 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteIcon() {
        if (isFavorite) {
            binding.imgDetailFavorite.setImageResource(R.drawable.love_icon)
        } else {
            binding.imgDetailFavorite.setImageResource(R.drawable.love_icon_outline)
        }
    }

    private fun openOrCreateChatRoom() {
        val myUserId = IdManager.getUserId(requireContext())
        val jwt = "Bearer " + TokenManager.getAccessToken(requireContext())
        lifecycleScope.launch {
            try {
                val opponentId = getOpponentIdFromDetail()
                if (opponentId == myUserId) {
                    Toast.makeText(requireContext(), "본인 글에는 채팅할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val req = CreateChatRoomRequest(
                    myUserId = myUserId,
                    opponentUserId = opponentId
                )
                val res = ChatRetrofitInstance.api.createOrEnterRoom(req, jwt)
                goToChatRoom(res.roomId)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "채팅방 생성 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ 상대방 ID 추출 로직 수정
    private fun getOpponentIdFromDetail(): Long {
        return when (val detail = lastLoadedDetail) {
            is TalentSellResponse -> detail.writerId
            // ✅ "삽니다" 게시글에서도 writerId를 정상적으로 가져오도록 수정
            is TalentBuyResponse -> detail.writerId
            else -> throw IllegalStateException("작성자 정보 없음")
        }
    }

    private fun goToChatRoom(roomId: Long) {
        val bundle = Bundle().apply { putLong("roomId", roomId) }
        val chatRoomFragment = ChatRoomFragment()
        chatRoomFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame, chatRoomFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(id: Long, type: String): TalentPostDetailFragment {
            val fragment = TalentPostDetailFragment()
            val args = Bundle()
            args.putLong("id", id)
            args.putString("type", type)
            fragment.arguments = args
            return fragment
        }
    }
}
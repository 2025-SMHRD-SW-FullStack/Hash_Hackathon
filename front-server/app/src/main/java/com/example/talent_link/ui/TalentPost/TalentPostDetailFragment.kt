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

    // ⭐️ 상세 데이터 저장을 위해 Any? 타입으로 변경
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
        loadDetail(postId, type) // ⭐️ type 인자 추가

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
    }

    // ✅ 게시글 상세 정보 로딩 로직 수정
    private fun loadDetail(id: Long, type: String) {
        lifecycleScope.launch {
            try {
                val jwt = "Bearer " + TokenManager.getAccessToken(requireContext())

                if (type == "sell") {
                    val detail = HomeRetrofitInstance.api.getTalentSellDetail(id, jwt)
                    lastLoadedDetail = detail // ⭐️ 불러온 데이터 저장
                    bindSellDetail(detail)
                } else { // "buy"
                    val detail = HomeRetrofitInstance.api.getTalentBuyDetail(id, jwt)
                    lastLoadedDetail = detail // ⭐️ 불러온 데이터 저장
                    bindBuyDetail(detail)
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "불러오기 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
    }

    // ✅ "팝니다" 게시글 UI 바인딩
    private fun bindSellDetail(detail: TalentSellResponse) {
        binding.tvDetailTitle.text = detail.title
        binding.tvDetailContent.text = detail.description
        binding.tvDetailPrice.text = "₩${detail.price}"
        binding.tvDetailInfo.text = "${detail.writerNickname} · ${detail.createdAt}"
        updateImage(detail.imageUrl)
    }

    // ✅ "삽니다" 게시글 UI 바인딩
    private fun bindBuyDetail(detail: TalentBuyResponse) {
        binding.tvDetailTitle.text = detail.title
        binding.tvDetailContent.text = detail.description
        binding.tvDetailPrice.text = "희망가: ₩${detail.budget}"
        binding.tvDetailInfo.text = "${detail.writerNickname} · ${detail.createdAt}"
        updateImage(detail.imageUrl)
    }

    // ✅ 이미지 UI 업데이트 공통 로직
    private fun updateImage(imageUrl: String?) {
        if (imageUrl.isNullOrBlank()) {
            binding.imgDetailPost.visibility = View.GONE
        } else {
            binding.imgDetailPost.visibility = View.VISIBLE
            Glide.with(requireContext()).load(imageUrl).into(binding.imgDetailPost)
        }
    }


    // 1. 즐겨찾기 상태 조회
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

    // 2. 즐겨찾기 추가
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

    // 3. 즐겨찾기 삭제
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

    // ✅ 채팅방 생성/입장 로직 수정
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
            is TalentBuyResponse -> {
                // TalentBuyResponse에는 writerId가 없으므로, writerNickname을 통해 User를 찾아야 합니다.
                // 이 예제에서는 TalentSellResponse와 동일하게 writerId가 있다고 가정합니다.
                // 실제로는 TalentBuyResponse에도 writerId 필드를 추가해야 합니다.
                // 현재 구조에서는 임시로 -1L을 반환합니다.
                -1L //TODO: TalentBuyResponse에 writerId 추가 후 수정 필요
            }
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
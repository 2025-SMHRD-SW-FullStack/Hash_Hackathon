package com.example.talent_link.ui.TalentSell

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
import com.example.talent_link.databinding.FragmentTalentSellDetailBinding
import com.example.talent_link.ui.Chat.ChatRoomFragment
import com.example.talent_link.ui.Chat.dto.CreateChatRoomRequest
import com.example.talent_link.ui.Favorite.FavoriteRetrofitInstance
import com.example.talent_link.ui.Favorite.dto.FavoriteDeleteRequest
import com.example.talent_link.ui.Favorite.dto.FavoriteRequest
import com.example.talent_link.ui.Home.HomeRetrofitInstance
import com.example.talent_link.ui.Home.SharedFavoriteViewModel
import com.example.talent_link.ui.Home.dto.TalentSellResponse // ← 이거 잊지 말기
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.launch

class TalentSellDetailFragment : Fragment() {

    private var _binding: FragmentTalentSellDetailBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedFavoriteViewModel by activityViewModels()

    private var isFavorite: Boolean = false

    private var postId: Long = -1L
    private var type: String = "sell"

    // ⭐️ 상세 데이터 임시 저장 (작성자ID 사용 위해)
    private var lastLoadedDetail: TalentSellResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTalentSellDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // arguments에서 데이터 추출
        postId = arguments?.getLong("id") ?: -1L
        type = arguments?.getString("type") ?: "sell"

        if (postId == -1L) {
            Toast.makeText(requireContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return
        }

        checkFavoriteStatus(postId, type)
        loadDetail(postId)

        binding.imgDetailFavorite.setOnClickListener {
            if (isFavorite) {
                removeFavorite(postId, type)
            } else {
                addFavorite(postId, type)
            }
        }

        // 💬 1:1 채팅하기 버튼
        binding.btnChat.setOnClickListener {
            openOrCreateChatRoom()
        }
    }

    // 글 상세조회 로직
    private fun loadDetail(id: Long) {
        lifecycleScope.launch {
            try {
                val jwt = "Bearer " + TokenManager.getAccessToken(requireContext())
                // 게시글 전체 리스트에서 해당 id의 게시글만 찾음
                val detail = HomeRetrofitInstance.api.getTalentSellList(jwt).find { it.id == id }
                if (detail != null) {
                    lastLoadedDetail = detail  // ⭐️ 여기서 저장!

                    binding.tvDetailTitle.text = detail.title
                    binding.tvDetailContent.text = detail.description
                    binding.tvDetailPrice.text = "₩${detail.price}"
                    binding.tvDetailInfo.text = "${detail.writerNickname} · ${detail.createdAt}"
                    val imageUrl = detail.imageUrl
                    if (imageUrl.isNullOrBlank()) {
                        binding.imgDetailPost.visibility = View.GONE
                    } else {
                        binding.imgDetailPost.visibility = View.VISIBLE
                        Glide.with(requireContext()).load(imageUrl).into(binding.imgDetailPost)
                    }
                } else {
                    Toast.makeText(requireContext(), "글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "불러오기 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
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

    // 하트 모양 UI 반영
    private fun updateFavoriteIcon() {
        if (isFavorite) {
            binding.imgDetailFavorite.setImageResource(R.drawable.love_icon)
        } else {
            binding.imgDetailFavorite.setImageResource(R.drawable.love_icon_outline)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(id: Long, type: String): TalentSellDetailFragment {
            val fragment = TalentSellDetailFragment()
            val args = Bundle()
            args.putLong("id", id)
            args.putString("type", type)
            fragment.arguments = args
            return fragment
        }
    }

    // 💬 채팅방 생성/입장
    private fun openOrCreateChatRoom() {
        val myUserId = IdManager.getUserId(requireContext())
        val jwt = "Bearer " + TokenManager.getAccessToken(requireContext())
        lifecycleScope.launch {
            try {
                val sellerId = getSellerIdFromDetail() // ⭐️ 상세 조회한 데이터에서 sellerId 추출
                if (sellerId == myUserId) {
                    Toast.makeText(requireContext(), "본인 글에는 채팅할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val req = CreateChatRoomRequest(
                    myUserId = myUserId,
                    opponentUserId = sellerId
                )
                val res = ChatRetrofitInstance.api.createOrEnterRoom(req, jwt)
                goToChatRoom(res.roomId)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "채팅방 생성 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // sellerId(작성자ID) 추출
    private fun getSellerIdFromDetail(): Long {
        return lastLoadedDetail?.writerId
            ?: throw IllegalStateException("작성자 정보 없음 (서버에서 writerId 응답 필수!)")
    }

    // 채팅방 화면으로 이동
    private fun goToChatRoom(roomId: Long) {
        val bundle = Bundle().apply { putLong("roomId", roomId) }
        val chatRoomFragment = ChatRoomFragment()
        chatRoomFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame, chatRoomFragment) // 🟢 컨테이너 id 맞게!
            .addToBackStack(null)
            .commit()
    }
}

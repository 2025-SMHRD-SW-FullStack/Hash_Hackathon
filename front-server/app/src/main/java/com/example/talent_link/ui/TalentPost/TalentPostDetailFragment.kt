package com.example.talent_link.ui.TalentPost

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.talent_link.Chat.ChatRetrofitInstance
import com.example.talent_link.NoNavActivity
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
    private var postWriterId: Long = -1L
    private var type: String = "sell"

    private var lastLoadedDetail: Any? = null

    // ✅ 수정 완료 후 화면을 새로고침하기 위한 Launcher
    private val editPostLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 수정이 성공적으로 완료되면 상세 정보를 다시 불러옵니다.
            loadDetail(postId, type)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        setupToolbar()
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
    }

    private fun setupToolbar() {
        binding.toolbarDetail.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.toolbarDetail.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit_post -> {
                    navigateToEdit()
                    true
                }
                R.id.menu_delete_post -> {
                    showDeleteConfirmDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadDetail(id: Long, type: String) {
        lifecycleScope.launch {
            try {
                val jwt = "Bearer " + TokenManager.getAccessToken(requireContext())
                val myUserId = IdManager.getUserId(requireContext())

                if (type == "sell") {
                    val detail = HomeRetrofitInstance.api.getTalentSellDetail(id, jwt)
                    lastLoadedDetail = detail
                    postWriterId = detail.writerId
                    bindSellDetail(detail)
                } else { // "buy"
                    val detail = HomeRetrofitInstance.api.getTalentBuyDetail(id, jwt)
                    lastLoadedDetail = detail
                    postWriterId = detail.writerId
                    bindBuyDetail(detail)
                }

                binding.toolbarDetail.menu.findItem(R.id.menu_edit_post).isVisible = (myUserId == postWriterId)
                binding.toolbarDetail.menu.findItem(R.id.menu_delete_post).isVisible = (myUserId == postWriterId)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "불러오기 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
    }

    private fun navigateToEdit() {
        val bundle = Bundle()
        bundle.putString("mode", "edit")
        bundle.putString("type", type)

        when (val detail = lastLoadedDetail) {
            is TalentSellResponse -> {
                bundle.putLong("id", detail.id)
                bundle.putString("title", detail.title)
                bundle.putString("description", detail.description)
                bundle.putInt("price", detail.price)
                bundle.putString("imageUrl", detail.imageUrl)
            }
            is TalentBuyResponse -> {
                bundle.putLong("id", detail.id)
                bundle.putString("title", detail.title)
                bundle.putString("description", detail.description)
                bundle.putInt("price", detail.budget)
                bundle.putString("imageUrl", detail.imageUrl)
            }
        }

        val intent = Intent(requireContext(), NoNavActivity::class.java).apply {
            putExtra(NoNavActivity.EXTRA_FRAGMENT_TYPE, NoNavActivity.TYPE_TALENT_POST)
            putExtra("fragment_bundle", bundle)
        }
        // ✅ 결과를 받기 위해 launcher로 Activity를 시작합니다.
        editPostLauncher.launch(intent)
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("삭제 확인")
            .setMessage("정말로 이 게시글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deletePost()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deletePost() {
        lifecycleScope.launch {
            try {
                val jwt = "Bearer " + TokenManager.getAccessToken(requireContext())
                val response = if (type == "sell") {
                    HomeRetrofitInstance.api.deleteTalentSell(postId, jwt)
                } else {
                    HomeRetrofitInstance.api.deleteTalentBuy(postId, jwt)
                }

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "삭제 중 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindSellDetail(detail: TalentSellResponse) {
        binding.tvDetailTitle.text = detail.title
        binding.tvDetailContent.text = detail.description
        binding.tvDetailPrice.text = "₩${detail.price}"
        binding.tvDetailInfo.text = "${detail.writerNickname} · ${detail.createdAt.substring(0, 10)}"
        updateImage(detail.imageUrl)
    }

    private fun bindBuyDetail(detail: TalentBuyResponse) {
        binding.tvDetailTitle.text = detail.title
        binding.tvDetailContent.text = detail.description
        binding.tvDetailPrice.text = "희망가: ₩${detail.budget}"
        binding.tvDetailInfo.text = "${detail.writerNickname} · ${detail.createdAt.substring(0, 10)}"
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
            } catch (_: Exception) {
            }
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

    private fun getOpponentIdFromDetail(): Long {
        return when (val detail = lastLoadedDetail) {
            is TalentSellResponse -> detail.writerId
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
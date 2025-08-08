package com.example.talent_link.ui.LocalLife

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talent_link.R
import com.example.talent_link.databinding.FragmentLocalDetailBinding
import com.example.talent_link.ui.LocalLife.dto.LikeRequest
import com.example.talent_link.ui.LocalLife.dto.LocalComment
import com.example.talent_link.ui.LocalLife.dto.LocalCommentRequest
import com.example.talent_link.ui.LocalLife.dto.LocalPost
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.launch

class LocalDetailFragment : Fragment() {

    private var _binding: FragmentLocalDetailBinding? = null
    private val binding get() = _binding!!

    private val commentList = mutableListOf<LocalComment>()
    private lateinit var commentAdapter: LocalCommentAdapter

    private var isLiked = false
    private var likeCount = 0

    private lateinit var jwt: String
    private var userId: Long = -1L
    private var postId: Long = -1L

    companion object {
        fun newInstance(postId: Long): LocalDetailFragment {
            val fragment = LocalDetailFragment()
            val args = Bundle()
            args.putLong("postId", postId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocalDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        jwt = "Bearer " + (TokenManager.getAccessToken(requireContext()) ?: "")
        userId = IdManager.getUserId(requireContext())
        postId = arguments?.getLong("postId", -1L) ?: -1L

        if (postId == -1L) {
            Toast.makeText(requireContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        setupUI()
        fetchPostDetails()
        fetchLikeStatus()
        fetchComments()
    }

    private fun setupUI() {
        binding.toolbarDetail.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.imgDetailLike.setOnClickListener {
            toggleLike()
        }

        commentAdapter = LocalCommentAdapter(commentList)
        binding.recyclerComments.adapter = commentAdapter
        binding.recyclerComments.layoutManager = LinearLayoutManager(requireContext())

        binding.btnSendComment.setOnClickListener {
            val content = binding.editComment.text.toString().trim()
            if (content.isNotEmpty()) {
                postComment(content)
                binding.editComment.text.clear()
            }
        }
    }

    private fun fetchPostDetails() {
        lifecycleScope.launch {
            try {
                val response = LocalLifeRetrofitInstance.api.getPost(postId, jwt)
                if (response.isSuccessful) {
                    response.body()?.let { updatePostUI(it) }
                } else {
                    Toast.makeText(requireContext(), "게시글을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("LocalDetailFragment", "게시글 로딩 오류", e)
            }
        }
    }

    private fun fetchLikeStatus() {
        lifecycleScope.launch {
            try {
                val status = LocalLifeRetrofitInstance.api.getMyLike(postId, userId, jwt)
                isLiked = status.liked
                likeCount = status.likeCount.toInt()
                updateLikeUI()
            } catch (e: Exception) {
                Log.e("LocalDetailFragment", "좋아요 상태 로딩 오류", e)
            }
        }
    }

    private fun fetchComments() {
        lifecycleScope.launch {
            try {
                val response = LocalLifeRetrofitInstance.api.getComments(postId, jwt)
                if (response.isSuccessful) {
                    commentList.clear()
                    commentList.addAll(response.body() ?: emptyList())
                    commentAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("LocalDetailFragment", "댓글 로딩 오류", e)
            }
        }
    }

    private fun toggleLike() {
        lifecycleScope.launch {
            try {
                if (isLiked) {
                    LocalLifeRetrofitInstance.api.unlikePost(postId, LikeRequest(userId), jwt)
                } else {
                    LocalLifeRetrofitInstance.api.likePost(postId, LikeRequest(userId), jwt)
                }
                fetchLikeStatus()
            } catch (e: Exception) {
                Log.e("LocalDetailFragment", "좋아요 토글 오류", e)
            }
        }
    }

    // 수정된 부분
    private fun postComment(content: String) {
        // 더 이상 닉네임과 주소를 직접 보내지 않고, 내용만 담아서 요청합니다.
        val commentRequest = LocalCommentRequest(
            content = content
        )
        lifecycleScope.launch {
            try {
                val response = LocalLifeRetrofitInstance.api.addComment(postId, commentRequest, jwt)
                if (response.isSuccessful) {
                    fetchComments() // 댓글 목록 새로고침
                }
            } catch (e: Exception) {
                Log.e("LocalDetailFragment", "댓글 등록 오류", e)
            }
        }
    }

    private fun updatePostUI(post: LocalPost) {
        binding.tvDetailTitle.text = post.title
        val formattedTime = if (post.createdAt.length >= 19) {
            post.createdAt.substring(0, 19).replace('T', ' ')
        } else {
            post.createdAt
        }
        binding.tvDetailInfo.text = "${post.writerNickname} · ${post.address} · $formattedTime"
        binding.tvDetailContent.text = post.content
        // TODO: Glide 등으로 이미지 로딩 로직 추가
    }

    private fun updateLikeUI() {
        binding.imgDetailLike.setImageResource(if (isLiked) R.drawable.like_icon else R.drawable.un_like_icon)
        binding.tvDetailLikeCount.text = likeCount.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
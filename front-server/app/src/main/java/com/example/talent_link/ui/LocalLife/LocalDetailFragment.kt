package com.example.talent_link.ui.LocalLife

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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.talent_link.NoNavActivity
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
    private var postWriterId: Long = -1L

    private var currentPost: LocalPost? = null

    // ✅ 수정 완료 후 화면을 새로고침하기 위한 Launcher
    private val editPostLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 수정이 성공적으로 완료되면 상세 정보를 다시 불러옵니다.
            fetchPostDetails()
        }
    }

    companion object {
        fun newInstance(postId: Long): LocalDetailFragment {
            return LocalDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong("postId", postId)
                }
            }
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

        setupToolbar()
        fetchPostDetails()
        fetchLikeStatus()
        fetchComments()
    }

    private fun setupToolbar() {
        binding.toolbarDetail.setNavigationOnClickListener {
            requireActivity().finish() // 👍 Activity를 종료하는 올바른 코드
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
                    response.body()?.let { post ->
                        currentPost = post
                        postWriterId = post.writerId
                        updatePostUI(post)

                        val myUserId = IdManager.getUserId(requireContext())
                        binding.toolbarDetail.menu.findItem(R.id.menu_edit_post).isVisible = (myUserId == postWriterId)
                        binding.toolbarDetail.menu.findItem(R.id.menu_delete_post).isVisible = (myUserId == postWriterId)
                    }
                } else {
                    Toast.makeText(requireContext(), "게시글을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("LocalDetailFragment", "게시글 로딩 오류", e)
            }
        }
    }

    private fun navigateToEdit() {
        currentPost?.let { post ->
            val bundle = Bundle().apply {
                putString("mode", "edit")
                putLong("id", post.id)
                putString("title", post.title)
                putString("content", post.content)
                putString("address", post.address)
                putString("imageUrl", post.imageUrl)
            }

            val intent = Intent(requireContext(), NoNavActivity::class.java).apply {
                putExtra(NoNavActivity.EXTRA_FRAGMENT_TYPE, NoNavActivity.TYPE_LOCAL_WRITE)
                putExtra("fragment_bundle", bundle)
            }
            // ✅ 결과를 받기 위해 launcher로 Activity를 시작합니다.
            editPostLauncher.launch(intent)
        }
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
                val response = LocalLifeRetrofitInstance.api.deletePost(postId, jwt)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "삭제에 실패했습니다: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("LocalDetailFragment", "삭제 오류", e)
                Toast.makeText(requireContext(), "삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchLikeStatus() {
        lifecycleScope.launch {
            try {
                val status = LocalLifeRetrofitInstance.api.getMyLike(postId, userId, jwt)
                isLiked = status.liked
                likeCount = status.likeCount
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

    private fun postComment(content: String) {
        val commentRequest = LocalCommentRequest(content = content)
        lifecycleScope.launch {
            try {
                val response = LocalLifeRetrofitInstance.api.addComment(postId, commentRequest, jwt)
                if (response.isSuccessful) {
                    fetchComments()
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

        if (!post.imageUrl.isNullOrEmpty()) {
            binding.imgDetailPost.visibility = View.VISIBLE
            Glide.with(this).load(post.imageUrl).into(binding.imgDetailPost)
        } else {
            binding.imgDetailPost.visibility = View.GONE
        }
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
package com.example.talent_link.ui.LocalLife

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talent_link.R
import com.example.talent_link.ui.LocalLife.dto.LikeRequest
import com.example.talent_link.ui.LocalLife.dto.LocalComment
import com.example.talent_link.ui.LocalLife.dto.LocalCommentRequest
import com.example.talent_link.ui.LocalLife.dto.LocalPost
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import kotlinx.coroutines.launch

class LocalDetailActivity : AppCompatActivity() {

    private val commentList = mutableListOf<LocalComment>()
    private lateinit var commentAdapter: LocalCommentAdapter

    private var isLiked = false
    private var likeCount = 0

    private lateinit var jwt: String
    private var userId: Long = -1L
    private var postId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_detail)

        jwt = "Bearer " + (TokenManager.getAccessToken(this) ?: "")
        userId = IdManager.getUserId(this)
        postId = intent.getLongExtra("postId", -1L)
        if (postId == -1L) {
            finish()
            return
        }

        // 1. 상세 데이터 가져오기
        lifecycleScope.launch {
            val response = LocalLifeRetrofitInstance.api.getPost(postId, jwt)
            if (response.isSuccessful) {
                val post = response.body()
                post?.let { updateUI(it) }
            } else {
                // 실패 처리
            }
        }

        // 2. **좋아요 상태/개수 서버에서 받아와서 UI 갱신**
        fetchLikeStatus()

        // 3. 좋아요 버튼 클릭 시 처리
        val likeIcon = findViewById<ImageView>(R.id.imgDetailLike)
        likeIcon.setOnClickListener {
            lifecycleScope.launch {
                if (isLiked) {
                    // 좋아요 취소
                    val response = LocalLifeRetrofitInstance.api.unlikePost(postId, LikeRequest(userId), jwt)
                    if (!response.isSuccessful) {
                        Log.e("좋아요", "좋아요 취소 실패: ${response.code()} / ${response.errorBody()?.string()}")
                    }
                } else {
                    // 좋아요 등록
                    val response = LocalLifeRetrofitInstance.api.likePost(postId, LikeRequest(userId), jwt)
                    if (!response.isSuccessful) {
                        Log.e("좋아요", "좋아요 등록 실패: ${response.code()} / ${response.errorBody()?.string()}")
                    }
                }
                // 서버에서 최신 상태 받아와서 갱신
                fetchLikeStatus()
            }
        }

        // --- 댓글 로직 ---
        fetchComments(postId)
        val recyclerComments = findViewById<RecyclerView>(R.id.recyclerComments)
        commentAdapter = LocalCommentAdapter(commentList)
        recyclerComments.adapter = commentAdapter
        recyclerComments.layoutManager = LinearLayoutManager(this)

        val editComment = findViewById<EditText>(R.id.editComment)
        val btnSendComment = findViewById<ImageButton>(R.id.btnSendComment)
        btnSendComment.setOnClickListener {
            val content = editComment.text.toString().trim()
            if (content.isNotEmpty()) {
                val nickname = IdManager.getNickname(this) ?: "알수없음"
                val commentRequest = LocalCommentRequest(
                    postId = postId,
                    content = content,
                    writerNickname = nickname,
                    address = "중흥3동"
                )
                lifecycleScope.launch {
                    val response = LocalLifeRetrofitInstance.api.addComment(postId, commentRequest, jwt)
                    if (response.isSuccessful) {
                        fetchComments(postId)
                        editComment.text.clear()
                    } else {
                        val errorMsg = response.errorBody()?.string()
                        Log.e("댓글등록에러", errorMsg ?: "Unknown error")
                    }
                }
            }
        }
    }

    // 좋아요 상태와 개수 갱신 함수
    private fun fetchLikeStatus() {
        lifecycleScope.launch {
            try {
                val status = LocalLifeRetrofitInstance.api.getMyLike(postId, userId, jwt)
                isLiked = status.liked
                likeCount = status.likeCount
                updateLikeUI()
            } catch (e: Exception) {
                Log.e("fetchLikeStatus", "예외 발생: ${e.message}", e)
            }
        }
    }

    // 좋아요 UI만 갱신
    private fun updateLikeUI() {
        val likeIcon = findViewById<ImageView>(R.id.imgDetailLike)
        val likeCountText = findViewById<TextView>(R.id.tvDetailLikeCount)
        likeIcon.setImageResource(if (isLiked) R.drawable.like_icon else R.drawable.un_like_icon)
        likeCountText.text = likeCount.toString()
    }

    private fun updateUI(post: LocalPost) {
        findViewById<TextView>(R.id.tvDetailTitle).text = post.title
        findViewById<TextView>(R.id.tvDetailInfo).text =
            "${post.writerNickname} · ${post.address} · ${post.createdAt?.substring(0, 19)?.replace('T', ' ') ?: ""}"
        findViewById<TextView>(R.id.tvDetailContent).text = post.content
        findViewById<ImageView>(R.id.imgDetailPost).setImageResource(R.drawable.ic_launcher_foreground)
    }

    private fun fetchComments(postId: Long) {
        lifecycleScope.launch {
            try {
                val response = LocalLifeRetrofitInstance.api.getComments(postId, jwt)
                if (response.isSuccessful) {
                    val comments = response.body() ?: emptyList()
                    Log.d("fetchComments", "댓글 목록: $comments")
                    commentList.clear()
                    commentList.addAll(comments)
                    commentAdapter.notifyDataSetChanged()
                } else {
                    Log.e("fetchComments", "응답 실패: ${response.code()} / ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("fetchComments", "예외 발생: ${e.message}", e)
            }
        }
    }
}

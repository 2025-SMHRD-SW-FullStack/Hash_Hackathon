package com.example.talent_link.ui.LocalLife

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.talent_link.R
import com.example.talent_link.ui.LocalLife.dto.LocalPost

class LocalLifeAdapter(
    private val postList: List<LocalPost>
) : RecyclerView.Adapter<LocalLifeAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val content: TextView = view.findViewById(R.id.tvContent)
        val nickname: TextView = view.findViewById(R.id.tvNickname)
        val address: TextView = view.findViewById(R.id.tvAddress)
        val createdAt: TextView = view.findViewById(R.id.tvCreatedAt)
        val likeCount: TextView = view.findViewById(R.id.tvLikeCount)
        val postImg: ImageView = view.findViewById(R.id.imgPost)
        val likeIcon: ImageView = view.findViewById(R.id.imgLike)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_local_life, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.title.text = post.title
        holder.content.text = post.content
        holder.nickname.text = post.writerNickname
        holder.address.text = post.address

        // createdAt 변환: yyyy-MM-ddTHH:mm:ss → yyyy-MM-dd HH:mm:ss
        val original = post.createdAt ?: ""
        val formattedTime = if (original.length >= 19) {
            original.substring(0, 19).replace('T', ' ')
        } else {
            original
        }
        holder.createdAt.text = formattedTime

        // 좋아요 개수 표시
        holder.likeCount.text = post.likeCount.toString()
        // (이미지, 좋아요 아이콘은 기본으로)
        holder.postImg.setImageResource(R.drawable.ic_launcher_foreground)
        holder.likeIcon.setImageResource(R.drawable.un_like_icon)

        // 게시글 클릭 시 상세페이지 이동(postId만 전달)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, LocalDetailActivity::class.java)
            intent.putExtra("postId", post.id)
            context.startActivity(intent)
        }

        // ★ 만약 목록에서 바로 좋아요 토글 기능을 넣고 싶으면 아래 참고
        // holder.likeIcon.setOnClickListener {
        //     // 좋아요 토글 API 호출 및 UI 갱신
        // }
    }

    override fun getItemCount() = postList.size
}

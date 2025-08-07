package com.example.talent_link.ui.LocalLife

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // 👈 Glide import
import com.example.talent_link.R
import com.example.talent_link.ui.LocalLife.dto.LocalPost

class LocalLifeAdapter(
    private val postList: List<LocalPost>
) : RecyclerView.Adapter<LocalLifeAdapter.PostViewHolder>() {

    // ✅ 1. ViewHolder 수정
    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val content: TextView = view.findViewById(R.id.tvContent)
        val metaInfo: TextView = view.findViewById(R.id.tvMetaInfo) // 👈 수정됨
        val likeCount: TextView = view.findViewById(R.id.tvLikeCount)
        val postImg: ImageView = view.findViewById(R.id.imgPost)
        val likeIcon: ImageView = view.findViewById(R.id.imgLike)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        // ✅ 제안했던 새 레이아웃 파일로 변경
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_local_life, parent, false) // item_local_life_revised.xml (새 레이아웃 파일명)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        holder.title.text = post.title
        holder.content.text = post.content
        holder.likeCount.text = post.likeCount.toString()

        // ✅ 2. MetaInfo 텍스트 조합
        val original = post.createdAt ?: ""
        val formattedTime = if (original.length >= 19) {
            original.substring(0, 19).replace('T', ' ')
        } else {
            original
        }
        val metaText = "${post.writerNickname} • ${post.address} • $formattedTime"
        holder.metaInfo.text = metaText

        // ✅ 3. (추천) Glide로 실제 이미지 로딩
        if (!post.imageUrl.isNullOrEmpty()) {
            holder.postImg.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.imageUrl) // post 데이터에 이미지 URL이 있다고 가정
                .placeholder(R.drawable.chat_bubble_you)
                .error(R.drawable.chat_bubble_you)
                .into(holder.postImg)
        } else {
            // 이미지가 없으면 ImageView를 숨김
            holder.postImg.visibility = View.GONE
        }

        // (필요 시) 좋아요 상태에 따라 아이콘 변경
        // holder.likeIcon.setImageResource(R.drawable.like_icon)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            if (context is AppCompatActivity) {
                val fragment = LocalDetailFragment.newInstance(post.id)
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.frame, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun getItemCount() = postList.size
}
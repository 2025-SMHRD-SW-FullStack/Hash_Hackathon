package com.example.talent_link.ui.LocalLife

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

        val original = post.createdAt ?: ""
        val formattedTime = if (original.length >= 19) {
            original.substring(0, 19).replace('T', ' ')
        } else {
            original
        }
        holder.createdAt.text = formattedTime

        holder.likeCount.text = post.likeCount.toString()
        holder.postImg.setImageResource(R.drawable.ic_launcher_foreground)
        holder.likeIcon.setImageResource(R.drawable.un_like_icon)

        // ✅ 게시글 클릭 시 Activity가 아닌 Fragment를 띄우도록 수정
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            if (context is AppCompatActivity) {
                val fragment = LocalDetailFragment.newInstance(post.id)
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.frame, fragment) // MainActivity의 FrameLayout ID
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun getItemCount() = postList.size
}
package com.example.talent_link.ui.LocalLife

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.talent_link.R
import com.example.talent_link.ui.LocalLife.dto.LocalComment

class LocalCommentAdapter(
    private val commentList: List<LocalComment>
) : RecyclerView.Adapter<LocalCommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nickname: TextView = view.findViewById(R.id.tvCommentUserNick)
//        val address: TextView = view.findViewById(R.id.localUserAdd)
        val content: TextView = view.findViewById(R.id.tvCommentContent) // 댓글 내용
        val createdAt: TextView = view.findViewById(R.id.tvCommentTime)
        val profileImg: ImageView = view.findViewById(R.id.ivCommentUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_local_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.nickname.text = comment.writerNickname
//        holder.address.text = comment.address
        holder.content.text = comment.content
        // createdAt을 "2025-08-07T10:53:36"까지만 나오게 변환
        val createdAtOrigin = comment.createdAt ?: ""
        val formattedTime = if (createdAtOrigin.length >= 19) {
            createdAtOrigin.substring(0, 19).replace('T', ' ')
        } else {
            createdAtOrigin
        }
        holder.createdAt.text = formattedTime
        holder.profileImg.setImageResource(R.drawable.user_icon) // 기본 프로필 이미지로 대체 (user_icon 예시)
    }

    override fun getItemCount() = commentList.size
}

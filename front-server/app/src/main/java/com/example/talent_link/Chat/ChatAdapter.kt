package com.example.talent_link.Chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.talent_link.Chat.dto.ChatMessageDto
import com.example.talent_link.databinding.ItemChatMeBinding
import com.example.talent_link.databinding.ItemChatYouBinding

class ChatAdapter(
    private val context: Context,
    private val messageList: List<ChatMessageDto>,
    private val myUserId: Long
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ME = 1
        private const val VIEW_TYPE_YOU = 2
    }

    override fun getItemViewType(position: Int): Int {
        // 내 닉네임과 메시지의 userNick 비교로 뷰 타입 결정
        // 메시지 작성자 닉네임이 내 닉네임과 같으면 내 메세지 아니면 상대방 메시지
        return if (messageList[position].senderId == myUserId) VIEW_TYPE_ME else VIEW_TYPE_YOU
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ME) {
            val binding = ItemChatMeBinding.inflate(LayoutInflater.from(context), parent, false)
            MeViewHolder(binding)
        } else {
            val binding = ItemChatYouBinding.inflate(LayoutInflater.from(context), parent, false)
            YouViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = messageList[position]
        // 뷰타입에 따라 각각 다른 뷰홀더 생성
        if (holder is MeViewHolder) {
            holder.bind(chat)
        } else if (holder is YouViewHolder) {
            holder.bind(chat)
        }
    }

    override fun getItemCount(): Int = messageList.size

    inner class MeViewHolder(private val binding: ItemChatMeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessageDto) {
            binding.Chatmsg.text = chat.content
        }
    }

    inner class YouViewHolder(private val binding: ItemChatYouBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessageDto) {
            binding.Chatmsg.text = chat.content
            binding.ChatNick.text = chat.senderNickname
            // 이미지뷰에 프로필 이미지를 넣고 싶으면 Glide 같은 라이브러리 활용 가능
            // 예: Glide.with(binding.imgMe.context).load(...).into(binding.imgMe)
        }
    }
}

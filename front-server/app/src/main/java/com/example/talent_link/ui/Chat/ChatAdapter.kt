import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.talent_link.Chat.dto.ChatMessageDto
import com.example.talent_link.databinding.ItemChatMeBinding
import com.example.talent_link.databinding.ItemChatYouBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        if (holder is MeViewHolder) {
            holder.bind(chat, position)
        } else if (holder is YouViewHolder) {
            holder.bind(chat)
        }
    }

    override fun getItemCount(): Int = messageList.size

    // ------------------------------
    // [내 메시지 ViewHolder]
    inner class MeViewHolder(private val binding: ItemChatMeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessageDto, position: Int) {
            binding.Chatmsg.text = chat.content
            binding.msgTime.text = chat.sentAt?.let { formatTime(it.toString()) } ?: ""
            // 모든 "내가 보낸 안읽힌 메시지"에 "안읽음" 표시
            if (!chat.isRead) {
                binding.readStatus.visibility = View.VISIBLE
                binding.readStatus.text = "안읽음"
            } else {
                binding.readStatus.visibility = View.GONE
            }
        }

        // "내가 보낸 메시지" + "isRead==false" 중 마지막에만 true 반환
        private fun isLastUnreadMyMessage(position: Int): Boolean {
            val isMe = messageList[position].senderId == myUserId
            val isUnread = !messageList[position].isRead
            if (!isMe || !isUnread) return false

            // 뒷쪽에 더 최근에 안읽힌 내 메시지가 있으면 false
            for (i in position + 1 until messageList.size) {
                val msg = messageList[i]
                if (msg.senderId == myUserId && !msg.isRead) {
                    return false
                }
            }
            return true
        }
    }

    // ------------------------------
    // [상대 메시지 ViewHolder]
    inner class YouViewHolder(private val binding: ItemChatYouBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessageDto) {
            binding.Chatmsg.text = chat.content
            binding.tvOpponentNick.text = chat.senderNickname
            binding.msgTime.text = chat.sentAt?.let { formatTime(it.toString()) } ?: ""
            // 상대 메시지에는 "안읽음" 배지 없음!
        }
    }

    // ------------------------------
    // [시간 포맷: "2024-08-06T11:32:00" → "오전 11:32"]
    private fun formatTime(dateTimeStr: String): String {
        return try {
            val localDateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val hour = localDateTime.hour
            val minute = localDateTime.minute
            val ampm = if (hour < 12) "오전" else "오후"
            val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            String.format("%s %d:%02d", ampm, hour12, minute)
        } catch (e: Exception) {
            ""
        }
    }
}

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.talent_link.Chat.dto.ChatRoomListItemDto
import com.example.talent_link.R
import com.example.talent_link.databinding.ItemChatUserlistBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatUserAdapter(
    private var chatRoomList: List<ChatRoomListItemDto>,
    private val onItemClick: (ChatRoomListItemDto) -> Unit
) : RecyclerView.Adapter<ChatUserAdapter.ViewHolder>() {

    fun updateList(newList: List<ChatRoomListItemDto>) {
        chatRoomList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatUserlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = chatRoomList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chatRoomList[position])
    }

    inner class ViewHolder(private val binding: ItemChatUserlistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatRoomListItemDto) {
            binding.tvOpponentNick.text = item.opponentNickname
            binding.tvLastMsg.text = item.lastMessage

            // 시간: "2024-08-06T11:32:00" → "오전 11:32"
            binding.tvTime.text = item.lastMessageAt?.let { formatTime(it.toString()) } ?: ""

            // 안읽은 메시지 숫자 뱃지
            if (item.unreadCount != null && item.unreadCount > 0) {
                binding.tvUnreadCount.text = item.unreadCount.toString()
                binding.tvUnreadCount.visibility = View.VISIBLE
            } else {
                binding.tvUnreadCount.visibility = View.GONE
            }

            // 프로필 이미지 (Glide)
            Glide.with(binding.ChatImg.context)
                .load(item.opponentProfileImageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .circleCrop()
                .into(binding.ChatImg)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }

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
}

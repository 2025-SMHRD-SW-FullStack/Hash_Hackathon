import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.talent_link.Chat.ChatUserVO
import com.example.talent_link.Chat.dto.ChatRoomListItemDto
import com.example.talent_link.databinding.ItemChatUserlistBinding

class ChatUserAdapter(private var userList: List<ChatRoomListItemDto>,
                      private val onItemClick: (ChatRoomListItemDto) -> Unit) :
    RecyclerView.Adapter<ChatUserAdapter.ChatUserViewHolder>() {

    inner class ChatUserViewHolder(private val binding: ItemChatUserlistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(room: ChatRoomListItemDto) {
            binding.ChatNick.text = room.opponentNickname
            binding.ChatLastmsg.text = room.lastMessage
            Glide.with(binding.ChatImg.context)
                .load(room.opponentProfileImageUrl)
                .circleCrop() // 원형 크롭
                .into(binding.ChatImg)
            // 클릭 이벤트
            binding.root.setOnClickListener {
                onItemClick(room)
                Log.d("클릭", "ChatRoom으로 이동")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUserViewHolder {
        val binding = ItemChatUserlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatUserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<ChatRoomListItemDto>){
        userList = newList
        notifyDataSetChanged()
    }
}

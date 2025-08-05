import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.talent_link.ui.Chat.ChatUserVO
import com.example.talent_link.databinding.ItemChatUserlistBinding

class ChatUserAdapter(private val userList: List<ChatUserVO>,
                      private val onItemClick: (ChatUserVO) -> Unit) :
    RecyclerView.Adapter<ChatUserAdapter.ChatUserViewHolder>() {

    inner class ChatUserViewHolder(private val binding: ItemChatUserlistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: ChatUserVO) {
            binding.ChatNick.text = user.userNick
            binding.ChatLastmsg.text = user.lastMsg
            Glide.with(binding.ChatImg.context)
                .load(user.userImg)
                .circleCrop() // 원형 크롭
                .into(binding.ChatImg)

            Log.d("ChatUserAdapter", "Binding lastMsg: ${user.lastMsg}")
            // 클릭 이벤트
            binding.root.setOnClickListener {
                onItemClick(user)
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
}

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talent_link.Chat.ChatRoomFragment
import com.example.talent_link.Chat.ChatUserVO
import com.example.talent_link.Chat.RetrofitInstance
import com.example.talent_link.R
import com.example.talent_link.databinding.FragmentChatListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatListFragment : Fragment() {

    private lateinit var binding: FragmentChatListBinding
    private lateinit var adapter: ChatUserAdapter
    private var myUserId : Long = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ChatUserAdapter(listOf()) { room ->
            val chatRoomFragment = ChatRoomFragment()
            val bundle = Bundle()
            bundle.putLong("roomId", room.roomId)
            chatRoomFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame, chatRoomFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // 서버 데이터 받아올 때
        lifecycleScope.launch (Dispatchers.IO){
            val jwt = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzU0MzgyMDQ2LCJleHAiOjE3NTU1OTE2NDZ9.8_htEX6zBiHh_Q9TADarlPbGK2gBzCS37RDOjYIhw78"
            val chatRooms = RetrofitInstance.api.getMyChatRooms(myUserId,jwt)
            withContext(Dispatchers.Main) {
                adapter.updateList(chatRooms)
            }
        }

    }
}

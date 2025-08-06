import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talent_link.Chat.ChatWebSocketManager
import com.example.talent_link.ui.chat.ChatRoomFragment
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
//    private var myUserId : Long = 2
    private val wsManagerList = mutableListOf<ChatWebSocketManager>()
    private lateinit var jwt: String

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
        jwt = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzU0NDQzNDE1LCJleHAiOjE3NTU2NTMwMTV9.RifmHhEOPvoO5uTC2QSvnzLN8JEQONrfm0QW4_5rdkI"
//        jwt = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwiaWF0IjoxNzU0NDQ2MzY1LCJleHAiOjE3NTU2NTU5NjV9.Fayk8coO2DTCs1QyrSMNor2x9mE2PLf7tYkRlOb0zY4"

        loadAndSubscribeRooms()
    }

    private fun loadAndSubscribeRooms() {
        lifecycleScope.launch(Dispatchers.IO) {
            val chatRooms = RetrofitInstance.api.getMyChatRooms(myUserId, jwt)
            // 서버에서 이미 최신 메시지 순 정렬해서 내려줌
            val myRoomIds = chatRooms.map { it.roomId }
            withContext(Dispatchers.Main) {
                adapter.updateList(chatRooms)
                subscribeRooms(myRoomIds)
            }
        }
    }

    private fun subscribeRooms(roomIds: List<Long>) {
        // 이전 구독 해제
        wsManagerList.forEach { it.disconnect() }
        wsManagerList.clear()

        roomIds.forEach { roomId ->
            val ws = ChatWebSocketManager(
                serverUrl = "ws://10.0.2.2:8099/ws/chat/websocket",
                roomId = roomId,
                myUserId = myUserId,
                myNick = "내닉",
                jwtToken = jwt
            ) { msg ->
                loadAndSubscribeRooms()
            }
            ws.connect()
            ws.subscribeReadEvent { event ->
                if (event.userId != myUserId) {
                    loadAndSubscribeRooms()
                }
            }
            wsManagerList.add(ws)
        }
    }

    // 3. Fragment 종료 시 구독 해제!
    override fun onDestroyView() {
        super.onDestroyView()
        wsManagerList.forEach { it.disconnect() }
    }

}

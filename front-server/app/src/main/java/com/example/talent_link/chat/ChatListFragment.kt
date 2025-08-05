import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talent_link.chat.ChatRoomFragment
import com.example.talent_link.chat.ChatUserVO
import com.example.talent_link.R
import com.example.talent_link.databinding.FragmentChatListBinding

class ChatListFragment : Fragment() {

    private lateinit var binding: FragmentChatListBinding
    private lateinit var adapter: ChatUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val testUserList = listOf(
            ChatUserVO(
                userId = "test123",
                userNick = "테스트유저",
                lastMsg = "안녕하세요! 테스트 중입니다.",
                userImg = R.drawable.ic_launcher_background
            )
        )

        adapter = ChatUserAdapter(testUserList) { clickedUser ->
            val chatRoomFragment = ChatRoomFragment()

            val bundle = Bundle()
            bundle.putString("userId", clickedUser.userId)
            bundle.putString("userNick", clickedUser.userNick)
            chatRoomFragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, chatRoomFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        parentFragmentManager.setFragmentResultListener("updateLastMsg", viewLifecycleOwner) { _, bundle ->
            val userId = bundle.getString("userId") ?: return@setFragmentResultListener
            val lastMsg = bundle.getString("lastMsg") ?: return@setFragmentResultListener

            // 리스트에서 해당 유저 찾아서 lastMsg 갱신
            val index = testUserList.indexOfFirst { it.userId == userId }
            if (index != -1) {
                testUserList[index].lastMsg = lastMsg
                adapter.notifyItemChanged(index)
            }
        }
    }
}

package com.example.talent_link.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talent_link.R

class ChatRoomFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var editText: EditText
    private lateinit var sendButton: Button

    private val messageList = ArrayList<ChatVO>()
    private lateinit var adapter: ChatAdapter

    private var userNick: String = "" // 상대방 닉네임
    private var myNick: String = "나" // 내 닉네임, 필요시 변경 가능

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chat_room, container, false)

        // ChatListFragment에서 전달된 상대방 닉네임 받기
        userNick = arguments?.getString("userNick") ?: "상대방"

        recyclerView = view.findViewById(R.id.recyclerView)
        editText = view.findViewById(R.id.ChatRoommsg)
        sendButton = view.findViewById(R.id.ChatSend)

        adapter = ChatAdapter(requireContext(), messageList, myNick)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadFakeChatData()

        sendButton.setOnClickListener {
            val message = editText.text.toString().trim()
            if (message.isNotEmpty()) {
                // 내 메시지로 추가
                val chat = ChatVO(userNick = myNick, Msg = message, time = System.currentTimeMillis())
                messageList.add(chat)
                adapter.notifyItemInserted(messageList.size - 1)
                recyclerView.scrollToPosition(messageList.size - 1)
                editText.text.clear()
            }
        }

        // 채팅방 최초 진입 시 상대방 인사 메시지
        if (messageList.isEmpty()) {
            messageList.add(ChatVO(userNick = userNick, Msg = "안녕하세요! 무엇을 도와드릴까요?", time = System.currentTimeMillis()))
            adapter.notifyItemInserted(messageList.size - 1)
        }

        return view
    }

    private fun loadFakeChatData() {
        messageList.clear()

        messageList.add(ChatVO(userNick = userNick, Msg = "안녕하세요!", time = System.currentTimeMillis() - 60000))
        messageList.add(ChatVO(userNick = userNick, Msg = "오늘도 좋은 하루 보내세요.", time = System.currentTimeMillis() - 50000))
        messageList.add(ChatVO(userNick = myNick, Msg = "네 감사합니다!", time = System.currentTimeMillis() - 40000))
        messageList.add(ChatVO(userNick = userNick, Msg = "혹시 이 프로젝트는 언제까지인가요?", time = System.currentTimeMillis() - 30000))
        messageList.add(ChatVO(userNick = myNick, Msg = "이번 주 일요일까지 마감입니다.", time = System.currentTimeMillis() - 20000))

        adapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(messageList.size - 1)
    }
}

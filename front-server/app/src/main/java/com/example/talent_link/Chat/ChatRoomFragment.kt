package com.example.talent_link.Chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talent_link.Chat.dto.ChatMessageDto
import com.example.talent_link.databinding.FragmentChatRoomBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomFragment : Fragment() {

    private lateinit var binding: FragmentChatRoomBinding
    private lateinit var adapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessageDto>()

    private lateinit var webSocketManager: ChatWebSocketManager

    private var myUserId: Long = 1 // 로그인 시 받아온 유저 ID
    private var myNick: String = "나"
    private var roomId: Long = 0
    private var jwt: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        roomId = arguments?.getLong("roomId") ?: 0L
        adapter = ChatAdapter(requireContext(), messageList, myUserId)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // 1️⃣ 이전 채팅 불러오기
        loadPreviousMessages()

        // 2️⃣ WebSocket 연결 및 구독
        webSocketManager = ChatWebSocketManager(
            serverUrl = "ws://10.0.2.2:8099/ws/chat/websocket", // 실제 IP로 변경 필요
            roomId = roomId,
            myUserId = myUserId,
            myNick = myNick
        ) { receivedMessage ->
            // 메인 쓰레드에서 UI 업데이트
            requireActivity().runOnUiThread {
                messageList.add(receivedMessage)
                adapter.notifyItemInserted(messageList.size - 1)
                binding.recyclerView.scrollToPosition(messageList.size - 1)
            }
        }

        webSocketManager.connect()

        // 3️⃣ 메시지 전송 버튼 클릭
        binding.ChatSend.setOnClickListener {
            val text = binding.ChatRoommsg.text.toString().trim()
            if (text.isNotEmpty()) {
                webSocketManager.sendMessage(text)
                binding.ChatRoommsg.text.clear()
            }
        }
    }

    // 서버에서 이전 메시지 불러오기
    private fun loadPreviousMessages() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val jwt = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzU0MzgyMDQ2LCJleHAiOjE3NTU1OTE2NDZ9.8_htEX6zBiHh_Q9TADarlPbGK2gBzCS37RDOjYIhw78"
                val messages = RetrofitInstance.api.getRoomMessages(roomId, jwt)
                withContext(Dispatchers.Main) {
                    messageList.clear()
                    messageList.addAll(messages)
                    adapter.notifyDataSetChanged()
                    binding.recyclerView.scrollToPosition(messageList.size - 1)
                }
            } catch (e: Exception) {
                // 네트워크 오류 처리
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webSocketManager.disconnect()
    }
}

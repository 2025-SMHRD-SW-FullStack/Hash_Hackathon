package com.example.talent_link.ui.Chat

import ChatAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talent_link.Chat.ChatWebSocketManager
import com.example.talent_link.Chat.ChatRetrofitInstance
import com.example.talent_link.Chat.dto.ChatMessageDto
import com.example.talent_link.databinding.FragmentChatRoomBinding
import com.example.talent_link.ui.Chat.dto.ChatReadEventDto
import com.example.talent_link.util.IdManager
import com.example.talent_link.util.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomFragment : Fragment() {

    private lateinit var binding: FragmentChatRoomBinding
    private lateinit var adapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessageDto>()
    private lateinit var webSocketManager: ChatWebSocketManager

    private var myUserId: Long = 1
    private var myNick: String = "나"
    private var jwt: String = ""
    private var roomId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 채팅방 id 전달 받기 (필수)
        roomId = arguments?.getLong("roomId") ?: 0L

        // ✅ 저장된 값 불러오기
        myUserId = IdManager.getUserId(requireContext())
        jwt = "Bearer "+TokenManager.getAccessToken(requireContext()) ?: ""

        if (myUserId == -1L || jwt.isBlank()) {
            // 예외처리
            return
        }

        adapter = ChatAdapter(requireContext(), messageList, myUserId)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // 1️⃣ 이전 메시지 불러오기
        loadPreviousMessages()

        // 채팅방 들어가면 읽음 처리
        markMessagesAsRead()

        // 2️⃣ WebSocket 연결
        webSocketManager = ChatWebSocketManager(
            serverUrl = "ws://10.0.2.2:8099/ws/chat/websocket",
            roomId = roomId,
            myUserId = myUserId,
            myNick = myNick,
            jwtToken = jwt
        ) { receivedMessage ->
            requireActivity().runOnUiThread {
                messageList.add(receivedMessage)
                adapter.notifyItemInserted(messageList.size - 1)
                binding.recyclerView.scrollToPosition(messageList.size - 1)
            }
        }

        webSocketManager.connect()

        // ✅ WebSocket 읽음 신호 전송 (입장하자마자 1회만!)
        sendReadEvent()

        // ✅ 읽음 이벤트 구독 (상대가 내 메시지 읽으면 새로고침)
        webSocketManager.subscribeReadEvent { event ->
            if (event.userId != myUserId) {
                // 상대방이 읽었다면 내 메시지들의 isRead 갱신 필요
                // 새로 메시지 로드 또는 UI 갱신
                loadPreviousMessages()
            }
        }

        // 3️⃣ 메시지 전송
        binding.ChatSend.setOnClickListener {
            val text = binding.ChatRoommsg.text.toString().trim()
            if (text.isNotEmpty()) {
                webSocketManager.sendMessage(text)
                binding.ChatRoommsg.text.clear()
            }
        }
    }

    private fun loadPreviousMessages() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val messages = ChatRetrofitInstance.api.getRoomMessages(roomId, jwt)
                messages.forEach { msg ->
                    Log.d("ChatDebug", "msg id:${msg.id}, isRead:${msg.isRead}, content:${msg.content}")
                }
                withContext(Dispatchers.Main) {
                    messageList.clear()
                    messageList.addAll(messages)
                    adapter.notifyDataSetChanged()
                    binding.recyclerView.scrollToPosition(messageList.size - 1)
                }
            } catch (e: Exception) {
                // 네트워크 오류 처리 등
            }
        }
    }

    private fun markMessagesAsRead() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                ChatRetrofitInstance.api.markAsRead(roomId, myUserId, jwt)
                // 읽음 처리 후, 채팅리스트 갱신이 필요하면 콜백 등으로 알려주기
                val messages = ChatRetrofitInstance.api.getRoomMessages(roomId, jwt)
                withContext(Dispatchers.Main) {
                    messageList.clear()
                    messageList.addAll(messages)
                    adapter.notifyDataSetChanged()
                }
            } catch (_: Exception) { }
        }
    }

    private fun sendReadEvent() {
        val event = ChatReadEventDto(roomId, myUserId)
        val json = Gson().toJson(event)
        webSocketManager.sendReadEvent(json)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::webSocketManager.isInitialized) {
            webSocketManager.disconnect()
        }
    }
}

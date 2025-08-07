package com.example.talent_link.ui.Chat

import ChatAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private var myUserId: Long = -1L
    private var myNick: String = "나"
    private var jwt: String = ""
    private var roomId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Argument에서 데이터 추출
        roomId = arguments?.getLong("roomId") ?: 0L
        myUserId = IdManager.getUserId(requireContext())
        myNick = IdManager.getNickname(requireContext()) ?: "나"
        jwt = "Bearer " + TokenManager.getAccessToken(requireContext())

        // roomId가 유효한지 먼저 확인
        if (roomId <= 0L || myUserId == -1L) {
            Toast.makeText(requireContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack() // 이전 화면으로 돌아가기
            return
        }

        // roomId가 유효한 경우에만 UI 및 네트워크 설정 진행
        setupUI()
        loadPreviousMessages()
        setupWebSocket()
        setupSendButton()
    }

    private fun setupUI() {
        adapter = ChatAdapter(requireContext(), messageList, myUserId)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true // 키보드 올라올 때 리스트도 같이 올라가도록
        }
        binding.recyclerView.adapter = adapter
    }

    private fun loadPreviousMessages() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val messages = ChatRetrofitInstance.api.getRoomMessages(roomId, jwt)
                withContext(Dispatchers.Main) {
                    messageList.clear()
                    messageList.addAll(messages)
                    adapter.notifyDataSetChanged()
                    binding.recyclerView.scrollToPosition(messageList.size - 1)
                    // 메시지 로드 후, '읽음' 처리 요청
                    markMessagesAsRead()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "메시지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupWebSocket() {
        webSocketManager = ChatWebSocketManager(
            serverUrl = "ws://10.0.2.2:8099/ws/chat/websocket",
            roomId = roomId,
            myUserId = myUserId,
            myNick = myNick,
            jwtToken = jwt
        ) { receivedMessage ->
            // 새 메시지 수신 시 UI 업데이트
            requireActivity().runOnUiThread {
                messageList.add(receivedMessage)
                adapter.notifyItemInserted(messageList.size - 1)
                binding.recyclerView.scrollToPosition(messageList.size - 1)
                // 상대방 메시지 수신 시 바로 읽음 처리
                if (receivedMessage.senderId != myUserId) {
                    markMessagesAsRead()
                }
            }
        }

        webSocketManager.connect()

        // 상대가 내 메시지를 읽었을 때의 이벤트 구독
        webSocketManager.subscribeReadEvent { event ->
            if (event.userId != myUserId) {
                // 상대가 읽었다는 신호이므로, 내 메시지들의 '안읽음' 표시를 업데이트하기 위해 목록 새로고침
                loadPreviousMessages()
            }
        }
    }

    private fun setupSendButton() {
        binding.ChatSend.setOnClickListener {
            val text = binding.ChatRoommsg.text.toString().trim()
            if (text.isNotEmpty()) {
                webSocketManager.sendMessage(text)
                binding.ChatRoommsg.text.clear()
            }
        }
    }

    private fun markMessagesAsRead() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // REST API로 읽음 처리 요청
                ChatRetrofitInstance.api.markAsRead(roomId, myUserId, jwt)
                // 웹소켓으로 다른 클라이언트(상대방)에게 내가 읽었음을 알림
                sendReadEventViaWebSocket()
            } catch (e: Exception) {
                Log.e("ChatRoomFragment", "읽음 처리 실패", e)
            }
        }
    }

    private fun sendReadEventViaWebSocket() {
        val event = ChatReadEventDto(roomId, myUserId)
        val json = Gson().toJson(event)
        if (::webSocketManager.isInitialized) {
            webSocketManager.sendReadEvent(json)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 웹소켓 연결 해제
        if (::webSocketManager.isInitialized) {
            webSocketManager.disconnect()
        }
    }
}
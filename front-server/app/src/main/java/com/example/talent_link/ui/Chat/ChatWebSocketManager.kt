package com.example.talent_link.Chat

import android.util.Log
import com.example.talent_link.Chat.dto.ChatMessageDto
import com.example.talent_link.ui.Chat.dto.ChatReadEventDto
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

class ChatWebSocketManager(
    private val serverUrl: String,        // ex: ws://10.0.2.2:8099/ws/chat/websocket
    private val roomId: Long,             // 채팅방 ID
    private val myUserId: Long,           // 내 userId (외부에서 전달!)
    private val myNick: String,           // 내 닉네임
    private var jwtToken: String,         // "Bearer ..."까지 포함해서 전달!
    private val onMessageReceived: (ChatMessageDto) -> Unit // 새 메시지 수신 시 콜백
) {
    private val gson = Gson()
    private var stompClient: StompClient? = null
    private var topicDisposable: Disposable? = null    // 메시지 구독용
    private var readEventDisposable: Disposable? = null // 읽음 이벤트 구독용

    /**
     * WebSocket 서버 연결 & 구독 시작
     */
    fun connect() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverUrl)
        stompClient?.withClientHeartbeat(10_000)?.withServerHeartbeat(10_000)

        jwtToken = jwtToken
        // 💡 반드시 외부에서 전달받은 jwtToken 사용!
        val headers = listOf(
            StompHeader("Authorization", jwtToken)   // "Bearer ..." 형태로 전달되어야 함
        )

        // 연결 상태 리스너 등록
        stompClient?.lifecycle()?.subscribe { event ->
            when (event.type) {
                LifecycleEvent.Type.OPENED -> {
                    Log.d("WS", "연결 성공")
                    subscribeToRoomTopic()
                }
                LifecycleEvent.Type.ERROR -> Log.e("WS", "연결 오류", event.exception)
                LifecycleEvent.Type.CLOSED -> Log.d("WS", "연결 종료")
                else -> { /* 무시 */ }
            }
        }

        // ✅ 실제 서버 연결
        stompClient?.connect(headers)
    }

    /**
     * 방의 채팅 메시지 구독 (연결 성공 시 자동 호출)
     */
    private fun subscribeToRoomTopic() {
        // 기존 구독 해제 (중복 방지)
        topicDisposable?.dispose()

        topicDisposable = stompClient?.topic("/sub/chat/$roomId")
            ?.subscribe({ message ->
                val chat = gson.fromJson(message.payload, ChatMessageDto::class.java)
                onMessageReceived(chat) // 새 메시지 도착 시 콜백 실행
            }, { error ->
                Log.e("WS", "메시지 수신 오류: ${error.message}", error)
            })
    }

    /**
     * 읽음 이벤트 구독 (상대방이 내 메시지를 읽었을 때)
     */
    fun subscribeReadEvent(onReadEvent: (ChatReadEventDto) -> Unit) {
        // 기존 구독 해제
        readEventDisposable?.dispose()

        readEventDisposable = stompClient?.topic("/sub/chat/$roomId/read")?.subscribe({ message ->
            val event = gson.fromJson(message.payload, ChatReadEventDto::class.java)
            onReadEvent(event)
        }, { error ->
            Log.e("WS", "읽음 이벤트 수신 오류: ${error.message}")
        })
    }

    /**
     * 채팅 메시지 전송
     */
    fun sendMessage(content: String) {
        if (stompClient?.isConnected != true) {
            Log.e("WS", "WebSocket 연결 안됨. 메시지 전송 실패.")
            return
        }
        val msg = ChatMessageDto(
            id = null,
            roomId = roomId,
            senderId = myUserId,
            senderNickname = myNick,
            content = content,
            sentAt = null,
            isRead = false
        )
        val json = gson.toJson(msg)

        stompClient?.send("/pub/chat/message", json)
            ?.subscribe({
                Log.d("WS", "메시지 전송 성공")
            }, { error ->
                Log.e("WS", "메시지 전송 실패: ${error.message}", error)
            })
    }

    /**
     * 읽음 이벤트 전송
     */
    fun sendReadEvent(json: String) {
        stompClient?.send("/pub/chat/read", json)?.subscribe()
    }

    /**
     * 연결 해제 및 구독 해제
     */
    fun disconnect() {
        topicDisposable?.dispose()
        readEventDisposable?.dispose()
        stompClient?.disconnect()
    }
}

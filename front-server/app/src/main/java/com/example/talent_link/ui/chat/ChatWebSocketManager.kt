package com.example.talent_link.Chat

import android.util.Log
import com.example.talent_link.Chat.dto.ChatMessageDto
import com.example.talent_link.ui.chat.dto.ChatReadEventDto
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

class ChatWebSocketManager(
    private val serverUrl: String,              // ex: ws://10.0.2.2:8080/ws/chat/websocket
    private val roomId: Long,
    private val myUserId: Long,
    private val myNick: String,
    private val jwtToken: String,
    private val onMessageReceived: (ChatMessageDto) -> Unit
) {
    private val gson = Gson()
    private var stompClient: StompClient? = null
    private var topicDisposable: Disposable? = null

    fun connect() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverUrl)

        // Heartbeat 설정
        stompClient?.withClientHeartbeat(10000)?.withServerHeartbeat(10000)

        // JWT 토큰 (실제 로그인 후 받은 값으로 대체해야 함!)
        val jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzU0MzgyMDQ2LCJleHAiOjE3NTU1OTE2NDZ9.8_htEX6zBiHh_Q9TADarlPbGK2gBzCS37RDOjYIhw78"
//        val jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzU0NDQzNDE1LCJleHAiOjE3NTU2NTMwMTV9.RifmHhEOPvoO5uTC2QSvnzLN8JEQONrfm0QW4_5rdkI"

        // ✅ Authorization 헤더 추가
        val headers = listOf(
            StompHeader("Authorization", "Bearer $jwtToken")
        )

        // 연결 상태 리스너 등록
        stompClient?.lifecycle()?.subscribe { event ->
            when (event.type) {
                LifecycleEvent.Type.OPENED -> {
                    Log.d("WS", "연결 성공")

                    // 연결 성공 후에 구독 시작
                    topicDisposable = stompClient?.topic("/sub/chat/$roomId")
                        ?.subscribe({ message ->
                            val chat = gson.fromJson(message.payload, ChatMessageDto::class.java)
                            onMessageReceived(chat)
                        }, { error ->
                            Log.e("WS", "메시지 수신 오류: ${error.message}", error)
                        })
                }

                LifecycleEvent.Type.ERROR -> Log.e("WS", "연결 오류", event.exception)
                LifecycleEvent.Type.CLOSED -> Log.d("WS", "연결 종료")
                else -> {}
            }
        }

        // ✅ 헤더 포함하여 연결 시도
        stompClient?.connect(headers)
    }



    fun sendMessage(content: String) {
        if (stompClient?.isConnected != true) {
            Log.e("WS", "WebSocket이 연결되지 않았습니다. 메시지를 보낼 수 없습니다.")
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

    fun subscribeReadEvent(onReadEvent: (ChatReadEventDto) -> Unit) {
        stompClient?.topic("/sub/chat/$roomId/read")?.subscribe({ message ->
            val event = Gson().fromJson(message.payload, ChatReadEventDto::class.java)
            onReadEvent(event)
        }, { error ->
            Log.e("WS", "읽음 이벤트 수신 오류: ${error.message}")
        })
    }


    fun sendReadEvent(json: String) {
        stompClient?.send("/pub/chat/read", json)?.subscribe()
    }

    fun disconnect() {
        topicDisposable?.dispose()
        stompClient?.disconnect()
    }
}

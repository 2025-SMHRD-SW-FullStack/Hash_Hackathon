package com.example.talent_link.ui.Chat

data class ChatVO(
    var userNick : String = "", // ChatNick(layout)
    var Msg : String = "", // Chatmsg (layout)
    var time: Long = 0L, // 메시지에 붙는 타임스탬프 역할
)

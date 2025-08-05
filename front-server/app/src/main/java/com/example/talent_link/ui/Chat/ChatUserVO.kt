package com.example.talent_link.ui.Chat

data class ChatUserVO(
    var userId: String, // ChatImg (layout)
    var userNick: String = "", // ChatNick (layout)
    var lastMsg: String = "",  // ChatLastmsg (layout)
    var userImg: Int,

    )

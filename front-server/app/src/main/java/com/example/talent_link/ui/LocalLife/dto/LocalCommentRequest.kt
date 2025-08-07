package com.example.talent_link.ui.LocalLife.dto

data class LocalCommentRequest(

    val postId: Long,
    val writerNickname:String,
    val content:String,
    val address:String

)

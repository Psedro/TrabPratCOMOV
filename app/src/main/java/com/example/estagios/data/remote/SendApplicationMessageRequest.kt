package com.example.estagios.data.remote

data class SendApplicationMessageRequest(
    val senderUserId: String,
    val content: String
)
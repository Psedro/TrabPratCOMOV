package com.example.estagios.data.remote

data class ApplicationMessageResponse(
    val _id: String,
    val applicationId: String,
    val senderUserId: String,
    val receiverUserId: String,
    val content: String,
    val createdAt: String
)
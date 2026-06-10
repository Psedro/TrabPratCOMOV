package com.example.estagios.data.remote

data class ApplicationConversationResponse(
    val applicationId: String,
    val offerTitle: String,
    val companyName: String? = null,
    val studentName: String? = null,
    val studentEmail: String? = null,
    val status: String,
    val lastMessage: String? = null,
    val lastMessageAt: String? = null
)
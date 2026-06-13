package com.example.estagios.data.remote

data class NotificationResponse(
    val _id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: String? = null,
    val relatedApplicationId: String? = null,
    val relatedOfferId: String? = null,
    val relatedUserId: String? = null
)
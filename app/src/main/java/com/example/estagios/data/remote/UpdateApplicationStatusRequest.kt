package com.example.estagios.data.remote

data class UpdateApplicationStatusRequest(
    val userId: String,
    val status: String
)
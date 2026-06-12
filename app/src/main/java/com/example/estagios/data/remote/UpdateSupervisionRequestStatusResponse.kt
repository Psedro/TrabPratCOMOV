package com.example.estagios.data.remote

data class UpdateSupervisionRequestStatusResponse(
    val message: String,
    val requestId: String,
    val status: String
)
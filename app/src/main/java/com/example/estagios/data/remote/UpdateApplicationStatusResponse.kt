package com.example.estagios.data.remote

data class UpdateApplicationStatusResponse(
    val message: String,
    val applicationId: String,
    val status: String
)
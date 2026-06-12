package com.example.estagios.data.remote

data class UpdateSupervisionRequestStatusRequest(
    val teacherUserId: String,
    val status: String
)
package com.example.estagios.data.remote

data class CreateSupervisionRequest(
    val applicationId: String,
    val studentUserId: String,
    val teacherUserId: String
)
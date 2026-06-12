package com.example.estagios.data.remote

data class TeacherResponse(
    val _id: String,
    val teacherId: String? = null,
    val name: String,
    val email: String,
    val academicTitle: String? = null,
    val department: String? = null
)
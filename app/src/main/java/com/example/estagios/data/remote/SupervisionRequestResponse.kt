package com.example.estagios.data.remote

data class SupervisionRequestResponse(
    val _id: String,
    val applicationId: String,
    val studentUserId: String,
    val teacherUserId: String,
    val status: String,
    val offerTitle: String,
    val companyName: String? = null,
    val studentName: String? = null,
    val studentEmail: String? = null,
    val teacherName: String? = null,
    val teacherEmail: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
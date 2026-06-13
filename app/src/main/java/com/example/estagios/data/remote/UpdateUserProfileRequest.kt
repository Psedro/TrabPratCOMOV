package com.example.estagios.data.remote

data class UpdateUserProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val username: String? = null,
    val email: String? = null,

    val indexNumber: Int? = null,
    val studyYear: Int? = null,
    val degreeLevel: String? = null,

    val academicTitle: String? = null,
    val teacherNumber: Int? = null,
    val department: String? = null,

    val companyName: String? = null,
    val website: String? = null,
    val description: String? = null
)
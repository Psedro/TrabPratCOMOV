package com.example.estagios.data.remote

data class StudentApplicationResponse(
    val _id: String,
    val status: String,
    val appliedDate: String,
    val cvName: String,
    val cvPath: String?,
    val offerTitle: String,
    val companyName: String?,
    val offerDescription: String?,
    val location: String?,
    val internshipOfferId: String? = null,
    // Estes só vêm quando o login é empresa
    val studentName: String? = null,
    val studentEmail: String? = null
)
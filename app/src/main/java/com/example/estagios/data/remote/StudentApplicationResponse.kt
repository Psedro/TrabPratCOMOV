package com.example.estagios.data.remote

data class StudentApplicationResponse(
    val _id: String,
    val status: String,
    val appliedDate: String,
    val cvName: String,
    val offerTitle: String,
    val companyName: String?,
    val offerDescription: String?,
    val location: String?
)
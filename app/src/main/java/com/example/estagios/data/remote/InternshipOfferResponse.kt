package com.example.estagios.data.remote

data class InternshipOfferResponse(
    val _id: String,
    val name: String,
    val description: String,
    val requirements: String,
    val durationInMonths: Int,
    val totalSpots: Int,
    val applicationDeadline: String,
    val isActive: Boolean,
    val companyName: String?,
    val location: String?,
    val workModel: String?
)
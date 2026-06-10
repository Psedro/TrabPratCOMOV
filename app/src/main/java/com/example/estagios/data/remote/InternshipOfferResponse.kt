package com.example.estagios.data.remote

data class InternshipOfferResponse(
    val _id: String,
    val name: String = "",
    val description: String = "",
    val requirements: String = "",
    val durationInMonths: Int = 0,
    val totalSpots: Int = 0,
    val applicationDeadline: String? = null,
    val isActive: Boolean = true,
    val companyName: String? = null,
    val location: String? = null,
    val workModel: String? = null
)

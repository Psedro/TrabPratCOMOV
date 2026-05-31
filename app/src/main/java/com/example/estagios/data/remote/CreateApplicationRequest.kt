package com.example.estagios.data.remote

data class CreateApplicationRequest(
    val internshipOfferId: String,
    val coverLetter: String = "Candidatura submetida através da aplicação móvel.",
    val availableFrom: String = "2026-06-01"
)
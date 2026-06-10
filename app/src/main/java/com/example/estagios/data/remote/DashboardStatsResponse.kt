package com.example.estagios.data.remote

data class DashboardStatsResponse(
    val nomeUtilizador: String,
    val candidaturasAtivas: Int,
    val candidaturasAceites: Int,
    val mensagensNovas: Int
)
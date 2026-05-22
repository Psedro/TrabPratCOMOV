package com.example.estagios.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Ofertas : Screen("ofertas")
    object Candidaturas : Screen("candidaturas")
    object Mensagens : Screen("mensagens")
    object DetalheOferta : Screen("detalhe_oferta/{ofertaId}") {
        fun createRoute(ofertaId: Int) = "detalhe_oferta/$ofertaId"
    }
}

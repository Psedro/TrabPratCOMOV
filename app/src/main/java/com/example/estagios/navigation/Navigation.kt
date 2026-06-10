package com.example.estagios.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Registo : Screen("registo")
    object Home : Screen("home")
    object Ofertas : Screen("ofertas")
    object MinhasOfertas : Screen("minhas_ofertas")
    object Candidaturas : Screen("candidaturas")
    object Mensagens : Screen("mensagens")
    object DetalheOferta : Screen("detalhe_oferta/{ofertaId}") {
        fun createRoute(ofertaId: Int) = "detalhe_oferta/$ofertaId"
    }
    object CriarOferta : Screen("criarOferta")
}

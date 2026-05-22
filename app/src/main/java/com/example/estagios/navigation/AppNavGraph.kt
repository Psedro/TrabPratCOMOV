package com.example.estagios.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.estagios.ui.screens.*

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegistar = {
                    // Navegar para ecrã de registo (a implementar)
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onVerOfertas = { navController.navigate(Screen.Ofertas.route) },
                onMinhasCandidaturas = { navController.navigate(Screen.Candidaturas.route) },
                onMensagens = { navController.navigate(Screen.Mensagens.route) }
            )
        }

        composable(Screen.Ofertas.route) {
            OfertasScreen(
                onVoltar = { navController.popBackStack() },
                onCandidatar = { oferta ->
                    // Lógica de candidatura
                }
            )
        }

        composable(Screen.Candidaturas.route) {
            CandidaturasScreen(
                onVoltar = { navController.popBackStack() }
            )
        }

        composable(Screen.Mensagens.route) {
            MensagensScreen(
                onVoltar = { navController.popBackStack() }
            )
        }
    }
}

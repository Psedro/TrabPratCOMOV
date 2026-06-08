package com.example.estagios.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.estagios.data.remote.RetrofitClient
import com.example.estagios.ui.screens.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.estagios.model.LoginRequest
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { email, password ->
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.login(
                                LoginRequest(
                                    email = email,
                                    password = password
                                )
                            )

                            if (response.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Login efetuado com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) {
                                        inclusive = true
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Email ou palavra-passe inválidos.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Erro de ligação ao servidor.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onRegistar = {
                    navController.navigate(Screen.Registo.route)
                }
            )
        }

        composable(Screen.Registo.route) {
            RegistoScreen(
                onRegistar = { pedidoRegisto ->
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.register(pedidoRegisto)

                            if (response.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Registo criado com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                navController.popBackStack()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Erro no registo. Verifica os dados.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Erro de ligação ao servidor.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onVoltarLogin = {
                    navController.popBackStack()
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

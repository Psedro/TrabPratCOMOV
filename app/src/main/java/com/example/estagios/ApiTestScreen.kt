package com.example.estagios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.estagios.data.remote.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun ApiTestScreen() {
    var resultado by remember { mutableStateOf("Ainda não testado") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = resultado)

        Button(
            onClick = {
                scope.launch {
                    try {
                        val health = RetrofitClient.apiService.checkHealth()
                        val roles = RetrofitClient.apiService.getRoles()

                        resultado = buildString {
                            append("${health.status} - ${health.message}\n\n")
                            append("Roles encontrados:\n")
                            roles.forEach { role ->
                                append("- ${role.name}: ${role.description}\n")
                            }
                        }
                    } catch (e: Exception) {
                        resultado = "Erro: ${e.message}"
                    }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Testar ligação à API")
        }
    }
}
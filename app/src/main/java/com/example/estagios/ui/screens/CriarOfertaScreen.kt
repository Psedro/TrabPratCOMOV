package com.example.estagios.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estagios.data.remote.RetrofitClient
import com.example.estagios.model.CreateInternshipOfferRequest
import com.example.estagios.ui.common.ProfileTopBar
import kotlinx.coroutines.launch

@Composable
fun CriarOfertaScreen(
    nomeUtilizador: String,
    onVoltar: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var durationInMonths by remember { mutableStateOf("") }
    var totalSpots by remember { mutableStateOf("") }
    var applicationDeadline by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        ProfileTopBar(
            nome = nomeUtilizador.uppercase(),
            mostrarNotificacoes = false,
            onVoltar = onVoltar,
            onLogout = onLogout
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Oferta",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(28.dp))

            CampoOferta(
                titulo = "Nome da oferta",
                valor = name,
                onValueChange = { name = it }
            )

            CampoOferta(
                titulo = "Empresa",
                valor = companyName,
                onValueChange = { companyName = it }
            )

            CampoOferta(
                titulo = "Descrição",
                valor = description,
                onValueChange = { description = it }
            )

            CampoOferta(
                titulo = "Requisitos",
                valor = requirements,
                onValueChange = { requirements = it }
            )

            CampoOferta(
                titulo = "Localização",
                valor = location,
                onValueChange = { location = it }
            )

            CampoOferta(
                titulo = "Duração em meses",
                valor = durationInMonths,
                keyboardType = KeyboardType.Number,
                onValueChange = { durationInMonths = it }
            )

            CampoOferta(
                titulo = "Número de vagas",
                valor = totalSpots,
                keyboardType = KeyboardType.Number,
                onValueChange = { totalSpots = it }
            )

            CampoOferta(
                titulo = "Prazo de candidatura",
                valor = applicationDeadline,
                placeholder = "YYYY-MM-DD",
                onValueChange = { applicationDeadline = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = onVoltar,
                    enabled = !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Voltar")
                }

                Button(
                    onClick = {
                        if (
                            name.isBlank() ||
                            companyName.isBlank() ||
                            description.isBlank() ||
                            requirements.isBlank() ||
                            location.isBlank() ||
                            durationInMonths.isBlank() ||
                            totalSpots.isBlank() ||
                            applicationDeadline.isBlank()
                        ) {
                            Toast.makeText(
                                context,
                                "Preenche todos os campos.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        val duration = durationInMonths.toIntOrNull()
                        val spots = totalSpots.toIntOrNull()

                        if (duration == null || spots == null) {
                            Toast.makeText(
                                context,
                                "Duração e número de vagas têm de ser números válidos.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        scope.launch {
                            try {
                                isLoading = true

                                val request = CreateInternshipOfferRequest(
                                    name = name,
                                    description = description,
                                    requirements = requirements,
                                    duration_in_months = duration,
                                    total_spots = spots,
                                    application_deadline = applicationDeadline,
                                    is_active = true,
                                    companyName = companyName,
                                    location = location
                                )

                                val response = RetrofitClient.apiService.criarOferta(request)

                                if (response.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Oferta criada com sucesso!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    onVoltar()
                                } else {
                                    val erro = response.errorBody()?.string()

                                    Toast.makeText(
                                        context,
                                        "Erro ${response.code()}: $erro",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Erro de ligação ao servidor: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2F80ED)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("Criar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(25.dp))
        }
    }
}

@Composable
private fun CampoOferta(
    titulo: String,
    valor: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = titulo,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = valor,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            placeholder = {
                if (placeholder.isNotBlank()) {
                    Text(placeholder)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = Color(0xFF2F80ED)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}
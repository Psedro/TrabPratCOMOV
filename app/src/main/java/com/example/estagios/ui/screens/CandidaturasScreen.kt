package com.example.estagios.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estagios.data.remote.RetrofitClient
import com.example.estagios.data.remote.StudentApplicationResponse
import com.example.estagios.ui.theme.Azul
import com.example.estagios.ui.theme.TextoEmpresa
import com.example.estagios.ui.theme.TextoSecundario

@Composable
fun CandidaturasScreen(onVoltar: () -> Unit) {
    var pesquisa by remember { mutableStateOf("") }
    var filtroAtivo by remember { mutableStateOf<String?>(null) }
    var candidaturas by remember { mutableStateOf<List<StudentApplicationResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            candidaturas = RetrofitClient.apiService.getStudentApplications()
            erro = null
        } catch (e: Exception) {
            erro = e.message
        } finally {
            isLoading = false
        }
    }

    val todas = candidaturas
    val pendentes = todas.filter { it.status == "pending" }
    val aceites = todas.filter { it.status == "accepted" }
    val emProgresso = todas.filter { it.status == "ongoing" || it.status == "in_progress" }

    val candidaturasFiltradas = when (filtroAtivo) {
        "pending" -> pendentes
        "accepted" -> aceites
        "progress" -> emProgresso
        else -> todas
    }.filter {
        pesquisa.isEmpty() ||
                it.offerTitle.contains(pesquisa, ignoreCase = true) ||
                (it.companyName ?: "").contains(pesquisa, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBarComPerfil(nome = "PEDRO SOUSA", mostrarVoltar = true, onVoltar = onVoltar)

        OutlinedTextField(
            value = pesquisa,
            onValueChange = { pesquisa = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = TextoSecundario)
            },
            placeholder = { Text("Pesquisar...", color = TextoSecundario) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFEEEEEE),
                focusedBorderColor = Azul
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                FiltroTab(
                    texto = "Todas(${todas.size})",
                    ativo = filtroAtivo == null,
                    onClick = { filtroAtivo = null },
                    modifier = Modifier.weight(1f)
                )
                FiltroTab(
                    texto = "Pendente (${pendentes.size})",
                    ativo = filtroAtivo == "pending",
                    onClick = { filtroAtivo = "pending" },
                    modifier = Modifier.weight(1f)
                )
                FiltroTab(
                    texto = "Aceite (${aceites.size})",
                    ativo = filtroAtivo == "accepted",
                    onClick = { filtroAtivo = "accepted" },
                    modifier = Modifier.weight(1f)
                )
                FiltroTab(
                    texto = "Em progresso (${emProgresso.size})",
                    ativo = filtroAtivo == "progress",
                    onClick = { filtroAtivo = "progress" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Azul)
                }
            }

            erro != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Erro ao carregar candidaturas: $erro")
                }
            }

            candidaturasFiltradas.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sem candidaturas")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(candidaturasFiltradas) { candidatura ->
                        CandidaturaCard(candidatura = candidatura)
                    }
                }
            }
        }
    }
}

@Composable
fun FiltroTab(texto: String, ativo: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (ativo) Color.White else Color.Transparent,
            contentColor = Color.Black
        ),
        elevation = if (ativo) ButtonDefaults.buttonElevation(2.dp) else ButtonDefaults.buttonElevation(0.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Text(
            texto,
            fontSize = 10.sp,
            fontWeight = if (ativo) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
fun CandidaturaCard(candidatura: StudentApplicationResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    candidatura.offerTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF2B5CE6)
                )
                Text(
                    candidatura.companyName ?: "Empresa não definida",
                    fontSize = 13.sp,
                    color = TextoEmpresa
                )
            }
            EstadoBadge(status = candidatura.status)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextoSecundario)
            Text("Candidatura enviada a ${formatDate(candidatura.appliedDate)}", fontSize = 12.sp, color = TextoSecundario)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextoSecundario)
            Text(candidatura.cvName, fontSize = 12.sp, color = TextoSecundario)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))
        ) {
            Text("Ver detalhes", fontSize = 13.sp)
        }
    }
}

@Composable
fun EstadoBadge(status: String) {
    val (texto, cor, corTexto, icone) = when (status) {
        "accepted" -> Quadruple("Aceite", Color(0xFFE8F5E9), Color(0xFF2E7D32), "✅")
        "ongoing", "in_progress" -> Quadruple("Em progresso", Color(0xFFE3F2FD), Color(0xFF1565C0), "🔄")
        else -> Quadruple("Pendente", Color(0xFFFFFDE7), Color(0xFFF57F17), "⏱")
    }

    Box(
        modifier = Modifier
            .background(cor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$icone $texto",
            color = corTexto,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatDate(date: String): String {
    return if (date.length >= 10) date.substring(0, 10) else date
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
package com.example.estagios.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estagios.data.remote.AuthSession
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
            val userId = AuthSession.userId

            if (userId == null) {
                erro = "Utilizador não autenticado"
                isLoading = false
                return@LaunchedEffect
            }

            candidaturas = RetrofitClient.apiService.getStudentApplications(userId)
            erro = null
        } catch (e: Exception) {
            erro = e.message ?: "Erro ao carregar candidaturas"
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
        TopBarComPerfil(
            nome = AuthSession.nome?.uppercase() ?: "ALUNO",
            mostrarVoltar = true,
            onVoltar = onVoltar
        )

        OutlinedTextField(
            value = pesquisa,
            onValueChange = { pesquisa = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = TextoSecundario
                )
            },
            placeholder = {
                Text("Pesquisar...", color = TextoSecundario)
            },
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
                    texto = "Todas (${todas.size})",
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
                    texto = "Progresso (${emProgresso.size})",
                    ativo = filtroAtivo == "progress",
                    onClick = { filtroAtivo = "progress" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Azul)
                }
            }

            erro != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Erro ao carregar candidaturas: $erro")
                }
            }

            candidaturasFiltradas.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
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
fun FiltroTab(
    texto: String,
    ativo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (ativo) Color.White else Color.Transparent,
            contentColor = Color.Black
        ),
        elevation = if (ativo) {
            ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        } else {
            ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        },
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Text(
            text = texto,
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
                    text = candidatura.offerTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF2B5CE6)
                )

                Text(
                    text = candidatura.companyName ?: "Empresa não definida",
                    fontSize = 13.sp,
                    color = TextoEmpresa
                )
            }

            EstadoBadge(status = candidatura.status)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = TextoSecundario
            )

            Text(
                text = "Candidatura enviada a ${formatDate(candidatura.appliedDate)}",
                fontSize = 12.sp,
                color = TextoSecundario
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = TextoSecundario
            )

            Text(
                text = candidatura.cvName,
                fontSize = 12.sp,
                color = TextoSecundario
            )
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
    val estado = when (status) {
        "accepted" -> EstadoVisual(
            texto = "Aceite",
            corFundo = Color(0xFFE8F5E9),
            corTexto = Color(0xFF2E7D32),
            icone = "✅"
        )

        "ongoing", "in_progress" -> EstadoVisual(
            texto = "Em progresso",
            corFundo = Color(0xFFE3F2FD),
            corTexto = Color(0xFF1565C0),
            icone = "🔄"
        )

        else -> EstadoVisual(
            texto = "Pendente",
            corFundo = Color(0xFFFFFDE7),
            corTexto = Color(0xFFF57F17),
            icone = "⏱"
        )
    }

    Box(
        modifier = Modifier
            .background(estado.corFundo, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${estado.icone} ${estado.texto}",
            color = estado.corTexto,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatDate(date: String): String {
    return if (date.length >= 10) {
        date.substring(0, 10)
    } else {
        date
    }
}

private data class EstadoVisual(
    val texto: String,
    val corFundo: Color,
    val corTexto: Color,
    val icone: String
)
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
import androidx.compose.ui.window.Dialog
import com.example.estagios.data.remote.RetrofitClient
import com.example.estagios.data.remote.StudentApplicationResponse
import com.example.estagios.data.remote.UpdateApplicationStatusRequest
import com.example.estagios.ui.common.ProfileTopBar
import com.example.estagios.ui.theme.Azul
import com.example.estagios.ui.theme.TextoEmpresa
import com.example.estagios.ui.theme.TextoSecundario
import kotlinx.coroutines.launch

@Composable
fun CandidaturasScreen(
    userId: String,
    nomeUtilizador: String,
    tipoUtilizador: TipoUtilizador,
    onVoltar: () -> Unit,
    onLogout: () -> Unit
) {
    var pesquisa by remember { mutableStateOf("") }
    var filtroAtivo by remember { mutableStateOf<String?>(null) }
    var candidaturas by remember { mutableStateOf<List<StudentApplicationResponse>>(emptyList()) }
    var candidaturaSelecionada by remember { mutableStateOf<StudentApplicationResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId, tipoUtilizador, refreshKey) {
        try {
            isLoading = true

            if (userId.isBlank()) {
                erro = "UserId do utilizador logado está vazio"
                return@LaunchedEffect
            }

            candidaturas = when (tipoUtilizador) {
                TipoUtilizador.ALUNO -> {
                    RetrofitClient.apiService.getStudentApplications(userId)
                }

                TipoUtilizador.EMPRESA -> {
                    RetrofitClient.apiService.getCompanyApplications(userId)
                }

                TipoUtilizador.DOCENTE -> {
                    emptyList()
                }
            }

            erro = null
        } catch (e: Exception) {
            erro = e.message ?: "Erro desconhecido"
        } finally {
            isLoading = false
        }
    }

    val todas = candidaturas
    val pendentes = todas.filter { it.status == "pending" }
    val aceites = todas.filter { it.status == "accepted" }
    val recusadas = todas.filter { it.status == "rejected" }
    val emProgresso = todas.filter { it.status == "ongoing" || it.status == "in_progress" }

    val candidaturasFiltradas = when (filtroAtivo) {
        "pending" -> pendentes
        "accepted" -> aceites
        "rejected" -> recusadas
        "progress" -> emProgresso
        else -> todas
    }.filter {
        pesquisa.isBlank() ||
                it.offerTitle.contains(pesquisa, ignoreCase = true) ||
                (it.companyName ?: "").contains(pesquisa, ignoreCase = true) ||
                (it.studentName ?: "").contains(pesquisa, ignoreCase = true) ||
                (it.studentEmail ?: "").contains(pesquisa, ignoreCase = true)
    }

    fun atualizarEstadoCandidatura(candidatura: StudentApplicationResponse, novoEstado: String) {
        scope.launch {
            try {
                RetrofitClient.apiService.updateApplicationStatus(
                    applicationId = candidatura._id,
                    request = UpdateApplicationStatusRequest(
                        userId = userId,
                        status = novoEstado
                    )
                )

                snackbarHostState.showSnackbar(
                    if (novoEstado == "accepted") {
                        "Candidatura aceite com sucesso"
                    } else {
                        "Candidatura recusada com sucesso"
                    }
                )

                candidaturaSelecionada = null
                refreshKey++
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erro ao atualizar candidatura")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ProfileTopBar(
                nome = nomeUtilizador.uppercase(),
                mostrarNotificacoes = false,
                onVoltar = onVoltar,
                onLogout = onLogout
            )

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
                        texto = "Pend.(${pendentes.size})",
                        ativo = filtroAtivo == "pending",
                        onClick = { filtroAtivo = "pending" },
                        modifier = Modifier.weight(1f)
                    )

                    FiltroTab(
                        texto = "Aceite(${aceites.size})",
                        ativo = filtroAtivo == "accepted",
                        onClick = { filtroAtivo = "accepted" },
                        modifier = Modifier.weight(1f)
                    )

                    FiltroTab(
                        texto = "Rec.(${recusadas.size})",
                        ativo = filtroAtivo == "rejected",
                        onClick = { filtroAtivo = "rejected" },
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
                            CandidaturaCard(
                                candidatura = candidatura,
                                tipoUtilizador = tipoUtilizador,
                                onVerDetalhes = {
                                    candidaturaSelecionada = candidatura
                                },
                                onAceitar = {
                                    atualizarEstadoCandidatura(candidatura, "accepted")
                                },
                                onRecusar = {
                                    atualizarEstadoCandidatura(candidatura, "rejected")
                                }
                            )
                        }
                    }
                }
            }
        }

        candidaturaSelecionada?.let { candidatura ->
            DetalheCandidaturaDialog(
                candidatura = candidatura,
                tipoUtilizador = tipoUtilizador,
                onDismiss = { candidaturaSelecionada = null },
                onAceitar = {
                    atualizarEstadoCandidatura(candidatura, "accepted")
                },
                onRecusar = {
                    atualizarEstadoCandidatura(candidatura, "rejected")
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
            ButtonDefaults.buttonElevation(2.dp)
        } else {
            ButtonDefaults.buttonElevation(0.dp)
        },
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
fun CandidaturaCard(
    candidatura: StudentApplicationResponse,
    tipoUtilizador: TipoUtilizador,
    onVerDetalhes: () -> Unit,
    onAceitar: () -> Unit,
    onRecusar: () -> Unit
) {
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
                    text = if (tipoUtilizador == TipoUtilizador.EMPRESA) {
                        candidatura.studentName?.takeIf { it.isNotBlank() }
                            ?: candidatura.studentEmail
                            ?: "Aluno não definido"
                    } else {
                        candidatura.companyName ?: "Empresa não definida"
                    },
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
                Icons.Outlined.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = TextoSecundario
            )

            Text(
                "Candidatura enviada a ${formatDate(candidatura.appliedDate)}",
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
                Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = TextoSecundario
            )

            Text(candidatura.cvName, fontSize = 12.sp, color = TextoSecundario)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (tipoUtilizador == TipoUtilizador.EMPRESA && candidatura.status == "pending") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRecusar,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
                ) {
                    Text("Recusar", fontSize = 13.sp)
                }

                Button(
                    onClick = onAceitar,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Aceitar", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onVerDetalhes,
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
fun DetalheCandidaturaDialog(
    candidatura: StudentApplicationResponse,
    tipoUtilizador: TipoUtilizador,
    onDismiss: () -> Unit,
    onAceitar: () -> Unit,
    onRecusar: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Text(
                text = "Detalhes da candidatura",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF2B5CE6)
            )

            Spacer(modifier = Modifier.height(14.dp))

            DetalheLinha("Oferta", candidatura.offerTitle)

            DetalheLinha(
                "Empresa",
                candidatura.companyName ?: "Empresa não definida"
            )

            DetalheLinha(
                "Localização",
                candidatura.location ?: "Local não definido"
            )

            DetalheLinha(
                "Data",
                formatDate(candidatura.appliedDate)
            )

            DetalheLinha(
                "Currículo",
                candidatura.cvName
            )

            if (tipoUtilizador == TipoUtilizador.EMPRESA) {
                DetalheLinha(
                    "Aluno",
                    candidatura.studentName?.takeIf { it.isNotBlank() } ?: "Aluno não definido"
                )

                DetalheLinha(
                    "Email",
                    candidatura.studentEmail ?: "Email não definido"
                )
            }

            DetalheLinha(
                "Estado",
                estadoTexto(candidatura.status)
            )

            candidatura.offerDescription?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Descrição da oferta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (tipoUtilizador == TipoUtilizador.EMPRESA && candidatura.status == "pending") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRecusar,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
                    ) {
                        Text("Recusar", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onAceitar,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Aceitar", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))
            ) {
                Text("Fechar", fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun DetalheLinha(label: String, valor: String) {
    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = label,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = TextoSecundario
    )

    Text(
        text = valor,
        fontSize = 14.sp,
        color = Color.Black
    )
}

@Composable
fun EstadoBadge(status: String) {
    val (texto, cor, corTexto, icone) = when (status) {
        "accepted" -> Quadruple("Aceite", Color(0xFFE8F5E9), Color(0xFF2E7D32), "✅")
        "rejected" -> Quadruple("Recusada", Color(0xFFFFEBEE), Color(0xFFC62828), "❌")
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

private fun estadoTexto(status: String): String {
    return when (status) {
        "accepted" -> "Aceite"
        "rejected" -> "Recusada"
        "ongoing", "in_progress" -> "Em progresso"
        else -> "Pendente"
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
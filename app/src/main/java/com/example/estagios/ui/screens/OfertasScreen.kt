package com.example.estagios.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LocationOn
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
import com.example.estagios.data.remote.AuthSession
import com.example.estagios.data.remote.CreateApplicationRequest
import com.example.estagios.data.remote.InternshipOfferResponse
import com.example.estagios.data.remote.RetrofitClient
import com.example.estagios.ui.theme.Azul
import com.example.estagios.ui.theme.TextoEmpresa
import com.example.estagios.ui.theme.TextoSecundario
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun OfertasScreen(
    onVoltar: () -> Unit,
    onCandidatar: (InternshipOfferResponse) -> Unit = {}
) {
    var pesquisa by remember { mutableStateOf("") }
    var ofertaSelecionada by remember { mutableStateOf<InternshipOfferResponse?>(null) }
    var ofertas by remember { mutableStateOf<List<InternshipOfferResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            erro = null

            val response = RetrofitClient.apiService.getInternshipOffers()

            if (response.isSuccessful) {
                ofertas = response.body() ?: emptyList()
            } else {
                erro = "Erro ao carregar ofertas"
            }
        } catch (e: Exception) {
            erro = "Erro de ligação ao servidor"
        } finally {
            isLoading = false
        }
    }

    val ofertasFiltradas = ofertas.filter {
        pesquisa.isEmpty() ||
                it.name.contains(pesquisa, ignoreCase = true) ||
                (it.companyName ?: "").contains(pesquisa, ignoreCase = true)
    }

    fun candidatar(oferta: InternshipOfferResponse) {
        val userId = AuthSession.userId

        if (userId == null) {
            scope.launch {
                snackbarHostState.showSnackbar("Utilizador não autenticado")
            }
            return
        }

        scope.launch {
            try {
                RetrofitClient.apiService.createApplication(
                    CreateApplicationRequest(
                        internshipOfferId = oferta._id,
                        userId = userId
                    )
                )

                snackbarHostState.showSnackbar("Candidatura submetida com sucesso")
                onCandidatar(oferta)
            } catch (e: HttpException) {
                if (e.code() == 409) {
                    snackbarHostState.showSnackbar("Já te candidataste a esta oferta")
                } else {
                    snackbarHostState.showSnackbar("Erro ao submeter candidatura")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erro de ligação ao servidor")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                erro != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(erro ?: "Erro", color = Color.Red)
                    }
                }

                ofertasFiltradas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Não existem ofertas disponíveis", color = TextoSecundario)
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(ofertasFiltradas) { oferta ->
                            OfertaCard(
                                oferta = oferta,
                                onVerDetalhes = { ofertaSelecionada = oferta },
                                onCandidatar = { candidatar(oferta) }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        ofertaSelecionada?.let { oferta ->
            DetalheOfertaDialog(
                oferta = oferta,
                onDismiss = { ofertaSelecionada = null },
                onCandidatar = {
                    candidatar(oferta)
                    ofertaSelecionada = null
                }
            )
        }
    }
}

@Composable
fun OfertaCard(
    oferta: InternshipOfferResponse,
    onVerDetalhes: () -> Unit,
    onCandidatar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = oferta.name,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF2B5CE6)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = oferta.companyName ?: "Empresa não indicada",
            fontSize = 14.sp,
            color = TextoEmpresa
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (oferta.description.length > 120) oferta.description.take(120) + "..." else oferta.description,
            fontSize = 13.sp,
            color = Color.Black,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TagChip(oferta.workModel ?: "Modelo não indicado")
            TagChip(" meses")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoItem(Icons.Outlined.LocationOn, oferta.location ?: "Local não indicado")
            InfoItem(Icons.Outlined.CalendarMonth, " meses")
            InfoItem(Icons.Outlined.Group, " vagas")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onVerDetalhes,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))
            ) {
                Text("Ver detalhes", fontSize = 13.sp)
            }

            Button(
                onClick = onCandidatar,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Azul)
            ) {
                Text("Candidatar", fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun TagChip(texto: String) {
    Box(
        modifier = Modifier
            .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(texto, fontSize = 11.sp, color = Color.DarkGray)
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = TextoSecundario
        )
        Text(texto, fontSize = 11.sp, color = TextoSecundario)
    }
}

@Composable
fun DetalheOfertaDialog(
    oferta: InternshipOfferResponse,
    onDismiss: () -> Unit,
    onCandidatar: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = oferta.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2B5CE6)
                )

                Text(
                    text = oferta.companyName ?: "Empresa não indicada",
                    fontSize = 14.sp,
                    color = TextoEmpresa
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = oferta.description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Requisitos:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = oferta.requirements,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))
                ) {
                    Text("Fechar", fontSize = 13.sp)
                }

                Button(
                    onClick = onCandidatar,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Azul)
                ) {
                    Text("Candidatar", fontSize = 13.sp)
                }
            }
        }
    }
}

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
import com.example.estagios.data.remote.InternshipOfferResponse
import com.example.estagios.data.remote.RetrofitClient
import com.example.estagios.ui.theme.Azul
import com.example.estagios.ui.theme.TextoEmpresa
import com.example.estagios.ui.theme.TextoSecundario

//ligar o botao candidatar
import com.example.estagios.data.remote.CreateApplicationRequest
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
    var erro by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun candidatar(oferta: InternshipOfferResponse) {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.createApplication(
                    CreateApplicationRequest(
                        internshipOfferId = oferta._id
                    )
                )

                snackbarHostState.showSnackbar(response.message)
                onCandidatar(oferta)
            } catch (e: HttpException) {
                if (e.code() == 409) {
                    snackbarHostState.showSnackbar("Já existe uma candidatura para esta oferta")
                } else {
                    snackbarHostState.showSnackbar("Erro ao criar candidatura")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erro: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            ofertas = RetrofitClient.apiService.getInternshipOffers()
            erro = null
        } catch (e: Exception) {
            erro = e.message
        } finally {
            isLoading = false
        }
    }

    val ofertasFiltradas = ofertas.filter {
        pesquisa.isEmpty() ||
                it.name.contains(pesquisa, ignoreCase = true) ||
                (it.companyName ?: "").contains(pesquisa, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarComPerfil(
                nome = "PEDRO SOUSA",
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
                        CircularProgressIndicator(color = Azul)
                    }
                }

                erro != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Erro ao carregar ofertas: $erro")
                    }
                }

                ofertasFiltradas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sem ofertas disponíveis")
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
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
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
        Text(oferta.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2B5CE6))
        Spacer(modifier = Modifier.height(2.dp))
        Text(oferta.companyName ?: "Empresa não definida", fontSize = 14.sp, color = TextoEmpresa)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = oferta.description.take(120) + "...",
            fontSize = 13.sp,
            color = Color.Black,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TagChip(oferta.workModel ?: "Modelo não definido")
            TagChip("Vagas: ${oferta.totalSpots}")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoItem(Icons.Outlined.LocationOn, oferta.location ?: "Localização")
            InfoItem(Icons.Outlined.CalendarMonth, "${oferta.durationInMonths} meses")
            InfoItem(Icons.Outlined.Group, "${oferta.totalSpots} pessoas")
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
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = TextoSecundario)
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
                Text(oferta.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2B5CE6))
                Text(oferta.companyName ?: "Empresa não definida", fontSize = 14.sp, color = TextoEmpresa)
                Spacer(modifier = Modifier.height(4.dp))
                Text(oferta.description.take(120) + "...", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TagChip(oferta.workModel ?: "Modelo não definido")
                    TagChip("Vagas: ${oferta.totalSpots}")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoItem(Icons.Outlined.LocationOn, oferta.location ?: "Localização")
                    InfoItem(Icons.Outlined.CalendarMonth, "${oferta.durationInMonths} meses")
                    InfoItem(Icons.Outlined.Group, "${oferta.totalSpots} pessoas")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(oferta.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2B5CE6))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(oferta.companyName ?: "Empresa não definida", fontSize = 13.sp, color = TextoEmpresa)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(oferta.description, fontSize = 13.sp, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Requisitos:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(oferta.requirements, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 16.dp),
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
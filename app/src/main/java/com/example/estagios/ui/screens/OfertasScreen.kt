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
import com.example.estagios.model.InternshipOfferResponse
import com.example.estagios.data.remote.RetrofitClient
import com.example.estagios.ui.theme.Azul
import com.example.estagios.ui.theme.TextoEmpresa
import com.example.estagios.ui.theme.TextoSecundario
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.estagios.utils.toTextRequestBody
import com.example.estagios.utils.uriToMultipart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


import com.example.estagios.ui.common.ProfileTopBar
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun OfertasScreen(
    nomeUtilizador: String,
    userId: String,
    minhasOfertas: Boolean = false,
    onVoltar: () -> Unit,
    onLogout: () -> Unit,
    onCandidatar: (InternshipOfferResponse) -> Unit = {}
) {
    var pesquisa by remember { mutableStateOf("") }
    var ofertaSelecionada by remember { mutableStateOf<InternshipOfferResponse?>(null) }
    var ofertaCandidatura by remember { mutableStateOf<InternshipOfferResponse?>(null) }
    var cvUri by remember { mutableStateOf<Uri?>(null) }
    var aEnviarCandidatura by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var ofertas by remember { mutableStateOf<List<InternshipOfferResponse>>(emptyList()) }
    var erro by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val escolherCvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        cvUri = uri

        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    fun abrirPopupCandidatura(oferta: InternshipOfferResponse) {
        ofertaCandidatura = oferta
        cvUri = null
    }

    fun submeterCandidatura(oferta: InternshipOfferResponse) {
        val offerId = oferta._id
        val uri = cvUri

        if (userId.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("Utilizador não encontrado. Faz login novamente.")
            }
            return
        }

        if (offerId.isNullOrBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("Esta oferta não tem um ID válido")
            }
            return
        }

        if (uri == null) {
            scope.launch {
                snackbarHostState.showSnackbar("Seleciona um currículo primeiro")
            }
            return
        }

        scope.launch {
            try {
                aEnviarCandidatura = true

                val availableFrom = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(Date())

                val response = RetrofitClient.apiService.candidatarOferta(
                    userId = userId.toTextRequestBody(),
                    internshipOfferId = offerId.toTextRequestBody(),
                    availableFrom = availableFrom.toTextRequestBody(),
                    cv = context.uriToMultipart(uri)
                )

                if (response.isSuccessful) {
                    snackbarHostState.showSnackbar("Candidatura submetida com sucesso")
                    ofertaCandidatura = null
                    cvUri = null
                    onCandidatar(oferta)
                } else {
                    val erroBackend = response.errorBody()?.string()
                    snackbarHostState.showSnackbar(
                        "Erro ${response.code()}: ${erroBackend ?: "Erro ao submeter candidatura"}"
                    )
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erro: ${e.message}")
            } finally {
                aEnviarCandidatura = false
            }
        }
    }

    LaunchedEffect(userId, minhasOfertas) {
        try {
            isLoading = true

            if (minhasOfertas && userId.isBlank()) {
                erro = "UserId da empresa logada está vazio"
                return@LaunchedEffect
            }

            val response = if (minhasOfertas) {
                RetrofitClient.apiService.getCompanyOffers(userId)
            } else {
                RetrofitClient.apiService.getInternshipOffers()
            }

            if (response.isSuccessful) {
                ofertas = response.body() ?: emptyList()
                erro = null
            } else {
                erro = "Erro ${response.code()}: ${response.message()}"
            }

        } catch (e: Exception) {
            erro = e.message
        } finally {
            isLoading = false
        }
    }

    val ofertasFiltradas = ofertas.filter {
        pesquisa.isEmpty() ||
                it.name.orEmpty().contains(pesquisa, ignoreCase = true) ||
                it.companyName.orEmpty().contains(pesquisa, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                                minhasOfertas = minhasOfertas,
                                onVerDetalhes = { ofertaSelecionada = oferta },
                                onCandidatar = { abrirPopupCandidatura(oferta) }
                            )
                        }
                    }
                }
            }
        }

        ofertaSelecionada?.let { oferta ->
            DetalheOfertaDialog(
                oferta = oferta,
                minhasOfertas = minhasOfertas,
                onDismiss = { ofertaSelecionada = null },
                onCandidatar = {
                    ofertaSelecionada = null
                    abrirPopupCandidatura(oferta)
                }
            )
        }
        ofertaCandidatura?.let { oferta ->
            AlertDialog(
                onDismissRequest = {
                    if (!aEnviarCandidatura) {
                        ofertaCandidatura = null
                        cvUri = null
                    }
                },
                title = {
                    Text("Candidatar à oferta")
                },
                text = {
                    Column {
                        Text("Seleciona o teu currículo para submeter a candidatura.")

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                escolherCvLauncher.launch(
                                    arrayOf(
                                        "application/pdf",
                                        "application/msword",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                    )
                                )
                            }
                        ) {
                            Text(
                                if (cvUri == null) {
                                    "Selecionar currículo"
                                } else {
                                    "Currículo selecionado"
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = cvUri != null && !aEnviarCandidatura,
                        onClick = {
                            submeterCandidatura(oferta)
                        }
                    ) {
                        Text(
                            if (aEnviarCandidatura) {
                                "A enviar..."
                            } else {
                                "Submeter"
                            }
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !aEnviarCandidatura,
                        onClick = {
                            ofertaCandidatura = null
                            cvUri = null
                        }
                    ) {
                        Text("Cancelar")
                    }
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
    minhasOfertas: Boolean,
    onVerDetalhes: () -> Unit,
    onCandidatar: () -> Unit
) {
    val nome = oferta.name ?: "Oferta sem título"
    val empresa = oferta.companyName ?: "Empresa não definida"
    val descricao = oferta.description.orEmpty()
    val descricaoCurta = if (descricao.length > 120) {
        descricao.take(120) + "..."
    } else {
        descricao.ifBlank { "Sem descrição disponível." }
    }

    val modeloTrabalho = oferta.workModel ?: "Modelo não definido"
    val localizacao = oferta.location ?: "Localização não definida"
    val vagas = oferta.totalSpots ?: 0
    val duracao = oferta.durationInMonths ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(nome, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2B5CE6))

        Spacer(modifier = Modifier.height(2.dp))

        Text(empresa, fontSize = 14.sp, color = TextoEmpresa)

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = descricaoCurta,
            fontSize = 13.sp,
            color = Color.Black,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TagChip(modeloTrabalho)
            TagChip("Vagas: $vagas")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoItem(Icons.Outlined.LocationOn, localizacao)
            InfoItem(Icons.Outlined.CalendarMonth, "$duracao meses")
            InfoItem(Icons.Outlined.Group, "$vagas pessoas")
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

            if (!minhasOfertas) {
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
    minhasOfertas: Boolean,
    onDismiss: () -> Unit,
    onCandidatar: () -> Unit
) {
    val nome = oferta.name ?: "Oferta sem título"
    val empresa = oferta.companyName ?: "Empresa não definida"
    val descricao = oferta.description.orEmpty()
    val descricaoCurta = if (descricao.length > 120) {
        descricao.take(120) + "..."
    } else {
        descricao.ifBlank { "Sem descrição disponível." }
    }

    val requisitos = oferta.requirements ?: "Requisitos não definidos."
    val modeloTrabalho = oferta.workModel ?: "Modelo não definido"
    val localizacao = oferta.location ?: "Localização não definida"
    val vagas = oferta.totalSpots ?: 0
    val duracao = oferta.durationInMonths ?: 0

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(nome, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2B5CE6))

                Text(empresa, fontSize = 14.sp, color = TextoEmpresa)

                Spacer(modifier = Modifier.height(4.dp))

                Text(descricaoCurta, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TagChip(modeloTrabalho)
                    TagChip("Vagas: $vagas")
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoItem(Icons.Outlined.LocationOn, localizacao)
                    InfoItem(Icons.Outlined.CalendarMonth, "$duracao meses")
                    InfoItem(Icons.Outlined.Group, "$vagas pessoas")
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(nome, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2B5CE6))

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(empresa, fontSize = 13.sp, color = TextoEmpresa)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(descricao.ifBlank { "Sem descrição disponível." }, fontSize = 13.sp, lineHeight = 18.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Requisitos:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Text(requisitos, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

                if (!minhasOfertas) {
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
}
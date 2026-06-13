package com.example.estagios.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.estagios.data.remote.CreateSupervisionRequest
import com.example.estagios.data.remote.RetrofitClient
import com.example.estagios.data.remote.StudentApplicationResponse
import com.example.estagios.data.remote.SupervisionRequestResponse
import com.example.estagios.data.remote.TeacherResponse
import com.example.estagios.data.remote.UpdateApplicationStatusRequest
import com.example.estagios.data.remote.UpdateSupervisionRequestStatusRequest
import kotlinx.coroutines.launch
import com.example.estagios.ui.common.ProfileTopBar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.estagios.utils.toTextRequestBody
import com.example.estagios.utils.uriToMultipart


private val Azul = Color(0xFF2B5CE6)
private val TextoEmpresa = Color(0xFF555555)
private val TextoSecundario = Color(0xFF777777)

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

    var docentes by remember { mutableStateOf<List<TeacherResponse>>(emptyList()) }
    var pedidosOrientacao by remember { mutableStateOf<List<SupervisionRequestResponse>>(emptyList()) }
    var candidaturaParaOrientador by remember { mutableStateOf<StudentApplicationResponse?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var candidaturaParaEliminar by remember { mutableStateOf<StudentApplicationResponse?>(null) }
    var candidaturaParaEditar by remember { mutableStateOf<StudentApplicationResponse?>(null) }
    var cvEditarUri by remember { mutableStateOf<Uri?>(null) }
    var aEliminarCandidatura by remember { mutableStateOf(false) }
    var aEditarCandidatura by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(userId, tipoUtilizador, refreshKey) {
        try {
            isLoading = true

            if (userId.isBlank()) {
                erro = "UserId do utilizador logado está vazio"
                return@LaunchedEffect
            }

            when (tipoUtilizador) {
                TipoUtilizador.ALUNO -> {
                    candidaturas = RetrofitClient.apiService.getStudentApplications(userId)
                    docentes = RetrofitClient.apiService.getTeachers()
                    pedidosOrientacao = RetrofitClient.apiService.getSupervisionRequests(
                        studentUserId = userId
                    )
                }

                TipoUtilizador.EMPRESA -> {
                    candidaturas = RetrofitClient.apiService.getCompanyApplications(userId)
                    docentes = emptyList()
                    pedidosOrientacao = emptyList()
                }

                TipoUtilizador.DOCENTE -> {
                    candidaturas = emptyList()
                    docentes = emptyList()
                    pedidosOrientacao = RetrofitClient.apiService.getSupervisionRequests(
                        teacherUserId = userId
                    )
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

    val pedidosOrientacaoFiltrados = pedidosOrientacao.filter {
        pesquisa.isBlank() ||
                it.offerTitle.contains(pesquisa, ignoreCase = true) ||
                (it.companyName ?: "").contains(pesquisa, ignoreCase = true) ||
                (it.studentName ?: "").contains(pesquisa, ignoreCase = true) ||
                (it.studentEmail ?: "").contains(pesquisa, ignoreCase = true) ||
                (it.teacherName ?: "").contains(pesquisa, ignoreCase = true) ||
                (it.teacherEmail ?: "").contains(pesquisa, ignoreCase = true)
    }

    val escolherCvEditarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        cvEditarUri = uri

        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
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

    fun pedirOrientador(candidatura: StudentApplicationResponse, docente: TeacherResponse) {
        scope.launch {
            try {
                RetrofitClient.apiService.createSupervisionRequest(
                    CreateSupervisionRequest(
                        applicationId = candidatura._id,
                        studentUserId = userId,
                        teacherUserId = docente._id
                    )
                )

                candidaturaParaOrientador = null

                snackbarHostState.showSnackbar(
                    "Pedido de orientação enviado ao docente"
                )

                refreshKey++
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    "Erro ao enviar pedido de orientação"
                )
            }
        }
    }
    fun eliminarCandidatura(candidatura: StudentApplicationResponse) {
        scope.launch {
            try {
                aEliminarCandidatura = true

                val response = RetrofitClient.apiService.deleteApplication(
                    applicationId = candidatura._id,
                    userId = userId
                )

                if (response.isSuccessful) {
                    candidaturas = candidaturas.filter { it._id != candidatura._id }
                    candidaturaParaEliminar = null
                    candidaturaSelecionada = null

                    snackbarHostState.showSnackbar("Candidatura eliminada com sucesso")
                } else {
                    val erroBackend = response.errorBody()?.string()
                    snackbarHostState.showSnackbar(
                        "Erro ${response.code()}: ${erroBackend ?: "Erro ao eliminar candidatura"}"
                    )
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erro: ${e.message}")
            } finally {
                aEliminarCandidatura = false
            }
        }
    }

    fun editarCandidatura(candidatura: StudentApplicationResponse) {
        val uri = cvEditarUri

        if (uri == null) {
            scope.launch {
                snackbarHostState.showSnackbar("Seleciona um currículo primeiro")
            }
            return
        }

        scope.launch {
            try {
                aEditarCandidatura = true

                val response = RetrofitClient.apiService.updateApplicationCv(
                    applicationId = candidatura._id,
                    userId = userId.toTextRequestBody(),
                    cv = context.uriToMultipart(uri)
                )

                if (response.isSuccessful) {
                    candidaturaParaEditar = null
                    cvEditarUri = null
                    refreshKey++

                    snackbarHostState.showSnackbar("Candidatura atualizada com sucesso")
                } else {
                    val erroBackend = response.errorBody()?.string()
                    snackbarHostState.showSnackbar(
                        "Erro ${response.code()}: ${erroBackend ?: "Erro ao editar candidatura"}"
                    )
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erro: ${e.message}")
            } finally {
                aEditarCandidatura = false
            }
        }
    }

    fun atualizarPedidoOrientacao(pedido: SupervisionRequestResponse, novoEstado: String) {
        scope.launch {
            try {
                RetrofitClient.apiService.updateSupervisionRequestStatus(
                    requestId = pedido._id,
                    request = UpdateSupervisionRequestStatusRequest(
                        teacherUserId = userId,
                        status = novoEstado
                    )
                )

                snackbarHostState.showSnackbar(
                    if (novoEstado == "accepted") {
                        "Pedido de orientação aceite"
                    } else {
                        "Pedido de orientação recusado"
                    }
                )

                refreshKey++
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    "Erro ao atualizar pedido de orientação"
                )
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

            Text(
                text = if (tipoUtilizador == TipoUtilizador.DOCENTE) {
                    "PEDIDOS DE ORIENTAÇÃO"
                } else {
                    "CANDIDATURAS"
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Azul
            )

            OutlinedTextField(
                value = pesquisa,
                onValueChange = { pesquisa = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = TextoSecundario
                    )
                },
                placeholder = {
                    Text(
                        text = if (tipoUtilizador == TipoUtilizador.DOCENTE) {
                            "Pesquisar pedidos..."
                        } else {
                            "Pesquisar..."
                        },
                        color = TextoSecundario
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFEEEEEE),
                    focusedBorderColor = Azul
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (tipoUtilizador != TipoUtilizador.DOCENTE) {
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
            }

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

                tipoUtilizador == TipoUtilizador.DOCENTE -> {
                    if (pedidosOrientacaoFiltrados.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sem pedidos de orientação")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(pedidosOrientacaoFiltrados) { pedido ->
                                PedidoOrientacaoCard(
                                    pedido = pedido,
                                    onAceitar = {
                                        atualizarPedidoOrientacao(pedido, "accepted")
                                    },
                                    onRecusar = {
                                        atualizarPedidoOrientacao(pedido, "rejected")
                                    }
                                )
                            }
                        }
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
                            val pedidoOrientacao = pedidosOrientacao
                                .filter {
                                    it.applicationId == candidatura._id &&
                                            it.status != "rejected"
                                }
                                .sortedBy {
                                    if (it.status == "accepted") 0 else 1
                                }
                                .firstOrNull()

                            CandidaturaCard(
                                candidatura = candidatura,
                                tipoUtilizador = tipoUtilizador,
                                pedidoOrientacao = pedidoOrientacao,
                                onVerDetalhes = {
                                    candidaturaSelecionada = candidatura
                                },
                                onAceitar = {
                                    atualizarEstadoCandidatura(candidatura, "accepted")
                                },
                                onRecusar = {
                                    atualizarEstadoCandidatura(candidatura, "rejected")
                                },
                                onEscolherOrientador = {
                                    candidaturaParaOrientador = candidatura
                                },
                                onEditar = {
                                    candidaturaParaEditar = candidatura
                                    cvEditarUri = null
                                },
                                onEliminar = {
                                    candidaturaParaEliminar = candidatura
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

        candidaturaParaOrientador?.let { candidatura ->
            EscolherOrientadorDialog(
                candidatura = candidatura,
                docentes = docentes,
                onFechar = {
                    candidaturaParaOrientador = null
                },
                onEscolher = { docente ->
                    pedirOrientador(candidatura, docente)
                }
            )
        }
        candidaturaParaEliminar?.let { candidatura ->
            AlertDialog(
                onDismissRequest = {
                    if (!aEliminarCandidatura) {
                        candidaturaParaEliminar = null
                    }
                },
                title = {
                    Text("Eliminar candidatura")
                },
                text = {
                    Text("Tens a certeza que queres eliminar esta candidatura?")
                },
                confirmButton = {
                    Button(
                        enabled = !aEliminarCandidatura,
                        onClick = {
                            eliminarCandidatura(candidatura)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB00020)
                        )
                    ) {
                        Text(
                            if (aEliminarCandidatura) {
                                "A eliminar..."
                            } else {
                                "Eliminar"
                            }
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !aEliminarCandidatura,
                        onClick = {
                            candidaturaParaEliminar = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        candidaturaParaEditar?.let { candidatura ->
            AlertDialog(
                onDismissRequest = {
                    if (!aEditarCandidatura) {
                        candidaturaParaEditar = null
                        cvEditarUri = null
                    }
                },
                title = {
                    Text("Editar candidatura")
                },
                text = {
                    Column {
                        Text("Seleciona um novo currículo para esta candidatura.")

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                escolherCvEditarLauncher.launch(
                                    arrayOf(
                                        "application/pdf",
                                        "application/msword",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                    )
                                )
                            }
                        ) {
                            Text(
                                if (cvEditarUri == null) {
                                    "Selecionar novo currículo"
                                } else {
                                    "Novo currículo selecionado"
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = cvEditarUri != null && !aEditarCandidatura,
                        onClick = {
                            editarCandidatura(candidatura)
                        }
                    ) {
                        Text(
                            if (aEditarCandidatura) {
                                "A guardar..."
                            } else {
                                "Guardar"
                            }
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !aEditarCandidatura,
                        onClick = {
                            candidaturaParaEditar = null
                            cvEditarUri = null
                        }
                    ) {
                        Text("Cancelar")
                    }
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
    pedidoOrientacao: SupervisionRequestResponse?,
    onVerDetalhes: () -> Unit,
    onAceitar: () -> Unit,
    onRecusar: () -> Unit,
    onEscolherOrientador: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
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

        if (tipoUtilizador == TipoUtilizador.ALUNO && candidatura.status == "accepted") {
            when (pedidoOrientacao?.status) {
                "pending" -> {
                    Text(
                        text = "Pedido de orientação pendente: ${pedidoOrientacao.teacherName ?: "Docente"}",
                        fontSize = 12.sp,
                        color = Color(0xFFF57F17),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                "accepted" -> {
                    Text(
                        text = "Orientador aceite: ${pedidoOrientacao.teacherName ?: "Docente"}",
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                else -> {
                    Button(
                        onClick = onEscolherOrientador,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Azul)
                    ) {
                        Text("Escolher orientador", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
        if (tipoUtilizador == TipoUtilizador.ALUNO && candidatura.status == "pending") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onEditar,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Azul)
                ) {
                    Text("Editar", fontSize = 13.sp)
                }

                Button(
                    onClick = onEliminar,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
                ) {
                    Text("Eliminar", fontSize = 13.sp)
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
fun EscolherOrientadorDialog(
    candidatura: StudentApplicationResponse,
    docentes: List<TeacherResponse>,
    onFechar: () -> Unit,
    onEscolher: (TeacherResponse) -> Unit
) {
    AlertDialog(
        onDismissRequest = onFechar,
        title = {
            Text(
                text = "Escolher orientador",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = candidatura.offerTitle,
                    fontSize = 13.sp,
                    color = TextoSecundario
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (docentes.isEmpty()) {
                    Text(
                        text = "Não existem docentes disponíveis.",
                        fontSize = 13.sp,
                        color = TextoSecundario
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(docentes) { docente ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        Color(0xFFE0E0E0),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = docente.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                Text(
                                    text = docente.email,
                                    fontSize = 12.sp,
                                    color = TextoSecundario
                                )

                                Text(
                                    text = docente.department ?: "Departamento não definido",
                                    fontSize = 12.sp,
                                    color = TextoSecundario
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        onEscolher(docente)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Azul)
                                ) {
                                    Text("Enviar pedido")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onFechar) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun PedidoOrientacaoCard(
    pedido: SupervisionRequestResponse,
    onAceitar: () -> Unit,
    onRecusar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = pedido.offerTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Azul
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Aluno: ${pedido.studentName?.takeIf { it.isNotBlank() } ?: pedido.studentEmail ?: "Aluno não definido"}",
            fontSize = 13.sp,
            color = TextoEmpresa
        )

        Text(
            text = "Empresa: ${pedido.companyName ?: "Empresa não definida"}",
            fontSize = 13.sp,
            color = TextoSecundario
        )

        Spacer(modifier = Modifier.height(10.dp))

        EstadoPedidoOrientacaoBadge(pedido.status)

        Spacer(modifier = Modifier.height(12.dp))

        if (pedido.status == "pending") {
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
        }
    }
}

@Composable
fun EstadoPedidoOrientacaoBadge(status: String) {
    val (texto, cor, corTexto, icone) = when (status) {
        "accepted" -> Quadruple("Aceite", Color(0xFFE8F5E9), Color(0xFF2E7D32), "✅")
        "rejected" -> Quadruple("Recusado", Color(0xFFFFEBEE), Color(0xFFC62828), "❌")
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

@Composable
fun DetalheCandidaturaDialog(
    candidatura: StudentApplicationResponse,
    tipoUtilizador: TipoUtilizador,
    onDismiss: () -> Unit,
    onAceitar: () -> Unit,
    onRecusar: () -> Unit
) {
    val context = LocalContext.current

    fun abrirCurriculo() {
        val caminhoCv = candidatura.cvPath

        if (!caminhoCv.isNullOrBlank()) {
            val url = "http://10.0.2.2:3000$caminhoCv"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }

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

            if (!candidatura.cvPath.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { abrirCurriculo() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B5CE6))
                ) {
                    Text("Abrir currículo", fontSize = 13.sp)
                }
            }

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
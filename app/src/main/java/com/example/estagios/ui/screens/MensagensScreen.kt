package com.example.estagios.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estagios.data.remote.ApplicationConversationResponse
import com.example.estagios.data.remote.ApplicationMessageResponse
import com.example.estagios.data.remote.RetrofitClient
import com.example.estagios.data.remote.SendApplicationMessageRequest
import com.example.estagios.ui.common.ProfileTopBar
import com.example.estagios.ui.theme.Azul
import com.example.estagios.ui.theme.TextoSecundario
import kotlinx.coroutines.launch

@Composable
fun MensagensScreen(
    userId: String,
    nomeUtilizador: String,
    tipoUtilizador: TipoUtilizador,
    onVoltar: () -> Unit,
    onLogout: () -> Unit
) {
    var conversas by remember { mutableStateOf<List<ApplicationConversationResponse>>(emptyList()) }
    var conversaSelecionada by remember { mutableStateOf<ApplicationConversationResponse?>(null) }
    var mensagens by remember { mutableStateOf<List<ApplicationMessageResponse>>(emptyList()) }
    var textoMensagem by remember { mutableStateOf("") }
    var isLoadingConversas by remember { mutableStateOf(true) }
    var isLoadingMensagens by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    var refreshConversas by remember { mutableStateOf(0) }
    var refreshMensagens by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(userId, refreshConversas) {
        try {
            isLoadingConversas = true

            if (userId.isBlank()) {
                erro = "UserId vazio"
                return@LaunchedEffect
            }

            conversas = RetrofitClient.apiService.getMessageConversations(userId)
            erro = null
        } catch (e: Exception) {
            erro = e.message ?: "Erro ao carregar conversas"
        } finally {
            isLoadingConversas = false
        }
    }

    LaunchedEffect(conversaSelecionada?.applicationId, refreshMensagens) {
        val conversa = conversaSelecionada ?: return@LaunchedEffect

        try {
            isLoadingMensagens = true

            mensagens = RetrofitClient.apiService.getApplicationMessages(
                applicationId = conversa.applicationId,
                userId = userId
            )

            erro = null
        } catch (e: Exception) {
            erro = e.message ?: "Erro ao carregar mensagens"
        } finally {
            isLoadingMensagens = false
        }
    }

    LaunchedEffect(mensagens.size) {
        if (mensagens.isNotEmpty()) {
            listState.animateScrollToItem(mensagens.size - 1)
        }
    }

    fun enviarMensagem() {
        val conversa = conversaSelecionada ?: return

        if (textoMensagem.isBlank()) {
            return
        }

        scope.launch {
            try {
                RetrofitClient.apiService.sendApplicationMessage(
                    applicationId = conversa.applicationId,
                    request = SendApplicationMessageRequest(
                        senderUserId = userId,
                        content = textoMensagem
                    )
                )

                textoMensagem = ""
                refreshMensagens++
                refreshConversas++
            } catch (e: Exception) {
                erro = e.message ?: "Erro ao enviar mensagem"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        ProfileTopBar(
            nome = if (conversaSelecionada == null) {
                nomeUtilizador.uppercase()
            } else {
                obterNomeConversa(conversaSelecionada, tipoUtilizador).uppercase()
            },
            mostrarNotificacoes = false,
            onVoltar = {
                if (conversaSelecionada != null) {
                    conversaSelecionada = null
                    mensagens = emptyList()
                    textoMensagem = ""
                } else {
                    onVoltar()
                }
            },
            onLogout = onLogout
        )

        if (conversaSelecionada == null) {
            when {
                isLoadingConversas -> {
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
                        Text(
                            text = "Erro: $erro",
                            color = Color(0xFFB00020),
                            fontSize = 14.sp
                        )
                    }
                }

                conversas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ainda não existem conversas.",
                            color = TextoSecundario,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(conversas) { conversa ->
                            ConversaCard(
                                conversa = conversa,
                                tipoUtilizador = tipoUtilizador,
                                onClick = {
                                    conversaSelecionada = conversa
                                    refreshMensagens++
                                }
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    isLoadingMensagens -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Azul)
                        }
                    }

                    mensagens.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ainda não existem mensagens.\nEscreve a primeira mensagem.",
                                color = TextoSecundario,
                                fontSize = 14.sp
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(mensagens) { mensagem ->
                                BubbleMensagemReal(
                                    mensagem = mensagem,
                                    minhaMensagem = mensagem.senderUserId == userId
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = textoMensagem,
                        onValueChange = { textoMensagem = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "Escrever mensagem...",
                                color = TextoSecundario,
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFEEEEEE),
                            focusedBorderColor = Azul
                        ),
                        singleLine = true,
                        maxLines = 1
                    )

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (textoMensagem.isBlank()) Color(0xFFBBBBBB) else Azul),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { enviarMensagem() },
                            enabled = textoMensagem.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopBarMensagens(
    titulo: String,
    subtitulo: String?,
    inicialPerfil: String,
    onVoltar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF2F2F7))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVoltar) {
            Text("‹", fontSize = 28.sp, fontWeight = FontWeight.Light)
        }

        Spacer(modifier = Modifier.width(4.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo.uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )

            if (!subtitulo.isNullOrBlank()) {
                Text(
                    text = subtitulo,
                    color = TextoSecundario,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFDDDDDD)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = inicialPerfil.ifBlank { "?" },
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun ConversaCard(
    conversa: ApplicationConversationResponse,
    tipoUtilizador: TipoUtilizador,
    onClick: () -> Unit
) {
    val nomeDestino = obterNomeConversa(conversa, tipoUtilizador)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Azul),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = obterIniciais(nomeDestino),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nomeDestino,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 1
                )

                Text(
                    text = conversa.offerTitle,
                    color = TextoSecundario,
                    fontSize = 12.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = conversa.lastMessage ?: "Sem mensagens ainda",
                    color = if (conversa.lastMessage == null) TextoSecundario else Color.DarkGray,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = estadoTextoMensagem(conversa.status),
                    fontSize = 11.sp,
                    color = corEstadoMensagem(conversa.status),
                    fontWeight = FontWeight.SemiBold
                )

                conversa.lastMessageAt?.let {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = formatDateMensagem(it),
                        fontSize = 10.sp,
                        color = TextoSecundario
                    )
                }
            }
        }
    }
}

@Composable
fun BubbleMensagemReal(
    mensagem: ApplicationMessageResponse,
    minhaMensagem: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (minhaMensagem) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .background(
                    color = if (minhaMensagem) Azul else Color.White,
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (minhaMensagem) 18.dp else 4.dp,
                        bottomEnd = if (minhaMensagem) 4.dp else 18.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = mensagem.content,
                    color = if (minhaMensagem) Color.White else Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatDateMensagem(mensagem.createdAt),
                        fontSize = 10.sp,
                        color = if (minhaMensagem) {
                            Color.White.copy(alpha = 0.7f)
                        } else {
                            TextoSecundario
                        }
                    )

                    if (minhaMensagem) {
                        Text(
                            text = "✓",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

private fun obterNomeConversa(
    conversa: ApplicationConversationResponse?,
    tipoUtilizador: TipoUtilizador
): String {
    if (conversa == null) return ""

    return when (tipoUtilizador) {
        TipoUtilizador.ALUNO -> conversa.companyName ?: "Empresa"
        TipoUtilizador.EMPRESA -> {
            conversa.studentName?.takeIf { it.isNotBlank() }
                ?: conversa.studentEmail
                ?: "Aluno"
        }
        TipoUtilizador.DOCENTE -> conversa.companyName ?: "Conversa"
    }
}

private fun obterIniciais(nome: String): String {
    val partes = nome.trim().split(" ").filter { it.isNotBlank() }

    if (partes.isEmpty()) {
        return "?"
    }

    return if (partes.size == 1) {
        partes[0].take(2).uppercase()
    } else {
        "${partes[0].first()}${partes[1].first()}".uppercase()
    }
}

private fun estadoTextoMensagem(status: String): String {
    return when (status) {
        "accepted" -> "Aceite"
        "rejected" -> "Recusada"
        "ongoing", "in_progress" -> "Em progresso"
        else -> "Pendente"
    }
}

private fun corEstadoMensagem(status: String): Color {
    return when (status) {
        "accepted" -> Color(0xFF2E7D32)
        "rejected" -> Color(0xFFC62828)
        "ongoing", "in_progress" -> Color(0xFF1565C0)
        else -> Color(0xFFF57F17)
    }
}

private fun formatDateMensagem(date: String): String {
    return if (date.length >= 10) {
        date.substring(0, 10)
    } else {
        date
    }
}
package com.example.estagios.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.estagios.data.remote.MarkAllNotificationsReadRequest
import com.example.estagios.data.remote.NotificationResponse
import com.example.estagios.data.remote.RetrofitClient
import com.example.estagios.ui.common.ProfileTopBar
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(
    userId: String,
    nomeUtilizador: String,
    onVoltar: () -> Unit
) {
    var notifications by remember { mutableStateOf<List<NotificationResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun carregarNotificacoes() {
        scope.launch {
            try {
                isLoading = true
                notifications = RetrofitClient.apiService.getNotifications(userId)
                erro = null
            } catch (e: Exception) {
                erro = e.message ?: "Erro ao carregar notificações"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId) {
        carregarNotificacoes()
    }

    fun marcarComoLida(notificationId: String) {
        scope.launch {
            try {
                RetrofitClient.apiService.markNotificationAsRead(notificationId)
                notifications = notifications.map {
                    if (it._id == notificationId) {
                        it.copy(isRead = true)
                    } else {
                        it
                    }
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erro ao marcar notificação como lida")
            }
        }
    }

    fun marcarTodasComoLidas() {
        scope.launch {
            try {
                RetrofitClient.apiService.markAllNotificationsAsRead(
                    MarkAllNotificationsReadRequest(userId = userId)
                )

                notifications = notifications.map {
                    it.copy(isRead = true)
                }

                snackbarHostState.showSnackbar("Todas as notificações foram marcadas como lidas")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erro ao marcar notificações")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ProfileTopBar(
                nome = "NOTIFICAÇÕES",
                mostrarNotificacoes = false,
                onVoltar = onVoltar
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "As tuas notificações",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "${notifications.count { !it.isRead }} por ler",
                        color = Color(0xFF777777),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                TextButton(
                    onClick = { marcarTodasComoLidas() },
                    enabled = notifications.any { !it.isRead }
                ) {
                    Text("Marcar todas")
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2B5CE6))
                    }
                }

                erro != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Erro: $erro",
                            color = Color(0xFFB00020)
                        )
                    }
                }

                notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ainda não tens notificações.",
                            color = Color(0xFF777777)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications) { notification ->
                            NotificationCard(
                                notification = notification,
                                onMarcarComoLida = {
                                    marcarComoLida(notification._id)
                                }
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
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationResponse,
    onMarcarComoLida: () -> Unit
) {
    val backgroundColor = if (notification.isRead) {
        Color.White
    } else {
        Color(0xFFEAF0FF)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (notification.isRead) Color.Transparent else Color(0xFF2B5CE6),
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = notification.message,
                        color = Color(0xFF555555),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = tipoNotificacao(notification.type),
                        color = Color(0xFF777777),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (!notification.isRead) {
                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onMarcarComoLida,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Marcar como lida")
                }
            }
        }
    }
}

private fun tipoNotificacao(type: String): String {
    return when (type) {
        "new_application" -> "Nova candidatura"
        "application_status" -> "Estado da candidatura"
        "supervision_request" -> "Pedido de orientação"
        "supervision_status" -> "Orientação"
        "message" -> "Mensagem"
        else -> "Notificação"
    }
}
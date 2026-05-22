package com.example.estagios.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estagios.ui.theme.Azul
import com.example.estagios.ui.theme.CinzaBotao
import com.example.estagios.ui.theme.TextoSecundario

@Composable
fun HomeScreen(
    nomeUtilizador: String = "PEDRO SOUSA",
    candidaturasAtivas: Int = 3,
    candidaturasAceites: Int = 2,
    mensagensNovas: Int = 1,
    onVerOfertas: () -> Unit,
    onMinhasCandidaturas: () -> Unit,
    onMensagens: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        // Top Bar
        TopBarComPerfil(
            nome = nomeUtilizador,
            mostrarNotificacoes = true,
            mostrarVoltar = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            // Cards de estatísticas (linha 1)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EstatisticaCard(
                    label = "Candidaturas ativas",
                    valor = candidaturasAtivas.toString(),
                    modifier = Modifier.weight(1f)
                )
                EstatisticaCard(
                    label = "Candidaturas aceites",
                    valor = candidaturasAceites.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card mensagens (linha 2 - meia largura)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EstatisticaCard(
                    label = "Mensagens novas",
                    valor = mensagensNovas.toString(),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botões de navegação
            BotaoNavegacao(texto = "Ver Ofertas disponiveis", onClick = onVerOfertas)
            Spacer(modifier = Modifier.height(12.dp))
            BotaoNavegacao(texto = "As minhas candidaturas", onClick = onMinhasCandidaturas)
            Spacer(modifier = Modifier.height(12.dp))
            BotaoNavegacao(texto = "Mensagens", onClick = onMensagens)
        }
    }
}

@Composable
fun EstatisticaCard(label: String, valor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextoSecundario, fontSize = 13.sp)
            Text(valor, fontSize = 48.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BotaoNavegacao(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CinzaBotao)
    ) {
        Text(texto, color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun TopBarComPerfil(
    nome: String,
    mostrarNotificacoes: Boolean = false,
    mostrarVoltar: Boolean = true,
    onVoltar: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF2F2F7))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (mostrarNotificacoes) {
            IconButton(onClick = { }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notificações")
            }
        } else if (mostrarVoltar) {
            IconButton(onClick = onVoltar) {
                Text("‹", fontSize = 28.sp, fontWeight = FontWeight.Light)
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        Text(nome, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp)

        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFDDDDDD)),
            contentAlignment = Alignment.Center
        ) {
            Text("P", fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    }
}

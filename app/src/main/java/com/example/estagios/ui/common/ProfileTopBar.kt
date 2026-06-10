package com.example.estagios.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileTopBar(
    nome: String,
    mostrarNotificacoes: Boolean = true,
    onVoltar: (() -> Unit)? = null,
    onLogout: () -> Unit = {}
) {
    var menuAberto by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF2F2F7))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            mostrarNotificacoes -> {
                IconButton(onClick = { /* Notificações */ }) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notificações")
                }
            }
            onVoltar != null -> {
                IconButton(onClick = onVoltar) {
                    Text("‹", fontSize = 28.sp, fontWeight = FontWeight.Light)
                }
            }
            else -> Spacer(modifier = Modifier.size(48.dp))
        }

        Text(nome, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp)

        // Avatar com menu
        Box {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDDDDDD))
                    .clickable { menuAberto = true },
                contentAlignment = Alignment.Center
            ) {
                Text(nome.firstOrNull()?.uppercase() ?: "P", fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            DropdownMenu(
                expanded = menuAberto,
                onDismissRequest = { menuAberto = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sair", color = Color.Red) },
                    onClick = {
                        menuAberto = false
                        onLogout()
                    }
                )
            }
        }
    }
}

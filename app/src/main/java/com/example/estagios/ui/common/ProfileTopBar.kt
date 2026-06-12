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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estagios.R
import com.example.estagios.utils.LanguageManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun ProfileTopBar(
    nome: String,
    mostrarNotificacoes: Boolean = true,
    onVoltar: (() -> Unit)? = null,
    onLogout: () -> Unit = {}
) {
    var menuAberto by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notificações"
                    )
                }
            }

            onVoltar != null -> {
                IconButton(onClick = onVoltar) {
                    Text(
                        text = "‹",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            else -> {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        Text(
            text = nome,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )

        Box {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDDDDDD))
                    .clickable { menuAberto = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nome.firstOrNull()?.uppercase() ?: "P",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }

            DropdownMenu(
                expanded = menuAberto,
                onDismissRequest = { menuAberto = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.language_english))
                    },
                    onClick = {
                        menuAberto = false
                        LanguageManager.mudarParaIngles(context)
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.language_portuguese))
                    },
                    onClick = {
                        menuAberto = false
                        LanguageManager.mudarParaPortugues(context)
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.logout),
                            color = Color.Red
                        )
                    },
                    onClick = {
                        menuAberto = false
                        onLogout()
                    }
                )
            }
        }
    }
}
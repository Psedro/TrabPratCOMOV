package com.example.estagios.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.estagios.model.Mensagem
import com.example.estagios.model.SampleData
import com.example.estagios.ui.theme.Azul
import com.example.estagios.ui.theme.TextoSecundario

@Composable
fun MensagensScreen(onVoltar: () -> Unit) {
    var textoMensagem by remember { mutableStateOf("") }
    val mensagens = remember { mutableStateListOf(*SampleData.mensagens.toTypedArray()) }
    val listState = rememberLazyListState()

    LaunchedEffect(mensagens.size) {
        if (mensagens.isNotEmpty()) {
            listState.animateScrollToItem(mensagens.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        // Top bar
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
            Text(
                "MENSAGENS",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDDDDDD)),
                contentAlignment = Alignment.Center
            ) {
                Text("P", fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Cabeçalho empresa
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF2F2F7))
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCC0000)),
                contentAlignment = Alignment.Center
            ) {
                Text("TC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("TECHCORP SOLUTIONS", fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 0.5.sp)
        }

        // Lista de mensagens
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mensagens) { mensagem ->
                BubbleMensagem(mensagem = mensagem)
            }
        }

        // Campo de input
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
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Add, contentDescription = "Anexar", tint = TextoSecundario)
                }

                OutlinedTextField(
                    value = textoMensagem,
                    onValueChange = { textoMensagem = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message ...", color = TextoSecundario, fontSize = 14.sp) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFEEEEEE),
                        focusedBorderColor = Azul
                    ),
                    singleLine = true,
                    maxLines = 1
                )

                // Botão enviar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Azul),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (textoMensagem.isNotBlank()) {
                                mensagens.add(
                                    Mensagem(
                                        id = mensagens.size + 1,
                                        texto = textoMensagem,
                                        hora = "agora",
                                        isEnviada = true
                                    )
                                )
                                textoMensagem = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BubbleMensagem(mensagem: Mensagem) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (mensagem.isEnviada) Alignment.End else Alignment.Start
    ) {
        if (!mensagem.isEnviada) {
            Text(mensagem.hora, fontSize = 11.sp, color = TextoSecundario, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .background(
                    color = if (mensagem.isEnviada) Azul else Color.White,
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (mensagem.isEnviada) 18.dp else 4.dp,
                        bottomEnd = if (mensagem.isEnviada) 4.dp else 18.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = mensagem.texto,
                    color = if (mensagem.isEnviada) Color.White else Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                if (mensagem.isEnviada) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(mensagem.hora, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                        Text("✓", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }

        if (!mensagem.isEnviada) {
            Text(mensagem.hora, fontSize = 11.sp, color = TextoSecundario, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
        }
    }
}

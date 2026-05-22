package com.example.estagios.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estagios.model.Candidatura
import com.example.estagios.model.EstadoCandidatura
import com.example.estagios.model.SampleData
import com.example.estagios.ui.theme.Azul
import com.example.estagios.ui.theme.TextoEmpresa
import com.example.estagios.ui.theme.TextoSecundario

@Composable
fun CandidaturasScreen(onVoltar: () -> Unit) {
    var pesquisa by remember { mutableStateOf("") }
    var filtroAtivo by remember { mutableStateOf<EstadoCandidatura?>(null) }

    val todas = SampleData.candidaturas
    val pendentes = todas.filter { it.estado == EstadoCandidatura.PENDENTE }
    val aceites = todas.filter { it.estado == EstadoCandidatura.ACEITE }
    val emProgresso = todas.filter { it.estado == EstadoCandidatura.EM_PROGRESSO }

    val candidaturasFiltradas = when (filtroAtivo) {
        EstadoCandidatura.PENDENTE -> pendentes
        EstadoCandidatura.ACEITE -> aceites
        EstadoCandidatura.EM_PROGRESSO -> emProgresso
        null -> todas
    }.filter {
        pesquisa.isEmpty() ||
        it.oferta.titulo.contains(pesquisa, ignoreCase = true) ||
        it.oferta.empresa.contains(pesquisa, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBarComPerfil(nome = "PEDRO SOUSA", mostrarVoltar = true, onVoltar = onVoltar)

        // Pesquisa
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

        Spacer(modifier = Modifier.height(8.dp))

        // Filtros de estado
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
                    texto = "Pendente (${pendentes.size})",
                    ativo = filtroAtivo == EstadoCandidatura.PENDENTE,
                    onClick = { filtroAtivo = EstadoCandidatura.PENDENTE },
                    modifier = Modifier.weight(1f)
                )
                FiltroTab(
                    texto = "Aceite (${aceites.size})",
                    ativo = filtroAtivo == EstadoCandidatura.ACEITE,
                    onClick = { filtroAtivo = EstadoCandidatura.ACEITE },
                    modifier = Modifier.weight(1f)
                )
                FiltroTab(
                    texto = "Em progresso (${emProgresso.size})",
                    ativo = filtroAtivo == EstadoCandidatura.EM_PROGRESSO,
                    onClick = { filtroAtivo = EstadoCandidatura.EM_PROGRESSO },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(candidaturasFiltradas) { candidatura ->
                CandidaturaCard(candidatura = candidatura)
            }
        }
    }
}

@Composable
fun FiltroTab(texto: String, ativo: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (ativo) Color.White else Color.Transparent,
            contentColor = Color.Black
        ),
        elevation = if (ativo) ButtonDefaults.buttonElevation(2.dp) else ButtonDefaults.buttonElevation(0.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Text(texto, fontSize = 10.sp, fontWeight = if (ativo) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
    }
}

@Composable
fun CandidaturaCard(candidatura: Candidatura) {
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
                    candidatura.oferta.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF2B5CE6)
                )
                Text(candidatura.oferta.empresa, fontSize = 13.sp, color = TextoEmpresa)
            }
            EstadoBadge(estado = candidatura.estado)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextoSecundario)
            Text("Candidatura enviada a ${candidatura.dataEnvio}", fontSize = 12.sp, color = TextoSecundario)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextoSecundario)
            Text(candidatura.cvNome, fontSize = 12.sp, color = TextoSecundario)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))
        ) {
            Text("Ver detalhes", fontSize = 13.sp)
        }
    }
}

@Composable
fun EstadoBadge(estado: EstadoCandidatura) {
    val (cor, corTexto, icone) = when (estado) {
        EstadoCandidatura.ACEITE -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "✅")
        EstadoCandidatura.PENDENTE -> Triple(Color(0xFFFFFDE7), Color(0xFFF57F17), "⏱")
        EstadoCandidatura.EM_PROGRESSO -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "🔄")
    }

    Box(
        modifier = Modifier
            .background(cor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$icone ${estado.label}",
            color = corTexto,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

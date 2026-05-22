package com.example.estagios.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Azul = Color(0xFF2979FF)
val AzulEscuro = Color(0xFF1565C0)
val Cinza = Color(0xFFF2F2F7)
val CinzaCard = Color(0xFFE8E8EE)
val CinzaBotao = Color(0xFFD9D9D9)
val TextoPrimario = Color(0xFF000000)
val TextoSecundario = Color(0xFF8E8E93)
val TextoEmpresa = Color(0xFF6B8CE8)
val Verde = Color(0xFF34C759)
val Amarelo = Color(0xFFFFCC00)
val Branco = Color(0xFFFFFFFF)
val FundoChat = Color(0xFFF2F2F7)

private val ColorScheme = lightColorScheme(
    primary = Azul,
    background = Branco,
    surface = Branco,
    onPrimary = Branco,
    onBackground = TextoPrimario,
    onSurface = TextoPrimario
)

@Composable
fun EstagiosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content
    )
}

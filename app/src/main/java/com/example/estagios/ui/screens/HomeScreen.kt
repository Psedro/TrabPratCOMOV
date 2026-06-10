package com.example.estagios.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estagios.ui.common.ProfileTopBar
import com.example.estagios.ui.theme.CinzaBotao
import com.example.estagios.ui.theme.TextoSecundario

enum class TipoUtilizador {
    ALUNO,
    DOCENTE,
    EMPRESA
}

@Composable
fun HomeScreen(
    tipoUtilizador: TipoUtilizador = TipoUtilizador.ALUNO,
    nomeUtilizador: String = "UTILIZADOR",

    candidaturasAtivas: Int = 3,
    candidaturasAceites: Int = 2,
    mensagensNovas: Int = 1,

    ofertasTotais: Int = 3,
    candidaturas: Int = 2,
    estagiosADecorrer: Int = 1,

    ofertasEmpresa: Int = 1,
    candidaturasRecebidas: Int = 1,
    candidaturasPendentes: Int = 1,

    onVerOfertas: () -> Unit = {},
    onMinhasCandidaturas: () -> Unit = {},
    onMensagens: () -> Unit,
    onLogout: () -> Unit,

    onOfertasEstagio: () -> Unit = {},
    onEstagiosOrientados: () -> Unit = {},

    onCriarOferta: () -> Unit = {},
    onVerCandidaturas: () -> Unit = {},
    onMinhasOfertas: () -> Unit = {}
) {
    val estatisticas = when (tipoUtilizador) {
        TipoUtilizador.ALUNO -> listOf(
            "Candidaturas ativas" to candidaturasAtivas.toString(),
            "Candidaturas aceites" to candidaturasAceites.toString(),
            "Mensagens novas" to mensagensNovas.toString()
        )

        TipoUtilizador.DOCENTE -> listOf(
            "Ofertas totais" to ofertasTotais.toString(),
            "Candidaturas" to candidaturas.toString(),
            "Estágios a decorrer" to estagiosADecorrer.toString()
        )

        TipoUtilizador.EMPRESA -> listOf(
            "Ofertas" to ofertasEmpresa.toString(),
            "Candidaturas recebidas" to candidaturasRecebidas.toString(),
            "Candidaturas pendentes" to candidaturasPendentes.toString()
        )
    }

    val botoes = when (tipoUtilizador) {
        TipoUtilizador.ALUNO -> listOf(
            "Ver Ofertas disponíveis" to onVerOfertas,
            "As minhas candidaturas" to onMinhasCandidaturas,
            "Mensagens" to onMensagens
        )

        TipoUtilizador.DOCENTE -> listOf(
            "Ofertas de Estágio" to onOfertasEstagio,
            "Estágios Orientados" to onEstagiosOrientados,
            "Mensagens" to onMensagens
        )

        TipoUtilizador.EMPRESA -> listOf(
            "Criar oferta" to onCriarOferta,
            "Ver Candidaturas" to onVerCandidaturas,
            "As minhas ofertas" to onMinhasOfertas,
            "Mensagens" to onMensagens
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        ProfileTopBar(
            nome = nomeUtilizador.uppercase(),
            mostrarNotificacoes = true,
            onLogout = onLogout
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EstatisticaCard(
                    label = estatisticas[0].first,
                    valor = estatisticas[0].second,
                    modifier = Modifier.weight(1f)
                )

                EstatisticaCard(
                    label = estatisticas[1].first,
                    valor = estatisticas[1].second,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EstatisticaCard(
                    label = estatisticas[2].first,
                    valor = estatisticas[2].second,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            botoes.forEachIndexed { index, botao ->
                BotaoNavegacao(
                    texto = botao.first,
                    onClick = botao.second
                )

                if (index != botoes.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun EstatisticaCard(
    label: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = TextoSecundario,
                fontSize = 13.sp
            )

            Text(
                text = valor,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BotaoNavegacao(
    texto: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CinzaBotao
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(
            text = texto,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}
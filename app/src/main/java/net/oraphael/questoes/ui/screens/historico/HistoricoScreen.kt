package net.oraphael.questoes.ui.screens.historico

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.oraphael.questoes.data.db.SessaoResumo
import net.oraphael.questoes.data.repo.SessaoRepository
import net.oraphael.questoes.ui.theme.QuestoesRadii
import net.oraphael.questoes.ui.theme.QuestoesTokens
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Tela 5 (Mapa de Navegação, §1) — lista as sessões já concluídas (mais recente primeiro).
 * Tocar numa sessão reabre o mesmo Resumo (Tela 4) usado ao fim de um Quiz — o Resumo não
 * é exclusivo do fim de sessão, então [ResultadoScreen][net.oraphael.questoes.ui.screens.resultado.ResultadoScreen]
 * não precisa de nenhum parâmetro extra pra isso.
 *
 * Sessões sem [net.oraphael.questoes.data.db.SessaoEntity.tempoTotalSessaoMs] (abandonadas
 * no meio do Quiz, nunca chegaram no "Ver resultado") não aparecem aqui — ver
 * `SessaoDao.listarResumo`.
 */
@Composable
fun HistoricoScreen(
    sessaoRepository: SessaoRepository,
    onAbrirSessao: (Long) -> Unit,
    onRevisarPorTagClick: () -> Unit,
    onVoltarHome: () -> Unit,
) {
    var sessoes by remember { mutableStateOf<List<SessaoResumo>>(emptyList()) }
    LaunchedEffect(Unit) { sessoes = sessaoRepository.listarResumoSessoes() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Histórico",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (sessoes.size >= 2) {
            GraficoEvolucao(sessoes = sessoes)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        if (sessoes.isNotEmpty()) {
            OutlinedButton(onClick = onRevisarPorTagClick, modifier = Modifier.fillMaxWidth()) {
                Text("Revisar por tag")
            }
        }

        if (sessoes.isEmpty()) {
            Text(
                text = "Nenhuma sessão concluída ainda.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sessoes.forEach { sessao ->
                    SessaoRow(sessao = sessao, onClick = { onAbrirSessao(sessao.id) })
                }
            }
        }

        OutlinedButton(onClick = onVoltarHome, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar")
        }
    }
}

@Composable
private fun SessaoRow(sessao: SessaoResumo, onClick: () -> Unit) {
    val modoLabel = if (sessao.modo == "simulado") {
        sessao.cargoSimulado?.let { "Simulado — $it" } ?: "Simulado"
    } else {
        "Livre"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QuestoesRadii.controle))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(QuestoesRadii.controle))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = modoLabel, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${sessao.acertos}/${sessao.totalQuestoes} acertos · ${formatarData(sessao.dataHoraInicio)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = "›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val formatoData = SimpleDateFormat("dd/MM HH:mm", Locale.forLanguageTag("pt-BR"))

private fun formatarData(ms: Long): String = formatoData.format(ms)

/**
 * Evolução do % de acertos ao longo das sessões concluídas, mais antiga à esquerda —
 * Canvas próprio em vez de lib de gráfico (decisão do usuário, pós-MVP), pra não somar
 * dependência de UI externa num projeto que só tem o Coil. Pede 2+ sessões porque uma
 * linha com 1 ponto não mostra evolução nenhuma — o chamador garante isso.
 */
@Composable
private fun GraficoEvolucao(sessoes: List<SessaoResumo>) {
    val percentuais = remember(sessoes) {
        sessoes.sortedBy { it.dataHoraInicio }.map { sessao ->
            if (sessao.totalQuestoes > 0) (sessao.acertos * 100f) / sessao.totalQuestoes else 0f
        }
    }

    val corLinha = MaterialTheme.colorScheme.primary
    val corPreenchimento = QuestoesTokens.cores.marcoDouradoSuave
    val corGrade = MaterialTheme.colorScheme.outlineVariant

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "EVOLUÇÃO · % DE ACERTOS POR SESSÃO",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(QuestoesRadii.cartao))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
        ) {
            val alturaUtil = size.height
            val larguraUtil = size.width
            fun y(percentual: Float) = alturaUtil - (percentual / 100f) * alturaUtil

            listOf(0f, 50f, 100f).forEach { marca ->
                drawLine(
                    color = corGrade,
                    start = Offset(0f, y(marca)),
                    end = Offset(larguraUtil, y(marca)),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val passo = if (percentuais.size > 1) larguraUtil / (percentuais.size - 1) else 0f
            val pontos = percentuais.mapIndexed { i, p -> Offset(i * passo, y(p)) }

            val caminho = Path().apply {
                moveTo(pontos.first().x, pontos.first().y)
                pontos.drop(1).forEach { lineTo(it.x, it.y) }
            }
            val area = Path().apply {
                addPath(caminho)
                lineTo(pontos.last().x, alturaUtil)
                lineTo(pontos.first().x, alturaUtil)
                close()
            }
            drawPath(area, color = corPreenchimento)
            drawPath(
                caminho,
                color = corLinha,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            pontos.forEach { ponto -> drawCircle(color = corLinha, radius = 3.dp.toPx(), center = ponto) }
        }
        Text(
            text = "Última: ${percentuais.last().toInt()}% · Média: ${percentuais.average().toInt()}% · " +
                "Melhor: ${percentuais.max().toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

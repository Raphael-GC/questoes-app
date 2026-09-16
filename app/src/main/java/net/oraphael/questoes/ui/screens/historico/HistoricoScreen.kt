package net.oraphael.questoes.ui.screens.historico

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.oraphael.questoes.data.db.SessaoResumo
import net.oraphael.questoes.data.repo.SessaoRepository
import net.oraphael.questoes.ui.theme.QuestoesRadii
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

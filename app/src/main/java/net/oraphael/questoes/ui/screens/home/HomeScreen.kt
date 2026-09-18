package net.oraphael.questoes.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.oraphael.questoes.data.db.DisciplinaContagem
import net.oraphael.questoes.data.repo.QuestaoRepository
import net.oraphael.questoes.ui.theme.QuestoesRadii
import net.oraphael.questoes.ui.theme.QuestoesTokens

/**
 * As 5 disciplinas do banco, na ordem fixa da Home (Mapa de Navegação, Fase 2 — Tela 1).
 * O id é a chave de [DisciplinaContagem.disciplinaId] — os mesmos nomes usados na
 * importação em QuestoesApplication.kt; o rótulo é só o texto exibido no grid.
 */
private val ORDEM_DISCIPLINAS = listOf(
    "geografia" to "geografia",
    "gerais" to "gerais",
    "portugues" to "português",
    "educacao-infantil" to "ed. infantil",
    "educacao-especial" to "ed. especial",
)

/**
 * Tela 1 (Mapa de Navegação, Fase 2) — três portas de entrada (Livre / Simulados /
 * Histórico) mais o atalho por disciplina, que hoje leva pra Seleção · Livre "crua"
 * (sem pré-marcar a disciplina nem abrir o pop-up de tags — isso é comportamento da
 * própria tela de Seleção, que ainda é placeholder, e fica pra etapa dela).
 *
 * [repository] é lido direto aqui porque o projeto ainda não tem um framework de DI
 * (Hilt/Koin) nem uma camada de ViewModel — decisão deliberadamente mínima enquanto só
 * a Home precisa de dados; se mais telas passarem a precisar de estado/ciclo de vida
 * mais sofisticado, vale revisitar.
 */
@Composable
fun HomeScreen(
    repository: QuestaoRepository,
    onLivreClick: () -> Unit,
    onSimuladosClick: () -> Unit,
    onHistoricoClick: () -> Unit,
    onDisciplinaClick: (String) -> Unit,
) {
    var disciplinas by remember { mutableStateOf<List<DisciplinaContagem>>(emptyList()) }

    LaunchedEffect(Unit) {
        disciplinas = repository.listarDisciplinasComContagem()
    }

    val contagemPorId = remember(disciplinas) { disciplinas.associateBy { it.disciplinaId } }
    val totalQuestoes = disciplinas.sumOf { it.total }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Questões", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$totalQuestoes questões · ${ORDEM_DISCIPLINAS.size} disciplinas",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HomeCard(
                titulo = "Livre",
                subtitulo = "disciplinas, tags e quantidade",
                corFundo = QuestoesTokens.cores.marcoDouradoSuave,
                corBorda = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                corTitulo = MaterialTheme.colorScheme.primary,
                onClick = onLivreClick,
            )
            HomeCard(
                titulo = "Simulados",
                subtitulo = "PND Geografia 2026",
                corFundo = QuestoesTokens.cores.aguaSuave,
                corBorda = MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f),
                corTitulo = MaterialTheme.colorScheme.secondary,
                onClick = onSimuladosClick,
            )
            HomeCard(
                titulo = "Ver histórico",
                subtitulo = "sessões e evolução",
                corFundo = Color.Transparent,
                corBorda = MaterialTheme.colorScheme.outlineVariant,
                corTitulo = MaterialTheme.colorScheme.onSurface,
                onClick = onHistoricoClick,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "POR DISCIPLINA · TOQUE PARA VER TAGS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ORDEM_DISCIPLINAS.forEach { (id, rotulo) ->
                    DisciplinaTile(
                        quantidade = contagemPorId[id]?.total ?: 0,
                        rotulo = rotulo,
                        modifier = Modifier.weight(1f),
                        onClick = { onDisciplinaClick(id) },
                    )
                }
            }
        }
    }
}

/** Base das três portas de entrada da Home (.card / .card.accent / .card.sim / .card.plain do Catálogo). */
@Composable
private fun HomeCard(
    titulo: String,
    subtitulo: String,
    corFundo: Color,
    corBorda: Color,
    corTitulo: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QuestoesRadii.cartao))
            .background(corFundo)
            .border(1.2.dp, corBorda, RoundedCornerShape(QuestoesRadii.cartao))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, style = MaterialTheme.typography.titleLarge, color = corTitulo)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Uma célula do grid "por disciplina" (.disc-tile do Catálogo). */
@Composable
private fun DisciplinaTile(
    quantidade: Int,
    rotulo: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(QuestoesRadii.controle))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = quantidade.toString(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = rotulo,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

package net.oraphael.questoes.ui.screens.resultado

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import net.oraphael.questoes.data.db.DisciplinaTagNome
import net.oraphael.questoes.data.db.SessaoCompleta
import net.oraphael.questoes.data.repo.QuestaoRepository
import net.oraphael.questoes.data.repo.SessaoRepository
import net.oraphael.questoes.ui.theme.QuestoesRadii
import net.oraphael.questoes.ui.theme.QuestoesTokens

/** Mesma ordem/rótulo fixo das disciplinas usado na Home e na Seleção · Livre. */
private val ROTULO_DISCIPLINA = mapOf(
    "geografia" to "Geografia",
    "gerais" to "Conhecimentos gerais",
    "portugues" to "Português",
    "educacao-infantil" to "Educação Infantil",
    "educacao-especial" to "Educação Especial",
)

/**
 * Tela 4 (Mapa de Navegação, §1) — o Resumo com que o laço Pergunta→Feedback termina.
 * Não é exclusivo do fim de sessão: o Histórico (Tela 5, ainda placeholder) vai reabrir
 * esta mesma tela pra qualquer sessão passada, por isso ela só depende de [sessaoId] — não
 * assume que acabou de vir do Quiz.
 *
 * Em vez do antigo modo "Erros" (§4 do Mapa de Navegação), a lista de tags das questões
 * erradas — agrupada por disciplina, sem repetição — é o que aparece aqui pra orientar a
 * revisão.
 */
@Composable
fun ResultadoScreen(
    sessaoId: Long,
    sessaoRepository: SessaoRepository,
    questaoRepository: QuestaoRepository,
    onVoltarHome: () -> Unit,
) {
    var sessaoCompleta by remember { mutableStateOf<SessaoCompleta?>(null) }
    var tagsErradas by remember { mutableStateOf<List<DisciplinaTagNome>>(emptyList()) }

    LaunchedEffect(sessaoId) {
        val completa = sessaoRepository.buscarSessaoCompleta(sessaoId)
        sessaoCompleta = completa
        val idsErrados = completa?.tentativas?.filter { !it.acerto }?.map { it.questaoId }?.distinct().orEmpty()
        tagsErradas = questaoRepository.listarTagsDasQuestoes(idsErrados)
    }

    val completa = sessaoCompleta
    if (completa == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Carregando resultado...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val tentativas = completa.tentativas
    val total = tentativas.size
    val acertos = tentativas.count { it.acerto }
    val percentual = if (total > 0) (acertos * 100) / total else 0
    val tagsPorDisciplina = remember(tagsErradas) {
        tagsErradas.groupBy { it.disciplinaId }.toList().sortedBy { (id, _) -> ROTULO_DISCIPLINA.keys.indexOf(id) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Resultado",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "ACERTOS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = "$acertos / $total", style = MaterialTheme.typography.displaySmall)
            Text(
                text = "$percentual% · ${formatarDuracao(completa.sessao.tempoTotalSessaoMs)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "TAGS DAS QUESTÕES ERRADAS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (tagsPorDisciplina.isEmpty()) {
                Text(
                    text = "Você acertou todas as questões desta sessão!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(QuestoesRadii.cartao))
                        .background(QuestoesTokens.cores.musgoSuave)
                        .padding(14.dp),
                    textAlign = TextAlign.Center,
                )
            } else {
                tagsPorDisciplina.forEach { (disciplinaId, tags) ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = ROTULO_DISCIPLINA[disciplinaId] ?: disciplinaId,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            tags.forEach { tag -> TagChip(nome = tag.nome) }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(onClick = onVoltarHome, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar à Home")
        }
    }
}

@Composable
private fun TagChip(nome: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(QuestoesRadii.capsula))
            .background(QuestoesTokens.cores.curvaRubraSuave)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(text = nome, style = MaterialTheme.typography.labelMedium)
    }
}

private fun formatarDuracao(ms: Long?): String {
    if (ms == null) return "--:--"
    val totalSegundos = ms / 1000
    val minutos = totalSegundos / 60
    val segundos = totalSegundos % 60
    return "%d:%02d".format(minutos, segundos)
}

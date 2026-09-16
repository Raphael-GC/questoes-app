package net.oraphael.questoes.ui.screens.quiz

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
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.oraphael.questoes.data.db.AlternativaEntity
import net.oraphael.questoes.data.db.QuestaoCompleta
import net.oraphael.questoes.data.db.SessaoEntity
import net.oraphael.questoes.data.db.TentativaEntity
import net.oraphael.questoes.data.repo.QuestaoRepository
import net.oraphael.questoes.data.repo.SessaoRepository
import net.oraphael.questoes.ui.theme.QuestoesRadii
import net.oraphael.questoes.ui.theme.QuestoesTokens

/**
 * Tela 3 (Mapa de Navegação, §1) — o laço Pergunta→Feedback: cada questão da sessão
 * (já sorteada por [net.oraphael.questoes.domain.MotorSessao] e fixada em
 * [SessaoEntity.questaoIdsJson] na Tela 2/6) responde e recebe feedback imediato —
 * alternativa certa destacada, explicação, e um cronômetro por questão e por sessão —
 * antes de liberar a próxima. Só a última questão troca "Próxima questão" por "Ver
 * resultado", que fecha [SessaoEntity.tempoTotalSessaoMs] e navega pro Resumo.
 *
 * Sem imagem ainda: [net.oraphael.questoes.data.db.QuestaoEntity.possuiImagem] mostra só
 * a descrição textual por enquanto — o carregamento via Coil/GitHub (README, "Imagens")
 * é etapa própria, ainda não cabeada em nenhuma tela.
 *
 * Mesma decisão de HomeScreen/SelecaoLivreScreen: sem ViewModel, estado vive na
 * composição — o projeto ainda não tem DI.
 */
@Composable
fun QuizScreen(
    sessaoId: Long,
    questaoRepository: QuestaoRepository,
    sessaoRepository: SessaoRepository,
    onSessaoConcluida: (Long) -> Unit,
) {
    var sessao by remember { mutableStateOf<SessaoEntity?>(null) }
    var idsQuestoes by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(sessaoId) {
        val completa = sessaoRepository.buscarSessaoCompleta(sessaoId)
        sessao = completa?.sessao
        idsQuestoes = completa?.sessao?.let { Json.decodeFromString<List<String>>(it.questaoIdsJson) } ?: emptyList()
    }

    var indice by remember { mutableStateOf(0) }
    var questaoAtual by remember { mutableStateOf<QuestaoCompleta?>(null) }
    var respostaSelecionada by remember { mutableStateOf<String?>(null) }
    var inicioQuestaoMs by remember { mutableStateOf(0L) }

    LaunchedEffect(indice, idsQuestoes) {
        val id = idsQuestoes.getOrNull(indice) ?: return@LaunchedEffect
        questaoAtual = questaoRepository.buscarQuestaoCompleta(id)
        respostaSelecionada = null
        inicioQuestaoMs = System.currentTimeMillis()
    }

    val scope = rememberCoroutineScope()
    val questao = questaoAtual

    if (sessao == null || idsQuestoes.isEmpty() || questao == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Carregando questão...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val ultimaQuestao = indice == idsQuestoes.lastIndex
    val alternativas = remember(questao) { questao.alternativas.sortedBy { it.letra } }
    val respondida = respostaSelecionada != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "QUESTÃO ${indice + 1} DE ${idsQuestoes.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { (indice + 1f) / idsQuestoes.size },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
            )
        }

        Text(text = questao.questao.enunciado, style = MaterialTheme.typography.bodyLarge)

        if (questao.questao.possuiImagem) {
            Text(
                text = "🖼 ${questao.questao.imagemDesc ?: "imagem da questão"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(QuestoesRadii.controle))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            alternativas.forEach { alternativa ->
                AlternativaRow(
                    alternativa = alternativa,
                    respostaCorreta = questao.questao.respostaCorreta,
                    respostaSelecionada = respostaSelecionada,
                    respondida = respondida,
                    onClick = {
                        if (respondida) return@AlternativaRow
                        respostaSelecionada = alternativa.letra
                        val tempoQuestaoMs = System.currentTimeMillis() - inicioQuestaoMs
                        scope.launch {
                            sessaoRepository.registrarTentativa(
                                TentativaEntity(
                                    sessaoId = sessaoId,
                                    questaoId = questao.questao.id,
                                    disciplinaId = questao.questao.disciplinaId,
                                    blocoSimulado = null,
                                    respostaSelecionada = alternativa.letra,
                                    acerto = alternativa.letra == questao.questao.respostaCorreta,
                                    tempoQuestaoMs = tempoQuestaoMs,
                                    dataHora = System.currentTimeMillis(),
                                ),
                            )
                        }
                    },
                )
            }
        }

        if (respondida) {
            val acertou = respostaSelecionada == questao.questao.respostaCorreta
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(QuestoesRadii.cartao))
                    .background(if (acertou) QuestoesTokens.cores.musgoSuave else QuestoesTokens.cores.curvaRubraSuave)
                    .padding(14.dp),
            ) {
                Text(
                    text = if (acertou) "Certa!" else "Errada — a certa é \"${questao.questao.respostaCorreta}\"",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(text = questao.questao.explicacao, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                if (ultimaQuestao) {
                    scope.launch {
                        val s = sessao
                        if (s != null) {
                            sessaoRepository.concluirSessao(
                                s.copy(tempoTotalSessaoMs = System.currentTimeMillis() - s.dataHoraInicio),
                            )
                        }
                        onSessaoConcluida(sessaoId)
                    }
                } else {
                    indice += 1
                }
            },
            enabled = respondida,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (ultimaQuestao) "Ver resultado" else "Próxima questão")
        }
    }
}

@Composable
private fun AlternativaRow(
    alternativa: AlternativaEntity,
    respostaCorreta: String,
    respostaSelecionada: String?,
    respondida: Boolean,
    onClick: () -> Unit,
) {
    val ehCorreta = alternativa.letra == respostaCorreta
    val ehSelecionada = alternativa.letra == respostaSelecionada

    val corFundo: Color
    val corBorda: Color
    when {
        !respondida -> {
            corFundo = MaterialTheme.colorScheme.surface
            corBorda = MaterialTheme.colorScheme.outlineVariant
        }
        ehCorreta -> {
            corFundo = QuestoesTokens.cores.musgoSuave
            corBorda = MaterialTheme.colorScheme.tertiary
        }
        ehSelecionada -> {
            corFundo = QuestoesTokens.cores.curvaRubraSuave
            corBorda = MaterialTheme.colorScheme.error
        }
        else -> {
            corFundo = MaterialTheme.colorScheme.surface
            corBorda = MaterialTheme.colorScheme.outlineVariant
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QuestoesRadii.controle))
            .background(corFundo)
            .border(1.2.dp, corBorda, RoundedCornerShape(QuestoesRadii.controle))
            .clickable(enabled = !respondida, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = alternativa.letra,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 10.dp),
        )
        Text(
            text = alternativa.texto,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
        )
    }
}

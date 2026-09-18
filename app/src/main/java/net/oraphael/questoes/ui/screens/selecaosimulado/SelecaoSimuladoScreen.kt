package net.oraphael.questoes.ui.screens.selecaosimulado

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.oraphael.questoes.data.db.SessaoEntity
import net.oraphael.questoes.data.repo.QuestaoRepository
import net.oraphael.questoes.data.repo.SessaoRepository
import net.oraphael.questoes.domain.FiltroDisciplina
import net.oraphael.questoes.domain.MotorSessao
import net.oraphael.questoes.domain.Ordem
import net.oraphael.questoes.domain.PoolInsuficienteException
import net.oraphael.questoes.ui.theme.QuestoesRadii
import net.oraphael.questoes.ui.theme.QuestoesTokens

/**
 * O único simulado hoje: PND Geografia 2026 (domingo, 20/09/2026), no formato oficial
 * da Prova Nacional Docente (Portarias Inep nº 315/2025 e nº 323/2025) — 30 questões de
 * Formação Geral Docente + 50 do Componente Específico de Geografia, sem a redação
 * (questão discursiva), que o app não avalia.
 *
 * Ainda não existem as tabelas Concurso/Simulado/BlocoSimulado do Mapa de Navegação
 * (§5) — em vez de bloquear neste único simulado até essa infraestrutura existir, a
 * composição fixa abaixo é resolvida com o mesmo [MotorSessao.resolverLivre] do modo
 * Livre (dois blocos, cada um sua própria disciplina/quantidade), em ordem SEQUENCIAL
 * pra reproduzir a ordem da prova real (FGD primeiro, Específico depois). Quando a
 * infraestrutura de verdade existir, isto migra pra lá; até lá é a ponte mais barata
 * pra ter um simulado funcional a tempo da prova de domingo.
 */
private const val CARGO_PND_GEOGRAFIA_2026 = "PND Geografia 2026"
private const val DISCIPLINA_FGD = "pnd-fgd"
private const val DISCIPLINA_GEOGRAFIA_ESPECIFICO = "pnd-geografia"
private const val QTD_FGD = 30
private const val QTD_ESPECIFICO = 50

@Serializable
private data class FiltroSnapshot(val disciplinaId: String, val tagIds: List<Long>, val quantidade: Int)

@Composable
fun SelecaoSimuladoScreen(
    repository: QuestaoRepository,
    sessaoRepository: SessaoRepository,
    motorSessao: MotorSessao,
    onSessaoIniciada: (Long) -> Unit,
) {
    var poolFgd by remember { mutableStateOf<Int?>(null) }
    var poolEspecifico by remember { mutableStateOf<Int?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    var iniciando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        poolFgd = repository.contarPool(DISCIPLINA_FGD, emptySet())
        poolEspecifico = repository.contarPool(DISCIPLINA_GEOGRAFIA_ESPECIFICO, emptySet())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Simulados",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(QuestoesRadii.cartao))
                .background(QuestoesTokens.cores.aguaSuave)
                .border(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f), RoundedCornerShape(QuestoesRadii.cartao))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = CARGO_PND_GEOGRAFIA_2026, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
            Text(
                text = "prova de domingo, 20/09/2026 · formato oficial da PND, sem a redação",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "COMPOSIÇÃO DA PROVA · 80 QUESTÕES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BlocoRow(
                titulo = "Formação Geral Docente",
                quantidade = QTD_FGD,
                pool = poolFgd,
            )
            BlocoRow(
                titulo = "Geografia · Componente Específico",
                quantidade = QTD_ESPECIFICO,
                pool = poolEspecifico,
            )
        }

        if (erro != null) {
            Text(
                text = erro ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        Button(
            onClick = {
                erro = null
                iniciando = true
                scope.launch {
                    val filtros = listOf(
                        FiltroDisciplina(DISCIPLINA_FGD, emptySet(), QTD_FGD),
                        FiltroDisciplina(DISCIPLINA_GEOGRAFIA_ESPECIFICO, emptySet(), QTD_ESPECIFICO),
                    )
                    try {
                        val ids = motorSessao.resolverLivre(filtros, Ordem.SEQUENCIAL)
                        val sessao = SessaoEntity(
                            modo = "simulado",
                            ordem = "sequencial",
                            cargoSimulado = CARGO_PND_GEOGRAFIA_2026,
                            dataHoraInicio = System.currentTimeMillis(),
                            tempoTotalSessaoMs = null,
                            filtrosJson = Json.encodeToString(
                                filtros.map { FiltroSnapshot(it.disciplinaId, it.tagIds.toList(), it.quantidade) },
                            ),
                            questaoIdsJson = Json.encodeToString(ids),
                        )
                        val sessaoId = sessaoRepository.iniciarSessao(sessao)
                        onSessaoIniciada(sessaoId)
                    } catch (e: PoolInsuficienteException) {
                        erro = "Banco de questões do PND Geografia 2026 incompleto: \"${e.disciplinaId}\" tem só " +
                            "${e.disponivel} de ${e.pedido} questões necessárias. Atualize o banco antes de iniciar."
                    } finally {
                        iniciando = false
                    }
                }
            },
            enabled = !iniciando,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (iniciando) "Sorteando questões..." else "Iniciar simulado")
        }
    }
}

@Composable
private fun BlocoRow(titulo: String, quantidade: Int, pool: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QuestoesRadii.controle))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (pool == null) {
                    "$quantidade questões"
                } else {
                    "$quantidade questões · $pool disponíveis no banco"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (pool != null && pool < quantidade) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

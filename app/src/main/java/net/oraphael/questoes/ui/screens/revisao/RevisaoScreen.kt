package net.oraphael.questoes.ui.screens.revisao

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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.oraphael.questoes.data.db.SessaoEntity
import net.oraphael.questoes.data.db.TagErroResumo
import net.oraphael.questoes.data.repo.QuestaoRepository
import net.oraphael.questoes.data.repo.SessaoRepository
import net.oraphael.questoes.domain.FiltroDisciplina
import net.oraphael.questoes.domain.FiltroSnapshot
import net.oraphael.questoes.domain.MotorSessao
import net.oraphael.questoes.domain.Ordem
import net.oraphael.questoes.domain.PoolInsuficienteException
import net.oraphael.questoes.ui.theme.QuestoesRadii

/** Mesmo rótulo fixo das disciplinas usado na Home, Seleção · Livre e Resultado. */
private val ROTULO_DISCIPLINA = mapOf(
    "geografia" to "Geografia",
    "gerais" to "Conhecimentos gerais",
    "portugues" to "Português",
    "educacao-infantil" to "Educação Infantil",
    "educacao-especial" to "Educação Especial",
)

/** Teto de questões por sessão de revisão — evita uma sessão gigante numa tag com muito erro. */
private const val QUANTIDADE_MAXIMA = 10

/**
 * Pós-MVP: ranking das tags com mais erro em todo o histórico (não só a última sessão),
 * pra atacar pontos fracos de verdade. Tocar numa tag monta um [FiltroDisciplina] com
 * aquela tag e dispara o mesmo [MotorSessao.resolverLivre] do modo Livre — cai no mesmo
 * laço Quiz→Resultado, sem tela nova pra isso. Acessada a partir do Histórico.
 */
@Composable
fun RevisaoScreen(
    sessaoRepository: SessaoRepository,
    repository: QuestaoRepository,
    motorSessao: MotorSessao,
    onSessaoIniciada: (Long) -> Unit,
    onVoltar: () -> Unit,
) {
    var carregando by remember { mutableStateOf(true) }
    var tags by remember { mutableStateOf<List<TagErroResumo>>(emptyList()) }
    var tagIniciando by remember { mutableStateOf<Long?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        tags = sessaoRepository.listarTagsComErros()
        carregando = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Revisão por tag",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        when {
            carregando -> Text(
                text = "Carregando...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            tags.isEmpty() -> Text(
                text = "Nenhuma tag com erro ainda — continue estudando que a revisão aparece aqui.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                tags.forEach { tag ->
                    TagErroRow(
                        tag = tag,
                        iniciando = tagIniciando == tag.tagId,
                        onClick = {
                            erro = null
                            tagIniciando = tag.tagId
                            scope.launch {
                                try {
                                    val tagIds = setOf(tag.tagId)
                                    val pool = repository.contarPool(tag.disciplinaId, tagIds)
                                    val quantidade = minOf(QUANTIDADE_MAXIMA, pool)
                                    if (quantidade <= 0) {
                                        erro = "Sem questões disponíveis agora para \"${tag.nome}\"."
                                        return@launch
                                    }
                                    val filtro = FiltroDisciplina(tag.disciplinaId, tagIds, quantidade)
                                    val ids = motorSessao.resolverLivre(listOf(filtro), Ordem.ALEATORIO)
                                    val sessao = SessaoEntity(
                                        modo = "livre",
                                        ordem = "aleatorio",
                                        cargoSimulado = null,
                                        dataHoraInicio = System.currentTimeMillis(),
                                        tempoTotalSessaoMs = null,
                                        filtrosJson = Json.encodeToString(
                                            listOf(FiltroSnapshot(tag.disciplinaId, tagIds.toList(), quantidade)),
                                        ),
                                        questaoIdsJson = Json.encodeToString(ids),
                                    )
                                    val sessaoId = sessaoRepository.iniciarSessao(sessao)
                                    onSessaoIniciada(sessaoId)
                                } catch (e: PoolInsuficienteException) {
                                    erro = "Banco de questões incompleto para \"${tag.nome}\"."
                                } finally {
                                    tagIniciando = null
                                }
                            }
                        },
                    )
                }
            }
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

        OutlinedButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar")
        }
    }
}

@Composable
private fun TagErroRow(tag: TagErroResumo, iniciando: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QuestoesRadii.controle))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(QuestoesRadii.controle))
            .clickable(enabled = !iniciando, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = tag.nome, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${ROTULO_DISCIPLINA[tag.disciplinaId] ?: tag.disciplinaId} · " +
                    "${tag.erros} erro${if (tag.erros == 1) "" else "s"} em ${tag.total} tentativa${if (tag.total == 1) "" else "s"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = if (iniciando) "..." else "›",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

package net.oraphael.questoes.ui.screens.selecaolivre

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.oraphael.questoes.data.db.DisciplinaContagem
import net.oraphael.questoes.data.db.SessaoEntity
import net.oraphael.questoes.data.db.TagContagem
import net.oraphael.questoes.data.repo.QuestaoRepository
import net.oraphael.questoes.data.repo.SessaoRepository
import net.oraphael.questoes.domain.FiltroDisciplina
import net.oraphael.questoes.domain.FiltroSnapshot
import net.oraphael.questoes.domain.MotorSessao
import net.oraphael.questoes.domain.Ordem
import net.oraphael.questoes.domain.PoolInsuficienteException
import net.oraphael.questoes.ui.components.CarregandoCheck
import net.oraphael.questoes.ui.theme.QuestoesRadii
import net.oraphael.questoes.ui.theme.QuestoesTokens

/** As 5 disciplinas do banco, na mesma ordem fixa da Home (Mapa de Navegação, Tela 2). */
private val DISCIPLINAS_EM_ORDEM = listOf(
    "geografia" to "Geografia",
    "gerais" to "Conhecimentos gerais",
    "portugues" to "Português",
    "educacao-infantil" to "Educação Infantil",
    "educacao-especial" to "Educação Especial",
)

/** Filtro já confirmado no pop-up pra uma disciplina — presença no mapa = disciplina marcada. */
private data class FiltroConfirmado(val tagIds: Set<Long>, val quantidade: Int)

/**
 * Tela 2 (Mapa de Navegação, Fase 2) — marca 1+ disciplinas, cada uma com seu pop-up
 * obrigatório de tags (§3/§4), define a ordem (aleatório/sequencial) e inicia a sessão.
 * O pop-up não é uma rota — é o `ModalBottomSheet` aberto por [disciplinaEditando].
 *
 * Sem ViewModel, mesma decisão deliberada da Home enquanto o projeto não tem DI: o
 * estado de seleção vive só nesta composição.
 */
@Composable
fun SelecaoLivreScreen(
    repository: QuestaoRepository,
    sessaoRepository: SessaoRepository,
    motorSessao: MotorSessao,
    disciplinaInicial: String?,
    onSessaoIniciada: (Long) -> Unit,
    onVoltarHome: () -> Unit,
) {
    var contagens by remember { mutableStateOf<List<DisciplinaContagem>>(emptyList()) }
    LaunchedEffect(Unit) { contagens = repository.listarDisciplinasComContagem() }
    val totalPorDisciplina = remember(contagens) { contagens.associate { it.disciplinaId to it.total } }

    var selecoes by remember { mutableStateOf<Map<String, FiltroConfirmado>>(emptyMap()) }
    var ordem by remember { mutableStateOf(Ordem.ALEATORIO) }
    var disciplinaEditando by remember { mutableStateOf<String?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    var iniciando by remember { mutableStateOf(false) }

    // Pequena margem de segurança: abrir o ModalBottomSheet no mesmo frame em que esta
    // tela ainda está entrando pela transição do NavHost, em testes manuais, às vezes
    // deixou o pop-up sem aparecer (sem crash, só não aparecia). Não isolei se é uma
    // corrida real ou só o emulador ainda “aquecendo” logo após o app abrir; o delay é
    // barato e imperceptível pro usuário, então ficou como está até haver sinal de que
    // não é mais necessário.
    LaunchedEffect(disciplinaInicial) {
        if (disciplinaInicial != null) {
            delay(120)
            disciplinaEditando = disciplinaInicial
        }
    }

    val totalSessao = selecoes.values.sumOf { it.quantidade }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Escolher disciplinas",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DISCIPLINAS_EM_ORDEM.forEach { (id, rotulo) ->
                val filtro = selecoes[id]
                DisciplinaRow(
                    rotulo = rotulo,
                    marcada = filtro != null,
                    subinfo = if (filtro != null) {
                        "${filtro.tagIds.size} tags · ${filtro.quantidade} questões"
                    } else {
                        "${totalPorDisciplina[id] ?: 0}"
                    },
                    onClick = { disciplinaEditando = id },
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "ORDEM",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OrdemChip(
                    texto = "Aleatório",
                    selecionada = ordem == Ordem.ALEATORIO,
                    onClick = { ordem = Ordem.ALEATORIO },
                )
                OrdemChip(
                    texto = "Sequencial",
                    selecionada = ordem == Ordem.SEQUENCIAL,
                    onClick = { ordem = Ordem.SEQUENCIAL },
                )
            }
            Text(
                text = if (ordem == Ordem.ALEATORIO) {
                    "aleatório = disciplinas misturadas na sessão"
                } else {
                    "sequencial = uma disciplina inteira por vez"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "TOTAL NESTA SESSÃO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = "$totalSessao questões", style = MaterialTheme.typography.headlineMedium)
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

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onVoltarHome, modifier = Modifier.weight(1f)) {
                Text("Voltar")
            }
            Button(
                onClick = {
                    erro = null
                    iniciando = true
                    scope.launch {
                        val filtros = selecoes.map { (id, f) -> FiltroDisciplina(id, f.tagIds, f.quantidade) }
                        try {
                            val ids = motorSessao.resolverLivre(filtros, ordem)
                            val sessao = SessaoEntity(
                                modo = "livre",
                                ordem = if (ordem == Ordem.ALEATORIO) "aleatorio" else "sequencial",
                                cargoSimulado = null,
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
                            erro = "\"${e.disciplinaId}\" tem só ${e.disponivel} questões disponíveis " +
                                "pras tags escolhidas (pedidas ${e.pedido}). Ajuste a quantidade ou marque mais tags."
                        } finally {
                            iniciando = false
                        }
                    }
                },
                enabled = totalSessao > 0 && !iniciando,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (iniciando) "Sorteando..." else "Iniciar")
            }
        }
    }

    if (disciplinaEditando != null) {
        val disciplinaId = disciplinaEditando as String
        val rotulo = DISCIPLINAS_EM_ORDEM.first { it.first == disciplinaId }.second
        TagsPopup(
            repository = repository,
            disciplinaId = disciplinaId,
            rotulo = rotulo,
            selecaoAtual = selecoes[disciplinaId],
            onVoltar = {
                selecoes = selecoes - disciplinaId
                disciplinaEditando = null
            },
            onConfirmar = { tagIds, quantidade ->
                selecoes = selecoes + (disciplinaId to FiltroConfirmado(tagIds, quantidade))
                disciplinaEditando = null
            },
        )
    }
}

@Composable
private fun DisciplinaRow(
    rotulo: String,
    marcada: Boolean,
    subinfo: String,
    onClick: () -> Unit,
) {
    val corBorda = if (marcada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val corFundo = if (marcada) QuestoesTokens.cores.marcoDouradoSuave else MaterialTheme.colorScheme.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QuestoesRadii.controle))
            .background(corFundo)
            .border(1.2.dp, corBorda, RoundedCornerShape(QuestoesRadii.controle))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = marcada, onCheckedChange = { onClick() })
        Spacer(modifier = Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = rotulo, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subinfo,
                style = MaterialTheme.typography.labelSmall,
                color = if (marcada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = "›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun OrdemChip(texto: String, selecionada: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(QuestoesRadii.capsula))
            .background(if (selecionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (selecionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(QuestoesRadii.capsula),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = if (selecionada) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Pop-up de tags (Tela 2 · Pop-up) — sempre abre com as tags desmarcadas, mesmo ao
 * reeditar uma disciplina já confirmada: reabrir não é "continuar de onde parou", é uma
 * escolha nova (decisão fechada no Mapa de Navegação, §4).
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TagsPopup(
    repository: QuestaoRepository,
    disciplinaId: String,
    rotulo: String,
    selecaoAtual: FiltroConfirmado?,
    onVoltar: () -> Unit,
    onConfirmar: (Set<Long>, Int) -> Unit,
) {
    var tags by remember(disciplinaId) { mutableStateOf<List<TagContagem>>(emptyList()) }
    var carregandoTags by remember(disciplinaId) { mutableStateOf(true) }
    LaunchedEffect(disciplinaId) {
        carregandoTags = true
        tags = repository.listarTagsComContagem(disciplinaId)
        carregandoTags = false
    }

    var busca by remember(disciplinaId) { mutableStateOf("") }
    var tagsMarcadas by remember(disciplinaId) { mutableStateOf(setOf<Long>()) }
    var quantidade by remember(disciplinaId) { mutableStateOf(0) }
    var pool by remember(disciplinaId) { mutableStateOf(0) }

    LaunchedEffect(tagsMarcadas, disciplinaId) {
        pool = if (tagsMarcadas.isEmpty()) 0 else repository.contarPool(disciplinaId, tagsMarcadas)
    }
    LaunchedEffect(pool) {
        quantidade = when {
            pool == 0 -> 0
            quantidade == 0 -> minOf(10, pool)
            else -> quantidade.coerceIn(1, pool)
        }
    }

    val sheetState = rememberModalBottomSheetState()
    val tagsFiltradas = remember(tags, busca) {
        if (busca.isBlank()) tags else tags.filter { it.nome.contains(busca, ignoreCase = true) }
    }

    ModalBottomSheet(onDismissRequest = onVoltar, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Coluna rolável (título até o seletor de quantidade) separada da área fixa
            // dos botões — em disciplinas com muitas tags, a lista cresce e rola por
            // dentro enquanto Voltar/Confirmar continuam visíveis, sem precisar rolar
            // até o fim pra alcançá-los (issue #1).
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Tags · $rotulo", style = MaterialTheme.typography.titleMedium)
                    if (selecaoAtual != null) {
                        Text(
                            text = "editando seleção — tags começam desmarcadas de novo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Text(
                    text = "Marque as tags que quer incluir nesta sessão de $rotulo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(QuestoesRadii.controle))
                        .background(QuestoesTokens.cores.marcoDouradoSuave)
                        .padding(10.dp),
                    textAlign = TextAlign.Center,
                )

                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    placeholder = { Text("Buscar tag...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = "TAGS DE ${rotulo.uppercase()} · POR FREQUÊNCIA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (carregandoTags) {
                    CarregandoCheck(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        legenda = "Carregando tags de $rotulo...",
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    tagsFiltradas.forEach { tag ->
                        val marcada = tag.id in tagsMarcadas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(QuestoesRadii.controle))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    tagsMarcadas = if (marcada) tagsMarcadas - tag.id else tagsMarcadas + tag.id
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = marcada,
                                onCheckedChange = {
                                    tagsMarcadas = if (marcada) tagsMarcadas - tag.id else tagsMarcadas + tag.id
                                },
                            )
                            Text(
                                text = tag.nome,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${tag.total}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "QUANTIDADE" + if (pool > 0) " · máx. $pool" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PassoQuantidade(texto = "−", habilitado = pool > 0 && quantidade > 1) {
                            quantidade = (quantidade - 1).coerceAtLeast(1)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(QuestoesRadii.controle))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                        ) {
                            Text(text = if (pool > 0) "$quantidade" else "—", style = MaterialTheme.typography.titleMedium)
                        }
                        PassoQuantidade(texto = "+", habilitado = pool > 0 && quantidade < pool) {
                            quantidade = (quantidade + 1).coerceAtMost(pool)
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 10.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onVoltar, modifier = Modifier.weight(1f)) {
                        Text("Voltar")
                    }
                    Button(
                        onClick = { onConfirmar(tagsMarcadas, quantidade) },
                        enabled = tagsMarcadas.isNotEmpty() && quantidade > 0,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Confirmar")
                    }
                }
                Text(
                    text = "Confirmar libera só depois de marcar 1+ tag",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PassoQuantidade(texto: String, habilitado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .size(36.dp)
            .clickable(enabled = habilitado, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (habilitado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

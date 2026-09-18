package net.oraphael.questoes.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.oraphael.questoes.data.db.AlternativaEntity
import net.oraphael.questoes.data.db.QuestaoCompleta
import net.oraphael.questoes.data.db.SessaoEntity
import net.oraphael.questoes.data.db.TentativaEntity
import net.oraphael.questoes.data.repo.QuestaoRepository
import net.oraphael.questoes.data.repo.SessaoRepository
import net.oraphael.questoes.domain.FiltroSnapshot
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
 * Imagens da questão (0+, [QuestaoEntity.imagensDescJson][net.oraphael.questoes.data.db.QuestaoEntity])
 * são carregadas via Coil ([ImagemQuestao]) a partir do repositório `questoes-banco`
 * (README, "Imagens") — a URL de cada uma é montada só com o id da questão e o índice
 * (1-based) dela na lista, nunca listada em manifesto nenhum. Enquanto uma imagem
 * específica não existir lá (banco de 68 pendentes), o erro de carregamento cai de volta
 * pra descrição textual daquela imagem.
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
    var blocoPorIndice by remember { mutableStateOf<List<String?>>(emptyList()) }
    LaunchedEffect(sessaoId) {
        val completa = sessaoRepository.buscarSessaoCompleta(sessaoId)
        sessao = completa?.sessao
        idsQuestoes = completa?.sessao?.let { Json.decodeFromString<List<String>>(it.questaoIdsJson) } ?: emptyList()
        // Só o modo "simulado" roda em Ordem.SEQUENCIAL com filtros por bloco — daí dá
        // pra reconstruir "qual bloco é a questão N" só andando pelas quantidades de
        // cada FiltroSnapshot na ordem em que aparecem (ver comentário em FiltroSnapshot).
        blocoPorIndice = if (completa?.sessao?.modo == "simulado") {
            val filtros = completa.sessao.let { Json.decodeFromString<List<FiltroSnapshot>>(it.filtrosJson) }
            filtros.flatMap { f -> List(f.quantidade) { f.blocoLabel } }
        } else {
            emptyList()
        }
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

        val imagensDesc = remember(questao) { Json.decodeFromString<List<String>>(questao.questao.imagensDescJson) }
        if (imagensDesc.isNotEmpty()) {
            CarrosselImagensQuestao(questaoId = questao.questao.id, descricoes = imagensDesc)
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
                                    blocoSimulado = blocoPorIndice.getOrNull(indice),
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

/**
 * URL montada só com o id da questão e o índice (1-based) da imagem — mesma convenção do
 * README de `questoes-banco`: arquivos `<id-da-questao>-<n>.png` dentro de
 * `files/images/`, sem manifesto nenhum listando o que existe. Sempre numerado, mesmo
 * quando a questão só tem uma imagem (`-1.png`) — sem caso especial pra "a primeira".
 * Só entra na branch `main` daquele repositório.
 */
private const val URL_BASE_IMAGENS = "https://raw.githubusercontent.com/Raphael-GC/questoes-banco/main/files/images/"

/**
 * Uma página por imagem, sempre — mesmo com 1 imagem só, pra não ter dois componentes
 * diferentes conforme a quantidade. Com 2+ páginas mostra os pontos de posição abaixo.
 * Toque numa imagem carregada abre [ImagemAmpliadaDialog]; se ainda não existir no
 * repositório (banco de pendentes), a página cai pro fallback textual e não é clicável.
 */
@Composable
private fun CarrosselImagensQuestao(questaoId: String, descricoes: List<String>) {
    val pagerState = rememberPagerState(pageCount = { descricoes.size })
    var indiceAmpliado by remember(questaoId) { mutableStateOf<Int?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { pagina ->
            ImagemQuestao(
                questaoId = questaoId,
                indice = pagina + 1,
                descricao = descricoes[pagina],
                onClickAmpliar = { indiceAmpliado = pagina },
            )
        }
        if (descricoes.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(descricoes.size) { i ->
                    val ativo = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (ativo) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (ativo) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                            ),
                    )
                }
            }
        }
    }

    val indice = indiceAmpliado
    if (indice != null) {
        ImagemAmpliadaDialog(
            url = "$URL_BASE_IMAGENS$questaoId-${indice + 1}.png",
            descricao = descricoes[indice],
            onDismiss = { indiceAmpliado = null },
        )
    }
}

/**
 * Sem tamanho fixo: [AsyncImage] com só a largura restrita preenche a linha e cresce na
 * altura conforme a proporção real da imagem — qualquer dimensão de imagem cabe sem
 * distorcer. [defaultMinSize] só evita o card colapsar pra altura zero enquanto carrega.
 * Se a imagem daquela questão ainda não existir no repositório (banco de 68 pendentes),
 * o erro do Coil cai de volta pra descrição textual, igual ao que já existia antes.
 */
@Composable
private fun ImagemQuestao(questaoId: String, indice: Int, descricao: String, onClickAmpliar: () -> Unit) {
    var falhouCarregar by remember(questaoId, indice) { mutableStateOf(false) }

    if (falhouCarregar) {
        Text(
            text = "🖼 $descricao",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(QuestoesRadii.controle))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
        )
    } else {
        AsyncImage(
            model = "$URL_BASE_IMAGENS$questaoId-$indice.png",
            contentDescription = descricao,
            contentScale = ContentScale.FillWidth,
            onError = { falhouCarregar = true },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 140.dp)
                .clip(RoundedCornerShape(QuestoesRadii.controle))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClickAmpliar),
        )
    }
}

/**
 * Tela cheia sem pinça-para-zoom (decisão explícita, mais simples de manter) — toque em
 * qualquer ponto fecha. [DialogProperties.usePlatformDefaultWidth] = false pra ocupar a
 * tela inteira em vez do tamanho padrão de diálogo do Material.
 */
@Composable
private fun ImagemAmpliadaDialog(url: String, descricao: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = url,
                contentDescription = descricao,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
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

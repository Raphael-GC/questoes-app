package net.oraphael.questoes.ui.screens.selecaosimulado

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
import net.oraphael.questoes.data.repo.QuestaoRepository
import net.oraphael.questoes.data.repo.SessaoRepository
import net.oraphael.questoes.domain.FiltroDisciplina
import net.oraphael.questoes.domain.FiltroSnapshot
import net.oraphael.questoes.domain.MotorSessao
import net.oraphael.questoes.domain.Ordem
import net.oraphael.questoes.domain.PoolInsuficienteException
import net.oraphael.questoes.ui.theme.QuestoesRadii
import net.oraphael.questoes.ui.theme.QuestoesTokens

/**
 * Um bloco de um simulado: N questões de uma disciplina, opcionalmente restritas às
 * tags de [topicos] (OR entre elas — mesma regra do modo Livre). [topicos] vazio =
 * todo o pool da disciplina, sem filtro (caso dos 3 cargos do PS 02/2026, cujo banco já
 * é só daquele processo seletivo). Nos blocos do PND, [topicos] são os rótulos oficiais
 * dos objetos de conhecimento do edital (Portarias Inep nº 315/2025 e nº 323/2025).
 */
private data class BlocoSimulado(
    val label: String,
    val disciplinaId: String,
    val topicos: List<String> = emptyList(),
    val quantidade: Int,
)

private data class DefinicaoSimulado(
    val nome: String,
    val subtitulo: String,
    val blocos: List<BlocoSimulado>,
) {
    val total: Int get() = blocos.sumOf { it.quantidade }
}

private val TOPICOS_FGD = listOf(
    "filosofia da educação",
    "história da educação",
    "sociologia da educação",
    "psicologia da educação",
    "teorias pedagógicas",
    "didática e metodologias de ensino",
    "teorias e práticas de currículo",
    "políticas públicas, organização, financiamento e avaliação da educação brasileira",
    "metodologia de pesquisa em educação e ensino",
    "tecnologias da comunicação e informação nas práticas educativas",
    "letramento científico",
    "educação especial e inclusiva",
    "libras, cultura e identidade surda",
    "identidade e especificidades do trabalho docente",
    "planejamento e avaliação do ensino e da aprendizagem",
    "práticas educativas para o processo de aprendizagem de crianças, adolescentes, jovens e adultos",
    "planejamento, organização e gestão democrática educacional em espaço escolar e não escolar",
    "implementação e avaliação de currículos, programas educacionais e projetos político-pedagógicos",
    "práticas de articulação entre escola, família, comunidade e movimentos sociais",
    "histórias e culturas africanas, afro-brasileiras e indígenas",
    "educação, inclusão e direitos humanos",
    "educação socioambiental",
    "educação para as relações de gênero e sexualidade",
    "educação para as relações étnico-raciais",
)

private val TOPICOS_GEOGRAFIA_ESPECIFICO = listOf(
    "fundamentos epistemológicos do pensamento geográfico",
    "categorias geográficas de espaço, região, paisagem, território e lugar",
    "uso dos recursos naturais e questões socioambientais",
    "aspectos físico-geográficos e dinâmicas da paisagem",
    "dinâmica populacional, elementos demográficos e urbanização",
    "saúde, população e ambiente",
    "espaços agrários e rurais",
    "processos de regionalização no Brasil e no mundo",
    "interações espaciais, fluxos e formação de redes geográficas",
    "reestruturação produtiva, sistema financeiro e produção do espaço",
    "diversidade étnico-racial, de gênero e cultural em geografia",
    "geografia histórica e formação territorial do Brasil",
    "movimentos sociais e dinâmicas espaciais",
    "geopolítica, geografia política, conflitos e redefinições territoriais",
    "cartografia escolar",
    "geotecnologias na educação geográfica",
    "pressupostos teóricos e metodológicos no ensino e na aprendizagem de geografia",
    "as diferentes linguagens na educação geográfica",
    "raciocínio geográfico e pensamento espacial",
    "comunidades tradicionais e suas territorialidades",
    "geografia inclusiva e direitos humanos",
    "cartografia tátil",
)

/**
 * Os 4 simulados de hoje — decisão do usuário (17/09): devolver os 3 cargos do PS
 * 02/2026 à lista (nunca chegaram a ser implementados de verdade, só um placeholder) e
 * somar o PND Geografia 2026 como um item novo, não uma substituição.
 *
 * Composição do PS 02/2026 (Edital de Abertura nº 02/2026, Catanduva/SP, Anexo II):
 * 10 Língua Portuguesa (peso 1,0) + 15 Conhecimentos Pedagógicos e Legislação (peso
 * 2,0) + 15 Conhecimentos Específicos (peso 4,0) = 40 questões, 4 alternativas cada.
 * Professor I não tem pool próprio de "Específicos" separado do pedagógico geral (seu
 * conteúdo programático é só didática/avaliação/BNCC, igual ao bloco comum) — por isso
 * os dois blocos aqui viram um só de 30 questões, ambos vindos de "gerais". O peso
 * (nota ponderada) não é calculado ainda — só os acertos brutos por bloco (decisão do
 * usuário, 17/09).
 *
 * Composição do PND Geografia 2026 (Portarias Inep nº 315/2025 e nº 323/2025): 30
 * Formação Geral Docente + 50 Geografia (Componente Específico) = 80 questões, sem a
 * redação. O pool de cada bloco é filtrado pelas tags dos 24/22 objetos de
 * conhecimento oficiais — assim qualquer questão do banco (nova ou já existente, com 4
 * ou 5 alternativas, sem distinção) que cubra aquele assunto entra no sorteio.
 */
private val SIMULADOS = listOf(
    DefinicaoSimulado(
        nome = "PND Geografia 2026",
        subtitulo = "prova de domingo, 20/09/2026 · formato oficial da PND, sem a redação",
        blocos = listOf(
            BlocoSimulado("Formação Geral Docente", "gerais", TOPICOS_FGD, 30),
            BlocoSimulado("Geografia · Componente Específico", "geografia", TOPICOS_GEOGRAFIA_ESPECIFICO, 50),
        ),
    ),
    DefinicaoSimulado(
        nome = "Professor II · Geografia",
        subtitulo = "PS 02/2026 (Catanduva) · 40 questões, 4 alternativas cada",
        blocos = listOf(
            BlocoSimulado("Língua Portuguesa", "portugues", quantidade = 10),
            BlocoSimulado("Conhecimentos Pedagógicos e Legislação", "gerais", quantidade = 15),
            BlocoSimulado("Conhecimentos Específicos · Geografia", "geografia", quantidade = 15),
        ),
    ),
    DefinicaoSimulado(
        nome = "Professor Berçarista",
        subtitulo = "PS 02/2026 (Catanduva) · 40 questões, 4 alternativas cada",
        blocos = listOf(
            BlocoSimulado("Língua Portuguesa", "portugues", quantidade = 10),
            BlocoSimulado("Conhecimentos Pedagógicos e Legislação", "gerais", quantidade = 15),
            BlocoSimulado("Conhecimentos Específicos · Educação Infantil", "educacao-infantil", quantidade = 15),
        ),
    ),
    DefinicaoSimulado(
        nome = "Professor I",
        subtitulo = "PS 02/2026 (Catanduva) · 40 questões, 4 alternativas cada",
        blocos = listOf(
            BlocoSimulado("Língua Portuguesa", "portugues", quantidade = 10),
            BlocoSimulado("Conhecimentos Pedagógicos e Específicos", "gerais", quantidade = 30),
        ),
    ),
)

@Composable
fun SelecaoSimuladoScreen(
    repository: QuestaoRepository,
    sessaoRepository: SessaoRepository,
    motorSessao: MotorSessao,
    onSessaoIniciada: (Long) -> Unit,
    onVoltarHome: () -> Unit,
) {
    var selecionado by remember { mutableStateOf<DefinicaoSimulado?>(null) }

    val simulado = selecionado
    if (simulado == null) {
        ListaSimuladosScreen(onSelecionar = { selecionado = it }, onVoltarHome = onVoltarHome)
    } else {
        DetalheSimuladoScreen(
            simulado = simulado,
            repository = repository,
            sessaoRepository = sessaoRepository,
            motorSessao = motorSessao,
            onVoltar = { selecionado = null },
            onSessaoIniciada = onSessaoIniciada,
        )
    }
}

@Composable
private fun ListaSimuladosScreen(onSelecionar: (DefinicaoSimulado) -> Unit, onVoltarHome: () -> Unit) {
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
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SIMULADOS.forEach { def ->
                CardSimulado(def = def, onClick = { onSelecionar(def) })
            }
        }
        OutlinedButton(onClick = onVoltarHome, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar")
        }
    }
}

@Composable
private fun CardSimulado(def: DefinicaoSimulado, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QuestoesRadii.cartao))
            .background(QuestoesTokens.cores.aguaSuave)
            .border(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f), RoundedCornerShape(QuestoesRadii.cartao))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = def.nome, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = def.subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = "›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DetalheSimuladoScreen(
    simulado: DefinicaoSimulado,
    repository: QuestaoRepository,
    sessaoRepository: SessaoRepository,
    motorSessao: MotorSessao,
    onVoltar: () -> Unit,
    onSessaoIniciada: (Long) -> Unit,
) {
    var poolPorBloco by remember(simulado) { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var erro by remember(simulado) { mutableStateOf<String?>(null) }
    var iniciando by remember(simulado) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(simulado) {
        poolPorBloco = simulado.blocos.associate { bloco ->
            val tagIds = if (bloco.topicos.isEmpty()) emptySet() else repository.buscarTagIdsPorNomes(bloco.topicos)
            bloco.label to repository.contarPool(bloco.disciplinaId, tagIds)
        }
    }

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
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = simulado.nome, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(
                text = simulado.subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "COMPOSIÇÃO DA PROVA · ${simulado.total} QUESTÕES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            simulado.blocos.forEach { bloco ->
                BlocoRow(titulo = bloco.label, quantidade = bloco.quantidade, pool = poolPorBloco[bloco.label])
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

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onVoltar, modifier = Modifier.weight(1f)) {
                Text("Voltar")
            }
            Button(
                onClick = {
                    erro = null
                    iniciando = true
                    scope.launch {
                        try {
                            val filtros = simulado.blocos.map { bloco ->
                                val tagIds = if (bloco.topicos.isEmpty()) emptySet() else repository.buscarTagIdsPorNomes(bloco.topicos)
                                bloco.label to FiltroDisciplina(bloco.disciplinaId, tagIds, bloco.quantidade)
                            }
                            val ids = motorSessao.resolverLivre(filtros.map { it.second }, Ordem.SEQUENCIAL)
                            val sessao = SessaoEntity(
                                modo = "simulado",
                                ordem = "sequencial",
                                cargoSimulado = simulado.nome,
                                dataHoraInicio = System.currentTimeMillis(),
                                tempoTotalSessaoMs = null,
                                filtrosJson = Json.encodeToString(
                                    filtros.map { (label, f) -> FiltroSnapshot(f.disciplinaId, f.tagIds.toList(), f.quantidade, label) },
                                ),
                                questaoIdsJson = Json.encodeToString(ids),
                            )
                            val sessaoId = sessaoRepository.iniciarSessao(sessao)
                            onSessaoIniciada(sessaoId)
                        } catch (e: PoolInsuficienteException) {
                            erro = "Banco de questões incompleto para este simulado: \"${e.disciplinaId}\" tem só " +
                                "${e.disponivel} de ${e.pedido} questões necessárias."
                        } finally {
                            iniciando = false
                        }
                    }
                },
                enabled = !iniciando,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (iniciando) "Sorteando..." else "Iniciar")
            }
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

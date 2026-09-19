package net.oraphael.questoes.data.importer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Espelha files/manifest.json do repositório questoes-banco (GitHub) — só os campos que
 * o app de fato consome pra decidir/aplicar uma atualização. `concursos`, `simulados_ref`
 * e `sources_ref` existem no arquivo remoto (proveniência/histórico) mas são ignorados
 * aqui (`ignoreUnknownKeys` no Json de [AtualizadorRemoto]).
 */
@Serializable
data class ManifestoJson(
    val versao: Int,
    @SerialName("total_questoes") val totalQuestoes: Int,
    val disciplinas: List<DisciplinaManifestoJson>,
)

@Serializable
data class DisciplinaManifestoJson(
    val id: String,
    val nome: String,
    val arquivo: String,
    @SerialName("total_questoes") val totalQuestoes: Int,
)

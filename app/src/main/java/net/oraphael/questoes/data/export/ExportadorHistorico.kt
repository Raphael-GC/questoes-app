package net.oraphael.questoes.data.export

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.oraphael.questoes.data.db.SessaoCompleta

/**
 * Formato de backup do histórico (pós-MVP) — só exportação por enquanto, sem import de
 * volta. Deliberadamente sem os ids autoGenerate de [net.oraphael.questoes.data.db.SessaoEntity]/
 * [net.oraphael.questoes.data.db.TentativaEntity]: são identificadores locais do Room
 * dessa instalação, sem significado num arquivo pra guardar/compartilhar.
 */
@Serializable
data class BackupHistorico(
    val versaoFormato: Int = 1,
    val exportadoEm: Long,
    val sessoes: List<SessaoExport>,
)

@Serializable
data class SessaoExport(
    val modo: String,
    val ordem: String?,
    val cargoSimulado: String?,
    val dataHoraInicio: Long,
    val tempoTotalSessaoMs: Long?,
    val filtrosJson: String,
    val questaoIdsJson: String,
    val tentativas: List<TentativaExport>,
)

@Serializable
data class TentativaExport(
    val questaoId: String,
    val disciplinaId: String,
    val blocoSimulado: String?,
    val respostaSelecionada: String,
    val acerto: Boolean,
    val tempoQuestaoMs: Long,
    val dataHora: Long,
)

object ExportadorHistorico {
    private val json = Json { prettyPrint = true }

    fun gerarJson(sessoesCompletas: List<SessaoCompleta>): String {
        val backup = BackupHistorico(
            exportadoEm = System.currentTimeMillis(),
            sessoes = sessoesCompletas.map { completa ->
                SessaoExport(
                    modo = completa.sessao.modo,
                    ordem = completa.sessao.ordem,
                    cargoSimulado = completa.sessao.cargoSimulado,
                    dataHoraInicio = completa.sessao.dataHoraInicio,
                    tempoTotalSessaoMs = completa.sessao.tempoTotalSessaoMs,
                    filtrosJson = completa.sessao.filtrosJson,
                    questaoIdsJson = completa.sessao.questaoIdsJson,
                    tentativas = completa.tentativas.map {
                        TentativaExport(
                            questaoId = it.questaoId,
                            disciplinaId = it.disciplinaId,
                            blocoSimulado = it.blocoSimulado,
                            respostaSelecionada = it.respostaSelecionada,
                            acerto = it.acerto,
                            tempoQuestaoMs = it.tempoQuestaoMs,
                            dataHora = it.dataHora,
                        )
                    },
                )
            },
        )
        return json.encodeToString(backup)
    }
}

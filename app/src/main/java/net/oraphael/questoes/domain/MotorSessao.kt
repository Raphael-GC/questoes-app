package net.oraphael.questoes.domain

import kotlinx.serialization.Serializable
import net.oraphael.questoes.data.repo.QuestaoRepository

enum class Ordem { ALEATORIO, SEQUENCIAL }

data class FiltroDisciplina(
    val disciplinaId: String,
    val tagIds: Set<Long> = emptySet(),
    val quantidade: Int,
)

/**
 * Retrato de um [FiltroDisciplina] usado numa sessão — persistido em
 * [net.oraphael.questoes.data.db.SessaoEntity.filtrosJson], único formato usado tanto
 * pelo modo Livre quanto pelos Simulados. [blocoLabel] é nulo no modo Livre; nos
 * Simulados é o rótulo do bloco (ex. "Língua Portuguesa") — como um Simulado sempre
 * roda em [Ordem.SEQUENCIAL], o Quiz (Tela 3) reconstrói "qual bloco é a questão N" só
 * andando pelas quantidades desta lista na ordem em que aparecem, sem precisar
 * reconsultar nada.
 */
@Serializable
data class FiltroSnapshot(
    val disciplinaId: String,
    val tagIds: List<Long> = emptyList(),
    val quantidade: Int,
    val blocoLabel: String? = null,
)

/** Pool de questões elegíveis menor que o pedido — nunca sorteamos menos silenciosamente. */
class PoolInsuficienteException(
    val disciplinaId: String,
    val disponivel: Int,
    val pedido: Int,
) : Exception("Pool insuficiente para \"$disciplinaId\": pedidas $pedido, disponíveis $disponivel")

/**
 * Monta a lista de ids de questão de uma sessão. Hoje resolve o modo Livre; o modo
 * Simulado entra quando as tabelas de Concurso/Simulado/Bloco forem implementadas (Tela 6).
 */
class MotorSessao(private val repo: QuestaoRepository) {
    suspend fun resolverLivre(filtros: List<FiltroDisciplina>, ordem: Ordem): List<String> {
        val porDisciplina = filtros.map { filtro ->
            val disponiveis = repo.buscarIdsQuestoes(filtro.disciplinaId, filtro.tagIds)
            if (disponiveis.size < filtro.quantidade) {
                throw PoolInsuficienteException(filtro.disciplinaId, disponiveis.size, filtro.quantidade)
            }
            disponiveis.shuffled().take(filtro.quantidade)
        }
        return when (ordem) {
            Ordem.SEQUENCIAL -> porDisciplina.flatten()
            Ordem.ALEATORIO -> porDisciplina.flatten().shuffled()
        }
    }
}

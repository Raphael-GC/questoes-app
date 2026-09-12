package net.oraphael.questoes.domain

import net.oraphael.questoes.data.repo.QuestaoRepository

enum class Ordem { ALEATORIO, SEQUENCIAL }

data class FiltroDisciplina(
    val disciplinaId: String,
    val tagIds: Set<Long> = emptySet(),
    val quantidade: Int,
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

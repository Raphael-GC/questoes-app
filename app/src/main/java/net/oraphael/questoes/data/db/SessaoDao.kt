package net.oraphael.questoes.data.db

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface SessaoDao {
    @Insert
    suspend fun inserirSessao(sessao: SessaoEntity): Long

    @Update
    suspend fun atualizarSessao(sessao: SessaoEntity)

    @Insert
    suspend fun inserirTentativa(tentativa: TentativaEntity): Long

    @Query("SELECT * FROM sessao ORDER BY dataHoraInicio DESC")
    suspend fun listarSessoes(): List<SessaoEntity>

    @Transaction
    @Query("SELECT * FROM sessao WHERE id = :sessaoId")
    suspend fun buscarCompleta(sessaoId: Long): SessaoCompleta?

    /**
     * Uma linha por sessão concluída, com acertos/total já agregados — Histórico (Tela 5)
     * lista isso sem precisar buscar cada [SessaoCompleta] inteira. Sessão sem
     * [SessaoEntity.tempoTotalSessaoMs] ainda não foi concluída (abandonada no meio do
     * Quiz) e não aparece.
     */
    @Query(
        """
        SELECT s.id as id, s.modo as modo, s.cargoSimulado as cargoSimulado,
               s.dataHoraInicio as dataHoraInicio, s.tempoTotalSessaoMs as tempoTotalSessaoMs,
               COUNT(t.id) as totalQuestoes, COALESCE(SUM(t.acerto), 0) as acertos
        FROM sessao s
        LEFT JOIN tentativa t ON t.sessaoId = s.id
        WHERE s.tempoTotalSessaoMs IS NOT NULL
        GROUP BY s.id
        ORDER BY s.dataHoraInicio DESC
        """
    )
    suspend fun listarResumo(): List<SessaoResumo>

    /**
     * Tags com 1+ erro em todo o histórico (todas as sessões, não só a última) —
     * Revisão por tag (pós-MVP). Agrupado também por [TagErroResumo.disciplinaId] porque
     * uma tag não é exclusiva de uma disciplina no schema (ex. "cartografia" pode
     * aparecer tanto em geografia quanto em gerais); sem isso o filtro de revisão não
     * saberia em qual disciplina buscar o pool. [TentativaEntity.disciplinaId] (gravado
     * no momento da tentativa) resolve essa ambiguidade sem precisar voltar em `questao`.
     */
    @Query(
        """
        SELECT t.id as tagId, t.nome as nome, tv.disciplinaId as disciplinaId,
               SUM(CASE WHEN tv.acerto = 0 THEN 1 ELSE 0 END) as erros,
               COUNT(*) as total
        FROM tentativa tv
        INNER JOIN questao_tag_cross_ref x ON x.questaoId = tv.questaoId
        INNER JOIN tag t ON t.id = x.tagId
        GROUP BY t.id, tv.disciplinaId
        HAVING erros > 0
        ORDER BY erros DESC, total DESC
        """
    )
    suspend fun listarTagsComErros(): List<TagErroResumo>
}

data class TagErroResumo(
    val tagId: Long,
    val nome: String,
    val disciplinaId: String,
    val erros: Int,
    val total: Int,
)

data class SessaoCompleta(
    @Embedded val sessao: SessaoEntity,
    @Relation(parentColumn = "id", entityColumn = "sessaoId")
    val tentativas: List<TentativaEntity>,
)

data class SessaoResumo(
    val id: Long,
    val modo: String,
    val cargoSimulado: String?,
    val dataHoraInicio: Long,
    val tempoTotalSessaoMs: Long?,
    val totalQuestoes: Int,
    val acertos: Int,
)

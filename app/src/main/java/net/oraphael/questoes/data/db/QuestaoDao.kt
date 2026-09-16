package net.oraphael.questoes.data.db

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction

@Dao
interface QuestaoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirQuestoes(qs: List<QuestaoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirAlternativas(alts: List<AlternativaEntity>)

    @Query("SELECT COUNT(*) FROM questao")
    suspend fun contar(): Int

    @Query("DELETE FROM questao WHERE disciplinaId = :disciplinaId")
    suspend fun apagarPorDisciplina(disciplinaId: String)

    @Query("SELECT disciplinaId, COUNT(*) as total FROM questao GROUP BY disciplinaId")
    suspend fun contarPorDisciplina(): List<DisciplinaContagem>

    @Query("SELECT id FROM questao WHERE disciplinaId = :disciplinaId")
    suspend fun idsPorDisciplina(disciplinaId: String): List<String>

    @Query(
        """
        SELECT DISTINCT q.id FROM questao q
        INNER JOIN questao_tag_cross_ref x ON x.questaoId = q.id
        WHERE q.disciplinaId = :disciplinaId AND x.tagId IN (:tagIds)
        """
    )
    suspend fun idsPorDisciplinaETags(disciplinaId: String, tagIds: Set<Long>): List<String>

    @Query("SELECT COUNT(*) FROM questao WHERE disciplinaId = :disciplinaId")
    suspend fun contarPorDisciplinaId(disciplinaId: String): Int

    @Query(
        """
        SELECT COUNT(DISTINCT q.id) FROM questao q
        INNER JOIN questao_tag_cross_ref x ON x.questaoId = q.id
        WHERE q.disciplinaId = :disciplinaId AND x.tagId IN (:tagIds)
        """
    )
    suspend fun contarPorDisciplinaETags(disciplinaId: String, tagIds: Set<Long>): Int

    @Transaction
    @Query("SELECT * FROM questao WHERE id = :id")
    suspend fun buscarCompleta(id: String): QuestaoCompleta?
}

data class DisciplinaContagem(
    val disciplinaId: String,
    val total: Int,
)

data class QuestaoCompleta(
    @Embedded val questao: QuestaoEntity,
    @Relation(parentColumn = "id", entityColumn = "questaoId")
    val alternativas: List<AlternativaEntity>,
)

@Dao
interface TagDao {
    @Query("SELECT id FROM tag WHERE nome = :nome")
    suspend fun buscar(nome: String): Long?

    @Insert
    suspend fun inserir(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun vincular(ref: QuestaoTagCrossRef)

    suspend fun obterOuCriar(nome: String): Long =
        buscar(nome) ?: inserir(TagEntity(nome = nome))

    @Query(
        """
        SELECT DISTINCT t.* FROM tag t
        INNER JOIN questao_tag_cross_ref x ON x.tagId = t.id
        INNER JOIN questao q ON q.id = x.questaoId
        WHERE q.disciplinaId = :disciplinaId
        ORDER BY t.nome
        """
    )
    suspend fun listarPorDisciplina(disciplinaId: String): List<TagEntity>

    /** Tags da disciplina por frequência (Tela 2 · Pop-up, §3 do Mapa de Navegação). */
    @Query(
        """
        SELECT t.id as id, t.nome as nome, COUNT(*) as total FROM tag t
        INNER JOIN questao_tag_cross_ref x ON x.tagId = t.id
        INNER JOIN questao q ON q.id = x.questaoId
        WHERE q.disciplinaId = :disciplinaId
        GROUP BY t.id
        ORDER BY total DESC, t.nome ASC
        """
    )
    suspend fun listarComContagemPorDisciplina(disciplinaId: String): List<TagContagem>

    /**
     * Tags das questões dadas, agrupáveis por disciplina — usado no Resumo (Tela 4) pra
     * exibir as tags das questões erradas da sessão, sem repetição (Mapa de Navegação,
     * §4: "o que substitui o antigo modo Erros").
     */
    @Query(
        """
        SELECT DISTINCT q.disciplinaId as disciplinaId, t.nome as nome FROM tag t
        INNER JOIN questao_tag_cross_ref x ON x.tagId = t.id
        INNER JOIN questao q ON q.id = x.questaoId
        WHERE q.id IN (:questaoIds)
        ORDER BY q.disciplinaId, t.nome
        """
    )
    suspend fun listarPorQuestoes(questaoIds: List<String>): List<DisciplinaTagNome>
}

data class TagContagem(
    val id: Long,
    val nome: String,
    val total: Int,
)

data class DisciplinaTagNome(
    val disciplinaId: String,
    val nome: String,
)

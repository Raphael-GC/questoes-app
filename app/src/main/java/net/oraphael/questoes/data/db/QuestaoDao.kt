package net.oraphael.questoes.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

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
}

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
}

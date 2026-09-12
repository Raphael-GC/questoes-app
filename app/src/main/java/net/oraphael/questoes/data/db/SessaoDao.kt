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
}

data class SessaoCompleta(
    @Embedded val sessao: SessaoEntity,
    @Relation(parentColumn = "id", entityColumn = "sessaoId")
    val tentativas: List<TentativaEntity>,
)

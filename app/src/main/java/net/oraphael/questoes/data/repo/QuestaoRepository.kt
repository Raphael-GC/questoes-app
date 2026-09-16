package net.oraphael.questoes.data.repo

import net.oraphael.questoes.data.db.AppDatabase
import net.oraphael.questoes.data.db.DisciplinaContagem
import net.oraphael.questoes.data.db.DisciplinaTagNome
import net.oraphael.questoes.data.db.QuestaoCompleta
import net.oraphael.questoes.data.db.TagContagem
import net.oraphael.questoes.data.db.TagEntity

/**
 * Fachada de leitura sobre QuestaoDao/TagDao — a UI e o domínio nunca falam com o DAO
 * diretamente, só com este repositório.
 */
class QuestaoRepository(private val db: AppDatabase) {
    suspend fun listarDisciplinasComContagem(): List<DisciplinaContagem> =
        db.questaoDao().contarPorDisciplina()

    suspend fun listarTags(disciplinaId: String): List<TagEntity> =
        db.tagDao().listarPorDisciplina(disciplinaId)

    /** Tags por frequência, pro pop-up da Tela 2 (a mais frequente primeiro). */
    suspend fun listarTagsComContagem(disciplinaId: String): List<TagContagem> =
        db.tagDao().listarComContagemPorDisciplina(disciplinaId)

    /** Ids de questão da disciplina; se [tagIds] vier vazio, ignora o filtro de tags. */
    suspend fun buscarIdsQuestoes(disciplinaId: String, tagIds: Set<Long>): List<String> =
        if (tagIds.isEmpty()) {
            db.questaoDao().idsPorDisciplina(disciplinaId)
        } else {
            db.questaoDao().idsPorDisciplinaETags(disciplinaId, tagIds)
        }

    /** Tamanho do pool elegível — mesma regra de OR entre tags do MotorSessao, sem trazer os ids. */
    suspend fun contarPool(disciplinaId: String, tagIds: Set<Long>): Int =
        if (tagIds.isEmpty()) {
            db.questaoDao().contarPorDisciplinaId(disciplinaId)
        } else {
            db.questaoDao().contarPorDisciplinaETags(disciplinaId, tagIds)
        }

    suspend fun buscarQuestaoCompleta(id: String): QuestaoCompleta? =
        db.questaoDao().buscarCompleta(id)

    /** Tags das questões dadas, por disciplina — Resumo (Tela 4), tags das erradas da sessão. */
    suspend fun listarTagsDasQuestoes(questaoIds: List<String>): List<DisciplinaTagNome> =
        if (questaoIds.isEmpty()) emptyList() else db.tagDao().listarPorQuestoes(questaoIds)
}

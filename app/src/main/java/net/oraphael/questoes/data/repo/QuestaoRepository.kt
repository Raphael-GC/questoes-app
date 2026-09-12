package net.oraphael.questoes.data.repo

import net.oraphael.questoes.data.db.AppDatabase
import net.oraphael.questoes.data.db.DisciplinaContagem
import net.oraphael.questoes.data.db.QuestaoCompleta
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

    /** Ids de questão da disciplina; se [tagIds] vier vazio, ignora o filtro de tags. */
    suspend fun buscarIdsQuestoes(disciplinaId: String, tagIds: Set<Long>): List<String> =
        if (tagIds.isEmpty()) {
            db.questaoDao().idsPorDisciplina(disciplinaId)
        } else {
            db.questaoDao().idsPorDisciplinaETags(disciplinaId, tagIds)
        }

    suspend fun buscarQuestaoCompleta(id: String): QuestaoCompleta? =
        db.questaoDao().buscarCompleta(id)
}

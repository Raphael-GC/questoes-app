package net.oraphael.questoes.data.repo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import net.oraphael.questoes.data.db.AppDatabase
import net.oraphael.questoes.data.importer.Importador
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuestaoRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: QuestaoRepository

    @Before
    fun criarBancoEImportar() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = QuestaoRepository(db)

        val json = """
            [
              {"id": "geo-1", "disciplina": "geografia", "enunciado": "e1", "alternativas": {"A": "a"}, "resposta_correta": "A", "explicacao": "x", "tags": ["cartografia"]},
              {"id": "geo-2", "disciplina": "geografia", "enunciado": "e2", "alternativas": {"A": "a"}, "resposta_correta": "A", "explicacao": "x", "tags": ["climatologia"]},
              {"id": "geo-3", "disciplina": "geografia", "enunciado": "e3", "alternativas": {"A": "a"}, "resposta_correta": "A", "explicacao": "x", "tags": ["cartografia", "climatologia"]}
            ]
        """.trimIndent()
        Importador(db, context).importarDeTexto(json, "geografia")
    }

    @After
    fun fecharBanco() {
        db.close()
    }

    @Test
    fun buscarIdsQuestoesSemTagsDevolveTodoPool() = runTest {
        val ids = repository.buscarIdsQuestoes("geografia", emptySet())
        assertEquals(setOf("geo-1", "geo-2", "geo-3"), ids.toSet())
    }

    @Test
    fun buscarIdsQuestoesComTagsFazOrEntreElas() = runTest {
        val tagIds = repository.buscarTagIdsPorNomes(listOf("cartografia", "climatologia"))
        val ids = repository.buscarIdsQuestoes("geografia", tagIds)
        assertEquals(setOf("geo-1", "geo-2", "geo-3"), ids.toSet())

        val tagIdCartografia = repository.buscarTagIdsPorNomes(listOf("cartografia"))
        val idsSoCartografia = repository.buscarIdsQuestoes("geografia", tagIdCartografia)
        assertEquals(setOf("geo-1", "geo-3"), idsSoCartografia.toSet())
    }

    @Test
    fun contarPoolRefleteOMesmoFiltroDeBuscarIds() = runTest {
        val tagIdCartografia = repository.buscarTagIdsPorNomes(listOf("cartografia"))
        assertEquals(2, repository.contarPool("geografia", tagIdCartografia))
        assertEquals(3, repository.contarPool("geografia", emptySet()))
    }

    @Test
    fun buscarTagIdsPorNomesIgnoraNomesInexistentes() = runTest {
        val tagIds = repository.buscarTagIdsPorNomes(listOf("cartografia", "nao existe"))
        assertEquals(1, tagIds.size)
    }
}

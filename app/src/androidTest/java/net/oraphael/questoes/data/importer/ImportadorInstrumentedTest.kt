package net.oraphael.questoes.data.importer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import net.oraphael.questoes.data.db.AppDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImportadorInstrumentedTest {
    private lateinit var db: AppDatabase
    private lateinit var importador: Importador

    private val jsonDuasQuestoes = """
        [
          {
            "id": "geo-1",
            "disciplina": "geografia",
            "enunciado": "Qual a capital do Brasil?",
            "alternativas": {"A": "Brasília", "B": "São Paulo"},
            "resposta_correta": "A",
            "explicacao": "Brasília é a capital desde 1960.",
            "tags": ["geografia política"]
          },
          {
            "id": "geo-2",
            "disciplina": "geografia",
            "enunciado": "Qual o maior bioma brasileiro?",
            "alternativas": [{"letra": "A", "texto": "Amazônia"}, {"letra": "B", "texto": "Cerrado"}],
            "resposta_correta": "A",
            "explicacao": "A Amazônia é o maior bioma.",
            "tags": ["biomas", "geografia política"]
          }
        ]
    """.trimIndent()

    @Before
    fun criarBanco() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        importador = Importador(db, context)
    }

    @After
    fun fecharBanco() {
        db.close()
    }

    @Test
    fun importaQuestoesAlternativasETags() = runTest {
        importador.importarDeTexto(jsonDuasQuestoes, "geografia")

        assertEquals(2, db.questaoDao().contarPorDisciplinaId("geografia"))

        val completa1 = db.questaoDao().buscarCompleta("geo-1")!!
        assertEquals("Qual a capital do Brasil?", completa1.questao.enunciado)
        assertEquals(
            setOf("A" to "Brasília", "B" to "São Paulo"),
            completa1.alternativas.map { it.letra to it.texto }.toSet(),
        )

        val completa2 = db.questaoDao().buscarCompleta("geo-2")!!
        assertEquals(
            setOf("A" to "Amazônia", "B" to "Cerrado"),
            completa2.alternativas.map { it.letra to it.texto }.toSet(),
        )

        val tagsGeo1 = db.tagDao().listarPorQuestoes(listOf("geo-1")).map { it.nome }
        assertTrue(tagsGeo1.contains("geografia política"))
        val tagsGeo2 = db.tagDao().listarPorQuestoes(listOf("geo-2")).map { it.nome }.toSet()
        assertEquals(setOf("biomas", "geografia política"), tagsGeo2)
    }

    @Test
    fun reimportarMesmaDisciplinaSubstituiSemDuplicar() = runTest {
        importador.importarDeTexto(jsonDuasQuestoes, "geografia")
        importador.importarDeTexto(jsonDuasQuestoes, "geografia")

        assertEquals(2, db.questaoDao().contarPorDisciplinaId("geografia"))
    }
}

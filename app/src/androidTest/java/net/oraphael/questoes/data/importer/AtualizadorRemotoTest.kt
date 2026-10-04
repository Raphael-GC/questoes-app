package net.oraphael.questoes.data.importer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import net.oraphael.questoes.data.db.AppDatabase
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercita AtualizadorRemoto contra um servidor HTTP fake local (MockWebServer), não
 * contra a URL de produção do questoes-banco — a URL oficial já foi validada manualmente
 * (ver README) e não deve ser uma dependência de rede da suíte automatizada.
 */
@RunWith(AndroidJUnit4::class)
class AtualizadorRemotoTest {
    private lateinit var server: MockWebServer
    private lateinit var db: AppDatabase
    private lateinit var atualizador: AtualizadorRemoto

    @Before
    fun subirServidorFakeEBanco() {
        server = MockWebServer()
        server.start()

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        atualizador = AtualizadorRemoto(Importador(db, context), baseUrl = server.url("/").toString())
    }

    @After
    fun desligarServidorEBanco() {
        server.shutdown()
        db.close()
    }

    @Test
    fun buscarManifestoDecodificaORetornoDoServidor() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "versao": 3,
                  "total_questoes": 1,
                  "disciplinas": [
                    {"id": "geografia", "nome": "Geografia", "arquivo": "geografia.json", "total_questoes": 1}
                  ]
                }
                """.trimIndent(),
            ),
        )

        val manifesto = atualizador.buscarManifesto()

        assertEquals(3, manifesto.versao)
        assertEquals(1, manifesto.disciplinas.size)
        assertEquals("geografia.json", manifesto.disciplinas.first().arquivo)
    }

    @Test
    fun aplicarBaixaEImportaCadaDisciplinaDoManifesto() = runTest {
        val manifesto = ManifestoJson(
            versao = 3,
            totalQuestoes = 1,
            disciplinas = listOf(DisciplinaManifestoJson("geografia", "Geografia", "geografia.json", 1)),
        )
        server.enqueue(
            MockResponse().setBody(
                """
                [{"id": "geo-1", "disciplina": "geografia", "enunciado": "e1", "alternativas": {"A": "a"}, "resposta_correta": "A", "explicacao": "x", "tags": []}]
                """.trimIndent(),
            ),
        )

        atualizador.aplicar(manifesto)

        assertEquals(1, db.questaoDao().contarPorDisciplinaId("geografia"))
    }
}

package net.oraphael.questoes.data.repo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import net.oraphael.questoes.data.db.AppDatabase
import net.oraphael.questoes.data.db.SessaoEntity
import net.oraphael.questoes.data.db.TentativaEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessaoRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: SessaoRepository

    @Before
    fun criarBanco() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = SessaoRepository(db)
    }

    @After
    fun fecharBanco() {
        db.close()
    }

    private fun sessaoLivre(concluida: Boolean) = SessaoEntity(
        modo = "livre",
        ordem = "sequencial",
        cargoSimulado = null,
        dataHoraInicio = 1_000L,
        tempoTotalSessaoMs = if (concluida) 60_000L else null,
        filtrosJson = "[]",
        questaoIdsJson = """["geo-1","geo-2"]""",
    )

    @Test
    fun cicloCompletoDeSessaoGravaERecuperaTentativas() = runTest {
        val sessaoId = repository.iniciarSessao(sessaoLivre(concluida = false))

        repository.registrarTentativa(
            TentativaEntity(
                sessaoId = sessaoId,
                questaoId = "geo-1",
                disciplinaId = "geografia",
                blocoSimulado = null,
                respostaSelecionada = "A",
                acerto = true,
                tempoQuestaoMs = 5_000L,
                dataHora = 2_000L,
            ),
        )
        repository.registrarTentativa(
            TentativaEntity(
                sessaoId = sessaoId,
                questaoId = "geo-2",
                disciplinaId = "geografia",
                blocoSimulado = null,
                respostaSelecionada = "B",
                acerto = false,
                tempoQuestaoMs = 4_000L,
                dataHora = 3_000L,
            ),
        )
        repository.concluirSessao(sessaoLivre(concluida = true).copy(id = sessaoId))

        val completa = repository.buscarSessaoCompleta(sessaoId)!!
        assertEquals(2, completa.tentativas.size)
        assertEquals(1, completa.tentativas.count { it.acerto })
    }

    @Test
    fun listarResumoIgnoraSessaoAbandonada() = runTest {
        val sessaoConcluidaId = repository.iniciarSessao(sessaoLivre(concluida = false))
        repository.registrarTentativa(
            TentativaEntity(
                sessaoId = sessaoConcluidaId,
                questaoId = "geo-1",
                disciplinaId = "geografia",
                blocoSimulado = null,
                respostaSelecionada = "A",
                acerto = true,
                tempoQuestaoMs = 1_000L,
                dataHora = 1_000L,
            ),
        )
        repository.concluirSessao(sessaoLivre(concluida = true).copy(id = sessaoConcluidaId))

        repository.iniciarSessao(sessaoLivre(concluida = false)) // abandonada, nunca concluída

        val resumos = repository.listarResumoSessoes()

        assertEquals(1, resumos.size)
        assertEquals(sessaoConcluidaId, resumos.first().id)
        assertEquals(1, resumos.first().acertos)
        assertEquals(1, resumos.first().totalQuestoes)
    }
}

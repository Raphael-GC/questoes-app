package net.oraphael.questoes.domain

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import net.oraphael.questoes.data.repo.QuestaoRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MotorSessaoTest {

    private val repo = mockk<QuestaoRepository>()
    private val motor = MotorSessao(repo)

    @Test
    fun `sequencial agrupa os blocos na ordem dos filtros`() = runTest {
        val idsGeografia = listOf("geo-1", "geo-2", "geo-3")
        val idsPortugues = listOf("port-1", "port-2")
        coEvery { repo.buscarIdsQuestoes("geografia", emptySet()) } returns idsGeografia
        coEvery { repo.buscarIdsQuestoes("portugues", emptySet()) } returns idsPortugues

        val filtros = listOf(
            FiltroDisciplina("geografia", quantidade = 3),
            FiltroDisciplina("portugues", quantidade = 2),
        )

        val resultado = motor.resolverLivre(filtros, Ordem.SEQUENCIAL)

        assertEquals(5, resultado.size)
        assertEquals(idsGeografia.toSet(), resultado.subList(0, 3).toSet())
        assertEquals(idsPortugues.toSet(), resultado.subList(3, 5).toSet())
    }

    @Test
    fun `aleatorio devolve a mesma colecao de ids, so a ordem pode mudar`() = runTest {
        val idsGeografia = listOf("geo-1", "geo-2", "geo-3")
        val idsPortugues = listOf("port-1", "port-2")
        coEvery { repo.buscarIdsQuestoes("geografia", emptySet()) } returns idsGeografia
        coEvery { repo.buscarIdsQuestoes("portugues", emptySet()) } returns idsPortugues

        val filtros = listOf(
            FiltroDisciplina("geografia", quantidade = 3),
            FiltroDisciplina("portugues", quantidade = 2),
        )

        val resultado = motor.resolverLivre(filtros, Ordem.ALEATORIO)

        assertEquals((idsGeografia + idsPortugues).toSet(), resultado.toSet())
    }

    @Test
    fun `sorteia exatamente a quantidade pedida de um pool maior, sem repetir ids`() = runTest {
        val pool = (1..20).map { "geo-$it" }
        coEvery { repo.buscarIdsQuestoes("geografia", emptySet()) } returns pool

        val resultado = motor.resolverLivre(
            listOf(FiltroDisciplina("geografia", quantidade = 5)),
            Ordem.SEQUENCIAL,
        )

        assertEquals(5, resultado.size)
        assertEquals(5, resultado.toSet().size)
        assertTrue(pool.containsAll(resultado))
    }

    @Test
    fun `pool insuficiente lanca excecao com os numeros certos`() = runTest {
        coEvery { repo.buscarIdsQuestoes("geografia", emptySet()) } returns listOf("geo-1", "geo-2")

        val excecao = try {
            motor.resolverLivre(listOf(FiltroDisciplina("geografia", quantidade = 5)), Ordem.SEQUENCIAL)
            null
        } catch (e: PoolInsuficienteException) {
            e
        }

        assertNotNull(excecao)
        assertEquals("geografia", excecao?.disciplinaId)
        assertEquals(2, excecao?.disponivel)
        assertEquals(5, excecao?.pedido)
    }
}

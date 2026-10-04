package net.oraphael.questoes.data.importer

import android.content.Context
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import net.oraphael.questoes.data.db.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Só a parte pura do Importador (sem tocar Room/Context de verdade) — a troca atômica
 * por disciplina em si é cobrida por ImportadorInstrumentedTest (androidTest).
 */
class ImportadorTest {
    private val importador = Importador(mockk<AppDatabase>(relaxed = true), mockk<Context>(relaxed = true))

    @Test
    fun `normaliza alternativas no formato dicionario`() {
        val alt: JsonElement = Json.parseToJsonElement("""{"A": "primeira", "B": "segunda"}""")

        val resultado = importador.normalizarAlternativas("q1", alt)

        assertEquals(
            setOf("A" to "primeira", "B" to "segunda"),
            resultado.map { it.letra to it.texto }.toSet(),
        )
        assertEquals(setOf("q1"), resultado.map { it.questaoId }.toSet())
    }

    @Test
    fun `normaliza alternativas no formato lista`() {
        val alt: JsonElement = Json.parseToJsonElement(
            """[{"letra": "A", "texto": "primeira"}, {"letra": "B", "texto": "segunda"}]""",
        )

        val resultado = importador.normalizarAlternativas("q1", alt)

        assertEquals(
            setOf("A" to "primeira", "B" to "segunda"),
            resultado.map { it.letra to it.texto }.toSet(),
        )
    }

    @Test
    fun `paraEntity mapeia os campos do json pra entity, incluindo imagens`() {
        val json = QuestaoJson(
            id = "q1",
            disciplina = "geografia",
            enunciado = "enunciado",
            alternativas = Json.parseToJsonElement("""{"A": "x"}"""),
            respostaCorreta = "A",
            explicacao = "explicacao",
            tags = listOf("tag1"),
            fonte = "fonte",
            imagensDesc = listOf("mapa do Brasil"),
        )

        val entity = json.paraEntity("geografia")

        assertEquals("q1", entity.id)
        assertEquals("geografia", entity.disciplinaId)
        assertEquals("enunciado", entity.enunciado)
        assertEquals("""["mapa do Brasil"]""", entity.imagensDescJson)
    }

    @Test
    fun `paraEntity sem imagens serializa lista vazia`() {
        val json = QuestaoJson(
            id = "q2",
            disciplina = "geografia",
            enunciado = "enunciado",
            alternativas = Json.parseToJsonElement("""{"A": "x"}"""),
            respostaCorreta = "A",
            explicacao = "explicacao",
        )

        val entity = json.paraEntity("geografia")

        assertEquals("[]", entity.imagensDescJson)
    }
}

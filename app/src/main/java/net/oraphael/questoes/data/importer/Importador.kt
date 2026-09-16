package net.oraphael.questoes.data.importer

import android.content.Context
import androidx.room.withTransaction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.oraphael.questoes.data.db.AlternativaEntity
import net.oraphael.questoes.data.db.AppDatabase
import net.oraphael.questoes.data.db.QuestaoEntity
import net.oraphael.questoes.data.db.QuestaoTagCrossRef

/**
 * O único lugar do app que sabe que `alternativas` tem duas formas no JSON de origem
 * (dict ou lista) — normaliza uma vez, na entrada, pra tabela `alternativa`. Daqui em
 * diante ninguém mais no app precisa saber que essa inconsistência existe.
 */
class Importador(private val db: AppDatabase, private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun importarDeAssets(arquivo: String, disciplinaId: String) {
        val texto = context.assets.open("questoes/$arquivo")
            .bufferedReader().use { it.readText() }
        val questoes = json.decodeFromString<List<QuestaoJson>>(texto)

        db.withTransaction {
            db.questaoDao().apagarPorDisciplina(disciplinaId)
            db.questaoDao().inserirQuestoes(questoes.map { it.paraEntity(disciplinaId) })
            questoes.forEach { q ->
                db.questaoDao().inserirAlternativas(normalizarAlternativas(q.id, q.alternativas))
                q.tags.forEach { nome ->
                    val tagId = db.tagDao().obterOuCriar(nome)
                    db.tagDao().vincular(QuestaoTagCrossRef(q.id, tagId))
                }
            }
        }
    }

    private fun normalizarAlternativas(questaoId: String, alt: JsonElement): List<AlternativaEntity> =
        when (alt) {
            is JsonObject -> alt.entries.map { (letra, v) ->
                AlternativaEntity(questaoId, letra, v.jsonPrimitive.content)
            }
            is JsonArray -> alt.map { item ->
                val o = item.jsonObject
                AlternativaEntity(
                    questaoId,
                    o["letra"]!!.jsonPrimitive.content,
                    o["texto"]!!.jsonPrimitive.content,
                )
            }
            else -> error("Formato de alternativas inesperado em $questaoId")
        }
}

private fun QuestaoJson.paraEntity(disciplinaId: String) = QuestaoEntity(
    id = id,
    disciplinaId = disciplinaId,
    enunciado = enunciado,
    respostaCorreta = respostaCorreta,
    explicacao = explicacao,
    fonte = fonte,
    imagensDescJson = Json.encodeToString(imagensDesc),
)

package net.oraphael.questoes.data.importer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URL

private const val BASE_URL = "https://raw.githubusercontent.com/Raphael-GC/questoes-banco/master/files/"

/**
 * Busca o manifest.json e os .json por disciplina do questoes-banco (GitHub, servido via
 * raw.githubusercontent.com — mesma fonte usada pras imagens, ver README daquele repo).
 * Repositório público, GET simples, sem autenticação.
 */
class AtualizadorRemoto(private val importador: Importador) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun buscarManifesto(): ManifestoJson = withContext(Dispatchers.IO) {
        json.decodeFromString(baixarTexto(BASE_URL + "manifest.json"))
    }

    /**
     * Baixa e reimporta cada disciplina do manifesto, substituindo o conteúdo local
     * (mesma troca atômica por disciplina do primeiro boot — ver [Importador.importarDeTexto]).
     */
    suspend fun aplicar(manifesto: ManifestoJson) {
        manifesto.disciplinas.forEach { disciplina ->
            val texto = withContext(Dispatchers.IO) { baixarTexto(BASE_URL + disciplina.arquivo) }
            importador.importarDeTexto(texto, disciplina.id)
        }
    }

    private fun baixarTexto(url: String): String =
        URL(url).openStream().bufferedReader().use { it.readText() }
}

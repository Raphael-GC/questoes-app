package net.oraphael.questoes.data.importer

import android.content.Context
import androidx.core.content.edit

/**
 * Guarda qual versão do banco de questões (campo `versao` de manifest.json, no
 * questoes-banco) já está aplicada localmente — é contra esse número que a Tela 7
 * (Atualizar banco) compara a versão remota antes de baixar qualquer coisa.
 */
object BancoVersao {
    /**
     * Versão do banco embutido em app/src/main/assets/questoes/ nesta build — bump junto
     * com manifest.json.versao sempre que os .json de lá forem sincronizados pra dentro
     * do app numa build nova (mesma disciplina do bump de AppDatabase.version pro schema).
     */
    const val BUNDLADA = 2

    /**
     * Sentinela pra "nunca gravado" — NUNCA use [BUNDLADA] aqui: quem já tinha o app
     * instalado antes desta versão de código (banco não vazio, então o import do primeiro
     * boot em QuestoesApplication nunca roda pra marcar nada) precisa continuar lendo um
     * valor baixo o bastante pra qualquer versão remota parecer mais nova, não o número
     * bundlado *desta* build (que pode já ter mudado e não corresponder ao que a pessoa
     * realmente tem instalado).
     */
    private const val NUNCA_GRAVADO = 0

    private const val PREFS = "banco_questoes"
    private const val CHAVE_VERSAO = "versao_aplicada"

    fun versaoAplicada(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(CHAVE_VERSAO, NUNCA_GRAVADO)

    fun marcarVersaoAplicada(context: Context, versao: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putInt(CHAVE_VERSAO, versao) }
    }
}

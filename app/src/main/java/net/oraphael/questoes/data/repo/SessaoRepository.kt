package net.oraphael.questoes.data.repo

import net.oraphael.questoes.data.db.AppDatabase
import net.oraphael.questoes.data.db.SessaoCompleta
import net.oraphael.questoes.data.db.SessaoEntity
import net.oraphael.questoes.data.db.SessaoResumo
import net.oraphael.questoes.data.db.TentativaEntity

/** Grava e lê sessões/tentativas — usado tanto durante o quiz quanto na tela de Histórico. */
class SessaoRepository(private val db: AppDatabase) {
    suspend fun iniciarSessao(sessao: SessaoEntity): Long =
        db.sessaoDao().inserirSessao(sessao)

    suspend fun concluirSessao(sessao: SessaoEntity) =
        db.sessaoDao().atualizarSessao(sessao)

    suspend fun registrarTentativa(tentativa: TentativaEntity): Long =
        db.sessaoDao().inserirTentativa(tentativa)

    suspend fun listarSessoes(): List<SessaoEntity> =
        db.sessaoDao().listarSessoes()

    suspend fun buscarSessaoCompleta(sessaoId: Long): SessaoCompleta? =
        db.sessaoDao().buscarCompleta(sessaoId)

    /** Sessões concluídas com acertos/total já agregados — Histórico (Tela 5). */
    suspend fun listarResumoSessoes(): List<SessaoResumo> =
        db.sessaoDao().listarResumo()
}

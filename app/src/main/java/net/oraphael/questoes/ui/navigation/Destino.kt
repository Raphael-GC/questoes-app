package net.oraphael.questoes.ui.navigation

import kotlinx.serialization.Serializable

/**
 * As 7 rotas do app (Mapa de Navegação, Fase 2). O pop-up de tags NÃO é uma rota — é
 * estado de UI dentro da própria tela de Seleção·Livre, aberto como ModalBottomSheet.
 *
 * `@Serializable` habilita a navegação type-safe do Navigation Compose (desde 2.8):
 * cada rota é passada como objeto Kotlin de verdade, sem strings mágicas.
 */
@Serializable
sealed interface Destino

@Serializable data object Home : Destino

/**
 * [disciplinaInicial] pré-marca uma disciplina e já abre o pop-up de tags dela — o
 * atalho da Home (Tela 1) confirmado no Mapa de Navegação. Nulo quando se entra pelo
 * cartão "Livre", que abre a tela vazia.
 */
@Serializable data class SelecaoLivre(val disciplinaInicial: String? = null) : Destino
@Serializable data class Quiz(val sessaoId: Long) : Destino
@Serializable data class Resultado(val sessaoId: Long) : Destino
@Serializable data object Historico : Destino
@Serializable data object SelecaoSimulado : Destino
@Serializable data object AtualizarBanco : Destino

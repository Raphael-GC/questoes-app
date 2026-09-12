package net.oraphael.questoes.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta "Carta anotada" (Fase 3, fechada em 08/09/2026 — Catálogo de Componentes).
 * Tema único, deliberadamente sem variação clara/escura nem cor dinâmica: é
 * identidade de marca, não chrome neutro de sistema. Nomes em português seguem o
 * vocabulário do próprio design system (Direções Visuais/Catálogo), não os nomes
 * técnicos do CSS original — mais fácil de conferir um contra o outro.
 */

// Fundos
val TintaPrancheta = Color(0xFF16324A) // fundo base de toda tela/painel
val Superficie = Color(0xFF1D3F5C) // cartões, linhas selecionáveis
val Superficie2 = Color(0xFF24496A) // variação mais clara (ex.: sheet sobre backdrop)

// Texto
val TracoClaro = Color(0xFFDBE7EF) // texto principal sobre o fundo escuro
val TracoClaroMuted = Color(0xFF9FB9C9) // texto secundário/legenda

// Estrutural — cantos de registro, divisórias, chrome; nunca texto de conteúdo
val LinhaDeCota = Color(0xFF4F8FA8)
val Linha = TracoClaro.copy(alpha = 0.14f) // divisória fina
val LinhaForte = TracoClaro.copy(alpha = 0.28f) // borda de controle

// Accent primário — botões, seleção ativa, destaque
val MarcoDourado = Color(0xFFD79B3D)
val MarcoDouradoSuave = MarcoDourado.copy(alpha = 0.15f)

// Semânticas
val Musgo = Color(0xFF7C9A5C) // acerto, nota boa
val MusgoSuave = Musgo.copy(alpha = 0.18f)
val CurvaRubra = Color(0xFFB6553A) // erro, tag pra revisar
val CurvaRubraSuave = CurvaRubra.copy(alpha = 0.18f)

// "Água" — identidade do contexto Simulado (derivada da Linha de Cota, não é cor nova)
val Agua = Color(0xFF6FB0C9)
val AguaSuave = LinhaDeCota.copy(alpha = 0.18f)

package net.oraphael.questoes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * As 4 famílias do sistema "Carta anotada" (Catálogo de Componentes, Fase 3): Archivo
 * (títulos/valores em destaque), Karla (corpo), JetBrains Mono (todo dado numérico/
 * rótulo curto) e Caveat (marginália, só em doses pequenas).
 *
 * FONTES PROVISÓRIAS (decisão de 12/09/2026): essas 4 famílias apontam pra fontes do
 * sistema, não as 4 do Google Fonts fechadas na Fase 3 — pra não depender de Google
 * Play Services nem de arquivo de fonte nenhum nesta etapa. Quando tiver os .ttf reais
 * (Archivo, Karla, JetBrains Mono, Caveat) em `res/font/`, troca só estas 4 linhas por
 * `FontFamily(Font(R.font.xxx, FontWeight.YYY), ...)` — o resto deste arquivo não muda.
 */
val QuestoesFontDisplay = FontFamily.SansSerif // provisório de Archivo
val QuestoesFontBody = FontFamily.SansSerif // provisório de Karla
val QuestoesFontMono = FontFamily.Monospace // provisório de JetBrains Mono
val QuestoesFontHand = FontFamily.Cursive // provisório de Caveat

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = QuestoesFontDisplay,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.2).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = QuestoesFontDisplay,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = QuestoesFontDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = QuestoesFontDisplay,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = QuestoesFontDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = QuestoesFontDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = QuestoesFontDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = QuestoesFontBody,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = QuestoesFontBody,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = QuestoesFontBody,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = QuestoesFontBody,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = QuestoesFontBody,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    // .btn é Archivo 700, não mono — por isso labelLarge (estilo padrão de texto de
    // botão no Material 3) usa a família de título, não a mono.
    labelLarge = TextStyle(
        fontFamily = QuestoesFontDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = QuestoesFontMono,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = QuestoesFontMono,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
    ),
)

/**
 * Marginália escrita à mão (.note / .t-hand no Catálogo) — uso pontual e discreto
 * (ex.: "revisar isso!"), nunca um estilo padrão do Typography acima.
 */
val CaveatMarginalia = TextStyle(
    fontFamily = QuestoesFontHand,
    fontWeight = FontWeight.Medium,
    fontSize = 17.sp,
)

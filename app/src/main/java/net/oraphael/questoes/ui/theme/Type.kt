package net.oraphael.questoes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import net.oraphael.questoes.R

/**
 * As 4 famílias do sistema "Carta anotada" (Catálogo de Componentes, Fase 3): Archivo
 * (títulos/valores em destaque), Karla (corpo), JetBrains Mono (todo dado numérico/
 * rótulo curto) e Caveat (marginália, só em doses pequenas). Arquivos .ttf (licença
 * OFL, textos em docs/fonts/) em res/font/, só os pesos usados no Typography abaixo.
 */
val QuestoesFontDisplay = FontFamily(
    Font(R.font.archivo_bold, FontWeight.Bold),
    Font(R.font.archivo_extrabold, FontWeight.ExtraBold),
)
val QuestoesFontBody = FontFamily(
    Font(R.font.karla_regular, FontWeight.Normal),
    Font(R.font.karla_semibold, FontWeight.SemiBold),
)
val QuestoesFontMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
)
val QuestoesFontHand = FontFamily(
    Font(R.font.caveat_medium, FontWeight.Medium),
)

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

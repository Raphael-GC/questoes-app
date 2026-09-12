package net.oraphael.questoes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens do sistema "Carta anotada" sem papel correspondente direto no ColorScheme
 * padrão do Material 3 (fundos translúcidos semânticos, cor estrutural, linhas, a
 * identidade "Água" do contexto Simulado). Acessados via `QuestoesTokens.cores` dentro
 * de qualquer @Composable — ver Catálogo de Componentes (Fase 3) pra onde cada um é
 * usado.
 */
data class QuestoesExtraColors(
    val superficie2: Color,
    val tracoClaroMuted: Color,
    val estrutural: Color,
    val linha: Color,
    val linhaForte: Color,
    val marcoDouradoSuave: Color,
    val musgoSuave: Color,
    val curvaRubraSuave: Color,
    val agua: Color,
    val aguaSuave: Color,
)

private val CartaAnotadaExtraColors = QuestoesExtraColors(
    superficie2 = Superficie2,
    tracoClaroMuted = TracoClaroMuted,
    estrutural = LinhaDeCota,
    linha = Linha,
    linhaForte = LinhaForte,
    marcoDouradoSuave = MarcoDouradoSuave,
    musgoSuave = MusgoSuave,
    curvaRubraSuave = CurvaRubraSuave,
    agua = Agua,
    aguaSuave = AguaSuave,
)

private val LocalQuestoesExtraColors = staticCompositionLocalOf { CartaAnotadaExtraColors }

/** Ponto de acesso aos tokens do tema que não cabem no ColorScheme do Material 3. */
object QuestoesTokens {
    val cores: QuestoesExtraColors
        @Composable get() = LocalQuestoesExtraColors.current
}

private val CartaAnotadaColorScheme = darkColorScheme(
    primary = MarcoDourado,
    onPrimary = TintaPrancheta,
    secondary = Agua,
    onSecondary = TintaPrancheta,
    tertiary = Musgo,
    onTertiary = TintaPrancheta,
    error = CurvaRubra,
    onError = TracoClaro,
    background = TintaPrancheta,
    onBackground = TracoClaro,
    surface = Superficie,
    onSurface = TracoClaro,
    surfaceVariant = Superficie2,
    onSurfaceVariant = TracoClaroMuted,
    outline = LinhaForte,
    outlineVariant = Linha,
)

/**
 * Tema do Questões — sistema "Carta anotada" (Fase 3, fechado em 08/09/2026): paleta
 * única, deliberadamente sem variação clara/escura nem cor dinâmica do Android 12+
 * (`dynamicColor`) — é identidade de marca, não chrome neutro de sistema.
 */
@Composable
fun QuestõesTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalQuestoesExtraColors provides CartaAnotadaExtraColors) {
        MaterialTheme(
            colorScheme = CartaAnotadaColorScheme,
            typography = Typography,
            shapes = QuestoesShapes,
            content = content,
        )
    }
}

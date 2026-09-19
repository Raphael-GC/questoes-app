package net.oraphael.questoes.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.oraphael.questoes.ui.theme.MarcoDourado
import net.oraphael.questoes.ui.theme.Musgo
import net.oraphael.questoes.ui.theme.QuestoesRadii
import net.oraphael.questoes.ui.theme.QuestoesTokens

/**
 * Indicador de espera pra requisições que não são instantâneas (ex.: pop-up de tags
 * enquanto a consulta ao Room roda) — a mesma caixinha+check do ícone do app ("Carta
 * anotada": caixinha Marco Dourado, check Musgo), desenhado em loop em vez de um
 * spinner genérico. Cada ciclo: caixinha vazia → check é traçado → some → repete.
 */
@Composable
fun CarregandoCheck(
    modifier: Modifier = Modifier,
    legenda: String? = null,
    tamanho: Dp = 40.dp,
) {
    val transicao = rememberInfiniteTransition(label = "carregando-check")
    val progresso by transicao.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "progresso-check",
    )
    // Traça o check nos primeiros 55% do ciclo, segura completo até 80% e some no resto
    // — dá tempo do olho perceber o check inteiro antes de reiniciar.
    val fracaoTraco = (progresso / 0.55f).coerceIn(0f, 1f)
    val fracaoOpacidade = if (progresso < 0.8f) 1f else (1f - (progresso - 0.8f) / 0.2f).coerceIn(0f, 1f)
    val corFundoCaixinha = QuestoesTokens.cores.marcoDouradoSuave

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Canvas(modifier = Modifier.size(tamanho)) {
            val raio = QuestoesRadii.checkbox.toPx()
            drawRoundRect(
                color = corFundoCaixinha,
                cornerRadius = CornerRadius(raio, raio),
            )
            drawRoundRect(
                color = MarcoDourado,
                cornerRadius = CornerRadius(raio, raio),
                style = Stroke(width = size.minDimension * 0.06f),
            )

            if (fracaoTraco > 0f) {
                val check = Path().apply {
                    moveTo(size.width * 0.24f, size.height * 0.52f)
                    lineTo(size.width * 0.42f, size.height * 0.70f)
                    lineTo(size.width * 0.78f, size.height * 0.32f)
                }
                val medidor = PathMeasure().apply { setPath(check, false) }
                val trecho = Path()
                medidor.getSegment(0f, medidor.length * fracaoTraco, trecho, true)
                drawPath(
                    path = trecho,
                    color = Musgo.copy(alpha = fracaoOpacidade),
                    style = Stroke(
                        width = size.minDimension * 0.09f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }
        if (legenda != null) {
            Text(
                text = legenda,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

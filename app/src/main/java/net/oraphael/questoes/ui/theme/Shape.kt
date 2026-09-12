package net.oraphael.questoes.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Escala de raio de borda do sistema "Carta anotada" (Catálogo de Componentes, Fase 3)
 * — não é acidental, distingue controle pequeno de cartão/linha de cápsula. Usar estes
 * tokens direto nos componentes das próximas etapas em vez de valores soltos em dp.
 */
object QuestoesRadii {
    val checkbox = 5.dp // checkbox, marcador de seleção pequeno
    val controle = 11.dp // botão, cartão pequeno, linha selecionável, ícone
    val cartao = 13.dp // cartão principal (.card), spark-card, dropzone
    val capsula = 20.dp // pill, chip, segmented control, sheet
    val moldura = 30.dp // moldura do "aparelho" — reservado, não usar em outro lugar
}

val QuestoesShapes = Shapes(
    extraSmall = RoundedCornerShape(QuestoesRadii.checkbox),
    small = RoundedCornerShape(QuestoesRadii.controle),
    medium = RoundedCornerShape(QuestoesRadii.cartao),
    large = RoundedCornerShape(QuestoesRadii.capsula),
    extraLarge = RoundedCornerShape(QuestoesRadii.moldura),
)

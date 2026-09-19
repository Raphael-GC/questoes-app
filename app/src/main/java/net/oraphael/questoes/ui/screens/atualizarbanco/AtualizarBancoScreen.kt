package net.oraphael.questoes.ui.screens.atualizarbanco

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.oraphael.questoes.data.importer.AtualizadorRemoto
import net.oraphael.questoes.data.importer.BancoVersao
import net.oraphael.questoes.data.importer.ManifestoJson
import net.oraphael.questoes.ui.components.CarregandoCheck
import net.oraphael.questoes.ui.theme.QuestoesRadii
import net.oraphael.questoes.ui.theme.QuestoesTokens

private sealed interface EstadoAtualizacao {
    data object Ocioso : EstadoAtualizacao
    data object Verificando : EstadoAtualizacao
    data object EmDia : EstadoAtualizacao
    data class Disponivel(val manifesto: ManifestoJson) : EstadoAtualizacao
    data object Baixando : EstadoAtualizacao
    data class Concluido(val versao: Int, val totalQuestoes: Int) : EstadoAtualizacao
    data class Erro(val mensagem: String) : EstadoAtualizacao
}

/**
 * Tela 7 (Mapa de Navegação) — verifica e aplica atualizações do banco de questões a
 * partir do manifest.json público do questoes-banco (GitHub). Compara [ManifestoJson.versao]
 * com [BancoVersao.versaoAplicada] antes de baixar qualquer coisa; ao aplicar, troca o
 * conteúdo local disciplina por disciplina (ver [Importador.importarDeTexto]) e grava a
 * nova versão aplicada — o histórico de sessões (tabelas separadas) não é afetado.
 */
@Composable
fun AtualizarBancoScreen(atualizador: AtualizadorRemoto, onVoltarHome: () -> Unit) {
    val context = LocalContext.current
    var estado by remember { mutableStateOf<EstadoAtualizacao>(EstadoAtualizacao.Ocioso) }
    var versaoAtual by remember { mutableStateOf(BancoVersao.versaoAplicada(context)) }
    val scope = rememberCoroutineScope()

    fun verificar() {
        estado = EstadoAtualizacao.Verificando
        scope.launch {
            estado = try {
                val manifesto = atualizador.buscarManifesto()
                if (manifesto.versao > versaoAtual) {
                    EstadoAtualizacao.Disponivel(manifesto)
                } else {
                    EstadoAtualizacao.EmDia
                }
            } catch (e: Exception) {
                // Rede indisponível (IOException) ou manifest.json com formato inesperado
                // (SerializationException) - nenhum dos dois pode derrubar a tela.
                EstadoAtualizacao.Erro("Não foi possível verificar agora. Confira sua conexão e tente de novo.")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Atualizar banco",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(QuestoesRadii.controle))
                .background(MaterialTheme.colorScheme.surface)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "BANCO ATUAL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = "Versão $versaoAtual", style = MaterialTheme.typography.titleMedium)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        when (val s = estado) {
            is EstadoAtualizacao.Ocioso -> {
                Text(
                    text = "Verifique se há uma versão mais nova do banco de questões publicada no GitHub.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is EstadoAtualizacao.Verificando -> {
                CarregandoCheck(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    legenda = "Verificando atualizações...",
                )
            }

            is EstadoAtualizacao.EmDia -> {
                Text(
                    text = "Você já está com a versão mais recente do banco.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is EstadoAtualizacao.Disponivel -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(QuestoesRadii.controle))
                        .background(QuestoesTokens.cores.marcoDouradoSuave)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(text = "Nova versão disponível", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Versão ${s.manifesto.versao} · ${s.manifesto.totalQuestoes} questões",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            is EstadoAtualizacao.Baixando -> {
                CarregandoCheck(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    legenda = "Baixando e atualizando o banco...",
                )
            }

            is EstadoAtualizacao.Concluido -> {
                Text(
                    text = "Banco atualizado pra versão ${s.versao} (${s.totalQuestoes} questões).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            is EstadoAtualizacao.Erro -> {
                Text(
                    text = s.mensagem,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        val estadoAtual = estado
        val podeVerificar = estadoAtual !is EstadoAtualizacao.Verificando && estadoAtual !is EstadoAtualizacao.Baixando
        val disponivel = estadoAtual as? EstadoAtualizacao.Disponivel

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onVoltarHome, modifier = Modifier.weight(1f)) {
                Text("Voltar")
            }
            if (disponivel != null) {
                Button(
                    onClick = {
                        val manifesto = disponivel.manifesto
                        estado = EstadoAtualizacao.Baixando
                        scope.launch {
                            estado = try {
                                atualizador.aplicar(manifesto)
                                BancoVersao.marcarVersaoAplicada(context, manifesto.versao)
                                versaoAtual = manifesto.versao
                                EstadoAtualizacao.Concluido(manifesto.versao, manifesto.totalQuestoes)
                            } catch (e: Exception) {
                                EstadoAtualizacao.Erro("Falha ao baixar a atualização. Tente de novo.")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Baixar e atualizar")
                }
            } else {
                Button(
                    onClick = { verificar() },
                    enabled = podeVerificar,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Verificar atualizações")
                }
            }
        }
    }
}

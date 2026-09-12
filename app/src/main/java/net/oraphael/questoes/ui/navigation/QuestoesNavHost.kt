package net.oraphael.questoes.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

/**
 * Raiz da navegação do app (Navigation Compose clássico — androidx.navigation.compose).
 *
 * Voltamos aqui depois de um crash em runtime com o Navigation 3 (biblioteca nova,
 * estável desde nov/2025, mas ainda instável na prática nesse projeto) sem diagnóstico
 * fechado via Logcat. O Navigation Compose clássico tem ~7 anos de maturidade e
 * documentação extensa, ainda que não seja mais a recomendação oficial do Google para
 * apps novos. Como só existiam telas placeholder até aqui, esse foi o ponto mais barato
 * possível para trocar de ferramenta.
 *
 * Usa navegação type-safe (desde Navigation 2.8): cada rota é o próprio objeto/data
 * class de [Destino], marcado com `@Serializable` — sem strings de rota "na mão".
 *
 * Cada rota hoje mostra um placeholder; as telas de verdade entram nas próximas etapas
 * da Fase 5, uma de cada vez, substituindo o conteúdo de cada `composable<...>` abaixo.
 */
@Composable
fun QuestoesApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            TelaPlaceholder("Home") {
                Button(onClick = { navController.navigate(SelecaoLivre) }) { Text("Seleção · Livre") }
                Button(onClick = { navController.navigate(SelecaoSimulado) }) { Text("Seleção de simulado") }
                Button(onClick = { navController.navigate(Historico) }) { Text("Histórico") }
            }
        }
        composable<SelecaoLivre> {
            TelaPlaceholder("Seleção · Livre") {
                Button(onClick = { navController.navigate(Quiz(sessaoId = 0L)) }) {
                    Text("Confirmar (placeholder)")
                }
            }
        }
        composable<Quiz> { backStackEntry ->
            val destino: Quiz = backStackEntry.toRoute()
            TelaPlaceholder("Quiz — sessão ${destino.sessaoId}") {
                Button(onClick = { navController.navigate(Resultado(sessaoId = destino.sessaoId)) }) {
                    Text("Concluir sessão")
                }
            }
        }
        composable<Resultado> { backStackEntry ->
            val destino: Resultado = backStackEntry.toRoute()
            TelaPlaceholder("Resultado — sessão ${destino.sessaoId}") {
                Button(onClick = {
                    navController.navigate(Home) {
                        popUpTo<Home> { inclusive = true }
                    }
                }) { Text("Voltar à Home") }
            }
        }
        composable<Historico> {
            TelaPlaceholder("Histórico") {
                Button(onClick = { navController.navigate(Resultado(sessaoId = 1L)) }) {
                    Text("Abrir uma sessão (placeholder)")
                }
            }
        }
        composable<SelecaoSimulado> {
            TelaPlaceholder("Seleção de simulado") {
                Button(onClick = { navController.navigate(Quiz(sessaoId = 0L)) }) {
                    Text("Confirmar (placeholder)")
                }
            }
        }
        composable<AtualizarBanco> {
            TelaPlaceholder("Atualizar banco") {}
        }
    }
}

@Composable
private fun TelaPlaceholder(titulo: String, acoes: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(titulo)
        acoes()
    }
}

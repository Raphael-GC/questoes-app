package net.oraphael.questoes.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import net.oraphael.questoes.QuestoesApplication
import net.oraphael.questoes.data.repo.QuestaoRepository
import net.oraphael.questoes.data.repo.SessaoRepository
import net.oraphael.questoes.domain.MotorSessao
import net.oraphael.questoes.ui.screens.historico.HistoricoScreen
import net.oraphael.questoes.ui.screens.home.HomeScreen
import net.oraphael.questoes.ui.screens.quiz.QuizScreen
import net.oraphael.questoes.ui.screens.resultado.ResultadoScreen
import net.oraphael.questoes.ui.screens.selecaolivre.SelecaoLivreScreen
import net.oraphael.questoes.ui.screens.selecaosimulado.SelecaoSimuladoScreen

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
 * Etapa 4 (Fase 5) começou a trocar os placeholders pelas telas de verdade, uma de cada
 * vez: Home ([HomeScreen]), Seleção · Livre ([SelecaoLivreScreen]), Quiz ([QuizScreen]),
 * Resultado ([ResultadoScreen]) e Histórico ([HistoricoScreen]) já são reais. As demais
 * rotas abaixo seguem placeholder até a etapa de cada uma.
 */
@Composable
fun QuestoesApp() {
    val context = LocalContext.current
    // Sem framework de DI ainda (ver comentário em HomeScreen.kt): os repositórios e o
    // MotorSessao são criados uma vez aqui, no topo da árvore de navegação, e passados
    // pra cada tela que precisar deles.
    val questaoRepository = remember {
        val app = context.applicationContext as QuestoesApplication
        QuestaoRepository(app.db)
    }
    val sessaoRepository = remember {
        val app = context.applicationContext as QuestoesApplication
        SessaoRepository(app.db)
    }
    val motorSessao = remember { MotorSessao(questaoRepository) }

    val navController = rememberNavController()

    // safeDrawing = status bar + barra de navegação + display cutout (câmera/notch): sem
    // esse padding aqui, o app fica atrás desses elementos (enableEdgeToEdge em
    // MainActivity) e título/conteúdo do topo de cada tela ficam por baixo da câmera em
    // aparelhos com furo central — Surface (MainActivity) continua pintando o fundo até a
    // borda da tela, só o conteúdo é que recua.
    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        composable<Home> {
            HomeScreen(
                repository = questaoRepository,
                onLivreClick = { navController.navigate(SelecaoLivre()) },
                onSimuladosClick = { navController.navigate(SelecaoSimulado) },
                onHistoricoClick = { navController.navigate(Historico) },
                onDisciplinaClick = { disciplinaId -> navController.navigate(SelecaoLivre(disciplinaInicial = disciplinaId)) },
            )
        }
        composable<SelecaoLivre> { backStackEntry ->
            val destino: SelecaoLivre = backStackEntry.toRoute()
            SelecaoLivreScreen(
                repository = questaoRepository,
                sessaoRepository = sessaoRepository,
                motorSessao = motorSessao,
                disciplinaInicial = destino.disciplinaInicial,
                onSessaoIniciada = { sessaoId -> navController.navigate(Quiz(sessaoId = sessaoId)) },
                onVoltarHome = { navController.popBackStack() },
            )
        }
        composable<Quiz> { backStackEntry ->
            val destino: Quiz = backStackEntry.toRoute()
            QuizScreen(
                sessaoId = destino.sessaoId,
                questaoRepository = questaoRepository,
                sessaoRepository = sessaoRepository,
                onSessaoConcluida = { sessaoId ->
                    navController.navigate(Resultado(sessaoId = sessaoId)) {
                        popUpTo<Quiz> { inclusive = true }
                    }
                },
            )
        }
        composable<Resultado> { backStackEntry ->
            val destino: Resultado = backStackEntry.toRoute()
            ResultadoScreen(
                sessaoId = destino.sessaoId,
                sessaoRepository = sessaoRepository,
                questaoRepository = questaoRepository,
                onVoltarHome = {
                    navController.navigate(Home) {
                        popUpTo<Home> { inclusive = true }
                    }
                },
            )
        }
        composable<Historico> {
            HistoricoScreen(
                sessaoRepository = sessaoRepository,
                onAbrirSessao = { sessaoId -> navController.navigate(Resultado(sessaoId = sessaoId)) },
            )
        }
        composable<SelecaoSimulado> {
            SelecaoSimuladoScreen(
                repository = questaoRepository,
                sessaoRepository = sessaoRepository,
                motorSessao = motorSessao,
                onSessaoIniciada = { sessaoId -> navController.navigate(Quiz(sessaoId = sessaoId)) },
                onVoltarHome = { navController.popBackStack() },
            )
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

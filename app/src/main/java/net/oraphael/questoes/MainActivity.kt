package net.oraphael.questoes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import net.oraphael.questoes.ui.navigation.QuestoesApp
import net.oraphael.questoes.ui.theme.QuestõesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuestõesTheme {
                // É o Surface que de fato pinta o fundo da tela com a cor do tema
                // (MaterialTheme sozinho só disponibiliza os tokens, não pinta nada).
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    QuestoesApp()
                }
            }
        }
    }
}

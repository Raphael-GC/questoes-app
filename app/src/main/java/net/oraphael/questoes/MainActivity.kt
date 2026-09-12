package net.oraphael.questoes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import net.oraphael.questoes.ui.navigation.QuestoesApp
import net.oraphael.questoes.ui.theme.QuestõesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuestõesTheme {
                QuestoesApp()
            }
        }
    }
}

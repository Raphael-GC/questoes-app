package net.oraphael.questoes

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.oraphael.questoes.data.db.AppDatabase
import net.oraphael.questoes.data.importer.Importador

class QuestoesApplication : Application() {
    lateinit var db: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDatabase::class.java, "questoes.db")
            // App ainda não lançado: sem migração real pra manter. Se o schema mudar sem
            // subir a versão (ou sem Migration definida), recria o banco do zero em vez de
            // crashar — as questões voltam pelo importador automático no próximo boot.
            .fallbackToDestructiveMigration()
            .build()

        // Roda só uma vez: se o banco já tem questões, não reimporta a cada abertura do app.
        CoroutineScope(Dispatchers.IO).launch {
            if (db.questaoDao().contar() == 0) {
                val importador = Importador(db, this@QuestoesApplication)
                importador.importarDeAssets("geografia.json", "geografia")
                importador.importarDeAssets("gerais-didatico-pedagogico.json", "gerais")
                importador.importarDeAssets("portugues.json", "portugues")
                importador.importarDeAssets("educacao-especial.json", "educacao-especial")
                importador.importarDeAssets("educacao-infantil.json", "educacao-infantil")
                importador.importarDeAssets("pnd-geografia-2026-fgd.json", "pnd-fgd")
                importador.importarDeAssets("pnd-geografia-2026-especifico.json", "pnd-geografia")
            }
        }
    }
}

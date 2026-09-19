package net.oraphael.questoes.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        QuestaoEntity::class,
        AlternativaEntity::class,
        TagEntity::class,
        QuestaoTagCrossRef::class,
        SessaoEntity::class,
        TentativaEntity::class,
    ],
    // Subiu de 1 pra 2 quando Sessao/Tentativa entraram no schema (Etapa 1, Fase 5), de
    // 2 pra 3 quando SessaoEntity ganhou questaoIdsJson (Etapa 2, Tela 2), de 3 pra 4
    // quando QuestaoEntity trocou possuiImagem/imagemDesc (uma imagem só) por
    // imagensDescJson (0+ imagens) e de 4 pra 5 pelos índices em disciplinaId/tagId que
    // tiravam a lentidão do pop-up de tags. Sem esse bump, o Room nunca aciona o
    // fallbackToDestructiveMigration() do QuestoesApplication — ele só entra em ação
    // quando a versão muda de fato.
    version = 5,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questaoDao(): QuestaoDao
    abstract fun tagDao(): TagDao
    abstract fun sessaoDao(): SessaoDao
}

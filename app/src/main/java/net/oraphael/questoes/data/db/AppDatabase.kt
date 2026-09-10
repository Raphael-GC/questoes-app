package net.oraphael.questoes.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [QuestaoEntity::class, AlternativaEntity::class, TagEntity::class, QuestaoTagCrossRef::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questaoDao(): QuestaoDao
    abstract fun tagDao(): TagDao
}

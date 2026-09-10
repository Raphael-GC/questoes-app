package net.oraphael.questoes.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questao")
data class QuestaoEntity(
    @PrimaryKey val id: String,
    val disciplinaId: String,
    val enunciado: String,
    val respostaCorreta: String,
    val explicacao: String,
    val fonte: String?,
    val possuiImagem: Boolean,
    val imagemDesc: String?,
)

@Entity(tableName = "alternativa", primaryKeys = ["questaoId", "letra"])
data class AlternativaEntity(
    val questaoId: String,
    val letra: String,
    val texto: String,
)

@Entity(tableName = "tag")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
)

@Entity(tableName = "questao_tag_cross_ref", primaryKeys = ["questaoId", "tagId"])
data class QuestaoTagCrossRef(
    val questaoId: String,
    val tagId: Long,
)

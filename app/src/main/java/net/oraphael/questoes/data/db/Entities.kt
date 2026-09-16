package net.oraphael.questoes.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "questao")
data class QuestaoEntity(
    @PrimaryKey val id: String,
    val disciplinaId: String,
    val enunciado: String,
    val respostaCorreta: String,
    val explicacao: String,
    val fonte: String?,
    // Lista de descrições (uma por imagem, na ordem de exibição) serializada como JSON,
    // mesmo padrão de SessaoEntity.filtrosJson/questaoIdsJson — "[]" = sem imagem. A URL
    // de cada imagem é montada em QuizScreen a partir do id da questão + índice (1-based),
    // nunca guardada aqui (ver README de questoes-banco, "Imagens").
    val imagensDescJson: String,
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

/**
 * Uma sessão de estudo: um "Livre" (1+ disciplinas com filtro próprio) ou um "Simulado"
 * (um cargo). [filtrosJson] guarda um retrato dos filtros usados (disciplinas/tags/quantidade,
 * ou o cargo/blocos), só pra exibição no Histórico — nunca é reconsultado estruturalmente.
 */
@Entity(tableName = "sessao")
data class SessaoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modo: String, // "livre" | "simulado"
    val ordem: String?, // "aleatorio" | "sequencial" — só quando modo = "livre"
    val cargoSimulado: String?, // só quando modo = "simulado"
    val dataHoraInicio: Long,
    val tempoTotalSessaoMs: Long?, // preenchido ao concluir a sessão
    val filtrosJson: String,
    // Ids das questões sorteadas por MotorSessao, na ordem exata em que o Quiz (Tela 3)
    // deve exibi-las — sorteado uma única vez, ao confirmar a Tela 2/Tela 6, e nunca
    // recalculado depois (senão cada reabertura da sessão trocaria a ordem/questões).
    val questaoIdsJson: String,
)

@Entity(
    tableName = "tentativa",
    foreignKeys = [
        ForeignKey(
            entity = SessaoEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessaoId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessaoId"), Index("questaoId")],
)
data class TentativaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessaoId: Long,
    val questaoId: String,
    val disciplinaId: String,
    val blocoSimulado: String?, // só quando a sessão é de um simulado
    val respostaSelecionada: String,
    val acerto: Boolean,
    val tempoQuestaoMs: Long,
    val dataHora: Long,
)

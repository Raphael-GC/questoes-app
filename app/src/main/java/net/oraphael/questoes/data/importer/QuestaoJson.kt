package net.oraphael.questoes.data.importer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class QuestaoJson(
    val id: String,
    val disciplina: String,
    val enunciado: String,
    val alternativas: JsonElement,
    @SerialName("resposta_correta") val respostaCorreta: String,
    val explicacao: String,
    val tags: List<String> = emptyList(),
    val fonte: String? = null,
    @SerialName("possui_imagem") val possuiImagem: Boolean = false,
    @SerialName("imagem_desc") val imagemDesc: String? = null,
)

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
    // Uma questão pode ter 0+ imagens — README de questoes-banco: arquivos
    // `<id-da-questao>-<n>.png` em `files/images/`, n de 1 até o tamanho desta lista, na
    // mesma ordem em que devem aparecer na tela. Lista vazia/ausente = sem imagem.
    @SerialName("imagens_desc") val imagensDesc: List<String> = emptyList(),
)

package io.solidar.donation.navigation

import kotlinx.serialization.Serializable

/** Esquema usado pelo deep link: `solidar://campanha/{id}`. */
const val DEEP_LINK_CAMPANHA = "solidar://campanha"

/** Grafo aninhado de autenticação (login + cadastro). */
@Serializable
object AuthGraph

@Serializable
object LoginRoute

@Serializable
object SignUpRoute

/** Shell principal com a barra de navegação. */
@Serializable
object MainRoute

@Serializable
object CriarDoacaoRoute

/** Detalhe de campanha; o argumento é o id, não o objeto inteiro. */
@Serializable
data class CampanhaDetalheRoute(
    val id: String,
)

/** Rota para a tela de escolha do tipo de doação (Dinheiro ou Itens). */
@Serializable
data class OpcoesDoacaoRoute(
    val id: String,
)
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

@Serializable
object CriarCampanhaRoute

/** Detalhe de campanha; o argumento é o id, não o objeto inteiro. */
@Serializable
data class CampanhaDetalheRoute(
    val id: String,
)

@Serializable
data class OpcoesDoacaoRoute(
    val id: String,
)

/** Detalhe de uma doação disponível (aba Explorar); o argumento é o id. */
@Serializable
data class DoacaoDetalheRoute(
    val id: String,
)

@Serializable
data class DoarItensCampanhaRoute(
    val id: String,
)

@Serializable
data class DoarDinheiroRoute(
    val id: String,
)

@Serializable
data class EfetivarDoacaoRoute(
    val id: String,
    val itemTitle: String,
)

/** Tela de edição dos dados cadastrais e endereço do perfil do usuário (#6). */
@Serializable
object EditarPerfilRoute

/** Tela de alteração de senha do usuário autenticado (#6). */
@Serializable
object AlterarSenhaRoute

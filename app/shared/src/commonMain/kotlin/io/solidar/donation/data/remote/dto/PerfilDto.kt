package io.solidar.donation.data.remote.dto

import io.solidar.donation.domain.model.PerfilUsuario
import kotlinx.serialization.Serializable

@Serializable
data class PessoaFisicaResponseDto(
    val id: Int? = null,
    val nome: String? = null,
    val email: String? = null,
    val cpf: String? = null,
    val dataNascimento: String? = null,
    val logradouro: String? = null,
    val bairro: String? = null,
    val numero: String? = null,
    val cep: String? = null,
)

@Serializable
data class InstituicaoResponseDto(
    val id: Int? = null,
    val nome: String? = null,
    val email: String? = null,
    val cnpj: String? = null,
    val site: String? = null,
    val logradouro: String? = null,
    val bairro: String? = null,
    val numero: String? = null,
    val cep: String? = null,
)

@Serializable
data class AtualizarPerfilRequestDto(
    val nome: String,
    val logradouro: String? = null,
    val bairro: String? = null,
    val numero: String? = null,
    val cep: String? = null,
)

@Serializable
data class AlterarSenhaRequestDto(
    val senhaAtual: String,
    val novaSenha: String,
)

fun PessoaFisicaResponseDto.toDomain(fallbackId: Int = 0): PerfilUsuario =
    PerfilUsuario(
        id = id ?: fallbackId,
        nome = nome.orEmpty(),
        email = email.orEmpty(),
        tipo = "PESSOA_FISICA",
        documento = cpf.orEmpty(),
        dataNascimento = dataNascimento,
        logradouro = logradouro.orEmpty(),
        numero = numero.orEmpty(),
        bairro = bairro.orEmpty(),
        cep = cep.orEmpty(),
    )

fun InstituicaoResponseDto.toDomain(fallbackId: Int = 0): PerfilUsuario =
    PerfilUsuario(
        id = id ?: fallbackId,
        nome = nome.orEmpty(),
        email = email.orEmpty(),
        tipo = "INSTITUICAO",
        documento = cnpj.orEmpty(),
        site = site,
        logradouro = logradouro.orEmpty(),
        numero = numero.orEmpty(),
        bairro = bairro.orEmpty(),
        cep = cep.orEmpty(),
    )

package io.solidar.donation.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val senha: String,
)

@Serializable
data class RegisterPfRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val cpf: String,
    val dataNascimento: String? = null,
    val logradouro: String? = null,
    val bairro: String? = null,
    val numero: String? = null,
    val cep: String? = null,
)

@Serializable
data class RegisterPjRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val cnpj: String,
    val site: String? = null,
    val logradouro: String? = null,
    val bairro: String? = null,
    val numero: String? = null,
    val cep: String? = null,
)

@Serializable
data class AuthResponse(
    val token: String? = null,
    val tipo: String? = null,
    val id: Int? = null,
    val nome: String? = null,
    val email: String? = null,
)

@Serializable
data class VerifyResponse(
    val logado: Boolean,
    val mensagem: String? = null,
    val usuario: AuthResponse? = null,
)

@Serializable
data class ApiErrorResponse(
    val status: Int? = null,
    val erro: String? = null,
    val mensagem: String? = null,
    val timestamp: String? = null,
)

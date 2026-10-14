package io.solidar.donation.domain.repository

import io.solidar.donation.model.UserSession

interface AuthRepository {
    suspend fun login(
        email: String,
        senha: String,
    ): Result<UserSession>

    suspend fun registerPessoaFisica(
        nome: String,
        email: String,
        senha: String,
        cpf: String,
    ): Result<UserSession>

    suspend fun registerInstituicao(
        nome: String,
        email: String,
        senha: String,
        cnpj: String,
    ): Result<UserSession>

    suspend fun verifySession(): Result<UserSession>

    fun getActiveSession(): UserSession?

    fun logout()
}

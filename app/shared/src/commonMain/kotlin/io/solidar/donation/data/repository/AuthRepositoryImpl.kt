package io.solidar.donation.data.repository

import io.solidar.donation.data.remote.AuthRemoteDataSource
import io.solidar.donation.data.remote.HttpClientFactory
import io.solidar.donation.data.remote.dto.LoginRequest
import io.solidar.donation.data.remote.dto.RegisterPfRequest
import io.solidar.donation.data.remote.dto.RegisterPjRequest
import io.solidar.donation.data.storage.InMemoryTokenStorage
import io.solidar.donation.data.storage.TokenStorage
import io.solidar.donation.domain.repository.AuthRepository
import io.solidar.donation.model.UserSession

class AuthRepositoryImpl(
    private val tokenStorage: TokenStorage = InMemoryTokenStorage(),
    private val remoteDataSource: AuthRemoteDataSource =
        AuthRemoteDataSource(
            httpClient = HttpClientFactory.create { tokenStorage.getToken() },
        ),
) : AuthRepository {
    override suspend fun login(
        email: String,
        senha: String,
    ): Result<UserSession> {
        return runCatching {
            val response =
                remoteDataSource.login(
                    LoginRequest(
                        email = email.trim(),
                        senha = senha,
                    ),
                )
            val session =
                UserSession(
                    token = response.token.orEmpty(),
                    tipo = response.tipo.orEmpty(),
                    id = response.id ?: 0,
                    nome = response.nome.orEmpty(),
                    email = response.email.orEmpty(),
                )
            tokenStorage.saveSession(session)
            session
        }
    }

    override suspend fun registerPessoaFisica(
        nome: String,
        email: String,
        senha: String,
        cpf: String,
    ): Result<UserSession> {
        return runCatching {
            val cleanCpf = cpf.filter { it.isDigit() }
            val response =
                remoteDataSource.registerPessoaFisica(
                    RegisterPfRequest(
                        nome = nome.trim(),
                        email = email.trim(),
                        senha = senha,
                        cpf = cleanCpf,
                    ),
                )
            val session =
                UserSession(
                    token = response.token.orEmpty(),
                    tipo = response.tipo ?: "PESSOA_FISICA",
                    id = response.id ?: 0,
                    nome = response.nome.orEmpty(),
                    email = response.email.orEmpty(),
                )
            tokenStorage.saveSession(session)
            session
        }
    }

    override suspend fun registerInstituicao(
        nome: String,
        email: String,
        senha: String,
        cnpj: String,
    ): Result<UserSession> {
        return runCatching {
            val cleanCnpj = cnpj.filter { it.isDigit() }
            val response =
                remoteDataSource.registerInstituicao(
                    RegisterPjRequest(
                        nome = nome.trim(),
                        email = email.trim(),
                        senha = senha,
                        cnpj = cleanCnpj,
                    ),
                )
            val session =
                UserSession(
                    token = response.token.orEmpty(),
                    tipo = response.tipo ?: "INSTITUICAO",
                    id = response.id ?: 0,
                    nome = response.nome.orEmpty(),
                    email = response.email.orEmpty(),
                )
            tokenStorage.saveSession(session)
            session
        }
    }

    override suspend fun verifySession(): Result<UserSession> {
        return runCatching {
            val response = remoteDataSource.verifySession()
            if (response.logado && response.usuario != null) {
                val user = response.usuario
                val session =
                    UserSession(
                        token = user.token ?: tokenStorage.getToken().orEmpty(),
                        tipo = user.tipo.orEmpty(),
                        id = user.id ?: 0,
                        nome = user.nome.orEmpty(),
                        email = user.email.orEmpty(),
                    )
                tokenStorage.saveSession(session)
                session
            } else {
                tokenStorage.clear()
                throw Exception(response.mensagem ?: "Sessão não autenticada.")
            }
        }
    }

    override fun getActiveSession(): UserSession? = tokenStorage.getSession()

    override fun logout() {
        tokenStorage.clear()
    }
}

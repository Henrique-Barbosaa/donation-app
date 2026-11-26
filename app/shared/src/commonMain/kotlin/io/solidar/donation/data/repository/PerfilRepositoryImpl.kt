package io.solidar.donation.data.repository

import io.solidar.donation.data.local.FileLocalCacheStorage
import io.solidar.donation.data.local.LocalCacheStorage
import io.solidar.donation.data.remote.HttpClientFactory
import io.solidar.donation.data.remote.NetworkErrorMapper
import io.solidar.donation.data.remote.PerfilRemoteDataSource
import io.solidar.donation.data.remote.dto.AlterarSenhaRequestDto
import io.solidar.donation.data.remote.dto.AtualizarPerfilRequestDto
import io.solidar.donation.data.remote.dto.toDomain
import io.solidar.donation.data.storage.SessionStorage
import io.solidar.donation.data.storage.TokenStorage
import io.solidar.donation.domain.error.FalhaDominio
import io.solidar.donation.domain.model.AlterarSenhaInput
import io.solidar.donation.domain.model.AtualizarPerfilInput
import io.solidar.donation.domain.model.PerfilUsuario
import io.solidar.donation.domain.model.ResultadoCacheado
import io.solidar.donation.domain.repository.PerfilRepository

class PerfilRepositoryImpl(
    private val tokenStorage: TokenStorage = SessionStorage.instance,
    private val remoteDataSource: PerfilRemoteDataSource =
        PerfilRemoteDataSource(
            httpClient = HttpClientFactory.create { tokenStorage.getToken() },
        ),
    private val localCacheStorage: LocalCacheStorage = FileLocalCacheStorage(),
) : PerfilRepository {
    override suspend fun carregarPerfil(): Result<ResultadoCacheado<PerfilUsuario>> {
        val sessao = tokenStorage.getSession()
        val cacheExistente = localCacheStorage.lerPerfil()

        if (sessao == null && cacheExistente == null) {
            return Result.failure(
                FalhaDominio.DadosInvalidos("Nenhum usuário autenticado no momento."),
            )
        }

        val idAlvo = sessao?.id ?: cacheExistente?.id ?: 0
        val tipoAlvo = sessao?.tipo?.ifBlank { cacheExistente?.tipo.orEmpty() } ?: cacheExistente?.tipo.orEmpty()

        return try {
            val perfilRemoto =
                if (tipoAlvo.equals("INSTITUICAO", ignoreCase = true)) {
                    remoteDataSource.buscarInstituicao(idAlvo).toDomain(fallbackId = idAlvo)
                } else {
                    remoteDataSource.buscarPessoaFisica(idAlvo).toDomain(fallbackId = idAlvo)
                }
            localCacheStorage.salvarPerfil(perfilRemoto)
            if (sessao != null && perfilRemoto.nome.isNotBlank() && perfilRemoto.nome != sessao.nome) {
                tokenStorage.saveSession(sessao.copy(nome = perfilRemoto.nome))
            }
            Result.success(ResultadoCacheado(dados = perfilRemoto, desatualizado = false))
        } catch (t: Throwable) {
            val falha = NetworkErrorMapper.mapear(t)
            if (falha is FalhaDominio.SemConexao) {
                val fallback =
                    cacheExistente
                        ?: sessao?.let {
                            PerfilUsuario(
                                id = it.id,
                                nome = it.nome,
                                email = it.email,
                                tipo = it.tipo.ifBlank { "PESSOA_FISICA" },
                            )
                        }
                if (fallback != null) {
                    return Result.success(ResultadoCacheado(dados = fallback, desatualizado = true))
                }
            }
            Result.failure(falha)
        }
    }

    override suspend fun atualizarPerfil(input: AtualizarPerfilInput): Result<PerfilUsuario> {
        return try {
            val cepLimpo = input.cep.filter { it.isDigit() }
            val request =
                AtualizarPerfilRequestDto(
                    nome = input.nome.trim(),
                    logradouro = input.logradouro.trim().ifBlank { null },
                    bairro = input.bairro.trim().ifBlank { null },
                    numero = input.numero.trim().ifBlank { null },
                    cep = cepLimpo.ifBlank { null },
                )
            remoteDataSource.atualizarPerfil(request)

            val sessaoAtual = tokenStorage.getSession()
            val cacheAtual = localCacheStorage.lerPerfil()
            val perfilAtualizado =
                (
                    cacheAtual ?: PerfilUsuario(
                        id = sessaoAtual?.id ?: 0,
                        nome = sessaoAtual?.nome.orEmpty(),
                        email = sessaoAtual?.email.orEmpty(),
                        tipo = sessaoAtual?.tipo?.ifBlank { "PESSOA_FISICA" } ?: "PESSOA_FISICA",
                    )
                ).copy(
                    nome = input.nome.trim(),
                    logradouro = input.logradouro.trim(),
                    numero = input.numero.trim(),
                    bairro = input.bairro.trim(),
                    cep = cepLimpo,
                )

            localCacheStorage.salvarPerfil(perfilAtualizado)
            if (sessaoAtual != null) {
                tokenStorage.saveSession(sessaoAtual.copy(nome = perfilAtualizado.nome))
            }
            Result.success(perfilAtualizado)
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }

    override suspend fun alterarSenha(input: AlterarSenhaInput): Result<Unit> {
        return try {
            remoteDataSource.alterarSenha(
                AlterarSenhaRequestDto(
                    senhaAtual = input.senhaAtual,
                    novaSenha = input.novaSenha,
                ),
            )
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }
}

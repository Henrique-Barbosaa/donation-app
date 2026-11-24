package io.solidar.donation.data.repository

import io.solidar.donation.data.local.FileLocalCacheStorage
import io.solidar.donation.data.local.LocalCacheStorage
import io.solidar.donation.data.remote.DoacaoRemoteDataSource
import io.solidar.donation.data.remote.HttpClientFactory
import io.solidar.donation.data.remote.NetworkErrorMapper
import io.solidar.donation.data.remote.dto.toCriarDoacaoRequest
import io.solidar.donation.data.remote.dto.toDomain
import io.solidar.donation.data.storage.SessionStorage
import io.solidar.donation.data.storage.TokenStorage
import io.solidar.donation.domain.error.FalhaDominio
import io.solidar.donation.domain.model.NovaDoacaoInput
import io.solidar.donation.domain.model.ResultadoCacheado
import io.solidar.donation.domain.repository.DoacaoRepository
import io.solidar.donation.model.Doacao
import io.solidar.donation.model.DoacaoDirecao

class DoacaoRepositoryImpl(
    private val tokenStorage: TokenStorage = SessionStorage.instance,
    private val remoteDataSource: DoacaoRemoteDataSource =
        DoacaoRemoteDataSource(
            httpClient = HttpClientFactory.create { tokenStorage.getToken() },
        ),
    private val localCacheStorage: LocalCacheStorage = FileLocalCacheStorage(),
) : DoacaoRepository {
    override suspend fun criarDoacao(
        input: NovaDoacaoInput,
        imagemBytes: ByteArray?,
        imagemNome: String?,
    ): Result<Doacao> {
        return try {
            val response =
                remoteDataSource.criarDoacao(
                    request = input.toCriarDoacaoRequest(),
                    imagemBytes = imagemBytes,
                    imagemNome = imagemNome,
                )
            val doacao = response.toDomain(DoacaoDirecao.FEITA)
            val atuais = localCacheStorage.lerDoacoes().orEmpty()
            localCacheStorage.salvarDoacoes(listOf(doacao) + atuais)
            Result.success(doacao)
        } catch (t: Throwable) {
            Result.failure(NetworkErrorMapper.mapear(t))
        }
    }

    override suspend fun listarHistoricoDoacoes(): Result<ResultadoCacheado<List<Doacao>>> {
        return try {
            val feitas = remoteDataSource.listarMinhasDoacoes().map { it.toDomain(DoacaoDirecao.FEITA) }
            val recebidas = remoteDataSource.listarDoacoesRecebidas().map { it.toDomain(DoacaoDirecao.RECEBIDA) }
            val todas = feitas + recebidas
            localCacheStorage.salvarDoacoes(todas)
            Result.success(ResultadoCacheado(dados = todas, desatualizado = false))
        } catch (t: Throwable) {
            val falha = NetworkErrorMapper.mapear(t)
            if (falha is FalhaDominio.SemConexao) {
                val cache = localCacheStorage.lerDoacoes()
                if (cache != null) {
                    return Result.success(ResultadoCacheado(dados = cache, desatualizado = true))
                }
            }
            Result.failure(falha)
        }
    }
}
